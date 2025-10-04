/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ctrmap.editor.gui.editors.util;

import ctrmap.formats.pokemon.text.GenVMessageHandler;
import ctrmap.formats.pokemon.text.TextFile;
import ctrmap.missioncontrol_ntr.fs.NARCRef;
import ctrmap.missioncontrol_ntr.fs.NTRGameFS;
import java.util.HashMap;
import java.util.Map;

public class TextSingleton {    
    private static Map<NARCRef, Map<Integer, TextFile>> TextMapping = new HashMap<NARCRef, Map<Integer, TextFile>> () {{
        put(NARCRef.MSGDATA_SYSTEM, new HashMap<Integer, TextFile>());
        put(NARCRef.MSGDATA_SCRIPT ,new HashMap<Integer, TextFile>());
    }};
    
    public static TextFile Load(NTRGameFS fs, NARCRef arc, int file) {
        if (arc != NARCRef.MSGDATA_SYSTEM && arc != NARCRef.MSGDATA_SCRIPT) {
            return null;
        }
        
        HashMap<Integer, TextFile> arcText = (HashMap<Integer, TextFile>) TextMapping.get(arc);
        
        if (!arcText.containsKey(file)) {
            arcText.put(file, new TextFile(fs.NARCGet(NARCRef.MSGDATA_SYSTEM, file), GenVMessageHandler.INSTANCE));
        }
        
        return arcText.get(file);
    }
}
