/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ctrmap.editor.gui.editors.gen5.battle.encounters;

import xstandard.formats.yaml.YamlNodeName;

public class VEncounterSlotBinding {
    @YamlNodeName("Species")
    public String species;

    @YamlNodeName("Form")
    public int form;

    @YamlNodeName("Minimum Level")
    public int minLevel;

    @YamlNodeName("Maximum Level")
    public int maxLevel;
}
