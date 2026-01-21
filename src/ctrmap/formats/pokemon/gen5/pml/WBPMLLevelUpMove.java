package ctrmap.formats.pokemon.gen5.pml;

import java.io.IOException;

public class WBPMLLevelUpMove {
    String Move;
    int LearnLevel;
    
    public WBPMLLevelUpMove () {
        Move = "0xFFFF";
        LearnLevel = 0xFFFF;
    }
    
    public WBPMLLevelUpMove (String inMove, int inLearnLevel) throws IOException {
        this();
        Move = inMove;
        LearnLevel = inLearnLevel;
    }
    
    public String GetMoveName() {
        return this.Move;
    }
    
    public void SetMoveName(String Move) {
        this.Move = Move;
    }
    
    public int GetLearnLevel() {
        return this.LearnLevel;
    }
    
    public void SetLearnLevel(int LearnLevel) {
        this.LearnLevel = LearnLevel;
    }
    
    
}
