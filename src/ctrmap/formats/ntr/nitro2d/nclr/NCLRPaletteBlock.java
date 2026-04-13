package ctrmap.formats.ntr.nitro2d.nclr;

import ctrmap.formats.ntr.common.NTRDataIOStream;
import java.io.IOException;

/**
 * PLTT (Palette) data block within an NCLR file.
 *
 * <p>Block magic in file: "TTLP" (PLTT reversed). The container layout is:</p>
 * <pre>
 * 0x00 dimensions       uint64   Bit depth + extPalette flag (see lookup table)
 * 0x08 lengthDataColor  uint32   Length in bytes of the BGR555 color data
 * 0x0C offsetDataColor  uint32   Offset (relative to container start) of color data
 * </pre>
 *
 * <p>The {@code dimensions} field uses a small lookup table:</p>
 * <ul>
 *   <li>0x00000000_000A0004: 1 palette x 16 colors</li>
 *   <li>0x00000000_00000004: 1 palette x 256 colors</li>
 *   <li>0x00000000_00000003: up to 16 palettes x 16 colors</li>
 *   <li>0x00000001_00000004: up to 16 palettes x 256 colors</li>
 * </ul>
 */
public class NCLRPaletteBlock {

	public static final String MAGIC = "TTLP";

	/** Common dimension lookup values from the documentation. */
	public static final long DIM_1PAL_16COL  = 0x00000000_000A0004L;
	public static final long DIM_1PAL_256COL = 0x00000000_00000004L;
	public static final long DIM_NPAL_16COL  = 0x00000000_00000003L;
	public static final long DIM_NPAL_256COL = 0x00000001_00000004L;

	/**
	 * Raw 64-bit "dimensions" field that encodes bit depth, palette count
	 * limit, and extended palette flag.
	 */
	public long dimensions;

	/**
	 * Bit depth of the palette: 4 (16 colors per palette) or 8 (256 colors per palette).
	 */
	public int bitDepth;

	/**
	 * True when the upper 32-bit half of {@link #dimensions} is non-zero
	 * ("extended palette" flag).
	 */
	public boolean extPalette;

	/**
	 * Raw BGR555 color data (16 bits per color).
	 */
	public short[] colors;

	public NCLRPaletteBlock() {
		colors = new short[0];
		dimensions = DIM_NPAL_16COL;
		bitDepth = 4;
	}

	/**
	 * Reads a PLTT block from the given stream at the current position.
	 */
	public NCLRPaletteBlock(NTRDataIOStream io) throws IOException {
		int blockStart = io.getPosition();
		String magic = io.readPaddedString(4);
		if (!MAGIC.equals(magic)) {
			throw new IOException("Expected PLTT (TTLP) magic, got: " + magic);
		}
		int blockSize = io.readInt();
		int containerStart = io.getPosition();

		int dimensionsLow = io.readInt();
		int dimensionsHigh = io.readInt();
		dimensions = ((long) dimensionsHigh << 32) | (dimensionsLow & 0xFFFFFFFFL);

		int lengthDataColor = io.readInt();
		int offsetDataColor = io.readInt();

		extPalette = dimensionsHigh != 0;
		bitDepth = decodeBitDepth(dimensions);

		// Color data is referenced by offsetDataColor relative to container start
		io.seek(containerStart + offsetDataColor);

		int colorCount = lengthDataColor / 2;
		if (colorCount < 0) {
			colorCount = 0;
		}
		colors = new short[colorCount];
		for (int i = 0; i < colorCount; i++) {
			colors[i] = io.readShort();
		}

		// Skip to end of block (in case there is trailing data)
		io.seek(blockStart + blockSize);
	}

	/**
	 * Decodes the bit depth from the dimensions field.
	 *
	 * <p>The standard cases are:</p>
	 * <ul>
	 *   <li>Low byte == 3: 4bpp (16 colors per palette)</li>
	 *   <li>Low byte == 4: 8bpp (256 colors per palette), unless the
	 *       upper 16 bits of the low word are non-zero (e.g. 0x000A0004),
	 *       which forces 16 colors per palette ("4bpp with format-4 marker")</li>
	 * </ul>
	 */
	public static int decodeBitDepth(long dimensions) {
		int low = (int) (dimensions & 0xFFFFFFFFL);
		int formatByte = low & 0xFF;
		if (formatByte == 3) {
			return 4;
		}
		if (formatByte == 4) {
			if ((low & 0xFFFF0000) != 0) {
				return 4; // 0x000A0004 special case
			}
			return 8;
		}
		return 4;
	}

	/**
	 * Returns the number of colors per sub-palette for this bit depth.
	 */
	public int getColorsPerPalette() {
		return bitDepth == 4 ? 16 : 256;
	}

	/**
	 * Returns the number of sub-palettes assuming the standard packing.
	 */
	public int getPaletteCount() {
		int cpp = getColorsPerPalette();
		return cpp > 0 ? colors.length / cpp : 0;
	}

	/**
	 * Writes this PLTT block to the given stream.
	 */
	public void write(NTRDataIOStream io) throws IOException {
		int blockStart = io.getPosition();
		io.writeStringUnterminated(MAGIC);
		int blockSizePos = io.getPosition();
		io.writeInt(0); // block size placeholder

		int containerStart = io.getPosition();

		// dimensions (uint64)
		io.writeInt((int) (dimensions & 0xFFFFFFFFL));
		io.writeInt((int) (dimensions >>> 32));
		io.writeInt(colors.length * 2);                // lengthDataColor
		io.writeInt(0x10);                              // offsetDataColor (= sizeof(header fields))

		for (short c : colors) {
			io.writeShort(c);
		}

		int blockEnd = io.getPosition();
		io.seek(blockSizePos);
		io.writeInt(blockEnd - blockStart);
		io.seek(blockEnd);
	}
}
