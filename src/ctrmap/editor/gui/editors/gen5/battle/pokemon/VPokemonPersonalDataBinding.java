/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ctrmap.editor.gui.editors.gen5.battle.pokemon;

import ctrmap.editor.gui.editors.gen5.battle.trainer.VTrainerPokemonEntryBinding;
import xstandard.formats.yaml.YamlNodeName;

public class VPokemonPersonalDataBinding {
    @YamlNodeName("Base HP")
    public int baseHP;

    @YamlNodeName("Base Attack")
    public int baseAttack;

    @YamlNodeName("Base Defense")
    public int baseDefense;

    @YamlNodeName("Base Speed")
    public int baseSpeed;

    @YamlNodeName("Base Special Attack")
    public int baseSpAttack;

    @YamlNodeName("Base Special Defense")
    public int baseSpDefense;

    @YamlNodeName("Primary Type")
    public String primaryType;

    @YamlNodeName("Secondary Type")
    public String secondaryType;

    @YamlNodeName("Capture Rate")
    public int captureRate;

    @YamlNodeName("Evolution Stage")
    public int evolutionStage;

    @YamlNodeName("EV Yield")
    public int evYield;

    @YamlNodeName("Wild Item (50%)")
    public int wildItem50;

    @YamlNodeName("Wild Item (5%)")
    public int wildItem5;

    @YamlNodeName("Wild Item (1%)")
    public int wildItem1;

    @YamlNodeName("Gender Probability")
    public int genderProbability;

    @YamlNodeName("Egg Happiness")
    public int eggHappiness;

    @YamlNodeName("Base Happiness")
    public int baseHappiness;

    @YamlNodeName("Experience Group")
    public int experienceGroup;

    @YamlNodeName("Egg Group 1")
    public int eggGroup1;

    @YamlNodeName("Egg Group 2")
    public int eggGroup2;

    @YamlNodeName("Primary Ability")
    public int primaryAbility;

    @YamlNodeName("Secondary Ability")
    public int secondaryAbility;

    @YamlNodeName("Hidden Ability")
    public int hiddenAbility;

    @YamlNodeName("Escape Rate")
    public int escapeRate;

    @YamlNodeName("Form Data Offset")
    public int formDataOffset;

    @YamlNodeName("Form Sprite Offset")
    public int formSpriteOffset;

    @YamlNodeName("Form Count")
    public int formCount;

    @YamlNodeName("Color")
    public int color;

    @YamlNodeName("Base Experience")
    public int baseExperience;

    @YamlNodeName("Height (cm)")
    public int heightCm;

    @YamlNodeName("Weight (cg)")
    public int weightCg;

    @YamlNodeName("TM HM 1")
    public int tmhm1;

    @YamlNodeName("TM HM 2")
    public int tmhm2;

    @YamlNodeName("TM HM 3")
    public int tmhm3;

    @YamlNodeName("TM HM 4")
    public int tmhm4;

    @YamlNodeName("Type Tutors")
    public int typeTutors;

    @YamlNodeName("Special Tutors")
    public int[] specialTutors;
}
