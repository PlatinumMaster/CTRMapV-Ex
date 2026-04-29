package ctrmap.formats.ntr.nitro2d.nmar;

import ctrmap.formats.ntr.common.NTRDataIOStream;
import java.io.IOException;

/**
 * A single frame in a multi-cell animation sequence.
 *
 * <p>Each frame entry on disk is identical to a NANR frame entry (8 bytes:
 * offsetProperties + duration + 0xBEEF). The property block referenced by
 * offsetProperties differs by animationType:</p>
 * <ul>
 *   <li>Type 0: 2 bytes - multiCellIndex</li>
 *   <li>Type 1: 16 bytes - multiCellIndex, rotate, scaleW, scaleH, translateX, translateY</li>
 *   <li>Type 2: 8 bytes - multiCellIndex, unknown0, translateX, translateY</li>
 * </ul>
 */
public class NMARFrame {

	public static final int BEEF = 0xBEEF;

	/** Duration of this frame in 60Hz frames. */
	public int duration;

	/** Multi-cell index referenced by this frame. */
	public int multiCellIndex;

	/** Rotation (Type 1 only). */
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

	public NMARFrame() {
		scaleW = 0x1000;
		scaleH = 0x1000;
	}

	public void readFrameProperties(NTRDataIOStream io, int animationType) throws IOException {
		switch (animationType) {
			case NMARAnimation.ANIM_TYPE_CELL:
				multiCellIndex = io.readUnsignedShort();
				break;
			case NMARAnimation.ANIM_TYPE_AFFINE:
				multiCellIndex = io.readUnsignedShort();
				rotation = io.readUnsignedShort();
				scaleW = io.readInt();
				scaleH = io.readInt();
				translateX = io.readShort();
				translateY = io.readShort();
				break;
			case NMARAnimation.ANIM_TYPE_TRANSLATE:
				multiCellIndex = io.readUnsignedShort();
				unknown0 = io.readUnsignedShort();
				translateX = io.readShort();
				translateY = io.readShort();
				break;
		}
	}

	public void writeFrameProperties(NTRDataIOStream io, int animationType) throws IOException {
		switch (animationType) {
			case NMARAnimation.ANIM_TYPE_CELL:
				io.writeShort(multiCellIndex);
				break;
			case NMARAnimation.ANIM_TYPE_AFFINE:
				io.writeShort(multiCellIndex);
				io.writeShort(rotation);
				io.writeInt(scaleW);
				io.writeInt(scaleH);
				io.writeShort(translateX);
				io.writeShort(translateY);
				break;
			case NMARAnimation.ANIM_TYPE_TRANSLATE:
				io.writeShort(multiCellIndex);
				io.writeShort(unknown0);
				io.writeShort(translateX);
				io.writeShort(translateY);
				break;
		}
	}

	public static int getPropertySize(int animationType) {
		switch (animationType) {
			case NMARAnimation.ANIM_TYPE_CELL:
				return 2;
			case NMARAnimation.ANIM_TYPE_AFFINE:
				return 16;
			case NMARAnimation.ANIM_TYPE_TRANSLATE:
				return 8;
			default:
				return 2;
		}
	}
}
