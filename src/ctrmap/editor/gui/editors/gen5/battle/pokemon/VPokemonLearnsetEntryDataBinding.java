package ctrmap.editor.gui.editors.gen5.battle.pokemon;

import xstandard.formats.yaml.YamlNodeName;

public class VPokemonLearnsetEntryDataBinding {
    @YamlNodeName("MOVE")
    public String move;
    
    @YamlNodeName("LEVEL")
    public int level;
}
