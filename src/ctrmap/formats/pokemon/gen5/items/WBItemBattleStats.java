/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ctrmap.formats.pokemon.gen5.items;

public class WBItemBattleStats {
    private int cureInflict;
    private int[] boost = new int[4]; // [HP, ATK, DEF, SPE] or as needed
    private int functionFlags;
    private int evHp;
    private int evAtk;
    private int evDef;
    private int evSpe;
    private int evSpa;
    private int evSpd;
    private int healAmount;
    private int ppGain;
    private int friendship1;
    private int friendship2;
    private int friendship3;
    private int field1F;
    private int field20;

    public int getCureInflict() {
        return cureInflict;
    }

    public void setCureInflict(int cureInflict) {
        this.cureInflict = cureInflict;
    }

    public int[] getBoost() {
        return boost;
    }

    public void setBoost(int[] boost) {
        if (boost != null && boost.length == 4) {
            this.boost = boost;
        }
    }

    public int getFunctionFlags() {
        return functionFlags;
    }

    public void setFunctionFlags(int functionFlags) {
        this.functionFlags = functionFlags;
    }

    public int getEvHp() {
        return evHp;
    }

    public void setEvHp(int evHp) {
        this.evHp = evHp;
    }

    public int getEvAtk() {
        return evAtk;
    }

    public void setEvAtk(int evAtk) {
        this.evAtk = evAtk;
    }

    public int getEvDef() {
        return evDef;
    }

    public void setEvDef(int evDef) {
        this.evDef = evDef;
    }

    public int getEvSpe() {
        return evSpe;
    }

    public void setEvSpe(int evSpe) {
        this.evSpe = evSpe;
    }

    public int getEvSpa() {
        return evSpa;
    }

    public void setEvSpa(int evSpa) {
        this.evSpa = evSpa;
    }

    public int getEvSpd() {
        return evSpd;
    }

    public void setEvSpd(int evSpd) {
        this.evSpd = evSpd;
    }

    public int getHealAmount() {
        return healAmount;
    }

    public void setHealAmount(int healAmount) {
        this.healAmount = healAmount;
    }

    public int getPpGain() {
        return ppGain;
    }

    public void setPpGain(int ppGain) {
        this.ppGain = ppGain;
    }

    public int getFriendship1() {
        return friendship1;
    }

    public void setFriendship1(int friendship1) {
        this.friendship1 = friendship1;
    }

    public int getFriendship2() {
        return friendship2;
    }

    public void setFriendship2(int friendship2) {
        this.friendship2 = friendship2;
    }

    public int getFriendship3() {
        return friendship3;
    }

    public void setFriendship3(int friendship3) {
        this.friendship3 = friendship3;
    }

    public int getField1F() {
        return field1F;
    }

    public void setField1F(int field1F) {
        this.field1F = field1F;
    }

    public int getField20() {
        return field20;
    }

    public void setField20(int field20) {
        this.field20 = field20;
    }
}
