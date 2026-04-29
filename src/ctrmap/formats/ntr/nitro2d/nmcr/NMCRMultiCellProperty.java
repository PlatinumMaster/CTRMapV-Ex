package ctrmap.formats.ntr.nitro2d.nmcr;

/**
 * A single MultiCell Property entry referenced by a {@link NMCRMultiCell}.
 *
 * <p>On-disk layout (8 bytes):</p>
 * <pre>
 * 0x00 indexSequence uint16   Index into the NANR sequence table
 * 0x02 translateX    int16    Local X position
 * 0x04 translateY    int16    Local Y position
 * 0x06 frameMode     uint8    0x20 = reset frame counter, 0x21 = continue
 * 0x07 uniqueID      uint8    Local unique multi-cell index (no duplicates)
 * </pre>
 */
public class NMCRMultiCellProperty {

	public static final int FRAME_MODE_RESET = 0x20;
	public static final int FRAME_MODE_CONTINUE = 0x21;

	/** Sequence index into the NANR animation bank. */
	public int indexSequence;

	/** X translation within the multi-cell. */
	public short translateX;

	/** Y translation within the multi-cell. */
	public short translateY;

	/** Frame mode (0x20 = reset, 0x21 = continue). */
	public int frameMode;

	/** Local unique ID (per-multi-cell). */
	public int uniqueID;

	public NMCRMultiCellProperty() {
		frameMode = FRAME_MODE_RESET;
	}
}
