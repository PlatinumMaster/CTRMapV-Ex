/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ctrmap.formats.pokemon.gen5.battle.encounters;

import java.io.DataInput;
import java.io.IOException;
<<<<<<< HEAD
=======
import xstandard.io.base.impl.ext.data.DataIOStream;
>>>>>>> 687ba7a2a4bb0efb135b3f8e32f3f1163e8d32f2

public class WBEncEntry {
    short DexNum, FormNum;
    byte MinLevel, MaxLevel;
    
    public WBEncEntry() {
        
    }
    
    public WBEncEntry(DataInput Input) throws IOException {
        short IDForm = Input.readShort();
        DexNum = (short) (IDForm & 0x7FF);
        FormNum = (short) (IDForm >>> 0xB);
        MinLevel = Input.readByte();
        MaxLevel = Input.readByte();
    }
    
<<<<<<< HEAD
    public short GetSpecies() {
        return this.DexNum;
    }
    
    public void SetSpecies(short DexNum) {
        this.DexNum = DexNum;
    }
        
    public short GetForm() {
        return this.FormNum;
    }
    
    public void SetForm(short FormNum) {
=======
    public short GetDexNum() {
        return this.DexNum;
    }
    
    public void SetDexNum(short DexNum) {
        this.DexNum = DexNum;
    }
        
    public short GetFormNum() {
        return this.FormNum;
    }
    
    public void SetFormNum(short FormNum) {
>>>>>>> 687ba7a2a4bb0efb135b3f8e32f3f1163e8d32f2
        this.FormNum = FormNum;
    }
    
    public short GetMinLevel() {
        return this.MinLevel;
    }
    
    public void SetMinLevel(byte MinLevel) {
        this.MinLevel = MinLevel;
    }
    
    public short GetMaxLevel() {
<<<<<<< HEAD
        return this.MinLevel;
    }
    
    public void SetMaxLevel(byte MinLevel) {
        this.MaxLevel = MaxLevel;
    }
=======
        return this.MaxLevel;
    }

    public void SetMaxLevel(byte MaxLevel) {
        this.MaxLevel = MaxLevel;
    }

    public void Serialize(DataIOStream out) throws IOException {
        short IDForm = (short) ((DexNum & 0x7FF) | (FormNum << 0xB));
        out.writeShort(IDForm);
        out.writeByte(MinLevel);
        out.writeByte(MaxLevel);
    }
>>>>>>> 687ba7a2a4bb0efb135b3f8e32f3f1163e8d32f2
}
