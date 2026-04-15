package ctrmap.formats.ntr.nitro2d.ncer;

import ctrmap.formats.ntr.common.NTRDataIOStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * CEBK (Cell Bank) data block within an NCER file.
 *
 * <p>Block magic in file: "KBEC" (CEBK reversed). Container layout matches
 * NitroPaint's {@code CellReadNcer}:</p>
 *
 * <pre>
 * 0x00 numberCells       uint16   Total cell count
 * 0x02 bankAttribs       uint16   Cell flags (bit 0 = bounding box present)
 * 0x04 offsetDataCell    uint32   Cell table offset (relative to container start)
 * 0x08 mappingMode       uint32   Compact OBJ VRAM mapping index (0..4)
 * 0x0C vramTransferOff   uint32   Optional VRAM transfer block offset
 * 0x10 reserved          uint32
 * 0x14 userExtendedOff   uint32
 * </pre>
 *
 * <p>The on-disk {@code mappingMode} field is NOT the raw NDS hardware
 * {@code GX_OBJVRAMMODE_CHAR_*} constant — it is a compact 5-value enum
 * matching NitroPaint's {@code mappingModes[]} table in {@code ncer.c}:</p>
 * <ul>
 *   <li>{@code 0} = 1D, 32 byte boundary (GX_OBJVRAMMODE_CHAR_1D_32K)</li>
 *   <li>{@code 1} = 1D, 64 byte boundary (GX_OBJVRAMMODE_CHAR_1D_64K)</li>
 *   <li>{@code 2} = 1D, 128 byte boundary (GX_OBJVRAMMODE_CHAR_1D_128K)</li>
 *   <li>{@code 3} = 1D, 256 byte boundary (GX_OBJVRAMMODE_CHAR_1D_256K)</li>
 *   <li>{@code 4} = 2D mapping (GX_OBJVRAMMODE_CHAR_2D)</li>
 * </ul>
 *
 * <p>This encoding exactly matches our internal {@link #MAPPING_MODE_1D_32K}
 * .. {@link #MAPPING_MODE_2D} constants, so the read/write code stores the
 * value verbatim. (Note: the NCGR CHAR block uses a DIFFERENT encoding —
 * the raw {@code GX_OBJVRAMMODE_CHAR_*} hardware constant — handled in
 * {@link ctrmap.formats.ntr.nitro2d.ncgr.NCGRCharacterBlock}.)</p>
 *
 * <p>Each cell entry is 8 bytes (bankAttribs=0) or 16 bytes (bankAttribs=1):</p>
 * <pre>
 * 0x00 numberObjects   uint16   Number of OAM objects in this cell
 * 0x02 cellAttr        uint16   Cell attributes (passed through verbatim)
 * 0x04 offsetObject    uint32   Offset to OAM data (relative to OAM data area start)
 *
 * (bankAttribs=1 only)
 * 0x08 boundRight      int16
 * 0x0A boundBottom     int16
 * 0x0C boundLeft       int16
 * 0x0E boundTop        int16
 * </pre>
 */
public class NCERCellBank {

	public static final String MAGIC = "KBEC";

	/** 1D mapping mode index, 32 byte char boundary (32 bytes per char). */
	public static final int MAPPING_MODE_1D_32K = 0;
	/** 1D mapping mode index, 64 byte char boundary (64 bytes per char). */
	public static final int MAPPING_MODE_1D_64K = 1;
	/** 1D mapping mode index, 128 byte char boundary. */
	public static final int MAPPING_MODE_1D_128K = 2;
	/** 1D mapping mode index, 256 byte char boundary. */
	public static final int MAPPING_MODE_1D_256K = 3;
	/** 2D mapping mode (32-char-wide grid). */
	public static final int MAPPING_MODE_2D = 4;

	/** True when each cell entry contains a bounding box (16 bytes vs 8). */
	public boolean useBounds;

	/**
	 * OBJ VRAM mapping mode (0..4). See class doc for values; defaults to
	 * {@link #MAPPING_MODE_2D} for new banks since modern bitmap-mode NCGRs
	 * pair naturally with 2D OAM addressing.
	 */
	public int mappingMode = MAPPING_MODE_2D;

	/** Optional VRAM transfer block offset (raw value, may be 0xFFFFFFFF). */
	public int vramTransferOffset;

	/** Reserved field (offset 0x10), preserved for round-trip writes. */
	public int reserved;

	/** Optional user extended attribute block offset. */
	public int userExtendedOffset;

	/** The list of cells in this bank. */
	public List<NCERCell> cells;

	public NCERCellBank() {
		cells = new ArrayList<>();
	}

	public NCERCellBank(NTRDataIOStream io) throws IOException {
		cells = new ArrayList<>();

		int blockStart = io.getPosition();
		String magic = io.readPaddedString(4);
		if (!MAGIC.equals(magic)) {
			throw new IOException("Expected CEBK (KBEC) magic, got: " + magic);
		}
		int blockSize = io.readInt();
		int containerStart = io.getPosition();

		int cellCount = io.readUnsignedShort();
		int bankAttribs = io.readUnsignedShort();
		useBounds = (bankAttribs & 1) != 0;
		int offsetDataCell = io.readInt();
		// On disk this is already a compact 0..4 index matching
		// NitroPaint's mappingModes[] table in ncer.c, which lines up
		// 1:1 with our MAPPING_MODE_* constants (0..3 = 1D 32K..256K,
		// 4 = 2D). Clamp out-of-range values to 1D 32K like NitroPaint.
		int rawMapping = io.readInt();
		mappingMode = (rawMapping >= 0 && rawMapping < 5) ? rawMapping : MAPPING_MODE_1D_32K;
		vramTransferOffset = io.readInt();
		reserved = io.readInt();
		userExtendedOffset = io.readInt();

		// Cell entry table
		int cellEntryStart = containerStart + offsetDataCell;
		int cellEntrySize = useBounds ? 16 : 8;
		io.seek(cellEntryStart);

		int[] objCounts = new int[cellCount];
		int[] objOffsets = new int[cellCount];
		for (int i = 0; i < cellCount; i++) {
			NCERCell cell = new NCERCell();
			objCounts[i] = io.readUnsignedShort();
			cell.cellAttr = io.readUnsignedShort();
			objOffsets[i] = io.readInt();

			if (useBounds) {
				cell.xMax = io.readShort(); // boundRight
				cell.yMax = io.readShort(); // boundBottom
				cell.xMin = io.readShort(); // boundLeft
				cell.yMin = io.readShort(); // boundTop
			}
			cells.add(cell);
		}

		// OAM data area starts immediately after all cell entries
		int oamDataAreaStart = cellEntryStart + cellCount * cellEntrySize;

		// Read OAM data for each cell
		for (int i = 0; i < cellCount; i++) {
			NCERCell cell = cells.get(i);
			io.seek(oamDataAreaStart + objOffsets[i]);
			for (int j = 0; j < objCounts[i]; j++) {
				cell.addOAM(new NCERCellOAM(io));
			}
		}

		// Optional VRAM transfer sub-block. Ports NitroPaint's CellReadNcer
		// VRAM transfer path (ncer.c). When present, each cell gets a
		// (srcAddr, size) pair that the renderer uses to remap OAM tile
		// indices into a different part of the NCGR — the NDS game engine
		// would DMA-copy `size` bytes starting at `srcAddr` into OBJ VRAM,
		// so from the OAM's point of view tile 0 is NCGR byte `srcAddr`.
		// Pokemon B/W battle sprite NCERs rely on this heavily: Cell_0 may
		// look at tiles 6 and 32, but those are OFFSETS past srcAddr, not
		// absolute NCGR positions.
		if (vramTransferOffset != 0 && vramTransferOffset != -1) {
			int saved = io.getPosition();
			int vramBlockStart = containerStart + vramTransferOffset;
			io.seek(vramBlockStart);
			int maxTransfer = io.readInt();        // engine's max bytes-per-transfer limit
			int dataOffsetRel = io.readInt();      // delta from vramBlockStart to per-cell data
			io.seek(vramBlockStart + dataOffsetRel);
			for (int i = 0; i < cellCount; i++) {
				NCERCell cell = cells.get(i);
				cell.vramTransferSrcAddr = io.readInt();
				cell.vramTransferSize = io.readInt();
				cell.hasVramTransfer = true;
			}
			io.seek(saved);
		}

		io.seek(blockStart + blockSize);
	}

	public void write(NTRDataIOStream io) throws IOException {
		int blockStart = io.getPosition();
		io.writeStringUnterminated(MAGIC);
		int blockSizePos = io.getPosition();
		io.writeInt(0);

		int cellCount = cells.size();
		int cellEntrySize = useBounds ? 16 : 8;

		io.writeShort(cellCount);
		io.writeShort(useBounds ? 1 : 0);
		io.writeInt(0x18);                  // offsetDataCell = right after this header
		// Write the compact 0..4 index back; the on-disk format
		// matches NitroPaint's mappingModes[] table exactly.
		io.writeInt(mappingMode);
		io.writeInt(vramTransferOffset);
		io.writeInt(reserved);
		io.writeInt(userExtendedOffset);

		// Compute object offsets within OAM data area
		int[] objOffsets = new int[cellCount];
		int currentOffset = 0;
		for (int i = 0; i < cellCount; i++) {
			objOffsets[i] = currentOffset;
			currentOffset += cells.get(i).getOAMCount() * 6;
		}

		// Write cell entry table
		for (int i = 0; i < cellCount; i++) {
			NCERCell cell = cells.get(i);
			io.writeShort(cell.getOAMCount());
			io.writeShort(cell.cellAttr);
			io.writeInt(objOffsets[i]);
			if (useBounds) {
				io.writeShort(cell.xMax);
				io.writeShort(cell.yMax);
				io.writeShort(cell.xMin);
				io.writeShort(cell.yMin);
			}
		}

		// Write OAM data
		for (NCERCell cell : cells) {
			for (NCERCellOAM oam : cell.oams) {
				oam.write(io);
			}
		}

		int blockEnd = io.getPosition();
		io.seek(blockSizePos);
		io.writeInt(blockEnd - blockStart);
		io.seek(blockEnd);
	}

	public int getCellCount() {
		return cells.size();
	}
}
