/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ctrmap.formats.pokemon.gen5.pml;

/**
 *
 * @author L33TG
 */
public class WBPMLMoves {
    private int type;
    private int quality;
    private int category;
    private int power;
    private int accuracy;
    private int basePP;
    private int priority;
    private int hitMinMax;
    private int inflictStatus;
    private int inflictChance;
    private int inflictDuration;
    private int turnMin;
    private int turnMax;
    private int critStage;
    private int moveAnimID;
    private int recoil;
    private int heal;
    private int target;
    private int[] statChangeStats;
    private int[] statChangeStages;
    private int[] statChangeChances;
    private int padding;
    private int flags1;
    private int flags2;
    
    public int GetType() { return type; }
    public void SetType(int type) { this.type = type; }

    public int getQuality() { return quality; }
    public void setQuality(int quality) { this.quality = quality; }

    public int getCategory() { return category; }
    public void setCategory(int category) { this.category = category; }

    public int getPower() { return power; }
    public void setPower(int power) { this.power = power; }

    public int getAccuracy() { return accuracy; }
    public void setAccuracy(int accuracy) { this.accuracy = accuracy; }

    public int getBasePP() { return basePP; }
    public void setBasePP(int basePP) { this.basePP = basePP; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    public int getHitMinMax() { return hitMinMax; }
    public void setHitMinMax(int hitMinMax) { this.hitMinMax = hitMinMax; }

    public int getInflictStatus() { return inflictStatus; }
    public void setInflictStatus(int inflictStatus) { this.inflictStatus = inflictStatus; }

    public int getInflictChance() { return inflictChance; }
    public void setInflictChance(int inflictChance) { this.inflictChance = inflictChance; }

    public int getInflictDuration() { return inflictDuration; }
    public void setInflictDuration(int inflictDuration) { this.inflictDuration = inflictDuration; }

    public int getTurnMin() { return turnMin; }
    public void setTurnMin(int turnMin) { this.turnMin = turnMin; }

    public int getTurnMax() { return turnMax; }
    public void setTurnMax(int turnMax) { this.turnMax = turnMax; }

    public int getCritStage() { return critStage; }
    public void setCritStage(int critStage) { this.critStage = critStage; }

    public int getMoveAnimID() { return moveAnimID; }
    public void setMoveAnimID(int moveAnimID) { this.moveAnimID = moveAnimID; }

    public int getRecoil() { return recoil; }
    public void setRecoil(int recoil) { this.recoil = recoil; }

    public int getHeal() { return heal; }
    public void setHeal(int heal) { this.heal = heal; }

    public int getTarget() { return target; }
    public void setTarget(int target) { this.target = target; }

    public int[] getStatChangeStats() { return statChangeStats; }
    public void setStatChangeStats(int[] statChangeStats) { this.statChangeStats = statChangeStats; }

    public int[] getStatChangeStages() { return statChangeStages; }
    public void setStatChangeStages(int[] statChangeStages) { this.statChangeStages = statChangeStages; }

    public int[] getStatChangeChances() { return statChangeChances; }
    public void setStatChangeChances(int[] statChangeChances) { this.statChangeChances = statChangeChances; }

    public int getPadding() { return padding; }
    public void setPadding(int padding) { this.padding = padding; }

    public int getFlags1() { return flags1; }
    public void setFlags1(int flags1) { this.flags1 = flags1; }

    public int getFlags2() { return flags2; }
    public void setFlags2(int flags2) { this.flags2 = flags2; }
}
