
package ctrmap.formats.pokemon.gen5.battle;

import java.io.DataInput;
import java.io.IOException;

public class WBTrainerPoke {
    final int MOVES_COUNT = 0x4;
    Integer[] Moves;
    int IVs, Ability, Gender, Level, Species, Form, Item;
    
    public WBTrainerPoke() {
        Moves = new Integer[MOVES_COUNT];
    }
    
    public WBTrainerPoke(DataInput in, boolean CanOverrideMoves, boolean CanOverrideItem) throws IOException {
        this();
        IVs = in.readUnsignedByte(); 
        int PID = in.readUnsignedByte();
        Ability = (PID >>> 0x4) & 0b1111;
        Gender = PID & 0b1111;
        Level = in.readUnsignedShort();
        Species = in.readUnsignedShort();
        Form = in.readUnsignedShort();
        Item = CanOverrideItem ? in.readUnsignedShort() : -1;
        
        if (CanOverrideMoves) {
            for (int Index = 0; Index < MOVES_COUNT; ++Index) {
                Moves[Index] = in.readUnsignedShort();
            }
        }
    }
    
    public int GetIV() {
        return this.IVs;
    }
    
    public void SetIV(int IVs) {
        this.IVs = IVs;
    }
    
    public int GetAbility() {
        return this.Ability;
    }
    
    public void SetAbility(int Ability) {
        this.Ability = Ability;
    }
    
    public int GetGender() {
        return this.Gender;
    }
    
    public void SetGender(int Gender) {
        this.Gender = Gender;
    }
    
    public int GetLevel() {
        return this.Level;
    }
    
    public void SetLevel(int Level) {
        this.Level = Level;
    }
    
    public int GetSpecies() {
        return this.Species;
    }
    
    public void SetSpecies(int Species) {
        this.Species = Species;
    }

    public int GetForm() {
        return this.Form;
    }
    
    public void SetForm(int Form) {
        this.Form = Form;
    }
    
    
    public int GetHeldItem() {
        return this.Item;
    }
    
    public void SetHeldItem(int Item) {
        this.Item = Item;
    }
    
    public int GetMove(int Index) {
        if (Index < 0 || Index >= MOVES_COUNT) {
            return -1;
        }
        return this.Moves[Index];
    }
    
    public void SetMove(int Index, int NewMove) {
        if (Index < 0 || Index >= MOVES_COUNT) {
            return;
        }
        this.Moves[Index] = NewMove;
    }
}
