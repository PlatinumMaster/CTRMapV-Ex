/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ctrmap.editor.gui.editors.util;

import java.util.HashMap;
import java.util.Map;
import xstandard.formats.yaml.Yaml;
import xstandard.formats.yaml.YamlNode;
import xstandard.fs.FSFile;

public class UserEnumHandler {
    private static HashMap<String, Integer> defs = new HashMap<String, Integer>();
    
    public static void Load(FSFile enum_def) {
        Yaml yml = new Yaml(enum_def);
        for (YamlNode node : yml.root.children) {
            if (node != null &&  node.getKey() != null && node.getKey().equals("DEFINE")) {
                for (YamlNode def : node.children) {
                    defs.put(def.getKey(), def.getValueInt());
                }
            }
            
        }
    }
    
    public static Integer Parse(Object value) throws Exception {
        if (value instanceof String && defs.containsKey((String)value)) {
            return defs.get(value);
        } else if (value instanceof Integer) {
            return (Integer) value;
        } else {
            throw new Exception(String.format("Not a string or integer! %s", value));
        }
    }
}
