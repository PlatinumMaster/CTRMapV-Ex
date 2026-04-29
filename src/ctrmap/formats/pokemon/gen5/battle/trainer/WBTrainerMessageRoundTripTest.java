package ctrmap.formats.pokemon.gen5.battle.trainer;

import ctrmap.formats.pokemon.gen5.battle.trainer.WBTrainerMessageTable.IndexedMessage;
import java.util.Arrays;
import java.util.List;
import xstandard.io.base.impl.access.MemoryStream;
import xstandard.io.base.impl.ext.data.DataIOStream;

/**
 * Round-trip verification for {@link WBTrainerMessage} / {@link WBTrainerMessageTable}.
 *
 * <p>Run as a stand-alone {@code main} (the module doesn't ship a JUnit
 * dependency — existing "tests" in this repo follow the same style, see
 * {@code ctrmap.editor.gui.editors.gen5.battle.SpriteRenderTest}). Every
 * assertion throws {@link AssertionError} on mismatch so a green run prints
 * "ALL TESTS PASSED" and a red run fails fast.</p>
 *
 * <p>Covers:</p>
 * <ol>
 *   <li>Per-record Serialize → Deserialize parity for {@link WBTrainerMessage}.</li>
 *   <li>Byte-level serialisation shape (4 bytes: u16 LE trainerId + u16 LE type).</li>
 *   <li>{@link WBTrainerMessageTable} table-level round-trip (list equal
 *       before/after writing to a {@code MemoryStream} and re-reading).</li>
 *   <li>{@link WBTrainerMessageTable#getByTrainer(int)} filters correctly
 *       and carries the right {@code textIndex}.</li>
 *   <li>{@code addEntry} appends at the end and returns the correct index.</li>
 *   <li>{@code removeEntry} deletes the right record and shifts subsequent
 *       indices down.</li>
 * </ol>
 */
public class WBTrainerMessageRoundTripTest {

    public static void main(String[] args) throws Exception {
        testRecordRoundTrip();
        testRecordByteLayout();
        testTableRoundTrip();
        testGetByTrainer();
        testAddEntry();
        testRemoveEntry();
        testNonMonotonicOffsets();
        System.out.println("ALL TESTS PASSED");
    }

    /** 7 — Vanilla BW2 regression: ARC 090 offsets are NOT monotonic.
     *  Trainer 2 lives at byte 32 (record 8) while trainer 1 lives at
     *  byte 48 (record 12) — a naive {@code [offsets[N], offsets[N+1])}
     *  slice would return records belonging to unrelated trainers.
     *  Verify that walking until the trainerId changes gives the
     *  correct per-trainer record lists. */
    static void testNonMonotonicOffsets() {
        WBTrainerMessageTable tbl = new WBTrainerMessageTable();
        // Mirror the real layout of vanilla ARC 089 records 0..19:
        //   trainer 8 (rec 0-3), 9 (4-7), 2 (8-11), 1 (12-15), 3 (16-19).
        int[][] records = {
            {8, 0}, {8, 1}, {8, 15}, {8, 16},
            {9, 0}, {9, 1}, {9, 15}, {9, 16},
            {2, 0}, {2, 1}, {2, 15}, {2, 16},
            {1, 0}, {1, 1}, {1, 15}, {1, 16},
            {3, 0}, {3, 1}, {3, 15}, {3, 16},
        };
        for (int[] r : records) tbl.entries.add(new WBTrainerMessage(r[0], r[1]));
        // Offsets indexed by trainer slot. Trainers 0, 4, 5, 6, 7 have
        // no records (offsets point elsewhere as sentinels). Trainers
        // 1/2/3/8/9 each have 4 records. Note the non-monotonic order.
        tbl.trainerOffsets = new int[]{
            0,    // trainer 0: no records (points at trainer 8's record)
            48,   // trainer 1: byte 48 (record 12)
            32,   // trainer 2: byte 32 (record 8)  <- earlier than trainer 1!
            64,   // trainer 3: byte 64 (record 16)
            0,    // trainer 4: no records
            0,    // trainer 5: no records
            0,    // trainer 6: no records
            0,    // trainer 7: no records
            0,    // trainer 8: byte 0 (record 0)
            16,   // trainer 9: byte 16 (record 4)
            80,   // sentinel = entries.size() * 4
        };
        tbl.markLoadedForTest();

        // Every trainer with records returns exactly their 4 records,
        // each with types 0/1/15/16.
        for (int tid : new int[]{1, 2, 3, 8, 9}) {
            List<IndexedMessage> recs = tbl.getByTrainer(tid);
            assertEq("trainer " + tid + " record count", 4, recs.size());
            int[] expectedTypes = {0, 1, 15, 16};
            for (int j = 0; j < 4; j++) {
                assertEq("trainer " + tid + " msg[" + j + "] tid",
                    tid, recs.get(j).message.trainerId);
                assertEq("trainer " + tid + " msg[" + j + "] type",
                    expectedTypes[j], recs.get(j).message.messageType);
            }
        }
        // Trainers without records (whose offsets point at another trainer's
        // records) return an empty list — the first record's trainerId
        // mismatches and the walk stops.
        for (int tid : new int[]{0, 4, 5, 6, 7}) {
            assertEq("empty trainer " + tid, 0, tbl.getByTrainer(tid).size());
        }
    }

    /** 1 — `WBTrainerMessage.Serialize` → `.Deserialize` parity. */
    static void testRecordRoundTrip() throws Exception {
        WBTrainerMessage src = new WBTrainerMessage(42, 3);
        MemoryStream ms = new MemoryStream();
        DataIOStream out = new DataIOStream(ms);
        src.Serialize(out);
        out.seek(0);
        WBTrainerMessage dst = new WBTrainerMessage();
        dst.Deserialize(out);
        assertEq("record.trainerId round-trip", 42, dst.trainerId);
        assertEq("record.messageType round-trip", 3, dst.messageType);
        assertEq("record equals", src, dst);
    }

    /** 2 — record must be 4 bytes, little-endian `{trainerId, messageType}`. */
    static void testRecordByteLayout() throws Exception {
        WBTrainerMessage src = new WBTrainerMessage(0x0102, 0x0304);
        MemoryStream ms = new MemoryStream();
        DataIOStream out = new DataIOStream(ms);
        src.Serialize(out);
        byte[] bytes = ms.toByteArray();
        assertEq("record byte-length", 4, bytes.length);
        // u16 LE -> low byte first
        assertEq("byte[0] = trainerId low",  (byte) 0x02, bytes[0]);
        assertEq("byte[1] = trainerId high", (byte) 0x01, bytes[1]);
        assertEq("byte[2] = msgType low",    (byte) 0x04, bytes[2]);
        assertEq("byte[3] = msgType high",   (byte) 0x03, bytes[3]);
    }

    /** 3 — Write table to stream, read back, compare record-by-record. */
    static void testTableRoundTrip() throws Exception {
        WBTrainerMessage[] records = {
            new WBTrainerMessage(1, 0),
            new WBTrainerMessage(1, 1),
            new WBTrainerMessage(2, 0),
            new WBTrainerMessage(2, 2),
        };
        MemoryStream ms = new MemoryStream();
        DataIOStream out = new DataIOStream(ms);
        for (WBTrainerMessage m : records) m.Serialize(out);

        // Re-read.
        out.seek(0);
        int count = out.getLength() / 4;
        assertEq("serialized record count", records.length, count);
        for (int i = 0; i < count; i++) {
            WBTrainerMessage rt = new WBTrainerMessage();
            rt.Deserialize(out);
            assertEq("table[" + i + "].trainerId",   records[i].trainerId,   rt.trainerId);
            assertEq("table[" + i + "].messageType", records[i].messageType, rt.messageType);
        }
    }

    /** 4 — getByTrainer returns the right subset for the new
     *  ARC089-records + ARC090-offsets layout. Records are contiguous
     *  per trainer; trainerOffsets stores byte offsets into ARC 089
     *  keyed by trainer slot; sentinel entry = total bytes. */
    static void testGetByTrainer() {
        WBTrainerMessageTable tbl = new WBTrainerMessageTable();
        // Layout: trainer 0 (no records), 1 (3), 2 (1), 3 (2). Records
        // are sequential; offsets are byte offsets into the ARC 089 wire
        // format (4 bytes per record).
        tbl.entries.add(new WBTrainerMessage(1, 0));   // idx 0
        tbl.entries.add(new WBTrainerMessage(1, 1));   // idx 1
        tbl.entries.add(new WBTrainerMessage(1, 2));   // idx 2
        tbl.entries.add(new WBTrainerMessage(2, 0));   // idx 3
        tbl.entries.add(new WBTrainerMessage(3, 0));   // idx 4
        tbl.entries.add(new WBTrainerMessage(3, 1));   // idx 5
        tbl.trainerOffsets = new int[]{0, 0, 12, 16, 24};
        // trainer 0: bytes 0..0     (no records)
        // trainer 1: bytes 0..12    (records 0..3) -> 3 records
        // trainer 2: bytes 12..16   (record 3..4)  -> 1 record
        // trainer 3: bytes 16..24   (records 4..6) -> 2 records
        tbl.markLoadedForTest();

        assertEq("trainer 0 count", 0, tbl.getByTrainer(0).size());

        List<IndexedMessage> forOne = tbl.getByTrainer(1);
        assertEq("trainer 1 count", 3, forOne.size());
        assertEq("trainer 1 msg[0] textIndex", 0, forOne.get(0).textIndex);
        assertEq("trainer 1 msg[2] type",      2, forOne.get(2).message.messageType);

        List<IndexedMessage> forTwo = tbl.getByTrainer(2);
        assertEq("trainer 2 count", 1, forTwo.size());
        assertEq("trainer 2 msg[0] textIndex", 3, forTwo.get(0).textIndex);

        List<IndexedMessage> forThree = tbl.getByTrainer(3);
        assertEq("trainer 3 count", 2, forThree.size());
        assertEq("trainer 3 msg[1] textIndex", 5, forThree.get(1).textIndex);

        assertEq("trainer 99 count (out of range)", 0, tbl.getByTrainer(99).size());
    }

    /** 5 — addEntry inserts at the end of the trainer's range; offsets
     *  for later trainers shift up by 4 bytes. */
    static void testAddEntry() {
        WBTrainerMessageTable tbl = new WBTrainerMessageTable();
        tbl.entries.add(new WBTrainerMessage(0, 0));
        tbl.entries.add(new WBTrainerMessage(1, 0));
        tbl.entries.add(new WBTrainerMessage(2, 0));
        // 3 trainers, 1 record each.
        tbl.trainerOffsets = new int[]{0, 4, 8, 12};
        tbl.markLoadedForTest();

        int newIdx = tbl.addEntry(1, 5);
        assertEq("inserted at end of trainer 1's range", 2, newIdx);
        assertEq("entries size after add", 4, tbl.entries.size());
        assertEq("trainer 1's new record at the right index",
            new WBTrainerMessage(1, 5), tbl.entries.get(newIdx));
        // After insert: trainer 0 [0..4), 1 [4..12), 2 [12..16), sentinel 16
        assertEq("offset[0] unchanged", 0, tbl.trainerOffsets[0]);
        assertEq("offset[1] unchanged", 4, tbl.trainerOffsets[1]);
        assertEq("offset[2] shifted by 4", 12, tbl.trainerOffsets[2]);
        assertEq("sentinel shifted by 4", 16, tbl.trainerOffsets[3]);
        // Trainer 1 now has 2 records.
        assertEq("trainer 1 count after add", 2, tbl.getByTrainer(1).size());
        assertEq("trainer 2 still has 1 record", 1, tbl.getByTrainer(2).size());
    }

    /** 6 — removeEntry deletes a record and shifts later trainer
     *  offsets down by 4 bytes. */
    static void testRemoveEntry() {
        WBTrainerMessageTable tbl = new WBTrainerMessageTable();
        tbl.entries.add(new WBTrainerMessage(0, 0));   // idx 0
        tbl.entries.add(new WBTrainerMessage(1, 0));   // idx 1   <- remove
        tbl.entries.add(new WBTrainerMessage(1, 1));   // idx 2
        tbl.entries.add(new WBTrainerMessage(2, 0));   // idx 3
        tbl.trainerOffsets = new int[]{0, 4, 12, 16};
        tbl.markLoadedForTest();

        tbl.removeEntry(1);
        assertEq("entries size after remove", 3, tbl.entries.size());
        assertEq("entries[1] shifted down", 1, tbl.entries.get(1).trainerId);
        assertEq("offset[2] shifted by -4 to 8", 8, tbl.trainerOffsets[2]);
        assertEq("sentinel shifted by -4", 12, tbl.trainerOffsets[3]);

        // Trainer 1 now has 1 record (the type-1 one), trainer 2 unchanged.
        assertEq("trainer 1 count after remove", 1, tbl.getByTrainer(1).size());
        assertEq("trainer 2 count after remove", 1, tbl.getByTrainer(2).size());

        // Out-of-range indexes must be no-ops.
        tbl.removeEntry(99);
        tbl.removeEntry(-1);
        assertEq("size unchanged after bogus remove", 3, tbl.entries.size());
    }

    // --- Mini assertion helpers ---

    private static void assertEq(String what, int expected, int actual) {
        if (expected != actual) {
            throw new AssertionError(what + ": expected " + expected + " but got " + actual);
        }
    }

    private static void assertEq(String what, byte expected, byte actual) {
        if (expected != actual) {
            throw new AssertionError(what
                + ": expected 0x" + String.format("%02X", expected & 0xFF)
                + " but got 0x" + String.format("%02X", actual & 0xFF));
        }
    }

    private static void assertEq(String what, Object expected, Object actual) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(what + ": expected " + expected + " but got " + actual);
        }
    }

    /** Convenience: compare two byte[] for parity (used for future ROM parity tests). */
    @SuppressWarnings("unused")
    private static void assertBytesEq(String what, byte[] expected, byte[] actual) {
        if (!Arrays.equals(expected, actual)) {
            throw new AssertionError(what
                + ": byte[] mismatch, expected length " + expected.length
                + " actual " + actual.length);
        }
    }
}
