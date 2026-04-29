package ctrmap.editor.gui.editors.gen5.battle;

import ctrmap.creativestudio.ngcs2d.canvas.SpriteRenderer;
import ctrmap.creativestudio.ngcs2d.res.Sprite2DCell;
import ctrmap.creativestudio.ngcs2d.res.Sprite2DMultiCell;
import ctrmap.creativestudio.ngcs2d.res.Sprite2DMultiCellAnimation;
import ctrmap.creativestudio.ngcs2d.res.Sprite2DOAM;
import ctrmap.creativestudio.ngcs2d.res.Sprite2DPalette;
import ctrmap.creativestudio.ngcs2d.res.Sprite2DResource;
import ctrmap.creativestudio.ngcs2d.res.Sprite2DTileSheet;
import ctrmap.creativestudio.nitro2dplugin.Gen5NGCS2DPlugin;
import ctrmap.formats.common.GameInfo;
import ctrmap.formats.ntr.common.compression.LZ1X;
import ctrmap.missioncontrol_ntr.fs.NARCRef;
import ctrmap.missioncontrol_ntr.fs.NTRGameFS;
import xstandard.fs.FSFile;
import xstandard.fs.accessors.MemoryFile;
import xstandard.io.base.impl.access.MemoryStream;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Shared utility class for loading Pokemon icons and trainer sprites
 * from NDS 2D graphics NARCs.
 *
 * Trainer sprite NARC layout (8 files per trainer class):
 *   8*index + 0 = NCBR  (character data, bitmap/linear mode)
 *   8*index + 1 = NCGR  (character data, tiled mode)
 *   8*index + 2 = NCER  (cell definitions)
 *   8*index + 3 = NANR  (cell animation)
 *   8*index + 4 = NMCR  (multi-cell)
 *   8*index + 5 = NMAR  (multi-cell animation)
 *   8*index + 6 = Transformation file
 *   8*index + 7 = NCLR  (palette)
 *
 * Pokemon icon NARC layout (Gen5 BW/BW2):
 *   One NCGR per species (32x32 or 32x64 icon), palette shared.
 *   NARC may store files as: all NCGRs followed by a shared NCLR,
 *   or interleaved NCGR+NCLR pairs. Files may be LZ-compressed.
 */
public class SpriteImageLoader {

    private static final int TRAINER_FILES_PER_CLASS = 8;
    private static final int TRAINER_OFF_NCBR = 0;
    private static final int TRAINER_OFF_NCGR = 1;
    private static final int TRAINER_OFF_NCER = 2;
    private static final int TRAINER_OFF_NANR = 3;
    private static final int TRAINER_OFF_NMCR = 4;
    private static final int TRAINER_OFF_NMAR = 5;
    private static final int TRAINER_OFF_XFORM = 6;
    private static final int TRAINER_OFF_NCLR = 7;

    /**
     * Pokemon battle sprite NARC layout (20 files per species). The 20 slots
     * are split into two parallel halves — a front sprite set (files +0..+8)
     * and a back sprite set (files +9..+17) — followed by two shared
     * palettes (files +18 = normal, +19 = shiny). Loading BOTH halves into
     * the same {@link Sprite2DResource} collides on cell names (both NCERs
     * use "Cell_0", "Cell_1", ...), so the front NMCR ends up pointing at
     * back-sprite cells. Always load exactly one half.
     */
    private static final int POKE_FILES_PER_SPECIES = 20;
    private static final int POKE_FRONT_NCGR_1D = 0;   // bitmap / 1D-mapped
    private static final int POKE_FRONT_NCGR_2D = 2;   // tiled / 2D-mapped
    private static final int POKE_FRONT_NCER = 4;
    private static final int POKE_FRONT_NANR = 5;
    private static final int POKE_FRONT_NMCR = 6;
    private static final int POKE_FRONT_NMAR = 7;
    private static final int POKE_BACK_NCGR_1D = 9;
    private static final int POKE_BACK_NCGR_2D = 11;
    private static final int POKE_BACK_NCER = 13;
    private static final int POKE_BACK_NANR = 14;
    private static final int POKE_BACK_NMCR = 15;
    private static final int POKE_BACK_NMAR = 16;
    private static final int POKE_NCLR_NORMAL = 18;
    private static final int POKE_NCLR_SHINY = 19;

    /** Trainer preview output size in pixels. */
    private static final int TRAINER_PREVIEW_SIZE = 96;

    /** First NCGR in the icon NARC (files 0-6 are palette + shared NANR/NCER). */
    private static final int ICON_NCGR_BASE = 7;
    /** Stride between consecutive species NCGRs (NCGR, empty, NCGR, empty...). */
    private static final int ICON_NCGR_STRIDE = 2;

    /** Cached fallback palette for Pokemon icons (lazy-initialized). */
    private static Sprite2DPalette cachedIconFallbackPalette = null;

    // ------------------------------------------------------------------
    // Animation frame cache (bounded LRU).
    //
    // Loading a trainer or Pokemon battle sprite is expensive: 7-8 NARC
    // reads + LZ decompression + full Nitro 2D parse + a 60-240 frame
    // render pass. Without caching, every trainer selection change in
    // the editor re-does all of it synchronously on the EDT for the
    // trainer itself AND for every party Pokemon (up to 6 more full
    // renders per switch). Caching the rendered frame list makes repeat
    // selections basically instant and bounds how much memory the
    // editor can accumulate over a session.
    //
    // Keyed by the integer class index / species index used by the
    // loaders. Invalidated when the underlying NARC is edited — see
    // {@link #invalidateTrainerSprite(int)}.
    // ------------------------------------------------------------------

    private static final int TRAINER_CACHE_CAPACITY = 24;
    private static final int POKEMON_CACHE_CAPACITY = 48;

    private static final java.util.Map<Integer, java.util.List<java.awt.image.BufferedImage>>
        trainerFrameCache = java.util.Collections.synchronizedMap(
            new java.util.LinkedHashMap<Integer, java.util.List<java.awt.image.BufferedImage>>(
                TRAINER_CACHE_CAPACITY + 4, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(java.util.Map.Entry<Integer, java.util.List<java.awt.image.BufferedImage>> e) {
                    return size() > TRAINER_CACHE_CAPACITY;
                }
            });

    private static final java.util.Map<Integer, java.util.List<java.awt.image.BufferedImage>>
        pokemonFrameCache = java.util.Collections.synchronizedMap(
            new java.util.LinkedHashMap<Integer, java.util.List<java.awt.image.BufferedImage>>(
                POKEMON_CACHE_CAPACITY + 4, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(java.util.Map.Entry<Integer, java.util.List<java.awt.image.BufferedImage>> e) {
                    return size() > POKEMON_CACHE_CAPACITY;
                }
            });

    /** Drops the cached frames for the given trainer class so the next
     *  preview load re-renders from the NARC. Call after writing edits
     *  back via {@link #saveTrainerSpriteResource}. */
    public static void invalidateTrainerSprite(int trainerClassIndex) {
        trainerFrameCache.remove(trainerClassIndex);
    }

    /** Drops the cached frames for the given species. Call after writing
     *  edits back to the Pokemon battle-sprite NARC. */
    public static void invalidatePokemonSprite(int speciesIndex) {
        pokemonFrameCache.remove(speciesIndex);
    }

    /** Clears both caches — call on project unload / swap. */
    public static void clearSpriteCaches() {
        trainerFrameCache.clear();
        pokemonFrameCache.clear();
    }

    /**
     * Loads a Pokemon icon from the PML_G2D_POKE_ICON NARC.
     *
     * Gen5 BW2 NARC layout (1431 files):
     *   File 0       = shared NCLR palette
     *   Files 1-2    = shared NANR + NCER (animation + cell defs, frame 0)
     *   Files 3-4    = shared NANR + NCER (frame 1)
     *   Files 5-6    = shared NANR + NCER (alternate)
     *   Files 7+     = NCGRs at every other index: 7, 9, 11, ...
     *                   NCGR for species N = file (7 + N * 2)
     *                   Odd indices = NCGR data, even indices = empty
     *
     * Files may be LZ10/LZ11 compressed.
     */
    public static BufferedImage loadPokemonIcon(NTRGameFS fs, int speciesIndex) {
        try {
            int maxFiles = fs.NARCGetDataMax(NARCRef.PML_G2D_POKE_ICON);

            // Shared palette is file 0
            if (cachedIconFallbackPalette == null) {
                cachedIconFallbackPalette = importPalette(loadNarcFile(fs, NARCRef.PML_G2D_POKE_ICON, 0));
                if (cachedIconFallbackPalette == null) {
                    cachedIconFallbackPalette = scanForPalette(fs, NARCRef.PML_G2D_POKE_ICON, maxFiles);
                }
            }
            Sprite2DPalette pal = cachedIconFallbackPalette;
            if (pal == null) {
                return null;
            }

            // NCGR at file (7 + speciesIndex * 2)
            // First 7 files are palette + shared NANR/NCER pairs;
            // then NCGRs at every other index starting from 7.
            int ncgrIndex = ICON_NCGR_BASE + speciesIndex * ICON_NCGR_STRIDE;
            if (ncgrIndex >= maxFiles) {
                return null;
            }
            Sprite2DTileSheet ts = importTileSheet(loadNarcFile(fs, NARCRef.PML_G2D_POKE_ICON, ncgrIndex));
            if (ts == null) {
                return null;
            }

            // Derive icon dimensions from tile count.
            // Icons are 32px wide (4 tiles); height depends on frame count.
            int tileCount = ts.getTileCount();
            int tilesWide = 4; // 32px / 8px-per-tile
            int tilesHigh = Math.max(1, tileCount / tilesWide);
            int iconH = Math.min(32, tilesHigh * 8); // show first frame only

            Sprite2DOAM oam = new Sprite2DOAM();
            oam.x = 0;
            oam.y = 0;
            oam.width = 32;
            oam.height = iconH;
            oam.tileIndex = 0;
            oam.paletteIndex = 0;
            oam.flipH = false;
            oam.flipV = false;
            oam.priority = 0;

            Sprite2DCell cell = new Sprite2DCell("icon");
            cell.addOAM(oam);

            return SpriteRenderer.renderCell(cell, ts, pal);
        } catch (Exception e) {
            System.err.println("[SpriteImageLoader] loadPokemonIcon failed for species " + speciesIndex + ": " + e);
            return null;
        }
    }

    /**
     * Convenience: loads only the first animation frame of a trainer sprite.
     * Returns a single 96x96 image, or null on failure.
     */
    public static BufferedImage loadTrainerSprite(NTRGameFS fs, GameInfo game, int trainerClassIndex) {
        List<BufferedImage> frames = loadTrainerSpriteFrames(fs, game, trainerClassIndex);
        return frames.isEmpty() ? null : frames.get(0);
    }

    /** Hard cap on frames returned per trainer to keep memory bounded. */
    private static final int MAX_TRAINER_FRAMES = 240;

    /**
     * Minimum ticks each NMAR is played in the preview. Ensures NMARs
     * with trivial 4-tick single-frame contents (the BW2 dancer case,
     * where each pose is its own NMAR) stay on screen long enough for
     * the sub-NANR body-part animations to complete at least one cycle.
     */
    private static final int MIN_NMAR_PLAY_TICKS = 60;

    /**
     * Loads all animation frames of a trainer battle sprite by walking the
     * NMAR's timeline. For each NDS tick in the full cycle we:
     * <ol>
     *   <li>Look up which NMAR frame is active at that tick (each NMAR
     *       frame has a duration and picks one MultiCell).</li>
     *   <li>Render that MultiCell at the tick, so each entry's sub-NANR
     *       advances in real time.</li>
     *   <li>Paint the result bottom-anchored into a 96x96 canvas.</li>
     * </ol>
     * This drives the whole NMAR → NMCR → NANR → NCER → NCGR chain from
     * the actual sprite data; trainers with multiple poses (dancers at
     * class 38+) animate those poses as authored.
     *
     * <p>For trainers whose NMAR is a single-frame idle (the common
     * Hilbert/Rosa case), the return list collapses to one frame and the
     * UI just shows a static sprite — matching in-battle behaviour
     * (the "breathing" oscillation is a UI effect added by the battle
     * engine, not part of the sprite data).</p>
     *
     * Returns a list of 96x96 images, or an empty list if the sprite
     * cannot be loaded (e.g. BW1, missing files).
     */
    public static List<BufferedImage> loadTrainerSpriteFrames(NTRGameFS fs, GameInfo game, int trainerClassIndex) {
        List<BufferedImage> cached = trainerFrameCache.get(trainerClassIndex);
        if (cached != null) {
            return cached;
        }
        Sprite2DResource res = loadTrainerSpriteResource(fs, game, trainerClassIndex);
        if (res == null) {
            List<BufferedImage> empty = Collections.emptyList();
            trainerFrameCache.put(trainerClassIndex, empty);
            return empty;
        }
        List<BufferedImage> frames = renderNmarTimeline(res);
        trainerFrameCache.put(trainerClassIndex, frames);
        return frames;
    }

    /**
     * Builds a {@link Sprite2DResource} from the 8-file slot the trainer
     * NARC reserves for one trainer class (NCBR, NCGR, NCER, NANR, NMCR,
     * NMAR, xform, NCLR). Shared by the animation-frame preview renderer
     * and the "Open in CS 2D" button — the latter hands the resource
     * straight to the embedded NGCS2D editor so the user edits live
     * sprite data with no intermediate re-import pass.
     *
     * @return a ready-to-render resource (cells/tileSheets/palettes all
     *         non-empty), or {@code null} if the NARC entry can't be
     *         loaded (BW1, missing files, empty slot).
     */
    public static Sprite2DResource loadTrainerSpriteResource(
            NTRGameFS fs, GameInfo game, int trainerClassIndex) {
        try {
            if (NARCRef.TRAINER_G2D_BTL_F.getARCID(game) < 0) {
                return null;
            }

            int maxFiles = fs.NARCGetDataMax(NARCRef.TRAINER_G2D_BTL_F);
            int base = trainerClassIndex * TRAINER_FILES_PER_CLASS;

            if (base + TRAINER_OFF_NCLR >= maxFiles) {
                return null;
            }

            // BW/BW2 trainer NARCs ship TWO character blocks per class:
            //   +0 NCBR — tiled / 1D-addressed (rasterLayout=false)
            //   +1 NCGR — raster bitmap / 2D-addressed (rasterLayout=true)
            // Which one the NCER's OAM tileIndex values address depends on
            // the NCER's mapping mode (1D vs 2D). Load both; let
            // Sprite2DResource.getActiveTileSheet() pick based on the NCER.
            FSFile ncbrFile = loadNarcFile(fs, NARCRef.TRAINER_G2D_BTL_F, base + TRAINER_OFF_NCBR);
            FSFile ncgrFile = loadNarcFile(fs, NARCRef.TRAINER_G2D_BTL_F, base + TRAINER_OFF_NCGR);
            FSFile nclrFile = loadNarcFile(fs, NARCRef.TRAINER_G2D_BTL_F, base + TRAINER_OFF_NCLR);
            FSFile ncerFile = loadNarcFile(fs, NARCRef.TRAINER_G2D_BTL_F, base + TRAINER_OFF_NCER);
            FSFile nanrFile = loadNarcFile(fs, NARCRef.TRAINER_G2D_BTL_F, base + TRAINER_OFF_NANR);
            FSFile nmcrFile = loadNarcFile(fs, NARCRef.TRAINER_G2D_BTL_F, base + TRAINER_OFF_NMCR);
            FSFile nmarFile = loadNarcFile(fs, NARCRef.TRAINER_G2D_BTL_F, base + TRAINER_OFF_NMAR);

            if ((ncbrFile == null && ncgrFile == null) || nclrFile == null) {
                return null;
            }

            // Build unified Sprite2DResource from all Nitro 2D files. Both
            // character blocks are imported; each becomes its own tile
            // sheet (renamed to prevent the name-based merge dedup from
            // collapsing them). getActiveTileSheet() picks the right one.
            Sprite2DResource res = new Sprite2DResource();
            if (ncbrFile != null) mergeImport(res, ncbrFile, ImportType.CGR, "NCBR");
            if (ncgrFile != null) mergeImport(res, ncgrFile, ImportType.CGR, "NCGR");
            for (int i = 0; i < res.tileSheets.size(); i++) {
                res.tileSheets.get(i).name = "TileSheet_" + i;
            }
            mergeImport(res, nclrFile, ImportType.CLR, "NCLR");
            mergeImport(res, ncerFile, ImportType.CER, "NCER");
            mergeImport(res, nanrFile, ImportType.ANR, "NANR");
            mergeImport(res, nmcrFile, ImportType.MCR, "NMCR");
            mergeImport(res, nmarFile, ImportType.MAR, "NMAR");
            res.linkTileSheetsToCells();

            if (res.cells.isEmpty() || res.tileSheets.isEmpty() || res.palettes.isEmpty()) {
                return null;
            }

            return res;
        } catch (Exception e) {
            System.err.println("[SpriteImageLoader] loadTrainerSpriteResource failed for class "
                + trainerClassIndex + ": " + e);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Writes an edited trainer {@link Sprite2DResource} back to the 8-file
     * slot the trainer NARC reserves for the given class. Each of the six
     * Nitro 2D blocks (NCLR/NCGR/NCER/NANR/NMCR/NMAR) is re-serialised
     * via the matching {@code Gen5NGCS2DPlugin.CSNNS_*} exporter, which
     * writes the updated bytes in place to the NARC's FSFile slot.
     *
     * <p>The transform file (slot +6) and the NCBR slot (+0, alternate
     * character-data layout) are NOT re-written — they aren't surfaced in
     * the NGCS2D editor UI, and the editor only produces a single active
     * tile sheet per save. Preserving the NCBR slot verbatim means a
     * save-then-reload round-trips cleanly on unedited tile data.</p>
     *
     * <p>Call sites are expected to also flush the NARC (e.g. via
     * {@code NTRGameFS.save()}) after this returns — this method only
     * updates the in-memory NARC entries.</p>
     */
    public static void saveTrainerSpriteResource(
            NTRGameFS fs, int trainerClassIndex, Sprite2DResource res) {
        if (res == null) return;
        try {
            int maxFiles = fs.NARCGetDataMax(NARCRef.TRAINER_G2D_BTL_F);
            int base = trainerClassIndex * TRAINER_FILES_PER_CLASS;
            if (base + TRAINER_OFF_NCLR >= maxFiles) {
                return;
            }
            writeSlot(fs, base + TRAINER_OFF_NCGR, res, ImportType.CGR, "NCGR");
            writeSlot(fs, base + TRAINER_OFF_NCER, res, ImportType.CER, "NCER");
            writeSlot(fs, base + TRAINER_OFF_NANR, res, ImportType.ANR, "NANR");
            writeSlot(fs, base + TRAINER_OFF_NMCR, res, ImportType.MCR, "NMCR");
            writeSlot(fs, base + TRAINER_OFF_NMAR, res, ImportType.MAR, "NMAR");
            writeSlot(fs, base + TRAINER_OFF_NCLR, res, ImportType.CLR, "NCLR");
            // Bust the frame cache so the next preview render picks up
            // the edited sprite instead of serving the pre-edit frames.
            invalidateTrainerSprite(trainerClassIndex);
        } catch (Exception e) {
            System.err.println("[SpriteImageLoader] saveTrainerSpriteResource failed for class "
                + trainerClassIndex + ": " + e);
            e.printStackTrace();
        }
    }

    /**
     * Writes one sprite block (one of NCLR/NCGR/NCER/NANR/NMCR/NMAR) back
     * to its NARC slot using the matching CSNNS_* exporter.
     */
    private static void writeSlot(NTRGameFS fs, int fileIndex,
            Sprite2DResource res, ImportType type, String label) {
        FSFile target = fs.NARCGet(NARCRef.TRAINER_G2D_BTL_F, fileIndex);
        if (target == null) return;
        try {
            switch (type) {
                case CGR: Gen5NGCS2DPlugin.CSNNS_CGR.exportResource(res, target, null); break;
                case CLR: Gen5NGCS2DPlugin.CSNNS_CLR.exportResource(res, target, null); break;
                case CER: Gen5NGCS2DPlugin.CSNNS_CER.exportResource(res, target, null); break;
                case ANR: Gen5NGCS2DPlugin.CSNNS_ANR.exportResource(res, target, null); break;
                case MCR: Gen5NGCS2DPlugin.CSNNS_MCR.exportResource(res, target, null); break;
                case MAR: Gen5NGCS2DPlugin.CSNNS_MAR.exportResource(res, target, null); break;
            }
        } catch (Exception e) {
            System.err.println("[SpriteImageLoader] Failed to export " + label
                + " to slot " + fileIndex + ": " + e);
        }
    }

    /** Pokemon battle sprite preview size for party slot previews. */
    private static final int POKEMON_BATTLE_PREVIEW_SIZE = 64;

    /**
     * Loads all animation frames of a Pokemon's FRONT battle sprite from the
     * PML_G2D_POKE_SPRITE NARC (the full animated battle sprite, not the
     * small party icon).
     *
     * <p>Layout per species (20 files):</p>
     * <ul>
     *   <li>+0  = NCGR (1D, rasterLayout=true per NitroPaint flags)</li>
     *   <li>+2  = NCGR (bitmap / 2D-mapped)</li>
     *   <li>+4  = NCER (species cells — 2D mapping for BW2 Pokemon)</li>
     *   <li>+5  = NANR, +6 = NMCR, +7 = NMAR</li>
     *   <li>+9..+17 = back sprite (skipped here — would collide on cell
     *       names otherwise)</li>
     *   <li>+18 = NCLR normal palette</li>
     * </ul>
     *
     * <p>Only the front-sprite half is loaded to avoid the name-collision
     * that makes front NCER entries point at back cells.</p>
     *
     * @return Animation frames at {@link #POKEMON_BATTLE_PREVIEW_SIZE}²,
     *         or empty list if the NARC can't be read or the species has
     *         no sprite data.
     */
    public static List<BufferedImage> loadPokemonBattleSpriteFrames(NTRGameFS fs, int speciesIndex) {
        List<BufferedImage> cached = pokemonFrameCache.get(speciesIndex);
        if (cached != null) {
            return cached;
        }
        List<BufferedImage> frames = loadPokemonBattleSpriteFramesUncached(fs, speciesIndex);
        pokemonFrameCache.put(speciesIndex, frames);
        return frames;
    }

    private static List<BufferedImage> loadPokemonBattleSpriteFramesUncached(NTRGameFS fs, int speciesIndex) {
        try {
            if (speciesIndex < 0) {
                return Collections.emptyList();
            }
            int maxFiles;
            try {
                maxFiles = fs.NARCGetDataMax(NARCRef.PML_G2D_POKE_SPRITE);
            } catch (Exception e) {
                // NARC not present in this ROM (BW1 before patches, partial
                // dumps). Fall back to the icon silently.
                return Collections.emptyList();
            }
            if (maxFiles <= 0) {
                return Collections.emptyList();
            }
            int base = speciesIndex * POKE_FILES_PER_SPECIES;
            if (base + POKE_NCLR_NORMAL >= maxFiles) {
                return Collections.emptyList();
            }

            // Load both front-half NCGRs (file +0 and +2). The BW2 Pokemon
            // NCER has mapping=4 (2D), so getActiveTileSheet() picks the
            // raster NCGR (+2, rasterLayout=true).
            FSFile ncgr1d = loadNarcFile(fs, NARCRef.PML_G2D_POKE_SPRITE, base + POKE_FRONT_NCGR_1D);
            FSFile ncgr2d = loadNarcFile(fs, NARCRef.PML_G2D_POKE_SPRITE, base + POKE_FRONT_NCGR_2D);
            FSFile ncerFile = loadNarcFile(fs, NARCRef.PML_G2D_POKE_SPRITE, base + POKE_FRONT_NCER);
            FSFile nanrFile = loadNarcFile(fs, NARCRef.PML_G2D_POKE_SPRITE, base + POKE_FRONT_NANR);
            FSFile nmcrFile = loadNarcFile(fs, NARCRef.PML_G2D_POKE_SPRITE, base + POKE_FRONT_NMCR);
            FSFile nmarFile = loadNarcFile(fs, NARCRef.PML_G2D_POKE_SPRITE, base + POKE_FRONT_NMAR);
            FSFile nclrFile = loadNarcFile(fs, NARCRef.PML_G2D_POKE_SPRITE, base + POKE_NCLR_NORMAL);

            if ((ncgr1d == null && ncgr2d == null) || nclrFile == null) {
                return Collections.emptyList();
            }

            Sprite2DResource res = new Sprite2DResource();
            if (ncgr1d != null) mergeImport(res, ncgr1d, ImportType.CGR, "NCGR_1D");
            if (ncgr2d != null) mergeImport(res, ncgr2d, ImportType.CGR, "NCGR_2D");
            for (int i = 0; i < res.tileSheets.size(); i++) {
                res.tileSheets.get(i).name = "TileSheet_" + i;
            }
            mergeImport(res, nclrFile, ImportType.CLR, "NCLR");
            mergeImport(res, ncerFile, ImportType.CER, "NCER");
            mergeImport(res, nanrFile, ImportType.ANR, "NANR");
            mergeImport(res, nmcrFile, ImportType.MCR, "NMCR");
            mergeImport(res, nmarFile, ImportType.MAR, "NMAR");
            res.linkTileSheetsToCells();

            if (res.cells.isEmpty() || res.tileSheets.isEmpty() || res.palettes.isEmpty()) {
                return Collections.emptyList();
            }

            return renderNmarTimeline(res, POKEMON_BATTLE_PREVIEW_SIZE);
        } catch (Exception e) {
            System.err.println("[SpriteImageLoader] loadPokemonBattleSpriteFrames failed for species "
                + speciesIndex + ": " + e);
            return Collections.emptyList();
        }
    }

    /**
     * Computes how long to hold a MultiCell so its longest sub-NANR
     * completes at least one full cycle. Minimum of
     * {@link #MIN_NMAR_PLAY_TICKS} so trivial 1-frame sub-NANRs still
     * stay on screen long enough for the user to see.
     */
    private static int perMcHoldTicks(int mcIdx, Sprite2DResource res) {
        if (mcIdx < 0 || mcIdx >= res.multiCells.size()) return MIN_NMAR_PLAY_TICKS;
        int longest = MIN_NMAR_PLAY_TICKS;
        for (Sprite2DMultiCell.MultiCellEntry e : res.multiCells.get(mcIdx).entries) {
            if (e.animIndex >= 0 && e.animIndex < res.cellAnimations.size()) {
                int d = res.cellAnimations.get(e.animIndex).getTotalDuration();
                if (d > longest) longest = d;
            }
        }
        return longest;
    }

    /**
     * Appends this NMAR's tick-by-tick MultiCell schedule to {@code out}.
     * The NMAR's authored frame durations are respected, but the whole
     * NMAR loops until it has run for at least the longest sub-NANR
     * cycle of any referenced MultiCell (so even 4-tick "placeholder"
     * NMARs visible in BW2 dancer sprites hold long enough for the
     * body-part animations to play).
     */
    private static void appendNmarToSchedule(java.util.List<Integer> out,
            Sprite2DMultiCellAnimation anim, Sprite2DResource res) {
        if (anim.frames.isEmpty()) return;
        int nmarIntrinsic = 0;
        int subNanrMax = MIN_NMAR_PLAY_TICKS;
        for (Sprite2DMultiCellAnimation.MultiCellAnimFrame f : anim.frames) {
            nmarIntrinsic += Math.max(1, f.duration);
            int h = perMcHoldTicks(f.multiCellIndex, res);
            if (h > subNanrMax) subNanrMax = h;
        }
        int target = Math.max(subNanrMax, nmarIntrinsic);
        int played = 0;
        while (played < target && out.size() < MAX_TRAINER_FRAMES) {
            for (Sprite2DMultiCellAnimation.MultiCellAnimFrame f : anim.frames) {
                int dur = Math.max(1, f.duration);
                int mcIdx = (f.multiCellIndex >= 0 && f.multiCellIndex < res.multiCells.size())
                    ? f.multiCellIndex : 0;
                for (int i = 0; i < dur && played < target && out.size() < MAX_TRAINER_FRAMES; i++) {
                    out.add(mcIdx);
                    played++;
                }
            }
        }
    }

    /**
     * Walks the full NMAR timeline across every NMAR in the resource and
     * renders one canvas per NDS tick.
     *
     * <p>Strategy:</p>
     * <ul>
     *   <li>For each NMAR in {@code res.multiCellAnimations} (in order),
     *       play it for at least {@link #MIN_NMAR_PLAY_TICKS} ticks —
     *       long enough for the sub-NANR body-part animations inside the
     *       referenced MultiCells to complete their cycles even when the
     *       NMAR itself is a trivial 4-tick single-frame pose placeholder.</li>
     *   <li>If NMAR has multiple frames, play them in sequence (each
     *       frame picks one MultiCell for its authored duration) and
     *       loop the NMAR as many times as needed to fill the minimum
     *       ticks.</li>
     *   <li>If the resource has no NMARs, cycle through every MultiCell.</li>
     *   <li>Always caps at {@link #MAX_TRAINER_FRAMES} to bound memory.</li>
     * </ul>
     *
     * @return a list of up to {@link #MAX_TRAINER_FRAMES} pre-rendered
     *         frames at {@code previewSize}²; empty if nothing renderable
     *         was found.
     */
    private static List<BufferedImage> renderNmarTimeline(Sprite2DResource res) {
        return renderNmarTimeline(res, TRAINER_PREVIEW_SIZE);
    }

    private static List<BufferedImage> renderNmarTimeline(Sprite2DResource res, int previewSize) {
        if (res.cells.isEmpty() || res.tileSheets.isEmpty()
            || res.palettes.isEmpty() || res.multiCells.isEmpty()) {
            return Collections.emptyList();
        }

        // Build the tick-to-MC lookup by walking every NMAR.
        java.util.List<Integer> schedule = new java.util.ArrayList<>();
        if (!res.multiCellAnimations.isEmpty()) {
            for (Sprite2DMultiCellAnimation anim : res.multiCellAnimations) {
                appendNmarToSchedule(schedule, anim, res);
                if (schedule.size() >= MAX_TRAINER_FRAMES) break;
            }
        }
        // No NMAR data — cycle through MCs directly.
        if (schedule.isEmpty()) {
            for (int mc = 0; mc < res.multiCells.size()
                && schedule.size() < MAX_TRAINER_FRAMES; mc++) {
                int hold = perMcHoldTicks(mc, res);
                for (int t = 0; t < hold && schedule.size() < MAX_TRAINER_FRAMES; t++) {
                    schedule.add(mc);
                }
            }
        }
        if (schedule.isEmpty()) {
            return Collections.emptyList();
        }
        int totalTicks = Math.min(schedule.size(), MAX_TRAINER_FRAMES);
        int[] mcAtTick = new int[totalTicks];
        for (int i = 0; i < totalTicks; i++) {
            mcAtTick[i] = schedule.get(i);
        }

        // First pass: render each tick into raw bbox-cropped images and
        // track the global max extents so we can centre them in a shared
        // bottom-anchored canvas.
        BufferedImage[] raw = new BufferedImage[totalTicks];
        int[] rawMinX = new int[totalTicks];
        int[] rawMinY = new int[totalTicks];
        int globalMinX = Integer.MAX_VALUE, globalMinY = Integer.MAX_VALUE;
        int globalMaxX = Integer.MIN_VALUE, globalMaxY = Integer.MIN_VALUE;
        for (int t = 0; t < totalTicks; t++) {
            int mcIdx = mcAtTick[t];
            if (mcIdx < 0 || mcIdx >= res.multiCells.size()) continue;
            SpriteRenderer.MultiCellLayout layout =
                SpriteRenderer.layoutMultiCell(res.multiCells.get(mcIdx), res, (long) t);
            if (layout == null || layout.image == null) continue;
            raw[t] = layout.image;
            rawMinX[t] = layout.unionMinX;
            rawMinY[t] = layout.unionMinY;
            int x0 = layout.unionMinX, y0 = layout.unionMinY;
            int x1 = x0 + layout.image.getWidth();
            int y1 = y0 + layout.image.getHeight();
            if (x0 < globalMinX) globalMinX = x0;
            if (y0 < globalMinY) globalMinY = y0;
            if (x1 > globalMaxX) globalMaxX = x1;
            if (y1 > globalMaxY) globalMaxY = y1;
        }
        if (globalMinX == Integer.MAX_VALUE) {
            return Collections.emptyList();
        }
        int spriteW = globalMaxX - globalMinX;
        int spriteH = globalMaxY - globalMinY;

        // Scale-to-fit factor so even oversized sprites fit inside the
        // preview. Use uniform scale to preserve proportions.
        double fit = Math.min(1.0,
            Math.min((double) previewSize / spriteW,
                     (double) previewSize / spriteH));

        List<BufferedImage> out = new ArrayList<>();
        for (int t = 0; t < totalTicks; t++) {
            BufferedImage canvas = new BufferedImage(
                previewSize, previewSize, BufferedImage.TYPE_INT_ARGB);
            if (raw[t] != null) {
                // Compute destination rect: this frame's sprite scaled to
                // `fit`, centred horizontally, bottom-anchored so each
                // frame's feet land on the same baseline as the global
                // union's bottom.
                int frameW = (int) Math.round(raw[t].getWidth() * fit);
                int frameH = (int) Math.round(raw[t].getHeight() * fit);
                int scaledSpriteH = (int) Math.round(spriteH * fit);
                int baseline = previewSize - ((previewSize - scaledSpriteH) / 2);
                // Offset within the frame's own bbox from the global bbox
                int dxInGlobal = rawMinX[t] - globalMinX;
                int dyInGlobal = rawMinY[t] - globalMinY;
                int dx = (previewSize - (int) Math.round(spriteW * fit)) / 2
                    + (int) Math.round(dxInGlobal * fit);
                int dy = baseline - scaledSpriteH + (int) Math.round(dyInGlobal * fit);
                Graphics2D g = canvas.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                g.drawImage(raw[t], dx, dy, frameW, frameH, null);
                g.dispose();
            }
            out.add(canvas);
        }
        return out;
    }

    /**
     * Renders the sprite using the best available method.
     *
     * For bitmap-mode tile sheets (rasterLayout), the standard tile-walk
     * renderer produces incorrect results because the tile indices assume
     * a per-OAM sequential VRAM layout while the imported tile data sits
     * in a 2D grid. We bypass the tile system entirely: reconstruct the
     * original bitmap from the tile grid, then blit each OAM's rectangle
     * using its cell position as the bitmap coordinate (OAM.x - cellMinX
     * gives the bitmap X, likewise for Y).
     *
     * For non-bitmap tile sheets, delegates to SpriteRenderer as before.
     */
    private static BufferedImage renderBestAvailable(Sprite2DResource res) {
        if (res.cells.isEmpty() || res.tileSheets.isEmpty() || res.palettes.isEmpty()) {
            return null;
        }
        Sprite2DTileSheet ts = res.getActiveTileSheet();
        Sprite2DPalette pal = res.palettes.get(0);

        BufferedImage raw = null;
        if (!res.multiCellAnimations.isEmpty() && !res.multiCells.isEmpty()) {
            raw = SpriteRenderer.renderMultiCellAnimFrame(
                res.multiCellAnimations.get(0), 0, res);
        }
        if ((raw == null || raw.getWidth() <= 1) && !res.multiCells.isEmpty()) {
            raw = SpriteRenderer.renderMultiCell(res.multiCells.get(0), res);
        }
        if ((raw == null || raw.getWidth() <= 1)) {
            raw = SpriteRenderer.renderCell(cell(res), ts, pal, res.mappingMode);
        }
        return raw;
    }

    private static Sprite2DCell cell(Sprite2DResource res) {
        return res.cells.get(0);
    }

    /**
     * Renders a cell from a bitmap-mode NCGR by reconstructing the
     * original pixel image from the tile grid and using each OAM's cell
     * position to determine which rectangle of the bitmap to blit.
     *
     * The bitmap-to-tile conversion (bitmapRasterToTiles) stores tiles in
     * 2D row-major grid order: tile[N] = grid(N % gridW, N / gridW).
     * This means pixel (x,y) of the original bitmap is at:
     *   tileSheet.getPixel((y/8)*gridW + (x/8), x%8, y%8)
     *
     * Each OAM's bitmap origin is derived from its cell position relative
     * to the cell's bounding-box minimum, which for BW2 trainer sprites
     * maps 1:1 to the sprite's pixel coordinates.
     */
    private static BufferedImage renderCellFromBitmap(
            Sprite2DCell cell, Sprite2DTileSheet ts, Sprite2DPalette pal) {
        if (cell == null || cell.oams.isEmpty()) {
            return null;
        }

        // Compute cell bbox
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
        for (Sprite2DOAM oam : cell.oams) {
            minX = Math.min(minX, oam.x);
            minY = Math.min(minY, oam.y);
            maxX = Math.max(maxX, oam.x + oam.width);
            maxY = Math.max(maxY, oam.y + oam.height);
        }
        int imgW = maxX - minX;
        int imgH = maxY - minY;
        if (imgW <= 0 || imgH <= 0) {
            return null;
        }

        int gridW = Math.max(1, ts.getEffectiveTileWidth());
        int tileCount = ts.getTileCount();
        int colorsPerSubPal = pal.format;

        BufferedImage img = new BufferedImage(imgW, imgH, BufferedImage.TYPE_INT_ARGB);

        // Composite OAMs back-to-front (last OAM drawn first, OAM 0 on top).
        for (int oi = cell.oams.size() - 1; oi >= 0; oi--) {
            Sprite2DOAM oam = cell.oams.get(oi);
            // OAM's top-left in the bitmap = OAM position relative to cell min
            int bmpX = oam.x - minX;
            int bmpY = oam.y - minY;

            for (int py = 0; py < oam.height; py++) {
                for (int px = 0; px < oam.width; px++) {
                    int srcX = bmpX + px;
                    int srcY = bmpY + py;
                    if (oam.flipH) {
                        srcX = bmpX + (oam.width - 1 - px);
                    }
                    if (oam.flipV) {
                        srcY = bmpY + (oam.height - 1 - py);
                    }

                    // Map bitmap pixel to tile in the grid
                    int tileX = srcX / 8;
                    int tileY = srcY / 8;
                    int tileIdx = tileY * gridW + tileX;
                    if (tileIdx < 0 || tileIdx >= tileCount) {
                        continue;
                    }
                    int palIdx = ts.getPixel(tileIdx, srcX % 8, srcY % 8);
                    if (palIdx == 0) {
                        continue; // transparent
                    }

                    int absIdx;
                    if (colorsPerSubPal <= 16) {
                        absIdx = oam.paletteIndex * 16 + palIdx;
                    } else {
                        absIdx = palIdx;
                    }
                    if (absIdx < 0 || absIdx >= pal.colors.length) {
                        continue;
                    }
                    int argb = pal.getColor(absIdx) | 0xFF000000;

                    int dstX = oam.x - minX + px;
                    int dstY = oam.y - minY + py;
                    if (dstX >= 0 && dstX < imgW && dstY >= 0 && dstY < imgH) {
                        img.setRGB(dstX, dstY, argb);
                    }
                }
            }
        }
        return img;
    }

    // --- Helpers ---

    /**
     * Scales an image to fit within a square of the given size,
     * preserving aspect ratio and using nearest-neighbor interpolation
     * to keep pixel art crisp.
     */
    private static BufferedImage scaleToPreview(BufferedImage src, int size) {
        int srcW = src.getWidth();
        int srcH = src.getHeight();
        if (srcW == size && srcH == size) {
            return src;
        }
        double scale = Math.min((double) size / srcW, (double) size / srcH);
        int dstW = Math.max(1, (int) (srcW * scale));
        int dstH = Math.max(1, (int) (srcH * scale));
        BufferedImage dst = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = dst.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        int ox = (size - dstW) / 2;
        int oy = (size - dstH) / 2;
        g.drawImage(src, ox, oy, dstW, dstH, null);
        g.dispose();
        return dst;
    }

    /**
     * Places a sprite in the preview canvas at native pixel resolution
     * with a vertical breathing scale applied. The sprite is bottom-
     * anchored (feet stay fixed at the canvas bottom) and horizontally
     * centred. No upscaling is performed — the sprite keeps its original
     * pixel art resolution within the larger canvas.
     *
     * @param src          Raw sprite image (e.g. 80x80).
     * @param size         Preview canvas size in pixels (e.g. 96).
     * @param breathScaleY Vertical scale factor (1.0 = no change).
     */
    private static BufferedImage scaleToPreviewWithBreath(BufferedImage src, int size, double breathScaleY) {
        int srcW = src.getWidth();
        int srcH = src.getHeight();
        // Scale down proportionally if the sprite exceeds the canvas
        double fitScale = 1.0;
        if (srcW > size || srcH > size) {
            fitScale = Math.min((double) size / srcW, (double) size / srcH);
        }
        int dstW = Math.max(1, (int) Math.round(srcW * fitScale));
        int dstH = Math.max(1, (int) Math.round(srcH * fitScale * breathScaleY));
        BufferedImage dst = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = dst.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        int ox = (size - dstW) / 2;
        int oy = size - dstH; // anchor at bottom
        g.drawImage(src, ox, oy, dstW, dstH, null);
        g.dispose();
        return dst;
    }

    private enum ImportType { CGR, CLR, CER, ANR, MCR, MAR }

    private static void mergeImport(Sprite2DResource res, FSFile file, ImportType type, String label) {
        if (file == null) return;
        try {
            Sprite2DResource imported;
            switch (type) {
                case CGR: imported = Gen5NGCS2DPlugin.CSNNS_CGR.importFile(file, null); break;
                case CLR: imported = Gen5NGCS2DPlugin.CSNNS_CLR.importFile(file, null); break;
                case CER: imported = Gen5NGCS2DPlugin.CSNNS_CER.importFile(file, null); break;
                case ANR: imported = Gen5NGCS2DPlugin.CSNNS_ANR.importFile(file, null); break;
                case MCR: imported = Gen5NGCS2DPlugin.CSNNS_MCR.importFile(file, null); break;
                case MAR: imported = Gen5NGCS2DPlugin.CSNNS_MAR.importFile(file, null); break;
                default: return;
            }
            if (imported != null) {
                res.merge(imported);
            }
        } catch (Exception e) {
            System.err.println("[SpriteImageLoader] Failed to import " + label + " from " + file.getName() + ": " + e);
        }
    }

    /** NCLR file magic: "RLCN" as 4 bytes (0x52, 0x4C, 0x43, 0x4E). */
    private static final byte[] NCLR_MAGIC = { 0x52, 0x4C, 0x43, 0x4E };

    /**
     * Quick check: does this (decompressed) file start with the NCLR magic?
     * Avoids expensive full-parse of non-NCLR files during scanning.
     */
    private static boolean looksLikeNCLR(FSFile file) {
        if (file == null) return false;
        try {
            byte[] data = file.getBytes();
            if (data == null || data.length < 16) return false;
            return data[0] == NCLR_MAGIC[0] && data[1] == NCLR_MAGIC[1]
                && data[2] == NCLR_MAGIC[2] && data[3] == NCLR_MAGIC[3];
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Scans the NARC for any valid NCLR palette file.
     * Checks from the end first (shared palettes are often appended),
     * then from the start. Files are decompressed before the magic-byte
     * check so that LZ-compressed NCLRs are recognised.
     */
    private static Sprite2DPalette scanForPalette(NTRGameFS fs, NARCRef narc, int maxFiles) {
        // Scan from end — shared palette is typically one of the last files
        for (int i = maxFiles - 1; i >= Math.max(0, maxFiles - 10); i--) {
            FSFile f = loadNarcFile(fs, narc, i);
            if (looksLikeNCLR(f)) {
                Sprite2DPalette pal = importPalette(f);
                if (pal != null) {
                    // Found valid palette
                    return pal;
                }
            }
        }
        // Scan from start
        for (int i = 0; i < maxFiles && i < 10; i++) {
            FSFile f = loadNarcFile(fs, narc, i);
            if (looksLikeNCLR(f)) {
                Sprite2DPalette pal = importPalette(f);
                if (pal != null) {
                    // Found valid palette
                    return pal;
                }
            }
        }
        System.err.println("[SpriteImageLoader] No valid palette found in NARC (maxFiles=" + maxFiles + ")");
        return null;
    }

    private static FSFile loadNarcFile(NTRGameFS fs, NARCRef narc, int index) {
        FSFile f = fs.NARCGet(narc, index);
        if (f == null) return null;
        return decompressIfLZ(f);
    }

    private static Sprite2DTileSheet importTileSheet(FSFile file) {
        if (file == null) return null;
        try {
            Sprite2DResource res = Gen5NGCS2DPlugin.CSNNS_CGR.importFile(file, null);
            if (res != null && !res.tileSheets.isEmpty()) {
                return res.tileSheets.get(0);
            }
        } catch (Exception e) {
            // fall through
        }
        return null;
    }

    private static Sprite2DPalette importPalette(FSFile file) {
        if (file == null) return null;
        try {
            Sprite2DResource res = Gen5NGCS2DPlugin.CSNNS_CLR.importFile(file, null);
            if (res != null && !res.palettes.isEmpty()) {
                Sprite2DPalette pal = res.palettes.get(0);
                // Reject palettes with no actual colours — they pass the null
                // check but produce fully-transparent renders.
                if (pal.colors != null && pal.colors.length > 0) {
                    return pal;
                }
            }
        } catch (Exception e) {
            // fall through
        }
        return null;
    }

    /**
     * Detects LZ10/LZ11 compression by checking the first byte of the file.
     * If compressed, decompresses and returns an in-memory FSFile.
     * Otherwise returns the original file unchanged.
     */
    static FSFile decompressIfLZ(FSFile file) {
        if (file == null) return null;
        try {
            byte[] raw = file.getBytes();
            if (raw == null || raw.length < 4) return file;

            int magic = raw[0] & 0xFF;
            if (magic == 0x10 || magic == 0x11) {
                MemoryStream ms = new MemoryStream(raw);
                byte[] decompressed = LZ1X.decompress(ms);
                return new MemoryFile(file.getName() + "_dec", decompressed);
            }
        } catch (IOException e) {
            // Not actually compressed or decompression failed
        }
        return file;
    }
}
