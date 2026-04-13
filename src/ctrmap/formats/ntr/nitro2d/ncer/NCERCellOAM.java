package ctrmap.formats.ntr.nitro2d.ncer;

import ctrmap.formats.ntr.common.NTRDataIOStream;
import ctrmap.formats.ntr.nitro2d.common.GXObjShape;
import java.io.IOException;

/**
 * Individual OAM (Object Attribute Memory) entry within a cell.
 *
 * Each OAM describes a single hardware sprite object with position, shape,
 * tile reference, and rendering attributes. The NDS OAM consists of three
 * 16-bit attributes (6 bytes total).
 */
public class NCERCellOAM {

	/** Signed Y coordinate (8-bit range: -128 to 127). */
	public int y;

	/** Signed X coordinate (9-bit range: -256 to 255). */
	public int x;

	/** Whether rotation/scaling is enabled for this OBJ. */
	public boolean rotationScaling;

	/** Double-size flag (only meaningful when rotationScaling is true). */
	public boolean doubleSize;

	/** OBJ disable flag (only meaningful when rotationScaling is false). */
	public boolean objDisable;

	/** OBJ mode: 0=Normal, 1=Semi-transparent, 2=OBJ Window. */
	public int objMode;

	/** Mosaic enable flag. */
	public boolean mosaic;

	/** Color mode: false=16 colors (4bpp), true=256 colors (8bpp). */
	public boolean colorMode256;

	/** Shape index (0=Square, 1=Horizontal, 2=Vertical) from Attr0 bits 14-15. */
	public int shape;

	/** Size index (0-3) from Attr1 bits 14-15, combined with shape for dimensions. */
	public int size;

	/** Horizontal flip (only meaningful when rotationScaling is false). */
	public boolean flipH;

	/** Vertical flip (only meaningful when rotationScaling is false). */
	public boolean flipV;

	/** Rotation/Scaling parameter group index (only meaningful when rotationScaling is true). */
	public int rsParamIndex;

	/** Tile index (character name, 0-1023). */
	public int tileIndex;

	/** Priority relative to BG layers (0=highest, 3=lowest). */
	public int priority;

	/** Palette number for 16-color mode (0-15). */
	public int paletteIndex;

	public NCERCellOAM() {
	}

	/**
	 * Reads an OAM entry (6 bytes) from the given stream.
	 *
	 * @param io The data stream positioned at the start of the OAM attributes.
	 * @throws IOException If an I/O error occurs.
	 */
	public NCERCellOAM(NTRDataIOStream io) throws IOException {
		int attr0 = io.readShort() & 0xFFFF;
		int attr1 = io.readShort() & 0xFFFF;
		int attr2 = io.readShort() & 0xFFFF;

		// Attr0 decode
		y = (byte) (attr0 & 0xFF); // sign-extend 8-bit
		rotationScaling = ((attr0 >> 8) & 1) != 0;
		if (rotationScaling) {
			doubleSize = ((attr0 >> 9) & 1) != 0;
		} else {
			objDisable = ((attr0 >> 9) & 1) != 0;
		}
		objMode = (attr0 >> 10) & 3;
		mosaic = ((attr0 >> 12) & 1) != 0;
		colorMode256 = ((attr0 >> 13) & 1) != 0;
		shape = (attr0 >> 14) & 3;

		// Attr1 decode
		int rawX = attr1 & 0x1FF;
		if (rawX >= 256) {
			rawX -= 512;
		}
		x = rawX;
		if (rotationScaling) {
			rsParamIndex = (attr1 >> 9) & 0x1F;
		} else {
			flipH = ((attr1 >> 12) & 1) != 0;
			flipV = ((attr1 >> 13) & 1) != 0;
		}
		size = (attr1 >> 14) & 3;

		// Attr2 decode
		tileIndex = attr2 & 0x3FF;
		priority = (attr2 >> 10) & 3;
		paletteIndex = (attr2 >> 12) & 0xF;
	}

	/**
	 * Writes this OAM entry (6 bytes) to the given stream.
	 *
	 * @param io The data stream to write to.
	 * @throws IOException If an I/O error occurs.
	 */
	public void write(NTRDataIOStream io) throws IOException {
		// Encode Attr0
		int attr0 = (y & 0xFF);
		if (rotationScaling) {
			attr0 |= (1 << 8);
			if (doubleSize) {
				attr0 |= (1 << 9);
			}
		} else {
			if (objDisable) {
				attr0 |= (1 << 9);
			}
		}
		attr0 |= (objMode & 3) << 10;
		if (mosaic) {
			attr0 |= (1 << 12);
		}
		if (colorMode256) {
			attr0 |= (1 << 13);
		}
		attr0 |= (shape & 3) << 14;

		// Encode Attr1
		int attr1 = (x & 0x1FF);
		if (rotationScaling) {
			attr1 |= (rsParamIndex & 0x1F) << 9;
		} else {
			if (flipH) {
				attr1 |= (1 << 12);
			}
			if (flipV) {
				attr1 |= (1 << 13);
			}
		}
		attr1 |= (size & 3) << 14;

		// Encode Attr2
		int attr2 = (tileIndex & 0x3FF);
		attr2 |= (priority & 3) << 10;
		attr2 |= (paletteIndex & 0xF) << 12;

		io.writeShort(attr0);
		io.writeShort(attr1);
		io.writeShort(attr2);
	}

	/**
	 * Gets the OBJ shape descriptor for this OAM's shape and size fields.
	 *
	 * @return The matching GXObjShape, or null if the combination is invalid.
	 */
	public GXObjShape getObjShape() {
		return GXObjShape.fromBits(shape, size);
	}

	/**
	 * Gets the pixel width of this OAM entry.
	 *
	 * @return The width in pixels, or 8 if the shape is invalid.
	 */
	public int getWidth() {
		GXObjShape s = getObjShape();
		return s != null ? s.width : 8;
	}

	/**
	 * Gets the pixel height of this OAM entry.
	 *
	 * @return The height in pixels, or 8 if the shape is invalid.
	 */
	public int getHeight() {
		GXObjShape s = getObjShape();
		return s != null ? s.height : 8;
	}
}
