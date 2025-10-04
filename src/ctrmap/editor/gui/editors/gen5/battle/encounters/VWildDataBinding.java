/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ctrmap.editor.gui.editors.gen5.battle.encounters;

import ctrmap.editor.gui.editors.gen5.battle.trainer.VTrainerPokemonEntryBinding;
import xstandard.formats.yaml.YamlNodeName;

public class VWildDataBinding {
    @YamlNodeName("CLASS")
    public int trainerClass;
    
    @YamlNodeName("BATTLE_TYPE")
    public int battleType;
    
    @YamlNodeName("ITEMS")
    public String[] items;
    
    @YamlNodeName("AI")
    public int AI;
    
    @YamlNodeName("CAN_HEAL")
    public int canHeal;
    
    @YamlNodeName("REWARD_MONEY")
    public int rewardMoney;
    
    @YamlNodeName("REWARD_ITEM")
    public String rewardItem;
    
    @YamlNodeName("PARTY")
    public VTrainerPokemonEntryBinding party[];  
}
