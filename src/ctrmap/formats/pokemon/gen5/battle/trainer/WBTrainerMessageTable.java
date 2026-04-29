package ctrmap.formats.pokemon.gen5.battle.trainer;

import ctrmap.missioncontrol_ntr.fs.NARCRef;
import ctrmap.missioncontrol_ntr.fs.NTRGameFS;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import xstandard.fs.FSFile;
import xstandard.io.base.impl.ext.data.DataIOStream;

/**
 * Manages the BW2 trainer-message tables. Two NARCs are involved:
 *
 * <ul>
 *   <li><b>{@link NARCRef#TRAINER_MESSAGE_MAP}</b> (ARC 089) — the
 *       <em>records</em>: one file containing a flat array of 4-byte
 *       {@code {u16 trainerId, u16 messageType}} entries. Records for the
 *       same trainer are contiguous. Parallel-indexed with the strings
 *       in {@code MSGDATA_SYSTEM:381}.</li>
 *   <li><b>{@link NARCRef#TRAINER_MESSAGE_INDEX}</b> (ARC 090) — the
 *       <em>per-trainer offset table</em>: one file containing
 *       {@code u16[trainerCount]} byte offsets into ARC 089. Trainer N's
 *       records run from {@code ARC089[ARC090[N]..ARC090[N+1])}. Indexed
 *       by trainer NARC slot index (combo position in the editor).</li>
 * </ul>
 *
 * <p>Pre-fix history: the editor used to read ARC 090 as if it were the
 * records, with the offsets misinterpreted as {trainerId, messageType}.
 * The first u16 of many ARC 090 entries happens to be 0, so every trainer
 * looked like it had a flood of records — and only "trainer 0" ever
 * matched anything.</p>
 *
 * <p>The user-facing record array is {@link #entries}. Edits go through
 * {@link #addEntry} / {@link #removeEntry}, which keep the per-trainer
 * offset table {@link #trainerOffsets} in sync. The caller is responsible
 * for keeping the parallel {@code MSGDATA_SYSTEM:381} {@code TextFile}
 * aligned with {@link #entries} (insert / remove a line at the same
 * index).</p>
 *
 * <p>BW1 does not ship these NARCs ({@code getARCID()} returns -1). On BW1
 * projects {@link #load} leaves the tables empty and {@link #save} is a
 * no-op — callers can still construct and interact with the object.</p>
 */
public class WBTrainerMessageTable {

    /** All records (sequential, parallel-indexed with TrDialogue lines). */
    public final List<WBTrainerMessage> entries = new ArrayList<>();

    /**
     * Byte offsets into ARC 089 keyed by trainer slot index.
     * {@code trainerOffsets[N]} is the byte offset where trainer N's
     * first record starts in ARC 089. Trainer N's records run from
     * that record forward until the {@code trainerId} field of the next
     * record changes — offsets are NOT monotonic (vanilla BW2 has
     * trainer 2 at byte 32, trainer 1 at byte 48), so we can't just
     * slice {@code [offsets[N], offsets[N+1])}.
     *
     * <p>A trailing sentinel of {@code entries.size() * 4} is appended
     * at load time so the array always has {@code trainerCount + 1}
     * elements — it lets the saver drop the sentinel back off and
     * write the exact on-disk byte count of ARC 090.</p>
     *
     * <p>Package-private so the round-trip test can drive the table
     * directly without a NARC mount; production code should never poke
     * at this array — it gets out of sync with {@link #entries}
     * trivially. Mutations must go through {@link #addEntry} or
     * {@link #removeEntry}.</p>
     */
    int[] trainerOffsets = new int[0];

    /** Test-only: marks the table as loaded so {@link #getByTrainer}
     *  and friends operate against synthetic data. Not for production. */
    void markLoadedForTest() {
        loaded = true;
    }

    /** True when load() successfully parsed real ARC 089 + 090 files. */
    private boolean loaded = false;

    public WBTrainerMessageTable() {
    }

    /**
     * Returns {@code true} if the table was populated from real NARCs
     * (BW2 project). BW1 projects leave this {@code false} and
     * {@link #save} becomes a no-op.
     */
    public boolean isLoaded() {
        return loaded;
    }

    /** @return the number of trainer slots covered by the index table. */
    public int getTrainerCount() {
        return Math.max(0, trainerOffsets.length - 1);
    }

    /**
     * Reads ARC 089 (records) + ARC 090 (per-trainer offset table). BW1
     * projects and missing NARCs leave the tables empty and return.
     */
    public void load(NTRGameFS fs) {
        entries.clear();
        trainerOffsets = new int[0];
        loaded = false;
        try {
            int idxMax = fs.NARCGetDataMax(NARCRef.TRAINER_MESSAGE_INDEX);
            int mapMax = fs.NARCGetDataMax(NARCRef.TRAINER_MESSAGE_MAP);
            if (idxMax <= 0 || mapMax <= 0) {
                return;
            }
            FSFile mapFile = fs.NARCGet(NARCRef.TRAINER_MESSAGE_MAP, 0);
            FSFile idxFile = fs.NARCGet(NARCRef.TRAINER_MESSAGE_INDEX, 0);
            if (mapFile == null || idxFile == null) {
                return;
            }

            // Read ARC 089: flat array of 4-byte records.
            try (DataIOStream in = mapFile.getDataIOStream()) {
                int recBytes = in.getLength();
                int recCount = recBytes / 4;
                for (int i = 0; i < recCount; i++) {
                    WBTrainerMessage m = new WBTrainerMessage();
                    m.Deserialize(in);
                    entries.add(m);
                }
            }

            // Read ARC 090: u16[trainerCount] of byte offsets into ARC 089.
            // Append a sentinel = entries.size() * 4 so trainer N's range
            // is always [offsets[N], offsets[N+1]) without a special case
            // for the last trainer.
            try (DataIOStream in = idxFile.getDataIOStream()) {
                int offsetBytes = in.getLength();
                int offsetCount = offsetBytes / 2;
                trainerOffsets = new int[offsetCount + 1];
                for (int i = 0; i < offsetCount; i++) {
                    trainerOffsets[i] = in.readUnsignedShort();
                }
                trainerOffsets[offsetCount] = entries.size() * 4;
            }

            loaded = true;
        } catch (Exception ex) {
            Logger.getLogger(WBTrainerMessageTable.class.getName())
                .log(Level.SEVERE, "Failed to load trainer message tables", ex);
        }
    }

    /**
     * Writes both ARC 089 (records) and ARC 090 (per-trainer offset
     * table) back to the host. No-op on BW1 / unloaded tables. Produces
     * byte-for-byte identical output when the in-memory state wasn't
     * edited since {@link #load}.
     */
    public void save(NTRGameFS fs) {
        if (!loaded) {
            return;
        }
        try {
            FSFile mapFile = fs.NARCGet(NARCRef.TRAINER_MESSAGE_MAP, 0);
            FSFile idxFile = fs.NARCGet(NARCRef.TRAINER_MESSAGE_INDEX, 0);
            if (mapFile != null) {
                try (DataIOStream out = mapFile.getDataIOStream()) {
                    for (WBTrainerMessage m : entries) {
                        m.Serialize(out);
                    }
                    out.setLength(out.getPosition());
                }
            }
            if (idxFile != null) {
                try (DataIOStream out = idxFile.getDataIOStream()) {
                    // Drop the sentinel (last entry) when writing — it's
                    // computed at load, not stored on disk.
                    int writeCount = Math.max(0, trainerOffsets.length - 1);
                    for (int i = 0; i < writeCount; i++) {
                        out.writeShort(trainerOffsets[i]);
                    }
                    out.setLength(out.getPosition());
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(WBTrainerMessageTable.class.getName())
                .log(Level.SEVERE, "Failed to save trainer message tables", ex);
        }
    }

    /**
     * Returns all records for the given trainer slot, each paired with
     * its position in {@link #entries} (which is also its index into the
     * parallel {@code MSGDATA_SYSTEM:381} TextFile).
     *
     * <p>Looks up trainer N's first record via
     * {@code trainerOffsets[N] / 4}, then iterates forward while the
     * record's {@code trainerId} field still matches. The offset table
     * is NOT monotonic — in vanilla BW2, trainer 2's records (byte 32)
     * come before trainer 1's (byte 48) — so a naive
     * {@code [offsets[N], offsets[N+1])} slice would grab unrelated
     * trainers. Walking until the trainerId changes is the right stop
     * condition because records for the same trainer are guaranteed
     * contiguous in ARC 089.</p>
     *
     * <p>A trainer with no messages has its offset pointing at some
     * other trainer's records; the very first record already mismatches
     * and the loop returns an empty list.</p>
     */
    public List<IndexedMessage> getByTrainer(int trainerId) {
        List<IndexedMessage> out = new ArrayList<>();
        if (trainerId < 0 || trainerId >= getTrainerCount()) {
            return out;
        }
        int startRec = trainerOffsets[trainerId] / 4;
        for (int i = startRec; i < entries.size(); i++) {
            WBTrainerMessage m = entries.get(i);
            if (m.trainerId != trainerId) {
                break;
            }
            out.add(new IndexedMessage(i, m));
        }
        return out;
    }

    /**
     * Inserts a new record at the end of {@code trainerId}'s existing
     * record run in ARC 089. The caller MUST also insert a matching
     * line into the parallel {@code TrDialogue} TextFile at the
     * returned index.
     *
     * <p>For a trainer that already has records, the new record goes
     * right after the last existing record so the run stays contiguous.
     * For a trainer with no records (its offset points at some other
     * trainer's start), we insert AT that offset and rewrite the
     * trainer's offset to claim the new record. Any other offsets that
     * were pointing at or past the insertion byte shift up by 4 — so
     * the rest of the offset table stays consistent regardless of which
     * trainer they reference, <em>without</em> assuming offsets are
     * sorted (vanilla has trainer 2 at byte 32 ahead of trainer 1 at
     * byte 48, so index-based shifts would corrupt the table).</p>
     *
     * @return The new record's index in {@link #entries} (= the line
     *         index in the parallel TextFile).
     */
    public int addEntry(int trainerId, int messageType) {
        if (!loaded || trainerId < 0 || trainerId >= getTrainerCount()) {
            int idx = entries.size();
            entries.add(new WBTrainerMessage(trainerId, messageType));
            if (loaded && trainerOffsets.length > 0) {
                trainerOffsets[trainerOffsets.length - 1] = entries.size() * 4;
            }
            return idx;
        }
        int startRec = trainerOffsets[trainerId] / 4;
        boolean hasExisting = startRec < entries.size()
            && entries.get(startRec).trainerId == trainerId;
        int insertRec = startRec;
        if (hasExisting) {
            // Walk forward past the last record belonging to this trainer.
            while (insertRec < entries.size()
                   && entries.get(insertRec).trainerId == trainerId) {
                insertRec++;
            }
        }
        int insertByte = insertRec * 4;
        entries.add(insertRec, new WBTrainerMessage(trainerId, messageType));
        // Shift every offset AT OR AFTER the insert byte forward by 4,
        // EXCEPT trainer N itself when it had no records (that offset is
        // rewritten below to point at the fresh record).
        for (int i = 0; i < trainerOffsets.length; i++) {
            if (i == trainerId && !hasExisting) continue;
            if (trainerOffsets[i] >= insertByte) {
                trainerOffsets[i] += 4;
            }
        }
        if (!hasExisting) {
            trainerOffsets[trainerId] = insertByte;
        }
        return insertRec;
    }

    /**
     * Removes the record at {@code textIndex} from {@link #entries} and
     * shifts any offsets that pointed past the removed byte down by 4.
     * Caller must also delete line {@code textIndex} from the parallel
     * {@code TrDialogue} TextFile.
     */
    public void removeEntry(int textIndex) {
        if (textIndex < 0 || textIndex >= entries.size()) {
            return;
        }
        int removedByte = textIndex * 4;
        entries.remove(textIndex);
        for (int i = 0; i < trainerOffsets.length; i++) {
            if (trainerOffsets[i] > removedByte) {
                trainerOffsets[i] -= 4;
            }
        }
    }

    /** Pair of an index into the parallel text table and its record. */
    public static final class IndexedMessage {
        public final int textIndex;
        public final WBTrainerMessage message;

        public IndexedMessage(int textIndex, WBTrainerMessage message) {
            this.textIndex = textIndex;
            this.message = message;
        }
    }
}
