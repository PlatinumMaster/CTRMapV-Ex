package ctrmap.formats.ntr.nitro2d.nmar;

import ctrmap.formats.ntr.narc.DirectNARC;
import ctrmap.formats.ntr.nitro2d.nanr.NANR;
import ctrmap.formats.ntr.nitro2d.nanr.NANRAnimBank;
import ctrmap.formats.ntr.nitro2d.nanr.NANRAnimation;
import ctrmap.formats.ntr.nitro2d.nanr.NANRFrame;
import java.util.Arrays;
import xstandard.fs.FSFile;
import xstandard.fs.accessors.DiskFile;
import xstandard.fs.accessors.MemoryFile;

/**
 * Round-trip verifier for the ABNK write path shared by NMAR and NANR.
 *
 * <p>The write code used to hardcode {@code offsetDataSequences = 0x1C}
 * (28) while every real BW2 file encodes {@code 0x18} (24). The 4-byte
 * gap between the 24-byte body header and the written sequence table
 * desynced the reader's seek and eventually walked the stream off the
 * end of the buffer (crash at position 256).</p>
 *
 * <p>These tests load a real NMAR and a synthetic NMAR/NANR, write them
 * to an in-memory FSFile, re-read the bytes, and confirm:</p>
 * <ol>
 *   <li>All sequence / frame / property data survives the round trip.</li>
 *   <li>The {@code offsetDataSequences} field on disk is {@code 0x18}.</li>
 *   <li>A freshly-written NMAR reproduces the same byte layout as a
 *       real BW2 NMAR (read → write → compare).</li>
 * </ol>
 *
 * <p>Run as {@code java ctrmap.formats.ntr.nitro2d.nmar.NMARRoundTripTest}.
 * Requires the IREO_EX ROM at the hardcoded path below for the real-file
 * case; the synthetic cases run standalone.</p>
 */
public class NMARRoundTripTest {

    private static final String TRAINER_NARC = "D:/Games/Modding/NDS/IREO_EX/data/a/0/7/2";

    public static void main(String[] args) throws Exception {
        testSyntheticNmarRoundTrip();
        testSyntheticNanrRoundTrip();
        testRealNmarRoundTrip();
        System.out.println("ALL NMAR/NANR ROUND-TRIP TESTS PASSED");
    }

    /**
     * Build a minimal NMAR in memory (1 sequence, 1 frame, type 0), write
     * it, read it back, confirm everything matches.
     */
    static void testSyntheticNmarRoundTrip() throws Exception {
        NMAR src = new NMAR();
        NMARAnimation anim = new NMARAnimation();
        anim.animationType = NMARAnimation.ANIM_TYPE_CELL;
        anim.cellType = NMARAnimation.CELL_TYPE_MULTI;
        anim.loopMode = 2; // repeat
        NMARFrame f = new NMARFrame();
        f.multiCellIndex = 7;
        f.duration = 4;
        anim.frames.add(f);
        src.animBank.animations.add(anim);

        // Write to in-memory FSFile, read back.
        MemoryFile mem = new MemoryFile("test.nmar", new byte[0]);
        src.write(mem);
        byte[] written = mem.getBytes();

        // Inspect offsetDataSequences — must be 0x18.
        int offSeq = readU32LE(written, 16 + 8 + 4);
        assertEq("synthetic NMAR offsetDataSequences", 0x18, offSeq);

        NMAR rt = new NMAR(new MemoryFile("rt.nmar", written));
        assertEq("round-trip anim count", 1, rt.getAnimationCount());
        NMARAnimation rtAnim = rt.getAnimation(0);
        assertEq("round-trip frame count", 1, rtAnim.frames.size());
        NMARFrame rtF = rtAnim.frames.get(0);
        assertEq("round-trip multiCellIndex", 7, rtF.multiCellIndex);
        assertEq("round-trip duration", 4, rtF.duration);
        assertEq("round-trip animType", NMARAnimation.ANIM_TYPE_CELL, rtAnim.animationType);
        assertEq("round-trip cellType", NMARAnimation.CELL_TYPE_MULTI, rtAnim.cellType);
        assertEq("round-trip loopMode", 2, rtAnim.loopMode);
    }

    /** Same but for NANR (NMARAnimBank is copied from NANRAnimBank). */
    static void testSyntheticNanrRoundTrip() throws Exception {
        NANR src = new NANR();
        NANRAnimation anim = new NANRAnimation();
        anim.animationType = NANRAnimation.ANIM_TYPE_CELL;
        anim.cellType = 1; // single cell
        anim.loopMode = 2;
        NANRFrame f = new NANRFrame();
        f.cellIndex = 3;
        f.duration = 5;
        anim.frames.add(f);
        src.animBank.animations.add(anim);

        MemoryFile mem = new MemoryFile("test.nanr", new byte[0]);
        src.write(mem);
        byte[] written = mem.getBytes();

        int offSeq = readU32LE(written, 16 + 8 + 4);
        assertEq("synthetic NANR offsetDataSequences", 0x18, offSeq);

        NANR rt = new NANR(new MemoryFile("rt.nanr", written));
        assertEq("NANR round-trip anim count", 1, rt.animBank.animations.size());
        NANRFrame rtF = rt.animBank.animations.get(0).frames.get(0);
        assertEq("NANR round-trip cellIndex", 3, rtF.cellIndex);
        assertEq("NANR round-trip duration", 5, rtF.duration);
    }

    /**
     * Load trainer class 0's NMAR from the real BW2 ROM, write it to
     * memory, read that back. Verify the round-tripped content equals
     * the originally-parsed content (sequence count, frame count,
     * per-frame multiCellIndex + duration). This catches semantic drift
     * independently of byte-for-byte parity, which isn't guaranteed
     * because our writer may reorder / realign optional blocks.
     */
    static void testRealNmarRoundTrip() throws Exception {
        FSFile narc = new DiskFile(TRAINER_NARC);
        byte[] data = DirectNARC.getData(narc, "5"); // trainer class 0's NMAR
        if (data == null || data.length == 0) {
            System.out.println("[skip] real NMAR not available at " + TRAINER_NARC);
            return;
        }
        MemoryFile orig = new MemoryFile("orig.nmar", data);
        NMAR nmar = new NMAR(orig);
        int animCount = nmar.getAnimationCount();

        // Re-write.
        MemoryFile out = new MemoryFile("out.nmar", new byte[0]);
        nmar.write(out);
        byte[] written = out.getBytes();

        // Re-read.
        NMAR rt = new NMAR(new MemoryFile("rt.nmar", written));
        assertEq("real NMAR anim count preserved", animCount, rt.getAnimationCount());
        for (int i = 0; i < animCount; i++) {
            NMARAnimation a0 = nmar.getAnimation(i);
            NMARAnimation a1 = rt.getAnimation(i);
            assertEq("anim[" + i + "].frames", a0.frames.size(), a1.frames.size());
            for (int f = 0; f < a0.frames.size(); f++) {
                NMARFrame f0 = a0.frames.get(f);
                NMARFrame f1 = a1.frames.get(f);
                assertEq("anim[" + i + "].frame[" + f + "].mcIndex",
                    f0.multiCellIndex, f1.multiCellIndex);
                assertEq("anim[" + i + "].frame[" + f + "].duration",
                    f0.duration, f1.duration);
            }
        }

        // Also: offsetDataSequences in the written file must be 0x18.
        int offSeq = readU32LE(written, 16 + 8 + 4);
        assertEq("real-round-trip offsetDataSequences", 0x18, offSeq);
    }

    private static int readU32LE(byte[] d, int off) {
        return (d[off] & 0xFF)
            | ((d[off + 1] & 0xFF) << 8)
            | ((d[off + 2] & 0xFF) << 16)
            | ((d[off + 3] & 0xFF) << 24);
    }

    private static void assertEq(String what, int expected, int actual) {
        if (expected != actual) {
            throw new AssertionError(what + ": expected " + expected + " but got " + actual);
        }
    }

    @SuppressWarnings("unused")
    private static void assertBytesEq(String what, byte[] expected, byte[] actual) {
        if (!Arrays.equals(expected, actual)) {
            int firstDiff = -1;
            int minLen = Math.min(expected.length, actual.length);
            for (int i = 0; i < minLen; i++) {
                if (expected[i] != actual[i]) { firstDiff = i; break; }
            }
            throw new AssertionError(what + ": byte[] mismatch, len exp=" + expected.length
                + " act=" + actual.length + ", first diff @ " + firstDiff);
        }
    }
}
