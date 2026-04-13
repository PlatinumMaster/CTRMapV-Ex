package ctrmap.formats.ntr.nitro2d.nanr;

import ctrmap.formats.ntr.common.NTRDataIOStream;
import java.io.IOException;

/**
 * A single frame in a NANR cell animation sequence.
 *
 * <p>The frame entry on disk is 8 bytes:</p>
 * <pre>
 * 0x00 offsetProperties uint32   Property data offset (relative to property data area)
 * 0x04 durationInFrames uint16   Frame duration (60fps base)
 * 0x06 unknown0         int16    Always 0xBEEF marker
 * </pre>
 *
 * <p>The frame property block referenced by {@code offsetProperties} is one
 * of three layouts depending on the parent {@link NANRAnimation#animationType}:</p>
 * <ul>
 *   <li>Type 0 (cell-only): 2 bytes - cellIndex</li>
 *   <li>Type 1 (affine): 16 bytes - cellIndex, rotate, scaleW, scaleH, translateX, translateY</li>
 *   <li>Type 2 (translate): 8 bytes - cellIndex, unknown0, translateX, translateY</li>
 * </ul>
 */
public class NANRFrame {

	public static final int BEEF = 0xBEEF;

	/** Duration of this frame in 60Hz frames. */
	public int duration;

	/** Index of the cell to display during this frame. */
	public int cellIndex;

	/** Rotation angle (Type 1 only). 16-bit fixed point. */
	public int rotation;

	/** Width scale (Type 1 only) in 1.19.12 fixed point. */
	public int scaleW;

	/** Height scale (Type 1 only) in 1.19.12 fixed point. */
	public int scaleH;

	/** X translation (Types 1 and 2). */
	public short translateX;

	/** Y translation (Types 1 and 2). */
	public short translateY;

	/** Unknown property field (Type 2 only). */
	public int unknown0;

	public NANRFrame() {
		scaleW = 0x1000;
		scaleH = 0x1000;
	}

	/**
	 * Reads the frame property block from the stream at the current position.
	 *
	 * @param io            The stream positioned at the start of the property block.
	 * @param animationType The parent animation's animationType.
	 */
	public void readFrameProperties(NTRDataIOStream io, int animationType) throws IOException {
		switch (animationType) {
			case NANRAnimation.ANIM_TYPE_CELL:
				cellIndex = io.readUnsignedShort();
				break;
			case NANRAnimation.ANIM_TYPE_AFFINE:
				cellIndex = io.readUnsignedShort();
				rotation = io.readUnsignedShort();
				scaleW = io.readInt();
				scaleH = io.readInt();
				translateX = io.readShort();
				translateY = io.readShort();
				break;
			case NANRAnimation.ANIM_TYPE_TRANSLATE:
				cellIndex = io.readUnsignedShort();
				unknown0 = io.readUnsignedShort();
				translateX = io.readShort();
				translateY = io.readShort();
				break;
		}
	}

	/** Writes the frame property block. */
	public void writeFrameProperties(NTRDataIOStream io, int animationType) throws IOException {
		switch (animationType) {
			case NANRAnimation.ANIM_TYPE_CELL:
				io.writeShort(cellIndex);
				break;
			case NANRAnimation.ANIM_TYPE_AFFINE:
				io.writeShort(cellIndex);
				io.writeShort(rotation);
				io.writeInt(scaleW);
				io.writeInt(scaleH);
				io.writeShort(translateX);
				io.writeShort(translateY);
				break;
			case NANRAnimation.ANIM_TYPE_TRANSLATE:
				io.writeShort(cellIndex);
				io.writeShort(unknown0);
				io.writeShort(translateX);
				io.writeShort(translateY);
				break;
		}
	}

	/**
	 * Returns the byte size of the property block for the given animation type.
	 */
	public static int getPropertySize(int animationType) {
		switch (animationType) {
			case NANRAnimation.ANIM_TYPE_CELL:
				return 2;
			case NANRAnimation.ANIM_TYPE_AFFINE:
				return 16;
			case NANRAnimation.ANIM_TYPE_TRANSLATE:
				return 8;
			default:
				return 2;
		}
	}
}
