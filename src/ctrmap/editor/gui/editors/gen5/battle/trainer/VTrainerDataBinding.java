package ctrmap.editor.gui.editors.gen5.battle.trainer;

import xstandard.formats.yaml.YamlNodeName;

public class VTrainerDataBinding {
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


