/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ctrmap.formats.pokemon.gen5.pml;

/**
 *
 * @author David
 */
public class WBPMLLevelUpMove {
    private String move;
    private int learnLevel;

    public String getMoveID() { return move; }
    public void setMoveID(String move) { this.move = move; }

    public int getLearnLevel() { return learnLevel; }
    public void setLearnLevel(int learnLevel) { this.learnLevel = learnLevel; }
}
