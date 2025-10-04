/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ctrmap.editor.gui.editors.gen5.battle.encounters;

import xstandard.formats.yaml.YamlNodeName;

/**
 *
 * @author L33TG
 */
public class VEncounterContainerBinding {
    @YamlNodeName("GRASS_SINGLES")
    public int grassSingles;

    @YamlNodeName("GRASS_DOUBLES")
    public int grassDoubles;

    @YamlNodeName("GRASS_RARE")
    public int grassRare;

    @YamlNodeName("SURF_SINGLES")
    public int surfSingles;

    @YamlNodeName("SURF_RARE")
    public int surfRare;

    @YamlNodeName("FISH_SINGLES")
    public int fishSingles;

    @YamlNodeName("FISH_RARE")
    public int fishRare;

    @YamlNodeName("UNKNOWN")
    public int unknown;

    @YamlNodeName("GRASS")
    public VEncounterMethodBinding grass;

    @YamlNodeName("SURF")
    public VEncounterMethodBinding surf;

    @YamlNodeName("FISH")
    public VEncounterMethodBinding fish;
}
