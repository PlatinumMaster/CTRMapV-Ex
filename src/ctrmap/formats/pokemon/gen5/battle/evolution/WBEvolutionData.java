package ctrmap.formats.pokemon.gen5.battle.evolution;

import java.io.DataInput;
import java.io.IOException;
import java.util.ArrayList;
import xstandard.io.base.impl.ext.data.DataIOStream;

/**
 * Gen 5 Evolution data format.
 * 42 bytes fixed from PML_EVOLUTION NARC.
 * 7 slots x 6 bytes each: ushort Method, ushort Parameter, ushort TargetSpecies.
 * Method=0 means empty slot.
 */
public class WBEvolutionData {

    public static final int MAX_SLOTS = 7;
    public static final int SLOT_SIZE = 6;
    public static final int ENTRY_SIZE = MAX_SLOTS * SLOT_SIZE;

    ArrayList<WBEvolutionEntry> Entries;

    public WBEvolutionData() {
        Entries = new ArrayList<>();
    }

    public WBEvolutionData(DataInput in) throws IOException {
        this();

        for (int i = 0; i < MAX_SLOTS; i++) {
            int method = in.readUnsignedShort();
            int parameter = in.readUnsignedShort();
            int targetSpecies = in.readUnsignedShort();
            Entries.add(new WBEvolutionEntry(method, parameter, targetSpecies));
        }
    }

    public int GetSlotCount() {
        return Entries.size();
    }

    /**
     * Returns the number of non-empty evolution slots (Method != 0).
     */
    public int GetActiveSlotCount() {
        int count = 0;
        for (WBEvolutionEntry entry : Entries) {
            if (entry.Method != 0) count++;
        }
        return count;
    }

    public WBEvolutionEntry GetEntry(int index) {
        return index >= 0 && index < Entries.size() ? Entries.get(index) : null;
    }

    public void SetEntry(int index, int method, int parameter, int targetSpecies) {
        if (index >= 0 && index < Entries.size()) {
            WBEvolutionEntry entry = Entries.get(index);
            entry.Method = method;
            entry.Parameter = parameter;
            entry.TargetSpecies = targetSpecies;
        }
    }

    public void ClearEntry(int index) {
        SetEntry(index, 0, 0, 0);
    }

    public void Serialize(DataIOStream out) throws IOException {
        for (int i = 0; i < MAX_SLOTS; i++) {
            if (i < Entries.size()) {
                WBEvolutionEntry entry = Entries.get(i);
                out.writeShort(entry.Method);
                out.writeShort(entry.Parameter);
                out.writeShort(entry.TargetSpecies);
            } else {
                // Empty slot
                out.writeShort(0);
                out.writeShort(0);
                out.writeShort(0);
            }
        }
    }

    // --- Inner class ---

    public static class WBEvolutionEntry {

        int Method;
        int Parameter;
        int TargetSpecies;

        public WBEvolutionEntry(int method, int parameter, int targetSpecies) {
            this.Method = method;
            this.Parameter = parameter;
            this.TargetSpecies = targetSpecies;
        }

        public int GetMethod() { return Method; }
        public void SetMethod(int val) { Method = val; }

        public int GetParameter() { return Parameter; }
        public void SetParameter(int val) { Parameter = val; }

        public int GetTargetSpecies() { return TargetSpecies; }
        public void SetTargetSpecies(int val) { TargetSpecies = val; }

        public boolean IsEmpty() { return Method == 0; }
    }

    // --- Evolution method constants (Gen 5) ---

    public static final int EVO_NONE = 0;
    public static final int EVO_HAPPINESS = 1;
    public static final int EVO_HAPPINESS_DAY = 2;
    public static final int EVO_HAPPINESS_NIGHT = 3;
    public static final int EVO_LEVEL_UP = 4;
    public static final int EVO_TRADE = 5;
    public static final int EVO_TRADE_ITEM = 6;
    public static final int EVO_ITEM = 7;
    public static final int EVO_LEVEL_ATK_GT_DEF = 8;
    public static final int EVO_LEVEL_ATK_EQ_DEF = 9;
    public static final int EVO_LEVEL_ATK_LT_DEF = 10;
    public static final int EVO_LEVEL_PID_LOW = 11;
    public static final int EVO_LEVEL_PID_HIGH = 12;
    public static final int EVO_LEVEL_NINJASK = 13;
    public static final int EVO_LEVEL_SHEDINJA = 14;
    public static final int EVO_LEVEL_BEAUTY = 15;
    public static final int EVO_ITEM_MALE = 16;
    public static final int EVO_ITEM_FEMALE = 17;
    public static final int EVO_ITEM_DAY = 18;
    public static final int EVO_ITEM_NIGHT = 19;
    public static final int EVO_KNOW_MOVE = 20;
    public static final int EVO_PARTY_SPECIES = 21;
    public static final int EVO_LEVEL_MALE = 22;
    public static final int EVO_LEVEL_FEMALE = 23;
    public static final int EVO_LEVEL_ELEC_FIELD = 24;
    public static final int EVO_LEVEL_MOSS_ROCK = 25;
    public static final int EVO_LEVEL_ICE_ROCK = 26;
    public static final int EVO_TRADE_SPECIES = 27;
}
