package ctrmap.formats.ntr.nitro2d.nmar;

import java.util.ArrayList;
import java.util.List;

/**
 * A single multi-cell animation sequence within an NMAR.
 *
 * <p>Mirrors the ABNK Sequence layout (which NMAR shares with NANR), but
 * frames reference multi-cell indices.</p>
 */
public class NMARAnimation {

	public static final int ANIM_TYPE_CELL = 0;
	public static final int ANIM_TYPE_AFFINE = 1;
	public static final int ANIM_TYPE_TRANSLATE = 2;

	public static final int CELL_TYPE_SINGLE = 1;
	public static final int CELL_TYPE_MULTI = 2;

	public static final int LOOP_STOP = 1;
	public static final int LOOP_REPEAT = 2;
	public static final int LOOP_BACKWARD = 3;
	public static final int LOOP_PINGPONG = 4;

	/** Animation property type (0=cell, 1=affine, 2=translate). */
	public int animationType;

	/** Cell type (1=single cell, 2=multi-cell). NMAR uses CELL_TYPE_MULTI. */
	public int cellType;

	/** Loop mode (1=stop, 2=repeat, 3=backward, 4=ping-pong). */
	public int loopMode;

	/** The list of frames in this animation. */
	public List<NMARFrame> frames;

	public NMARAnimation() {
		frames = new ArrayList<>();
		animationType = ANIM_TYPE_CELL;
		cellType = CELL_TYPE_MULTI;
		loopMode = LOOP_REPEAT;
	}

	public int getFrameCount() {
		return frames.size();
	}

	public int getTotalDuration() {
		int total = 0;
		for (NMARFrame f : frames) {
			total += f.duration;
		}
		return total;
	}
}
