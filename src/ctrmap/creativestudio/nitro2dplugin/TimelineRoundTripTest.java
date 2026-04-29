package ctrmap.creativestudio.nitro2dplugin;

import ctrmap.creativestudio.ngcs2d.canvas.undo.InsertFrameAction;
import ctrmap.creativestudio.ngcs2d.canvas.undo.RemoveFrameAction;
import ctrmap.creativestudio.ngcs2d.canvas.undo.ResizeFrameDurationAction;
import ctrmap.creativestudio.ngcs2d.canvas.undo.SetFrameAffineAction;
import ctrmap.creativestudio.ngcs2d.res.Sprite2DAnimFrame;
import ctrmap.creativestudio.ngcs2d.res.Sprite2DCellAnimation;
import ctrmap.creativestudio.ngcs2d.res.Sprite2DMultiCellAnimation;

/**
 * Round-trip checks for the Phase-4 timeline undo actions. Each check
 * mutates a synthesised animation in memory, verifies the mutation,
 * and then undoes / redoes to confirm parity.
 *
 * <p>Also exercises the tick-to-frame mapping we rely on for playhead
 * scrubbing (identical logic in
 * {@code CellTrackComponent.frameIndexAtTick} and
 * {@code CS2DAnimControlPanel.seekToTick}).</p>
 *
 * <p>Run: {@code java ctrmap.creativestudio.nitro2dplugin.TimelineRoundTripTest}.
 * Prints {@code ALL TIMELINE TESTS PASSED} on success; throws
 * {@link AssertionError} on first failure.</p>
 */
public class TimelineRoundTripTest {

    public static void main(String[] args) {
        testResizeCellFrameDuration();
        testResizeMultiCellFrameDuration();
        testInsertCellFrame();
        testRemoveMultiCellFrame();
        testSetFrameAffineRotation();
        testSetFrameAffineScaleAndTranslate();
        testTickToFrameMapping();
        System.out.println("ALL TIMELINE TESTS PASSED");
    }

    static void testResizeCellFrameDuration() {
        Sprite2DAnimFrame f = new Sprite2DAnimFrame(3, 10);
        ResizeFrameDurationAction action =
            ResizeFrameDurationAction.forCellFrame(f, 10, 30);
        action.execute();
        assertEq("cell resize applied", 30, f.duration);
        action.undo();
        assertEq("cell resize undone", 10, f.duration);
        action.execute();
        assertEq("cell resize redone", 30, f.duration);
    }

    static void testResizeMultiCellFrameDuration() {
        Sprite2DMultiCellAnimation.MultiCellAnimFrame f =
            new Sprite2DMultiCellAnimation.MultiCellAnimFrame(2, 4);
        ResizeFrameDurationAction action =
            ResizeFrameDurationAction.forMultiCellFrame(f, 4, 16);
        action.execute();
        assertEq("mc resize applied", 16, f.duration);
        action.undo();
        assertEq("mc resize undone", 4, f.duration);
    }

    static void testInsertCellFrame() {
        Sprite2DCellAnimation anim = new Sprite2DCellAnimation("T");
        anim.frames.add(new Sprite2DAnimFrame(0, 4));
        anim.frames.add(new Sprite2DAnimFrame(1, 4));
        anim.frames.add(new Sprite2DAnimFrame(2, 4));
        Sprite2DAnimFrame newFrame = new Sprite2DAnimFrame(99, 7);
        InsertFrameAction<Sprite2DAnimFrame> action =
            new InsertFrameAction<>(anim.frames, 2, newFrame);
        action.execute();
        assertEq("insert grows list", 4, anim.frames.size());
        assertSame("inserted at requested index", newFrame, anim.frames.get(2));
        action.undo();
        assertEq("undo shrinks list", 3, anim.frames.size());
        // Frame instance that was at index 2 before insert still at 2.
        assertEq("original frame order restored", 2, anim.frames.get(2).cellIndex);
    }

    static void testRemoveMultiCellFrame() {
        Sprite2DMultiCellAnimation anim = new Sprite2DMultiCellAnimation("T");
        Sprite2DMultiCellAnimation.MultiCellAnimFrame f0 = new Sprite2DMultiCellAnimation.MultiCellAnimFrame(0, 1);
        Sprite2DMultiCellAnimation.MultiCellAnimFrame f1 = new Sprite2DMultiCellAnimation.MultiCellAnimFrame(1, 2);
        Sprite2DMultiCellAnimation.MultiCellAnimFrame f2 = new Sprite2DMultiCellAnimation.MultiCellAnimFrame(2, 3);
        anim.frames.add(f0);
        anim.frames.add(f1);
        anim.frames.add(f2);

        RemoveFrameAction<Sprite2DMultiCellAnimation.MultiCellAnimFrame> action =
            new RemoveFrameAction<>(anim.frames, f1);
        action.execute();
        assertEq("remove size", 2, anim.frames.size());
        assertSame("middle gone", f0, anim.frames.get(0));
        assertSame("tail shifted down", f2, anim.frames.get(1));
        action.undo();
        assertEq("undo restores size", 3, anim.frames.size());
        assertSame("undo restores middle at original idx", f1, anim.frames.get(1));
    }

    static void testSetFrameAffineRotation() {
        Sprite2DAnimFrame f = new Sprite2DAnimFrame(0, 4);
        // default rotation = 0
        SetFrameAffineAction action = new SetFrameAffineAction(
            f, SetFrameAffineAction.Field.ROTATION, 0f, 45f);
        action.execute();
        assertEq("rotation set", 45f, f.rotation);
        action.undo();
        assertEq("rotation undone", 0f, f.rotation);
    }

    static void testSetFrameAffineScaleAndTranslate() {
        Sprite2DAnimFrame f = new Sprite2DAnimFrame(0, 4);
        new SetFrameAffineAction(f, SetFrameAffineAction.Field.SCALE_X, 1f, 2f).execute();
        assertEq("scaleX", 2f, f.scaleX);
        new SetFrameAffineAction(f, SetFrameAffineAction.Field.SCALE_Y, 1f, 0.5f).execute();
        assertEq("scaleY", 0.5f, f.scaleY);
        new SetFrameAffineAction(f, SetFrameAffineAction.Field.TRANSLATE_X, 0f, -8f).execute();
        assertEq("translateX", -8f, f.translateX);
        new SetFrameAffineAction(f, SetFrameAffineAction.Field.TRANSLATE_Y, 0f, 24f).execute();
        assertEq("translateY", 24f, f.translateY);
    }

    /**
     * Playback uses a walk-the-durations algorithm to map absolute
     * tick → frame index. Both CellTrackComponent and
     * CS2DAnimControlPanel implement the same function; we mirror it
     * here as an isolated reference so their assumptions stay honest.
     */
    static void testTickToFrameMapping() {
        int[] durations = {4, 8, 2}; // total 14
        // frame 0 covers [0,4), frame 1 covers [4,12), frame 2 covers [12,14)
        assertEq("tick 0 → frame 0", 0, frameIdxAt(0, durations));
        assertEq("tick 3 → frame 0", 0, frameIdxAt(3, durations));
        assertEq("tick 4 → frame 1", 1, frameIdxAt(4, durations));
        assertEq("tick 11 → frame 1", 1, frameIdxAt(11, durations));
        assertEq("tick 12 → frame 2", 2, frameIdxAt(12, durations));
        assertEq("tick 13 → frame 2", 2, frameIdxAt(13, durations));
        // Past-the-end returns -1 per the helper's contract.
        assertEq("tick 14 → -1 (past end)", -1, frameIdxAt(14, durations));
    }

    // Reference impl — mirrors CellTrackComponent.frameIndexAtTick
    private static int frameIdxAt(long tick, int[] durations) {
        long acc = 0;
        for (int i = 0; i < durations.length; i++) {
            int dur = Math.max(1, durations[i]);
            if (tick >= acc && tick < acc + dur) return i;
            acc += dur;
        }
        return -1;
    }

    // --- Mini assertion helpers ---

    private static void assertEq(String what, int expected, int actual) {
        if (expected != actual) {
            throw new AssertionError(what + ": expected " + expected + " but got " + actual);
        }
    }

    private static void assertEq(String what, float expected, float actual) {
        if (Math.abs(expected - actual) > 1e-5f) {
            throw new AssertionError(what + ": expected " + expected + " but got " + actual);
        }
    }

    private static void assertSame(String what, Object expected, Object actual) {
        if (expected != actual) {
            throw new AssertionError(what + ": identity mismatch");
        }
    }
}
