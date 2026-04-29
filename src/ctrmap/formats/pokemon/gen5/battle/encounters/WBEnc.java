package ctrmap.formats.pokemon.gen5.battle.encounters;

import java.io.DataInput;
import java.io.IOException;
import java.util.ArrayList;
<<<<<<< HEAD
=======
import xstandard.fs.FSFile;
import xstandard.io.base.impl.ext.data.DataIOStream;
>>>>>>> 687ba7a2a4bb0efb135b3f8e32f3f1163e8d32f2

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
<<<<<<< HEAD
    
    WBEncEntry[] GrassEnc, GrassDoubleEnc, GrassRareEnc;
    WBEncEntry[] SurfEnc, SurfRareEnc;
    WBEncEntry[] FishEnc, FishRareEnc;
=======
    ArrayList<WBEncEntry> GrassEnc, GrassDoubleEnc, GrassRareEnc;
    ArrayList<WBEncEntry> SurfEnc, SurfRareEnc;
    ArrayList<WBEncEntry> FishEnc, FishRareEnc;
>>>>>>> 687ba7a2a4bb0efb135b3f8e32f3f1163e8d32f2
    byte[] UserDataProps;
    byte FishEncProb, FishEncProb2, Flags;
    public static final int GRASS_ENC_SLOTS = 0xC, SURF_ENC_SLOTS = 0x5, FISH_ENC_SLOTS = 0x5;
    
    public WBEnc() {
        UserDataProps = new byte[] {0, 0, 0, 0, 0};
        FishEncProb = 0;
        FishEncProb2 = 0;
        Flags = 0;
<<<<<<< HEAD
        
        GrassEnc = new WBEncEntry[12];
        GrassDoubleEnc = new WBEncEntry[12];
        GrassRareEnc = new WBEncEntry[12];
        SurfEnc = new WBEncEntry[5];
        SurfRareEnc = new WBEncEntry[5];
        FishEnc = new WBEncEntry[5];
        FishRareEnc = new WBEncEntry[5];
=======
        // Realistically, it should be 
        GrassEnc = new ArrayList<>();
        GrassDoubleEnc = new ArrayList<>();
        GrassRareEnc = new ArrayList<>();
        SurfEnc = new ArrayList<>();
        SurfRareEnc = new ArrayList<>();
        FishEnc = new ArrayList<>();
        FishRareEnc = new ArrayList<>();
>>>>>>> 687ba7a2a4bb0efb135b3f8e32f3f1163e8d32f2
    }
    
    public WBEnc(DataInput Input) throws IOException {
        this();
<<<<<<< HEAD
=======
        System.out.println("Encounter test...");
>>>>>>> 687ba7a2a4bb0efb135b3f8e32f3f1163e8d32f2
        // Header
        for (int Index = 0; Index < 0x5; ++Index) {     
            this.UserDataProps[Index] = Input.readByte();
        }
        this.FishEncProb = Input.readByte();
        this.FishEncProb2 = Input.readByte();
        this.Flags = Input.readByte();
<<<<<<< HEAD
        
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
=======
        // Grass
        for (int Index = 0; Index < GRASS_ENC_SLOTS; ++Index) {
           this.GrassEnc.add(new WBEncEntry(Input));
        }
        for (int Index = 0; Index < GRASS_ENC_SLOTS; ++Index) {
           this.GrassDoubleEnc.add(new WBEncEntry(Input));
        }
        for (int Index = 0; Index < GRASS_ENC_SLOTS; ++Index) {
           this.GrassRareEnc.add(new WBEncEntry(Input));
        }
        // Surf
        for (int Index = 0; Index < SURF_ENC_SLOTS; ++Index) {
           this.SurfEnc.add(new WBEncEntry(Input));
        }
        for (int Index = 0; Index < SURF_ENC_SLOTS; ++Index) {
           this.SurfRareEnc.add(new WBEncEntry(Input));
        }
        // Fish
        for (int Index = 0; Index < FISH_ENC_SLOTS; ++Index) {
           this.FishEnc.add(new WBEncEntry(Input));
        }
        for (int Index = 0; Index < FISH_ENC_SLOTS; ++Index) {
           this.FishRareEnc.add(new WBEncEntry(Input));
>>>>>>> 687ba7a2a4bb0efb135b3f8e32f3f1163e8d32f2
        }
    }
    
    public WBEncEntry GetGrassEncounter(WBEncType Type, int Slot) {
        switch (Type) {
            case GRASS:
<<<<<<< HEAD
                return Slot < GRASS_ENC_SLOTS ? GrassEnc[Slot] : null;
            case GRASS_DOUBLE:
                return Slot < GRASS_ENC_SLOTS ? GrassDoubleEnc[Slot] : null;
            case GRASS_RARE:
                return Slot < GRASS_ENC_SLOTS ? GrassRareEnc[Slot] : null;
=======
                return Slot < Math.min(GRASS_ENC_SLOTS, GrassEnc.size()) ? GrassEnc.get(Slot) : null;
            case GRASS_DOUBLE:
                return Slot < Math.min(GRASS_ENC_SLOTS, GrassDoubleEnc.size()) ? GrassDoubleEnc.get(Slot) : null;
            case GRASS_RARE:
                return Slot < Math.min(GRASS_ENC_SLOTS, GrassRareEnc.size()) ? GrassRareEnc.get(Slot) : null;
>>>>>>> 687ba7a2a4bb0efb135b3f8e32f3f1163e8d32f2
            default:
                return null;
        }
    }
    
<<<<<<< HEAD
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
=======
    public WBEncEntry GetSurfEncounter(WBEncType Type, int Slot) {
        switch (Type) {
            case SURF:
                return Slot < SurfEnc.size() ? SurfEnc.get(Slot) : null;
            case SURF_RARE:
                return Slot < SurfRareEnc.size() ? SurfRareEnc.get(Slot) : null;
>>>>>>> 687ba7a2a4bb0efb135b3f8e32f3f1163e8d32f2
            default:
                return null;
        }
    }
    
<<<<<<< HEAD
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
=======
    public void Serialize(DataIOStream out) throws IOException {
        for (int Index = 0; Index < 0x5; ++Index) {
            out.writeByte(UserDataProps[Index]);
        }
        out.writeByte(FishEncProb);
        out.writeByte(FishEncProb2);
        out.writeByte(Flags);
        for (WBEncEntry e : GrassEnc) e.Serialize(out);
        for (WBEncEntry e : GrassDoubleEnc) e.Serialize(out);
        for (WBEncEntry e : GrassRareEnc) e.Serialize(out);
        for (WBEncEntry e : SurfEnc) e.Serialize(out);
        for (WBEncEntry e : SurfRareEnc) e.Serialize(out);
        for (WBEncEntry e : FishEnc) e.Serialize(out);
        for (WBEncEntry e : FishRareEnc) e.Serialize(out);
    }

    public WBEncEntry GetFishEncounter(WBEncType Type, int Slot) {
        switch (Type) {
            case FISH:
                return Slot < FishEnc.size() ? FishEnc.get(Slot) : null;
            case FISH_RARE:
                return Slot < FishRareEnc.size() ? FishRareEnc.get(Slot) : null;
>>>>>>> 687ba7a2a4bb0efb135b3f8e32f3f1163e8d32f2
            default:
                return null;
        }
    }
<<<<<<< HEAD
    
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
=======
>>>>>>> 687ba7a2a4bb0efb135b3f8e32f3f1163e8d32f2
}
