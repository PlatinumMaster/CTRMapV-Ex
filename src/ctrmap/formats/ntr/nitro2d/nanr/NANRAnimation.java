package ctrmap.formats.ntr.nitro2d.nanr;

import java.util.ArrayList;
import java.util.List;

/**
 * A single animation sequence within a NANR resource.
 *
 * <p>Mirrors the ABNK "Sequence" entry: each animation has an
 * {@link #animationType} (which determines the layout of frame property data)
 * a {@link #cellType} (single cell or multi-cell), and a {@link #loopMode}.</p>
 */
public class NANRAnimation {

	/** Cell-only frames (Type 0). */
	public static final int ANIM_TYPE_CELL = 0;
	/** Affine transform frames (Type 1). */
	public static final int ANIM_TYPE_AFFINE = 1;
	/** Translation-only frames (Type 2). */
	public static final int ANIM_TYPE_TRANSLATE = 2;

	public static final int CELL_TYPE_SINGLE = 1;
	public static final int CELL_TYPE_MULTI = 2;

	public static final int LOOP_STOP = 1;
	public static final int LOOP_REPEAT = 2;
	public static final int LOOP_BACKWARD = 3;
	public static final int LOOP_PINGPONG = 4;

	/** Animation property type (0=cell, 1=affine, 2=translate). */
	public int animationType;

	/** Cell type (1=single cell, 2=multi-cell). */
	public int cellType;

	/** Loop mode (1=stop, 2=repeat, 3=backward, 4=ping-pong). */
	public int loopMode;

	/** The ordered list of frames in this animation sequence. */
	public List<NANRFrame> frames;

	public NANRAnimation() {
		frames = new ArrayList<>();
		animationType = ANIM_TYPE_CELL;
		cellType = CELL_TYPE_SINGLE;
		loopMode = LOOP_REPEAT;
	}

	public int getFrameCount() {
		return frames.size();
	}

	/** Sums each frame's duration to get the total run time in 60Hz ticks. */
	public int getTotalDuration() {
		int total = 0;
		for (NANRFrame frame : frames) {
			total += frame.duration;
		}
		return total;
	}
}
