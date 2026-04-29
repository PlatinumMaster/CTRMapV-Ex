package ctrmap.formats.pokemon.gen5.battle.learnset;

import java.io.DataInput;
import java.io.IOException;
import java.util.ArrayList;
import xstandard.io.base.impl.ext.data.DataIOStream;

/**
 * Gen 5 Learnset data format.
 * Variable length from PML_LEARNSETS NARC.
 * Repeated ushort entries until 0xFFFF terminator.
 * Each entry: bits 0-8 = MoveID, bits 9-15 = Level.
 */
public class WBLearnsetData {

    public static final int TERMINATOR = 0xFFFF;

    ArrayList<WBLearnsetEntry> Entries;

    public WBLearnsetData() {
        Entries = new ArrayList<>();
    }

    public WBLearnsetData(DataInput in) throws IOException {
        this();

        int val;
        while ((val = in.readUnsignedShort()) != TERMINATOR) {
            int moveID = val & 0x1FF;
            int level = (val >>> 9) & 0x7F;
            Entries.add(new WBLearnsetEntry(moveID, level));
        }
    }

    public int GetEntryCount() {
        return Entries.size();
    }

    public WBLearnsetEntry GetEntry(int index) {
        return index >= 0 && index < Entries.size() ? Entries.get(index) : null;
    }

    public void AddEntry(int moveID, int level) {
        Entries.add(new WBLearnsetEntry(moveID, level));
    }

    public void RemoveEntry(int index) {
        if (index >= 0 && index < Entries.size()) {
            Entries.remove(index);
        }
    }

    public void SetEntry(int index, int moveID, int level) {
        if (index >= 0 && index < Entries.size()) {
            WBLearnsetEntry entry = Entries.get(index);
            entry.MoveID = moveID;
            entry.Level = level;
        }
    }

    public void Serialize(DataIOStream out) throws IOException {
        for (WBLearnsetEntry entry : Entries) {
            int packed = (entry.MoveID & 0x1FF) | ((entry.Level & 0x7F) << 9);
            out.writeShort(packed);
        }
        out.writeShort(TERMINATOR);
    }

    // --- Inner class ---

    public static class WBLearnsetEntry {

        int MoveID;
        int Level;

        public WBLearnsetEntry(int moveID, int level) {
            this.MoveID = moveID;
            this.Level = level;
        }

        public int GetMoveID() { return MoveID; }
        public void SetMoveID(int val) { MoveID = val; }

        public int GetLevel() { return Level; }
        public void SetLevel(int val) { Level = val; }
    }
}
