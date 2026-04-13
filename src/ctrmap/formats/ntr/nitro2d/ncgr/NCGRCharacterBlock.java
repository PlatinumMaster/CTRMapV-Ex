package ctrmap.formats.ntr.nitro2d.ncgr;

import ctrmap.formats.ntr.common.NTRDataIOStream;
import java.io.IOException;

/**
 * CHAR (Character) data block within an NCGR file.
 *
 * <p>Block magic in file: "RAHC" (CHAR reversed).</p>
 *
 * <pre>
 * 0x00 heightInTiles    int16    Sprite height in tiles; -1 indicates 1D mapping
 * 0x02 widthInTiles     int16    Sprite width in tiles; -1 indicates 1D mapping
 * 0x04 pixelFormat      uint32   3 = 4bpp (16 colors), 4 = 8bpp (256 colors)
 * 0x08 mappingType0     uint16   0 = 2D mapping; non-zero values (e.g. 0x10) = 1D mapping
 * 0x0A mappingType1     uint16   Used in combination with mappingType0
 * 0x0C type             uint32   1 = bitmap (linear raster) mode; 0 = tile (character) mode
 * 0x10 lengthDataImage  uint32   Image data size in bytes
 * 0x14 offsetDataImage  uint32   Offset to image data (relative to container start)
 * 0x18+ dataImage       uint8[]  Palette index data
 * </pre>
 *
 * <p>The {@code type} field at 0x0C is interpreted by NitroPaint as a bitmap
 * flag: when {@code type == 1} the pixel data is a flat linear raster of
 * {@code (tileWidth * 8)} by {@code (tileHeight * 8)} pixels rather than a
 * sequence of 8x8 tiles. We convert bitmap data to tile-major on read and
 * back to linear on write so the rest of the editor can treat all NCGRs
 * uniformly.</p>
 */
public class NCGRCharacterBlock {

	public static final String MAGIC = "RAHC";

	/** Width in tiles. May be -1 for 1D / lineal mapping. */
	public int tileWidth;

	/** Height in tiles. May be -1 for 1D / lineal mapping. */
	public int tileHeight;

	/** Pixel format: 3 = 4bpp (16 colors), 4 = 8bpp (256 colors). */
	public int format;

	/** Mapping type field 0 (0 = 2D, non-zero = 1D). */
	public int mappingType0;

	/** Mapping type field 1. */
	public int mappingType1;

	/**
	 * Raw value of the {@code type} field at 0x0C. {@code 1} signals bitmap
	 * mode (linear raster); {@code 0} is the standard tile / character mode.
	 */
	public int flags;

	/**
	 * {@code true} when the source NCGR stored its pixel data as a linear
	 * raster (bitmap mode). The in-memory {@link #tileData} buffer is always
	 * tile-major regardless; this flag is preserved so {@link #write} can
	 * round-trip the on-disk layout.
	 */
	public boolean bitmap;

	/** Tile-major indexed pixel data (8x8 tiles, row-major within each tile). */
	public byte[] tileData;

	public NCGRCharacterBlock() {
		tileData = new byte[0];
	}

	public NCGRCharacterBlock(NTRDataIOStream io) throws IOException {
		int blockStart = io.getPosition();
		String magic = io.readPaddedString(4);
		if (!MAGIC.equals(magic)) {
			throw new IOException("Expected CHAR (RAHC) magic, got: " + magic);
		}
		int blockSize = io.readInt();
		int containerStart = io.getPosition();

		tileHeight = io.readShort();
		tileWidth = io.readShort();
		format = io.readInt();
		mappingType0 = io.readUnsignedShort();
		mappingType1 = io.readUnsignedShort();
		flags = io.readInt();
		int lengthDataImage = io.readInt();
		int offsetDataImage = io.readInt();

		// Per NitroPaint: type==1 → bitmap (linear raster) mode
		bitmap = (flags == 1);

		// Seek to image data using offsetDataImage relative to container start
		io.seek(containerStart + offsetDataImage);

		if (lengthDataImage < 0) {
			lengthDataImage = 0;
		}
		byte[] rawData = new byte[lengthDataImage];
		io.read(rawData);

		// Guess dimensions when the file stores sentinel values. This
		// mirrors NitroPaint's ChrReadNcgr, which recomputes tilesX/tilesY
		// whenever the mapping is 1D OR the file-declared tile count
		// doesn't agree with the present tile data. Pokemon B/W sprite
		// NCGRs consistently store tileWidth=tileHeight=0xFFFF (-1) and
		// rely on this guess. Without it, downstream renderers see a
		// negative width and either fall back to defaults or (in bitmap
		// mode) skip the raster→tile conversion entirely.
		int bytesPerTile = format == 3 ? 32 : 64;
		int nPresentTiles = bytesPerTile > 0 ? (lengthDataImage / bytesPerTile) : 0;
		int declaredTileCount = (tileWidth > 0 && tileHeight > 0) ? (tileWidth * tileHeight) : -1;
		boolean isLinear = ((mappingType1 << 16) | mappingType0) != 0;
		if (isLinear || declaredTileCount != nPresentTiles) {
			int guessedW = guessWidth(nPresentTiles);
			tileWidth = guessedW;
			tileHeight = guessedW > 0 ? (nPresentTiles / guessedW) : 0;
		}

		// In bitmap mode the source bytes are a linear raster sized
		// (tileWidth*8) x (tileHeight*8) pixels. Convert to tile-major
		// so the rest of the editor sees a uniform layout.
		if (bitmap && tileWidth > 0 && tileHeight > 0) {
			tileData = bitmapRasterToTiles(rawData, tileWidth, tileHeight, format);
		} else {
			tileData = rawData;
		}

		// Skip to end of block
		io.seek(blockStart + blockSize);
	}

	/**
	 * Ports NitroPaint's {@code ChrGuessWidth} from {@code ncgr.c}. Returns
	 * the most sensible tile-grid width for a flat tile count when the
	 * source file did not store usable dimensions. Any multiple of 32 short-
	 * circuits to 32 (the NDS OAM/BG row width), matching what
	 * NitroPaint displays for every Pokemon B/W battle sprite.
	 */
	private static int guessWidth(int nTiles) {
		if (nTiles <= 0) {
			return 1;
		}
		if (nTiles % 32 == 0) {
			return 32;
		}
		int width = 1;
		for (int i = 1; i < nTiles; i++) {
			if (i * i > nTiles) {
				break;
			}
			if (nTiles % i == 0) {
				width = i;
			}
		}
		int height = nTiles / width;
		return width > height ? width : height;
	}

	public void write(NTRDataIOStream io) throws IOException {
		int blockStart = io.getPosition();
		io.writeStringUnterminated(MAGIC);
		int blockSizePos = io.getPosition();
		io.writeInt(0);

		io.writeShort(tileHeight);
		io.writeShort(tileWidth);
		io.writeInt(format);
		io.writeShort(mappingType0);
		io.writeShort(mappingType1);
		io.writeInt(flags);

		// If we read this as a bitmap, write it back as a linear raster so
		// the on-disk format matches what the source ROM expects.
		byte[] outBytes;
		if (bitmap && tileWidth > 0 && tileHeight > 0) {
			outBytes = tilesToBitmapRaster(tileData, tileWidth, tileHeight, format);
		} else {
			outBytes = tileData;
		}

		io.writeInt(outBytes.length);
		io.writeInt(0x18); // image data offset = container header size

		io.write(outBytes);

		int blockEnd = io.getPosition();
		io.seek(blockSizePos);
		io.writeInt(blockEnd - blockStart);
		io.seek(blockEnd);
	}

	/**
	 * Converts a linear bitmap raster to tile-major storage.
	 *
	 * <p>Implements NitroPaint's {@code ChrReadBitmap} algorithm: the source
	 * is treated as a single image of {@code (tilesX*8) x (tilesY*8)} pixels.
	 * Each output tile is an 8x8 sub-rectangle copied row by row.</p>
	 */
	private static byte[] bitmapRasterToTiles(byte[] raster, int tilesX, int tilesY, int format) {
		int totalTiles = tilesX * tilesY;
		if (format == 3) {
			// 4bpp: 4 bytes per row of 8 pixels, 32 bytes per tile.
			// Raster row stride = tilesX * 4 bytes.
			int rowStride = tilesX * 4;
			byte[] out = new byte[totalTiles * 32];
			for (int ty = 0; ty < tilesY; ty++) {
				for (int tx = 0; tx < tilesX; tx++) {
					int tileIdx = ty * tilesX + tx;
					int dstBase = tileIdx * 32;
					int srcCol = tx * 4;
					for (int row = 0; row < 8; row++) {
						int srcOff = (ty * 8 + row) * rowStride + srcCol;
						int dstOff = dstBase + row * 4;
						if (srcOff + 4 <= raster.length) {
							System.arraycopy(raster, srcOff, out, dstOff, 4);
						}
					}
				}
			}
			return out;
		} else {
			// 8bpp: 8 bytes per row of 8 pixels, 64 bytes per tile.
			int rowStride = tilesX * 8;
			byte[] out = new byte[totalTiles * 64];
			for (int ty = 0; ty < tilesY; ty++) {
				for (int tx = 0; tx < tilesX; tx++) {
					int tileIdx = ty * tilesX + tx;
					int dstBase = tileIdx * 64;
					int srcCol = tx * 8;
					for (int row = 0; row < 8; row++) {
						int srcOff = (ty * 8 + row) * rowStride + srcCol;
						int dstOff = dstBase + row * 8;
						if (srcOff + 8 <= raster.length) {
							System.arraycopy(raster, srcOff, out, dstOff, 8);
						}
					}
				}
			}
			return out;
		}
	}

	/**
	 * Inverse of {@link #bitmapRasterToTiles}: rebuilds a linear raster from
	 * the tile-major buffer for round-trip writing.
	 */
	private static byte[] tilesToBitmapRaster(byte[] tiles, int tilesX, int tilesY, int format) {
		if (format == 3) {
			int rowStride = tilesX * 4;
			byte[] out = new byte[rowStride * tilesY * 8];
			for (int ty = 0; ty < tilesY; ty++) {
				for (int tx = 0; tx < tilesX; tx++) {
					int tileIdx = ty * tilesX + tx;
					int srcBase = tileIdx * 32;
					int dstCol = tx * 4;
					for (int row = 0; row < 8; row++) {
						int srcOff = srcBase + row * 4;
						int dstOff = (ty * 8 + row) * rowStride + dstCol;
						if (srcOff + 4 <= tiles.length && dstOff + 4 <= out.length) {
							System.arraycopy(tiles, srcOff, out, dstOff, 4);
						}
					}
				}
			}
			return out;
		} else {
			int rowStride = tilesX * 8;
			byte[] out = new byte[rowStride * tilesY * 8];
			for (int ty = 0; ty < tilesY; ty++) {
				for (int tx = 0; tx < tilesX; tx++) {
					int tileIdx = ty * tilesX + tx;
					int srcBase = tileIdx * 64;
					int dstCol = tx * 8;
					for (int row = 0; row < 8; row++) {
						int srcOff = srcBase + row * 8;
						int dstOff = (ty * 8 + row) * rowStride + dstCol;
						if (srcOff + 8 <= tiles.length && dstOff + 8 <= out.length) {
							System.arraycopy(tiles, srcOff, out, dstOff, 8);
						}
					}
				}
			}
			return out;
		}
	}

	/**
	 * Returns the number of bytes per 8x8 tile.
	 */
	public int getBytesPerTile() {
		return format == 3 ? 32 : 64;
	}

	/**
	 * Returns {@code true} when the character data uses 1D / "lineal" OBJ
	 * mapping rather than the standard row-major 2D tile grid.
	 *
	 * <p>This is determined by the combined {@code mappingMode} field at
	 * offset 0x08 (the high half is {@code mappingType1}, the low half is
	 * {@code mappingType0}). 0 means the canonical 2D row-major layout used
	 * by NitroPaint's flat renderer; non-zero values such as 0x10 / 0x20 /
	 * 0x40 / 0x80 indicate 1D OBJ mapping with various boundary sizes.</p>
	 *
	 * <p>NitroPaint always renders tiles as a flat row-major grid regardless
	 * of mapping mode &mdash; the mapping mode only affects VRAM placement
	 * at runtime, not the in-memory storage layout &mdash; so most renderers
	 * can ignore this distinction.</p>
	 *
	 * @return {@code true} if the file declares 1D OBJ mapping; {@code false}
	 *         for canonical 2D mapping.
	 */
	public boolean isLinearMapped() {
		return ((mappingType1 << 16) | mappingType0) != 0;
	}

	/**
	 * Returns the total number of 8x8 tiles based on raw data size.
	 */
	public int getTileCount() {
		int bpt = getBytesPerTile();
		return bpt > 0 ? tileData.length / bpt : 0;
	}

	/**
	 * Reads the palette index of a pixel within an 8x8 tile.
	 */
	public int getPixel(int tileIdx, int x, int y) {
		if (format == 3) {
			int offset = tileIdx * 32 + y * 4 + x / 2;
			if (offset < 0 || offset >= tileData.length) {
				return 0;
			}
			int b = tileData[offset] & 0xFF;
			return (x & 1) == 0 ? (b & 0x0F) : ((b >> 4) & 0x0F);
		} else {
			int offset = tileIdx * 64 + y * 8 + x;
			if (offset < 0 || offset >= tileData.length) {
				return 0;
			}
			return tileData[offset] & 0xFF;
		}
	}

	/**
	 * Sets the palette index of a pixel within an 8x8 tile.
	 */
	public void setPixel(int tileIdx, int x, int y, int paletteIdx) {
		if (format == 3) {
			int offset = tileIdx * 32 + y * 4 + x / 2;
			if (offset < 0 || offset >= tileData.length) {
				return;
			}
			int b = tileData[offset] & 0xFF;
			if ((x & 1) == 0) {
				b = (b & 0xF0) | (paletteIdx & 0x0F);
			} else {
				b = (b & 0x0F) | ((paletteIdx & 0x0F) << 4);
			}
			tileData[offset] = (byte) b;
		} else {
			int offset = tileIdx * 64 + y * 8 + x;
			if (offset < 0 || offset >= tileData.length) {
				return;
			}
			tileData[offset] = (byte) (paletteIdx & 0xFF);
		}
	}
}
