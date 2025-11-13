package ctrmap.formats.pokemon.gen5.pml;

public class WBPMLLevelUpMove {
    private String move;
    private int learnLevel;
    
    public String GetMoveName() { return move; }
    public void SetMoveName(String move) { this.move = move; }
    
    public int GetLearnLevel() { return learnLevel; }
    public void SetLearnLevel(int learnLevel) { this.learnLevel = learnLevel; }
}
