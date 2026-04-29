package ctrmap.formats.ntr.nitro2d.nclr;

import ctrmap.formats.ntr.common.NTRDataIOStream;
import java.io.IOException;

/**
 * Optional PCMP (Palette Compression) data block within an NCLR file.
 *
 * <p>Block magic in file: "PMCP" (PCMP reversed). When present, this block
 * limits how many of the up-to-16 sub-palettes from the {@link NCLRPaletteBlock}
 * are actually used and provides an index map.</p>
 *
 * <pre>
 * 0x00 numberPalettes     uint16   Number of active palettes
 * 0x02 unknown0           uint16   Always 0xBEEF
 * 0x04 offsetDataIndices  uint32   Relative offset to the index table
 *        ...
 *      dataIndices        uint16[numberPalettes]
 * </pre>
 */
public class NCLRPCMPBlock {

	public static final String MAGIC = "PMCP";
	public static final int BEEF = 0xBEEF;

	/** Active palette count override. */
	public int numberPalettes;

	/** Sub-palette index table. */
	public int[] indices;

	public NCLRPCMPBlock() {
		indices = new int[0];
	}

	/**
	 * Reads a PCMP block from the given stream at the current position.
	 */
	public NCLRPCMPBlock(NTRDataIOStream io) throws IOException {
		int blockStart = io.getPosition();
		String magic = io.readPaddedString(4);
		if (!MAGIC.equals(magic)) {
			throw new IOException("Expected PCMP (PMCP) magic, got: " + magic);
		}
		int blockSize = io.readInt();
		int containerStart = io.getPosition();

		numberPalettes = io.readUnsignedShort();
		int marker = io.readUnsignedShort(); // expected 0xBEEF
		int offsetDataIndices = io.readInt();

		io.seek(containerStart + offsetDataIndices);
		indices = new int[numberPalettes];
		for (int i = 0; i < numberPalettes; i++) {
			indices[i] = io.readUnsignedShort();
		}

		io.seek(blockStart + blockSize);
	}

	/**
	 * Writes this PCMP block to the given stream.
	 */
	public void write(NTRDataIOStream io) throws IOException {
		int blockStart = io.getPosition();
		io.writeStringUnterminated(MAGIC);
		int blockSizePos = io.getPosition();
		io.writeInt(0); // block size placeholder

		io.writeShort(numberPalettes);
		io.writeShort(BEEF);
		io.writeInt(8); // offset to index table (= header field size)

		for (int idx : indices) {
			io.writeShort(idx);
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
}
