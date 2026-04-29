package ctrmap.formats.pokemon.gen5.battle.moves;

import java.io.DataInput;
import java.io.IOException;
import xstandard.io.base.impl.ext.data.DataIOStream;

/**
 * Gen 5 Move data format.
 * 16 bytes per entry from PML_MOVE NARC.
 */
public class WBMoveData {

    public static final int ENTRY_SIZE = 16;

    byte Type;
    byte Category;         // 0=Status, 1=Physical, 2=Special
    byte DamageCategory;
    byte Power;
    byte Accuracy;
    byte PP;
    byte EffectChance;
    short Flags;           // contact, protect, mirror, etc.
    byte Priority;         // signed
    byte HitCountMin;      // lower 4 bits
    byte HitCountMax;      // upper 4 bits
    byte StatusInflict;
    byte StatusDuration;
    byte CritStage;
    byte DrainPercent;     // signed: negative = recoil
    byte HealPercent;

    public WBMoveData() {
    }

    public WBMoveData(DataInput in) throws IOException {
        this();

        Type = in.readByte();
        Category = in.readByte();
        DamageCategory = in.readByte();
        Power = in.readByte();
        Accuracy = in.readByte();
        PP = in.readByte();
        EffectChance = in.readByte();

        Flags = in.readShort();

        Priority = in.readByte();

        byte hitCountPacked = in.readByte();
        HitCountMin = (byte) (hitCountPacked & 0xF);
        HitCountMax = (byte) ((hitCountPacked >>> 4) & 0xF);

        StatusInflict = in.readByte();
        StatusDuration = in.readByte();
        CritStage = in.readByte();
        DrainPercent = in.readByte();
        HealPercent = in.readByte();
    }

    // --- Type ---

    public int GetType() { return Type & 0xFF; }
    public void SetType(int val) { Type = (byte) val; }

    // --- Category ---

    public int GetCategory() { return Category & 0xFF; }
    public void SetCategory(int val) { Category = (byte) val; }

    public int GetDamageCategory() { return DamageCategory & 0xFF; }
    public void SetDamageCategory(int val) { DamageCategory = (byte) val; }

    // --- Power ---

    public int GetPower() { return Power & 0xFF; }
    public void SetPower(int val) { Power = (byte) val; }

    // --- Accuracy ---

    public int GetAccuracy() { return Accuracy & 0xFF; }
    public void SetAccuracy(int val) { Accuracy = (byte) val; }

    // --- PP ---

    public int GetPP() { return PP & 0xFF; }
    public void SetPP(int val) { PP = (byte) val; }

    // --- Effect Chance ---

    public int GetEffectChance() { return EffectChance & 0xFF; }
    public void SetEffectChance(int val) { EffectChance = (byte) val; }

    // --- Flags ---

    public int GetFlags() { return Flags & 0xFFFF; }
    public void SetFlags(int val) { Flags = (short) val; }

    public boolean GetFlag(int bitIndex) {
        if (bitIndex < 0 || bitIndex >= 16) return false;
        return ((Flags >>> bitIndex) & 1) == 1;
    }

    public void SetFlag(int bitIndex, boolean val) {
        if (bitIndex < 0 || bitIndex >= 16) return;
        if (val) {
            Flags |= (short) (1 << bitIndex);
        } else {
            Flags &= (short) ~(1 << bitIndex);
        }
    }

    // Flag indices
    public static final int FLAG_CONTACT = 0;
    public static final int FLAG_CHARGE = 1;
    public static final int FLAG_RECHARGE = 2;
    public static final int FLAG_PROTECT = 3;
    public static final int FLAG_MAGIC_COAT = 4;
    public static final int FLAG_SNATCH = 5;
    public static final int FLAG_MIRROR_MOVE = 6;
    public static final int FLAG_KINGS_ROCK = 7;

    // --- Priority ---

    public int GetPriority() { return Priority; }  // signed
    public void SetPriority(int val) { Priority = (byte) val; }

    // --- Hit Count ---

    public int GetHitCountMin() { return HitCountMin & 0xF; }
    public void SetHitCountMin(int val) { HitCountMin = (byte) (val & 0xF); }

    public int GetHitCountMax() { return HitCountMax & 0xF; }
    public void SetHitCountMax(int val) { HitCountMax = (byte) (val & 0xF); }

    // --- Status ---

    public int GetStatusInflict() { return StatusInflict & 0xFF; }
    public void SetStatusInflict(int val) { StatusInflict = (byte) val; }

    public int GetStatusDuration() { return StatusDuration & 0xFF; }
    public void SetStatusDuration(int val) { StatusDuration = (byte) val; }

    // --- Crit Stage ---

    public int GetCritStage() { return CritStage & 0xFF; }
    public void SetCritStage(int val) { CritStage = (byte) val; }

    // --- Drain/Recoil ---

    public int GetDrainPercent() { return DrainPercent; }  // signed
    public void SetDrainPercent(int val) { DrainPercent = (byte) val; }

    // --- Heal ---

    public int GetHealPercent() { return HealPercent & 0xFF; }
    public void SetHealPercent(int val) { HealPercent = (byte) val; }

    // --- Serialization ---

    public void Serialize(DataIOStream out) throws IOException {
        out.writeByte(Type);
        out.writeByte(Category);
        out.writeByte(DamageCategory);
        out.writeByte(Power);
        out.writeByte(Accuracy);
        out.writeByte(PP);
        out.writeByte(EffectChance);

        out.writeShort(Flags);

        out.writeByte(Priority);

        byte hitCountPacked = (byte) ((HitCountMin & 0xF) | ((HitCountMax & 0xF) << 4));
        out.writeByte(hitCountPacked);

        out.writeByte(StatusInflict);
        out.writeByte(StatusDuration);
        out.writeByte(CritStage);
        out.writeByte(DrainPercent);
        out.writeByte(HealPercent);
    }
}
