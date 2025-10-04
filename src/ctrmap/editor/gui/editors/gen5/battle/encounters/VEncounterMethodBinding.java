/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ctrmap.editor.gui.editors.gen5.battle.encounters;

import xstandard.formats.yaml.YamlNodeName;

public class VEncounterMethodBinding {
    @YamlNodeName("SINGLES")
    public VEncounterSlotBinding[] singles;

    @YamlNodeName("DOUBLES")
    public VEncounterSlotBinding[] doubles;

    @YamlNodeName("SPECIAL")
    public VEncounterSlotBinding[] special;
}

