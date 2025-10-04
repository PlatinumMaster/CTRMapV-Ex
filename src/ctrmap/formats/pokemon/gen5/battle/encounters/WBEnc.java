package ctrmap.formats.pokemon.gen5.battle.encounters;

import java.io.DataInput;
import java.io.IOException;
import java.util.ArrayList;

public class WBEnc {
    public static enum WBEncType {
            GRASS,
            GRASS_DOUBLE,
            GRASS_RARE,
            SURF,
            SURF_RARE,
            FISH,
            FISH_RARE;
            public static final WBEncType[] VALUES = values();
    }
    
    WBEncEntry[] GrassEnc, GrassDoubleEnc, GrassRareEnc;
    WBEncEntry[] SurfEnc, SurfRareEnc;
    WBEncEntry[] FishEnc, FishRareEnc;
    byte[] UserDataProps;
    byte FishEncProb, FishEncProb2, Flags;
    public static final int GRASS_ENC_SLOTS = 0xC, SURF_ENC_SLOTS = 0x5, FISH_ENC_SLOTS = 0x5;
    
    public WBEnc() {
        UserDataProps = new byte[] {0, 0, 0, 0, 0};
        FishEncProb = 0;
        FishEncProb2 = 0;
        Flags = 0;
        
        GrassEnc = new WBEncEntry[12];
        GrassDoubleEnc = new WBEncEntry[12];
        GrassRareEnc = new WBEncEntry[12];
        SurfEnc = new WBEncEntry[5];
        SurfRareEnc = new WBEncEntry[5];
        FishEnc = new WBEncEntry[5];
        FishRareEnc = new WBEncEntry[5];
    }
    
    public WBEnc(DataInput Input) throws IOException {
        this();
        // Header
        for (int Index = 0; Index < 0x5; ++Index) {     
            this.UserDataProps[Index] = Input.readByte();
        }
        this.FishEncProb = Input.readByte();
        this.FishEncProb2 = Input.readByte();
        this.Flags = Input.readByte();
        
        // Grass
        for (int Index = 0; Index < GRASS_ENC_SLOTS; ++Index) {
           this.GrassEnc[Index] = new WBEncEntry(Input);
        }
        for (int Index = 0; Index < GRASS_ENC_SLOTS; ++Index) {
           this.GrassDoubleEnc[Index] = (new WBEncEntry(Input));
        }
        for (int Index = 0; Index < GRASS_ENC_SLOTS; ++Index) {
           this.GrassRareEnc[Index] = (new WBEncEntry(Input));
        }
        // Surf
        for (int Index = 0; Index < SURF_ENC_SLOTS; ++Index) {
           this.SurfEnc[Index] = (new WBEncEntry(Input));
        }
        for (int Index = 0; Index < SURF_ENC_SLOTS; ++Index) {
           this.SurfRareEnc[Index] = (new WBEncEntry(Input));
        }
        // Fish
        for (int Index = 0; Index < FISH_ENC_SLOTS; ++Index) {
           this.FishEnc[Index] = (new WBEncEntry(Input));
        }
        for (int Index = 0; Index < FISH_ENC_SLOTS; ++Index) {
           this.FishRareEnc[Index] = (new WBEncEntry(Input));
        }
    }
    
    public WBEncEntry GetGrassEncounter(WBEncType Type, int Slot) {
        switch (Type) {
            case GRASS:
                return Slot < GRASS_ENC_SLOTS ? GrassEnc[Slot] : null;
            case GRASS_DOUBLE:
                return Slot < GRASS_ENC_SLOTS ? GrassDoubleEnc[Slot] : null;
            case GRASS_RARE:
                return Slot < GRASS_ENC_SLOTS ? GrassRareEnc[Slot] : null;
            default:
                return null;
        }
    }
    
    public void SetGrassEncounter(WBEncType Type, int Slot, WBEncEntry entry) {
        switch (Type) {
            case GRASS:
               GrassEnc[Slot] = entry;
               break;
            case GRASS_DOUBLE:
               GrassDoubleEnc[Slot] = entry;
               break;
            case GRASS_RARE:
               GrassRareEnc[Slot] = entry;
               break;
            default:
                break;
        }  
    }
    
    public WBEncEntry GetSurfEncounter(WBEncType Type, int Slot) {
        switch (Type) {
            case SURF:
                return Slot < SURF_ENC_SLOTS ? SurfEnc[Slot] : null;
            case SURF_RARE:
                return Slot < SURF_ENC_SLOTS ? SurfRareEnc[Slot] : null;
            default:
                return null;
        }
    }
    
    public void SetSurfEncounter(WBEncType Type, int Slot, WBEncEntry entry) {
        switch (Type) {
            case SURF:
               SurfEnc[Slot] = entry;
               break;
            case SURF_RARE:
               SurfRareEnc[Slot] = entry;
               break;
            default:
                break;
        }  
    }
    
    public WBEncEntry GetFishEncounter(WBEncType Type, int Slot) {
        switch (Type) {
            case FISH:
                return Slot < FISH_ENC_SLOTS ? FishEnc[Slot] : null;
            case FISH_RARE:
                return Slot < FISH_ENC_SLOTS ? FishRareEnc[Slot] : null;
            default:
                return null;
        }
    }
    
    public void SetFishEncounter(WBEncType Type, int Slot, WBEncEntry entry) {
        switch (Type) {
            case FISH:
               FishEnc[Slot] = entry;
               break;
            case FISH_RARE:
               FishRareEnc[Slot] = entry;
               break;
            default:
                break;
        }  
    }
}
