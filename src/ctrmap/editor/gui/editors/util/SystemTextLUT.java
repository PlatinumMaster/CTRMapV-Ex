/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ctrmap.editor.gui.editors.util;

import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SystemTextLUT {
    private static final Map<String, Integer> lutMap = Stream.of(
            new AbstractMap.SimpleEntry<>("ItemDescriptions", 63), 
            new AbstractMap.SimpleEntry<>("Items", 64), 
            new AbstractMap.SimpleEntry<>("Pokemon", 90),   
            new AbstractMap.SimpleEntry<>("BattleTypes", 357),
            new AbstractMap.SimpleEntry<>("SpecialTrainers", 368),
            new AbstractMap.SimpleEntry<>("Abilities", 374),
            new AbstractMap.SimpleEntry<>("TrainerDialogue", 381),
            new AbstractMap.SimpleEntry<>("Trainers", 382),
            new AbstractMap.SimpleEntry<>("TrainerClasses", 383),
            new AbstractMap.SimpleEntry<>("MovesDescriptions", 402),
            new AbstractMap.SimpleEntry<>("Moves", 403)
        )
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)
    );
    
    public static int get(String value) {
        return lutMap.get(value);
    }
}
