package ctrmap.formats.pokemon.gen5.battle.personal;

import java.io.DataInput;
import java.io.IOException;
import xstandard.io.base.impl.ext.data.DataIOStream;

/**
 * Gen 5 Personal (base stats) data format.
 * 108 bytes (0x6C) per entry from PML_PERSONAL NARC.
 */
public class WBPersonalData {

    public static final int ENTRY_SIZE = 0x6C;
    public static final int TM_FLAG_BYTES = 16;
    public static final int TUTOR_FLAG_BYTES = 12;
    public static final int STAT_COUNT = 6;

    // Base stats
    byte HP, ATK, DEF, SPE, SPA, SPD;

    // Types
    byte Type1, Type2;

    // Catch/stage
    byte CatchRate, StageIndex;

    // EV yield packed: 2 bits per stat (HP, ATK, DEF, SPE, SPA, SPD)
    short EVYield;

    // Wild held items
    short WildItem1, WildItem2, WildItem3;

    // Misc
    byte GenderRatio, HatchSteps, BaseHappiness, ExpGrowthRate;

    // Egg groups
    byte EggGroup1, EggGroup2;

    // Abilities
    byte Ability1, Ability2, HiddenAbility;

    // Flee rate
    byte FleeRate;

    // Form data
    short FormID, FormSprite;
    byte FormCount, SpriteColor;

    // Base exp
    short BaseExp;

    // Height/Weight
    short Height, Weight;

    // TM compatibility flags (128 bits = 16 bytes)
    byte[] TMFlags;

    // Tutor flags (BW2 only, 12 bytes; zeros in BW)
    byte[] TutorFlags;

    public WBPersonalData() {
        TMFlags = new byte[TM_FLAG_BYTES];
        TutorFlags = new byte[TUTOR_FLAG_BYTES];
    }

    public WBPersonalData(DataInput in) throws IOException {
        this();

        HP = in.readByte();
        ATK = in.readByte();
        DEF = in.readByte();
        SPE = in.readByte();
        SPA = in.readByte();
        SPD = in.readByte();

        Type1 = in.readByte();
        Type2 = in.readByte();

        CatchRate = in.readByte();
        StageIndex = in.readByte();

        EVYield = in.readShort();

        WildItem1 = in.readShort();
        WildItem2 = in.readShort();
        WildItem3 = in.readShort();

        GenderRatio = in.readByte();
        HatchSteps = in.readByte();
        BaseHappiness = in.readByte();
        ExpGrowthRate = in.readByte();

        EggGroup1 = in.readByte();
        EggGroup2 = in.readByte();

        Ability1 = in.readByte();
        Ability2 = in.readByte();
        HiddenAbility = in.readByte();

        FleeRate = in.readByte();

        FormID = in.readShort();
        FormSprite = in.readShort();

        FormCount = in.readByte();
        SpriteColor = in.readByte();

        BaseExp = in.readShort();

        Height = in.readShort();
        Weight = in.readShort();

        // TM flags: 16 bytes (128 TM+HM bits)
        for (int i = 0; i < TM_FLAG_BYTES; i++) {
            TMFlags[i] = in.readByte();
        }

        // Tutor flags: 12 bytes (BW2 only)
        for (int i = 0; i < TUTOR_FLAG_BYTES; i++) {
            TutorFlags[i] = in.readByte();
        }
    }

    // --- Base Stats ---

    public int GetHP() { return HP & 0xFF; }
    public void SetHP(int val) { HP = (byte) val; }

    public int GetATK() { return ATK & 0xFF; }
    public void SetATK(int val) { ATK = (byte) val; }

    public int GetDEF() { return DEF & 0xFF; }
    public void SetDEF(int val) { DEF = (byte) val; }

    public int GetSPE() { return SPE & 0xFF; }
    public void SetSPE(int val) { SPE = (byte) val; }

    public int GetSPA() { return SPA & 0xFF; }
    public void SetSPA(int val) { SPA = (byte) val; }

    public int GetSPD() { return SPD & 0xFF; }
    public void SetSPD(int val) { SPD = (byte) val; }

    public int GetBaseStat(int index) {
        switch (index) {
            case 0: return GetHP();
            case 1: return GetATK();
            case 2: return GetDEF();
            case 3: return GetSPE();
            case 4: return GetSPA();
            case 5: return GetSPD();
            default: return 0;
        }
    }

    public void SetBaseStat(int index, int val) {
        switch (index) {
            case 0: SetHP(val); break;
            case 1: SetATK(val); break;
            case 2: SetDEF(val); break;
            case 3: SetSPE(val); break;
            case 4: SetSPA(val); break;
            case 5: SetSPD(val); break;
        }
    }

    // --- Types ---

    public int GetType1() { return Type1 & 0xFF; }
    public void SetType1(int val) { Type1 = (byte) val; }

    public int GetType2() { return Type2 & 0xFF; }
    public void SetType2(int val) { Type2 = (byte) val; }

    // --- Catch/Stage ---

    public int GetCatchRate() { return CatchRate & 0xFF; }
    public void SetCatchRate(int val) { CatchRate = (byte) val; }

    public int GetStageIndex() { return StageIndex & 0xFF; }
    public void SetStageIndex(int val) { StageIndex = (byte) val; }

    // --- EV Yield (2 bits per stat) ---

    public int GetEVYield(int statIndex) {
        if (statIndex < 0 || statIndex >= STAT_COUNT) return 0;
        return (EVYield >>> (statIndex * 2)) & 0x3;
    }

    public void SetEVYield(int statIndex, int val) {
        if (statIndex < 0 || statIndex >= STAT_COUNT) return;
        int shift = statIndex * 2;
        EVYield = (short) ((EVYield & ~(0x3 << shift)) | ((val & 0x3) << shift));
    }

    // --- Wild Items ---

    public int GetWildItem1() { return WildItem1 & 0xFFFF; }
    public void SetWildItem1(int val) { WildItem1 = (short) val; }

    public int GetWildItem2() { return WildItem2 & 0xFFFF; }
    public void SetWildItem2(int val) { WildItem2 = (short) val; }

    public int GetWildItem3() { return WildItem3 & 0xFFFF; }
    public void SetWildItem3(int val) { WildItem3 = (short) val; }

    // --- Misc ---

    public int GetGenderRatio() { return GenderRatio & 0xFF; }
    public void SetGenderRatio(int val) { GenderRatio = (byte) val; }

    public int GetHatchSteps() { return HatchSteps & 0xFF; }
    public void SetHatchSteps(int val) { HatchSteps = (byte) val; }

    public int GetBaseHappiness() { return BaseHappiness & 0xFF; }
    public void SetBaseHappiness(int val) { BaseHappiness = (byte) val; }

    public int GetExpGrowthRate() { return ExpGrowthRate & 0xFF; }
    public void SetExpGrowthRate(int val) { ExpGrowthRate = (byte) val; }

    // --- Egg Groups ---

    public int GetEggGroup1() { return EggGroup1 & 0xFF; }
    public void SetEggGroup1(int val) { EggGroup1 = (byte) val; }

    public int GetEggGroup2() { return EggGroup2 & 0xFF; }
    public void SetEggGroup2(int val) { EggGroup2 = (byte) val; }

    // --- Abilities ---

    public int GetAbility1() { return Ability1 & 0xFF; }
    public void SetAbility1(int val) { Ability1 = (byte) val; }

    public int GetAbility2() { return Ability2 & 0xFF; }
    public void SetAbility2(int val) { Ability2 = (byte) val; }

    public int GetHiddenAbility() { return HiddenAbility & 0xFF; }
    public void SetHiddenAbility(int val) { HiddenAbility = (byte) val; }

    // --- Flee Rate ---

    public int GetFleeRate() { return FleeRate & 0xFF; }
    public void SetFleeRate(int val) { FleeRate = (byte) val; }

    // --- Form Data ---

    public int GetFormID() { return FormID & 0xFFFF; }
    public void SetFormID(int val) { FormID = (short) val; }

    public int GetFormSprite() { return FormSprite & 0xFFFF; }
    public void SetFormSprite(int val) { FormSprite = (short) val; }

    public int GetFormCount() { return FormCount & 0xFF; }
    public void SetFormCount(int val) { FormCount = (byte) val; }

    public int GetSpriteColor() { return SpriteColor & 0xFF; }
    public void SetSpriteColor(int val) { SpriteColor = (byte) val; }

    // --- Base Exp ---

    public int GetBaseExp() { return BaseExp & 0xFFFF; }
    public void SetBaseExp(int val) { BaseExp = (short) val; }

    // --- Height/Weight ---

    public int GetHeight() { return Height & 0xFFFF; }
    public void SetHeight(int val) { Height = (short) val; }

    public int GetWeight() { return Weight & 0xFFFF; }
    public void SetWeight(int val) { Weight = (short) val; }

    // --- TM Flags ---

    public boolean GetTMFlag(int tmIndex) {
        if (tmIndex < 0 || tmIndex >= TM_FLAG_BYTES * 8) return false;
        int byteIndex = tmIndex / 8;
        int bitIndex = tmIndex % 8;
        return ((TMFlags[byteIndex] >>> bitIndex) & 1) == 1;
    }

    public void SetTMFlag(int tmIndex, boolean val) {
        if (tmIndex < 0 || tmIndex >= TM_FLAG_BYTES * 8) return;
        int byteIndex = tmIndex / 8;
        int bitIndex = tmIndex % 8;
        if (val) {
            TMFlags[byteIndex] |= (1 << bitIndex);
        } else {
            TMFlags[byteIndex] &= ~(1 << bitIndex);
        }
    }

    // --- Tutor Flags ---

    public boolean GetTutorFlag(int tutorIndex) {
        if (tutorIndex < 0 || tutorIndex >= TUTOR_FLAG_BYTES * 8) return false;
        int byteIndex = tutorIndex / 8;
        int bitIndex = tutorIndex % 8;
        return ((TutorFlags[byteIndex] >>> bitIndex) & 1) == 1;
    }

    public void SetTutorFlag(int tutorIndex, boolean val) {
        if (tutorIndex < 0 || tutorIndex >= TUTOR_FLAG_BYTES * 8) return;
        int byteIndex = tutorIndex / 8;
        int bitIndex = tutorIndex % 8;
        if (val) {
            TutorFlags[byteIndex] |= (1 << bitIndex);
        } else {
            TutorFlags[byteIndex] &= ~(1 << bitIndex);
        }
    }

    // --- Serialization ---

    public void Serialize(DataIOStream out) throws IOException {
        out.writeByte(HP);
        out.writeByte(ATK);
        out.writeByte(DEF);
        out.writeByte(SPE);
        out.writeByte(SPA);
        out.writeByte(SPD);

        out.writeByte(Type1);
        out.writeByte(Type2);

        out.writeByte(CatchRate);
        out.writeByte(StageIndex);

        out.writeShort(EVYield);

        out.writeShort(WildItem1);
        out.writeShort(WildItem2);
        out.writeShort(WildItem3);

        out.writeByte(GenderRatio);
        out.writeByte(HatchSteps);
        out.writeByte(BaseHappiness);
        out.writeByte(ExpGrowthRate);

        out.writeByte(EggGroup1);
        out.writeByte(EggGroup2);

        out.writeByte(Ability1);
        out.writeByte(Ability2);
        out.writeByte(HiddenAbility);

        out.writeByte(FleeRate);

        out.writeShort(FormID);
        out.writeShort(FormSprite);

        out.writeByte(FormCount);
        out.writeByte(SpriteColor);

        out.writeShort(BaseExp);

        out.writeShort(Height);
        out.writeShort(Weight);

        for (int i = 0; i < TM_FLAG_BYTES; i++) {
            out.writeByte(TMFlags[i]);
        }

        for (int i = 0; i < TUTOR_FLAG_BYTES; i++) {
            out.writeByte(TutorFlags[i]);
        }
    }
}
