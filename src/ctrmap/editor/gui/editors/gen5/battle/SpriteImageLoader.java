package ctrmap.editor.gui.editors.gen5.battle;

import ctrmap.creativestudio.ngcs2d.canvas.SpriteRenderer;
import ctrmap.creativestudio.ngcs2d.res.Sprite2DCell;
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

    /** Trainer preview output size in pixels. */
    private static final int TRAINER_PREVIEW_SIZE = 96;

    /** First NCGR in the icon NARC (files 0-6 are palette + shared NANR/NCER). */
    private static final int ICON_NCGR_BASE = 7;
    /** Stride between consecutive species NCGRs (NCGR, empty, NCGR, empty...). */
    private static final int ICON_NCGR_STRIDE = 2;

    /** Cached fallback palette for Pokemon icons (lazy-initialized). */
    private static Sprite2DPalette cachedIconFallbackPalette = null;

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

    /** Number of frames in one full breathing cycle. */
    private static final int BREATH_FRAME_COUNT = 32;

    /** Peak vertical scale offset for the breathing animation (±2%). */
    private static final double BREATH_AMPLITUDE = 0.02;

    /**
     * Loads all animation frames of a trainer battle sprite.
     *
     * BW2 trainer sprites are rendered from a single NCER cell composited
     * via NMCR/NMAR, with body-part tiles sourced from a bitmap-mode NCGR
     * (file 1). The NANR/NMAR each contain exactly one frame — the
     * "breathing" animation visible on Bulbapedia is driven entirely by
     * the game engine, which applies a subtle sinusoidal vertical scale
     * to the rendered sprite, anchored at the feet.
     *
     * This method renders the static base sprite and generates
     * {@value #BREATH_FRAME_COUNT} frames of the breathing cycle via
     * programmatic vertical scaling. The caller should play these
     * sequentially in a loop.
     *
     * Returns a list of 96x96 images, or an empty list if the sprite
     * cannot be loaded (e.g. BW1, missing files).
     */
    public static List<BufferedImage> loadTrainerSpriteFrames(NTRGameFS fs, GameInfo game, int trainerClassIndex) {
        try {
            if (NARCRef.TRAINER_G2D_BTL_F.getARCID(game) < 0) {
                return Collections.emptyList();
            }

            int maxFiles = fs.NARCGetDataMax(NARCRef.TRAINER_G2D_BTL_F);
            int base = trainerClassIndex * TRAINER_FILES_PER_CLASS;

            if (base + TRAINER_OFF_NCLR >= maxFiles) {
                return Collections.emptyList();
            }

            // Load all constituent files. Use the tiled NCGR (file 1) which
            // has body parts split out, not the bitmap NCBR (file 0).
            FSFile ncgrFile = loadNarcFile(fs, NARCRef.TRAINER_G2D_BTL_F, base + TRAINER_OFF_NCGR);
            if (ncgrFile == null) {
                ncgrFile = loadNarcFile(fs, NARCRef.TRAINER_G2D_BTL_F, base + TRAINER_OFF_NCBR);
            }
            FSFile nclrFile = loadNarcFile(fs, NARCRef.TRAINER_G2D_BTL_F, base + TRAINER_OFF_NCLR);
            FSFile ncerFile = loadNarcFile(fs, NARCRef.TRAINER_G2D_BTL_F, base + TRAINER_OFF_NCER);
            FSFile nanrFile = loadNarcFile(fs, NARCRef.TRAINER_G2D_BTL_F, base + TRAINER_OFF_NANR);
            FSFile nmcrFile = loadNarcFile(fs, NARCRef.TRAINER_G2D_BTL_F, base + TRAINER_OFF_NMCR);
            FSFile nmarFile = loadNarcFile(fs, NARCRef.TRAINER_G2D_BTL_F, base + TRAINER_OFF_NMAR);

            if (ncgrFile == null || nclrFile == null) {
                return Collections.emptyList();
            }

            // Build unified Sprite2DResource from all Nitro 2D files.
            Sprite2DResource res = new Sprite2DResource();
            mergeImport(res, ncgrFile, ImportType.CGR, "NCGR");
            mergeImport(res, nclrFile, ImportType.CLR, "NCLR");
            mergeImport(res, ncerFile, ImportType.CER, "NCER");
            mergeImport(res, nanrFile, ImportType.ANR, "NANR");
            mergeImport(res, nmcrFile, ImportType.MCR, "NMCR");
            mergeImport(res, nmarFile, ImportType.MAR, "NMAR");
            res.linkTileSheetsToCells();

            if (res.cells.isEmpty() || res.tileSheets.isEmpty() || res.palettes.isEmpty()) {
                return Collections.emptyList();
            }

            // Render the static base sprite.
            BufferedImage raw = renderBestAvailable(res);
            if (raw == null || raw.getWidth() <= 1) {
                return Collections.emptyList();
            }

            // Generate breathing animation frames by applying a sinusoidal
            // vertical scale, anchored at the bottom of the sprite.
            List<BufferedImage> frames = new ArrayList<>();
            for (int k = 0; k < BREATH_FRAME_COUNT; k++) {
                double phase = 2.0 * Math.PI * k / BREATH_FRAME_COUNT;
                double scaleY = 1.0 + BREATH_AMPLITUDE * Math.sin(phase);
                frames.add(scaleToPreviewWithBreath(raw, TRAINER_PREVIEW_SIZE, scaleY));
            }

            return frames;
        } catch (Exception e) {
            System.err.println("[SpriteImageLoader] loadTrainerSpriteFrames failed for class " + trainerClassIndex + ": " + e);
            e.printStackTrace();
            return Collections.emptyList();
        }
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
        Sprite2DTileSheet ts = res.tileSheets.get(0);
        Sprite2DPalette pal = res.palettes.get(0);

        // Non-bitmap: use standard SpriteRenderer pipeline.
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
