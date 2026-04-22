package ctrmap.formats.ntr.nitro2d.common;

/**
 * NDS OBJ shape and size lookup table (from GBATek).
 *
 * Shape (2 bits from Attr0) + Size (2 bits from Attr1) determine the pixel dimensions.
 */
public enum GXObjShape {
	// Square shapes
	SQUARE_8x8(0, 0, 8, 8),
	SQUARE_16x16(0, 1, 16, 16),
	SQUARE_32x32(0, 2, 32, 32),
	SQUARE_64x64(0, 3, 64, 64),

	// Horizontal shapes
	HORIZ_16x8(1, 0, 16, 8),
	HORIZ_32x8(1, 1, 32, 8),
	HORIZ_32x16(1, 2, 32, 16),
	HORIZ_64x32(1, 3, 64, 32),

	// Vertical shapes
	VERT_8x16(2, 0, 8, 16),
	VERT_8x32(2, 1, 8, 32),
	VERT_16x32(2, 2, 16, 32),
	VERT_32x64(2, 3, 32, 64);

	public final int shape;
	public final int size;
	public final int width;
	public final int height;

	private GXObjShape(int shape, int size, int width, int height) {
		this.shape = shape;
		this.size = size;
		this.width = width;
		this.height = height;
	}

	/**
	 * Resolves the OBJ shape from OAM attribute fields.
	 *
	 * @param shape 2-bit shape value from Attr0 bits 14-15.
	 * @param size  2-bit size value from Attr1 bits 14-15.
	 * @return The matching GXObjShape, or null if invalid.
	 */
	public static GXObjShape fromBits(int shape, int size) {
		for (GXObjShape s : values()) {
			if (s.shape == shape && s.size == size) {
				return s;
			}
		}
		return null;
	}

	/**
	 * Resolves the OBJ shape from pixel dimensions. Inverse of
	 * {@link #fromBits} — used by the NCER exporter to encode an OAM's
	 * {@code width}/{@code height} pair into the 2-bit shape + 2-bit
	 * size attribute fields.
	 *
	 * @return The matching shape, or {@link #SQUARE_8x8} as a safe
	 *         fallback for invalid combinations.
	 */
	public static GXObjShape fromDimensions(int width, int height) {
		for (GXObjShape s : values()) {
			if (s.width == width && s.height == height) {
				return s;
			}
		}
		return SQUARE_8x8;
	}

	/**
	 * Gets the width in 8x8 tiles.
	 */
	public int getTileWidth() {
		return width >> 3;
	}

	/**
	 * Gets the height in 8x8 tiles.
	 */
	public int getTileHeight() {
		return height >> 3;
	}

	/**
	 * Gets the total number of 8x8 tiles.
	 */
	public int getTileCount() {
		return getTileWidth() * getTileHeight();
	}
}
