package ctrmap.formats.pokemon.gen5.pml;

import java.io.DataInput;
import java.io.IOException;
import java.util.ArrayList;

public class WBPMLLearnsets {
    public static final int MOVES_COUNT_MAX = 26;
    ArrayList<WBPMLLevelUpMove> LevelUpMoves;
    
    public WBPMLLearnsets() {
        LevelUpMoves = new ArrayList<>();
    }
    
    public WBPMLLearnsets(WBPMLLevelUpMove move) throws IOException {
        this();
        
        LevelUpMoves.add(move);

    }
    
    public int GetLearnsetSize() {
        return this.LevelUpMoves.size();
    }
    
    public WBPMLLevelUpMove GetLevelUpMove(int Index) {
        return Index < GetLearnsetSize() ? this.LevelUpMoves.get(Index) : null;
    }
    
    public void SetLevelUpMove(int MoveIndex, WBPMLLevelUpMove move) {
        if (LevelUpMoves.size() < 26) {
            LevelUpMoves.set(MoveIndex, move);
        }
    }
    
    public void addLevelUpMove(WBPMLLevelUpMove move) {
        this.LevelUpMoves.add(move);
    }
    
    public String printMoveName(int Index) {
        return this.LevelUpMoves.get(Index).GetMoveName();
    }
}
