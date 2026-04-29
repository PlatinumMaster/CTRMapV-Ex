package ctrmap.formats.ntr.nitro2d.nmcr;

import ctrmap.formats.ntr.common.NTRDataIOStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * MCBK (Multi-Cell Bank) data block within an NMCR file.
 *
 * <p>Block magic in file: "KCBM" (MCBK reversed). Container layout (matches
 * NitroPaint's {@code McbkReadNmcr} which only reads three fields from the
 * header):</p>
 *
 * <pre>
 * 0x00 numberMultiCells              uint16
 * 0x02 marker                        uint16   Often 0xBEEF, but optional
 * 0x04 offsetDataMultiCell           uint32   Relative to container start
 * 0x08 offsetDataMultiCellProperties uint32   Relative to container start
 * 0x0C unknown1                      uint32   Reserved / preserved verbatim
 * 0x10 unknown2                      uint32   Reserved / preserved verbatim
 * </pre>
 *
 * <p>Each MultiCell entry is 8 bytes mirroring NitroPaint's
 * {@code MULTI_CELL}: nNodes (2) / nCellAnim (2) / hierarchyOffset (4).</p>
 *
 * <p>Each MultiCellProperty entry is 8 bytes mirroring NitroPaint's
 * {@code CELL_HIERARCHY}: sequenceNumber (2) / x (2) / y (2) / nodeAttr (2),
 * where {@code nodeAttr} is split here into a frameMode byte and a
 * uniqueID byte for write-side convenience.</p>
 */
public class NMCRMultiCellBank {

	// Block signatures in NITRO files are 4 ASCII bytes stored as a
	// little-endian uint32, so reading them back as a 4-char string
	// reverses the original ID. The block name is "MCBK", giving the
	// in-file byte sequence K,B,C,M → "KBCM".
	public static final String MAGIC = "KBCM";
	public static final int BEEF = 0xBEEF;

	public int marker = BEEF;
	public int unknown1;
	public int unknown2;

	/** The list of multi-cells in this bank. */
	public List<NMCRMultiCell> multiCells;

	public NMCRMultiCellBank() {
		multiCells = new ArrayList<>();
	}

	public NMCRMultiCellBank(NTRDataIOStream io) throws IOException {
		multiCells = new ArrayList<>();

		int blockStart = io.getPosition();
		String magic = io.readPaddedString(4);
		if (!MAGIC.equals(magic)) {
			throw new IOException("Expected MCBK (KCBM) magic, got: " + magic);
		}
		int blockSize = io.readInt();
		int containerStart = io.getPosition();

		// NitroPaint only consumes the count + the two relative offsets
		// (see McbkReadNmcr): nMultiCell at +0, offsetMultiCells at +4,
		// offsetHierarchy at +8. The +2 short and the +0xC / +0x10 dwords
		// are observed in the wild but ignored by the canonical parser.
		// Read them so we can preserve / round-trip them on save.
		int numberMultiCells = io.readUnsignedShort();
		marker = io.readUnsignedShort();
		int offsetDataMultiCell = io.readInt();
		int offsetDataMultiCellProperties = io.readInt();
		// The trailing two dwords are present in the layout used by
		// CTRMap-CE writes but may be absent in some authoring tools'
		// dumps; only consume them when they fit inside the block size.
		if (io.getPosition() - blockStart + 8 <= blockSize) {
			unknown1 = io.readInt();
			unknown2 = io.readInt();
		}

		int multiCellTableStart = containerStart + offsetDataMultiCell;
		int propertyTableStart = containerStart + offsetDataMultiCellProperties;

		// Read multi-cell entries (MULTI_CELL: 8 bytes each)
		int[] propOffsets = new int[numberMultiCells];
		io.seek(multiCellTableStart);
		for (int i = 0; i < numberMultiCells; i++) {
			NMCRMultiCell mc = new NMCRMultiCell();
			mc.numberDisplayedCells = io.readUnsignedShort();
			mc.numberLoadedCells = io.readUnsignedShort();
			propOffsets[i] = io.readInt();
			multiCells.add(mc);
		}

		// Read CELL_HIERARCHY nodes per multi-cell. The number of nodes
		// is numberDisplayedCells per NitroPaint's MULTI_CELL::nNodes.
		for (int i = 0; i < numberMultiCells; i++) {
			NMCRMultiCell mc = multiCells.get(i);
			io.seek(propertyTableStart + propOffsets[i]);
			for (int j = 0; j < mc.numberDisplayedCells; j++) {
				NMCRMultiCellProperty p = new NMCRMultiCellProperty();
				p.indexSequence = io.readUnsignedShort();
				p.translateX = io.readShort();
				p.translateY = io.readShort();
				p.frameMode = io.readUnsignedByte();
				p.uniqueID = io.readUnsignedByte();
				mc.properties.add(p);
			}
		}

		io.seek(blockStart + blockSize);
	}

	public void write(NTRDataIOStream io) throws IOException {
		int blockStart = io.getPosition();
		io.writeStringUnterminated(MAGIC);
		int blockSizePos = io.getPosition();
		io.writeInt(0);

		int numberMultiCells = multiCells.size();

		// Layout (matches real BW2 NMCR layout — verified by dumping
		// trainer-class-0 NMCR from a live ROM):
		//   container header is 0x14 (20) bytes:
		//     u16 numMultiCells + u16 marker + 3x u32 (2 offsets + 2 unknown)
		//   WAIT — 20 bytes fits 2 u16s + 4 u32s = 4 + 16 = 20 bytes:
		//     u16 numMC + u16 marker + u32 offMC + u32 offProps + u32 u1 + u32 u2
		//   Sequence table follows immediately (each entry 8 bytes).
		//
		// Hardcoding 0x18 here was the same bug we hit in NMAR/NANR —
		// the writer lied about where the entry table starts, so the
		// reader seeked 4 bytes past the actual entry data and saw
		// numberDisplayedCells = 0 (garbage), losing every multi-cell.
		int offsetDataMultiCell = 0x14;
		int offsetDataMultiCellProperties = offsetDataMultiCell + numberMultiCells * 8;

		io.writeShort(numberMultiCells);
		io.writeShort(BEEF);
		io.writeInt(offsetDataMultiCell);
		io.writeInt(offsetDataMultiCellProperties);
		io.writeInt(unknown1);
		io.writeInt(unknown2);

		// Multi-cell entry table
		int currentPropOffset = 0;
		for (NMCRMultiCell mc : multiCells) {
			io.writeShort(mc.numberDisplayedCells);
			io.writeShort(mc.numberLoadedCells);
			io.writeInt(currentPropOffset);
			currentPropOffset += mc.numberDisplayedCells * 8;
		}

		// Property data
		for (NMCRMultiCell mc : multiCells) {
			for (NMCRMultiCellProperty p : mc.properties) {
				io.writeShort(p.indexSequence);
				io.writeShort(p.translateX);
				io.writeShort(p.translateY);
				io.writeByte(p.frameMode);
				io.writeByte(p.uniqueID);
			}
		}

		// Pad to 4-byte alignment
		while ((io.getPosition() - blockStart) % 4 != 0) {
			io.writeByte((byte) 0);
		}

		int blockEnd = io.getPosition();
		io.seek(blockSizePos);
		io.writeInt(blockEnd - blockStart);
		io.seek(blockEnd);
	}

	public int getMultiCellCount() {
		return multiCells.size();
	}
}
