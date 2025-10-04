/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ctrmap.editor.gui.editors.gen5.battle.trainer;

import xstandard.formats.yaml.YamlNodeName;

public class VTrainerPokemonEntryBinding {
    @YamlNodeName("SPECIES")
    public String species;
    
    @YamlNodeName("LEVEL")
    public int level;
    
    @YamlNodeName("FORM")
    public int form;
    
    @YamlNodeName("GENDER")
    public int gender;
    
    @YamlNodeName("ABILITY")
    public int ability;
    
    @YamlNodeName("DIFFICULTY_VALUE")
    public int difficultyValue;
}
