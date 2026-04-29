package ctrmap.editor.gui.editors.gen5.battle;

import ctrmap.creativestudio.ngcs2d.canvas.SpriteRenderer;
import ctrmap.creativestudio.ngcs2d.res.*;
import ctrmap.creativestudio.nitro2dplugin.Gen5NGCS2DPlugin;
import ctrmap.formats.ntr.common.compression.LZ1X;
import ctrmap.formats.ntr.narc.DirectNARC;
import xstandard.fs.FSFile;
import xstandard.fs.accessors.DiskFile;
import xstandard.fs.accessors.MemoryFile;
import xstandard.io.base.impl.access.MemoryStream;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SpriteRenderTest {

    static final String TRAINER_NARC = "D:/Games/Modding/NDS/IREO_EX/data/a/0/7/2";
    static final String OUTPUT_DIR = "D:/Games/Modding/NDS/sprite_test_output";

    public static void main(String[] args) throws Exception {
        new File(OUTPUT_DIR).mkdirs();
        FSFile trainerNarc = new DiskFile(TRAINER_NARC);

        // Analyze class 0 (Hilbert) animation in detail
        int cls = 0;
        int base = cls * 8;

        FSFile ncgrFile = getNarcFile(trainerNarc, base + 1);
        if (ncgrFile == null) ncgrFile = getNarcFile(trainerNarc, base + 0);
        FSFile nclrFile = getNarcFile(trainerNarc, base + 7);
        FSFile ncerFile = getNarcFile(trainerNarc, base + 2);
        FSFile nanrFile = getNarcFile(trainerNarc, base + 3);
        FSFile nmcrFile = getNarcFile(trainerNarc, base + 4);
        FSFile nmarFile = getNarcFile(trainerNarc, base + 5);

        Sprite2DResource res = new Sprite2DResource();
        mergeImport(res, ncgrFile, "CGR");
        mergeImport(res, nclrFile, "CLR");
        mergeImport(res, ncerFile, "CER");
        mergeImport(res, nanrFile, "ANR");
        mergeImport(res, nmcrFile, "MCR");
        mergeImport(res, nmarFile, "MAR");
        res.linkTileSheetsToCells();

        System.out.println("=== Class " + cls + " Animation Detail ===");
        System.out.println("Cells: " + res.cells.size());
        System.out.println("CellAnimations: " + res.cellAnimations.size());
        System.out.println("MultiCells: " + res.multiCells.size());
        System.out.println("MultiCellAnimations: " + res.multiCellAnimations.size());
        System.out.println("MappingMode: " + res.mappingMode);

        for (int a = 0; a < res.cellAnimations.size(); a++) {
            Sprite2DCellAnimation anim = res.cellAnimations.get(a);
            System.out.println("\nCellAnimation[" + a + "] '" + anim.name + "': "
                + anim.frames.size() + " frames, totalDuration=" + anim.getTotalDuration()
                + " ticks, playMode=" + anim.playMode);
            for (int f = 0; f < anim.frames.size(); f++) {
                Sprite2DAnimFrame frame = anim.frames.get(f);
                System.out.println("  Frame " + f + ": cellIdx=" + frame.cellIndex
                    + " dur=" + frame.duration
                    + " tx=" + frame.translateX + " ty=" + frame.translateY
                    + " sx=" + frame.scaleX + " sy=" + frame.scaleY
                    + " rot=" + frame.rotation);
            }
        }

        for (int m = 0; m < res.multiCells.size(); m++) {
            Sprite2DMultiCell mc = res.multiCells.get(m);
            System.out.println("\nMultiCell[" + m + "] '" + mc.name + "': " + mc.entries.size() + " entries");
            for (int e = 0; e < mc.entries.size(); e++) {
                Sprite2DMultiCell.MultiCellEntry entry = mc.entries.get(e);
                System.out.println("  Entry " + e + ": animIdx=" + entry.animIndex
                    + " x=" + entry.x + " y=" + entry.y);
            }
        }

        // Check raw NANR/NCER binary
        System.out.println("\n=== Raw NANR binary check ===");
        byte[] nanrRaw = nanrFile.getBytes();
        System.out.println("NANR file size: " + nanrRaw.length + " bytes");
        // Dump first 64 bytes as hex
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(80, nanrRaw.length); i++) {
            sb.append(String.format("%02X ", nanrRaw[i] & 0xFF));
            if ((i + 1) % 16 == 0) sb.append("\n");
        }
        System.out.println(sb);

        System.out.println("=== Raw NCER binary check ===");
        byte[] ncerRaw = ncerFile.getBytes();
        System.out.println("NCER file size: " + ncerRaw.length + " bytes");
        sb = new StringBuilder();
        for (int i = 0; i < Math.min(80, ncerRaw.length); i++) {
            sb.append(String.format("%02X ", ncerRaw[i] & 0xFF));
            if ((i + 1) % 16 == 0) sb.append("\n");
        }
        System.out.println(sb);

        // Check all 8 files for this trainer - what are their types and sizes?
        System.out.println("=== All 8 files for class 0 ===");
        String[] fileNames = {"NCBR", "NCGR", "NCER", "NANR", "NMCR", "NMAR", "XFORM", "NCLR"};
        for (int i = 0; i < 8; i++) {
            FSFile f = getNarcFile(trainerNarc, base + i);
            if (f == null) {
                System.out.println("  File " + i + " (" + fileNames[i] + "): null/empty");
            } else {
                byte[] d = f.getBytes();
                String magic = d.length >= 4 ? new String(new byte[]{d[0], d[1], d[2], d[3]}) : "?";
                System.out.println("  File " + i + " (" + fileNames[i] + "): " + d.length + " bytes, magic=" + magic);
            }
        }

        // Dump XFORM file completely
        FSFile xformFile = getNarcFile(trainerNarc, base + 6);
        if (xformFile != null) {
            byte[] xData = xformFile.getBytes();
            System.out.println("\n=== XFORM file (file 6) - " + xData.length + " bytes ===");
            sb = new StringBuilder();
            for (int i = 0; i < xData.length; i++) {
                sb.append(String.format("%02X ", xData[i] & 0xFF));
                if ((i + 1) % 16 == 0) sb.append("\n");
            }
            System.out.println(sb);

            // Try interpreting as a list of short pairs (translate X,Y per frame)
            System.out.println("\nAs uint16 pairs:");
            for (int i = 0; i + 3 < xData.length; i += 4) {
                int v0 = (xData[i] & 0xFF) | ((xData[i+1] & 0xFF) << 8);
                int v1 = (xData[i+2] & 0xFF) | ((xData[i+3] & 0xFF) << 8);
                short s0 = (short) v0;
                short s1 = (short) v1;
                System.out.println("  [" + (i/4) + "] " + v0 + "," + v1 + " (signed: " + s0 + "," + s1 + ")");
            }
        }

        // Also check NCBR vs NCGR sizes - NCBR is smaller, might be animation frames
        FSFile ncbrFile = getNarcFile(trainerNarc, base + 0);
        if (ncbrFile != null) {
            byte[] ncbrData = ncbrFile.getBytes();
            System.out.println("\n=== NCBR (bitmap) file check ===");
            System.out.println("Size: " + ncbrData.length + " bytes");
            sb = new StringBuilder();
            for (int i = 0; i < Math.min(48, ncbrData.length); i++) {
                sb.append(String.format("%02X ", ncbrData[i] & 0xFF));
                if ((i + 1) % 16 == 0) sb.append("\n");
            }
            System.out.println(sb);
            // Try import as NCGR
            Sprite2DResource ncbrRes = Gen5NGCS2DPlugin.CSNNS_CGR.importFile(ncbrFile, null);
            if (ncbrRes != null && !ncbrRes.tileSheets.isEmpty()) {
                Sprite2DTileSheet ts = ncbrRes.tileSheets.get(0);
                System.out.println("NCBR tiles: count=" + ts.getTileCount()
                    + " w=" + ts.getEffectiveTileWidth() + " h=" + ts.tileHeight
                    + " format=" + ts.format);
            }
        }

        System.out.println("\n=== NCGR (tiled) file check ===");
        Sprite2DResource ncgrRes = Gen5NGCS2DPlugin.CSNNS_CGR.importFile(ncgrFile, null);
        if (ncgrRes != null && !ncgrRes.tileSheets.isEmpty()) {
            Sprite2DTileSheet ts = ncgrRes.tileSheets.get(0);
            System.out.println("NCGR tiles: count=" + ts.getTileCount()
                + " w=" + ts.getEffectiveTileWidth() + " h=" + ts.tileHeight
                + " format=" + ts.format);
        }

        // Import NANR directly and check
        System.out.println("\n=== Direct NANR import ===");
        ctrmap.formats.ntr.nitro2d.nanr.NANR nanrDirect = new ctrmap.formats.ntr.nitro2d.nanr.NANR(nanrFile);
        System.out.println("Animation count: " + nanrDirect.getAnimationCount());
        for (int a = 0; a < nanrDirect.getAnimationCount(); a++) {
            ctrmap.formats.ntr.nitro2d.nanr.NANRAnimation na = nanrDirect.getAnimation(a);
            System.out.println("  Anim " + a + ": type=" + na.animationType
                + " cellType=" + na.cellType + " loopMode=" + na.loopMode
                + " frames=" + na.frames.size());
            for (int f = 0; f < na.frames.size(); f++) {
                ctrmap.formats.ntr.nitro2d.nanr.NANRFrame nf = na.frames.get(f);
                System.out.println("    Frame " + f + ": cell=" + nf.cellIndex + " dur=" + nf.duration
                    + " tx=" + nf.translateX + " ty=" + nf.translateY
                    + " rot=" + nf.rotation + " sw=" + nf.scaleW + " sh=" + nf.scaleH);
            }
        }

        // Import NCER directly
        System.out.println("\n=== Direct NCER import ===");
        ctrmap.formats.ntr.nitro2d.ncer.NCER ncerDirect = new ctrmap.formats.ntr.nitro2d.ncer.NCER(ncerFile);
        System.out.println("Cell count: " + ncerDirect.getCellCount());
        for (int c = 0; c < ncerDirect.getCellCount(); c++) {
            ctrmap.formats.ntr.nitro2d.ncer.NCERCell ncerCell = ncerDirect.getCell(c);
            System.out.println("  Cell " + c + ": " + ncerCell.oams.size() + " OAMs");
        }

        // Print VRAM transfer details
        System.out.println("\n=== VRAM Transfer Details ===");
        Sprite2DCell cell0 = res.cells.get(0);
        System.out.println("Cell 0 hasVramTransfer=" + cell0.hasVramTransfer
            + " srcAddr=" + cell0.vramTransferSrcAddr
            + " size=" + cell0.vramTransferSize);
        if (!res.tileSheets.isEmpty()) {
            Sprite2DTileSheet ts = res.tileSheets.get(0);
            int bytesPerTile = ts.format == 3 ? 32 : 64; // 4bpp=32, 8bpp=64
            System.out.println("TileSheet: totalTiles=" + ts.getTileCount()
                + " totalBytes=" + (ts.getTileCount() * bytesPerTile)
                + " bytesPerTile=" + bytesPerTile);

            // If VRAM transfer is used, compute frame count
            // Compute frame size from OAMs: max tile used + tiles in that OAM
            int maxTileEnd = 0;
            for (Sprite2DOAM oam : cell0.oams) {
                int oamTiles = (oam.width / 8) * (oam.height / 8);
                int end = oam.tileIndex + oamTiles;
                if (end > maxTileEnd) maxTileEnd = end;
            }
            int frameSizeBytes = maxTileEnd * bytesPerTile;
            int totalBytes = ts.getTileCount() * bytesPerTile;
            int frameCount = totalBytes / frameSizeBytes;
            System.out.println("Max tile end: " + maxTileEnd + " -> frameSizeBytes=" + frameSizeBytes
                + " totalBytes=" + totalBytes + " -> " + frameCount + " frames");

            // Enable VRAM transfer and render each frame
            System.out.println("\n=== VRAM Transfer Animation Frames ===");
            boolean origHasVram = cell0.hasVramTransfer;
            int origSrcAddr = cell0.vramTransferSrcAddr;
            int origSize = cell0.vramTransferSize;

            cell0.hasVramTransfer = true;
            cell0.vramTransferSize = frameSizeBytes;

            for (int frame = 0; frame < frameCount; frame++) {
                cell0.vramTransferSrcAddr = frame * frameSizeBytes;
                BufferedImage frameImg = SpriteRenderer.renderMultiCell(
                    res.multiCells.get(0), res);
                if (frameImg != null && frameImg.getWidth() > 1) {
                    String path = OUTPUT_DIR + "/trainer_0_vram_frame_" + frame + ".png";
                    ImageIO.write(frameImg, "PNG", new File(path));
                    System.out.println("  Frame " + frame + " (srcAddr=" + cell0.vramTransferSrcAddr + "): "
                        + frameImg.getWidth() + "x" + frameImg.getHeight() + " -> " + path);
                }
            }

            // Delta-composite: overlay each frame onto frame 0 base
            System.out.println("\n=== Delta-composited animation frames ===");
            cell0.vramTransferSrcAddr = 0;
            BufferedImage baseFrame = SpriteRenderer.renderMultiCell(res.multiCells.get(0), res);
            String basePath = OUTPUT_DIR + "/trainer_0_delta_frame_0.png";
            ImageIO.write(baseFrame, "PNG", new File(basePath));
            System.out.println("  Frame 0 (base): " + baseFrame.getWidth() + "x" + baseFrame.getHeight());

            for (int frame = 1; frame < frameCount; frame++) {
                cell0.vramTransferSrcAddr = frame * frameSizeBytes;
                BufferedImage deltaFrame = SpriteRenderer.renderMultiCell(res.multiCells.get(0), res);

                // Composite: start with base, overlay non-transparent pixels from delta
                BufferedImage composited = new BufferedImage(
                    baseFrame.getWidth(), baseFrame.getHeight(), BufferedImage.TYPE_INT_ARGB);
                // Copy base
                for (int y = 0; y < baseFrame.getHeight(); y++) {
                    for (int x = 0; x < baseFrame.getWidth(); x++) {
                        composited.setRGB(x, y, baseFrame.getRGB(x, y));
                    }
                }
                // Tile-level replacement: for each 8x8 tile region,
                // if the delta has ANY non-transparent pixel, replace entire tile
                if (deltaFrame != null) {
                    int minW = Math.min(composited.getWidth(), deltaFrame.getWidth());
                    int minH = Math.min(composited.getHeight(), deltaFrame.getHeight());
                    for (int ty = 0; ty < minH; ty += 8) {
                        for (int tx = 0; tx < minW; tx += 8) {
                            // Check if this 8x8 tile has any content in delta
                            boolean hasContent = false;
                            for (int py = 0; py < 8 && ty + py < minH; py++) {
                                for (int px = 0; px < 8 && tx + px < minW; px++) {
                                    if ((deltaFrame.getRGB(tx + px, ty + py) >>> 24) != 0) {
                                        hasContent = true;
                                        break;
                                    }
                                }
                                if (hasContent) break;
                            }
                            if (hasContent) {
                                // Replace entire tile with delta
                                for (int py = 0; py < 8 && ty + py < minH; py++) {
                                    for (int px = 0; px < 8 && tx + px < minW; px++) {
                                        composited.setRGB(tx + px, ty + py,
                                            deltaFrame.getRGB(tx + px, ty + py));
                                    }
                                }
                            }
                        }
                    }
                }
                String path = OUTPUT_DIR + "/trainer_0_delta_frame_" + frame + ".png";
                ImageIO.write(composited, "PNG", new File(path));
                System.out.println("  Frame " + frame + " (delta): " + composited.getWidth() + "x" + composited.getHeight());
            }

            // Restore
            cell0.hasVramTransfer = origHasVram;
            cell0.vramTransferSrcAddr = origSrcAddr;
            cell0.vramTransferSize = origSize;
        }

        // Print OAM details
        System.out.println("\n=== Cell 0 OAM details ===");
        for (int o = 0; o < res.cells.get(0).oams.size(); o++) {
            Sprite2DOAM oam = res.cells.get(0).oams.get(o);
            System.out.println("  OAM " + o + ": " + oam.width + "x" + oam.height
                + " at (" + oam.x + "," + oam.y + ")"
                + " tileIdx=" + oam.tileIndex + " palIdx=" + oam.paletteIndex
                + " flipH=" + oam.flipH + " flipV=" + oam.flipV
                + " pri=" + oam.priority);
        }

        // Try rendering with NCBR (bitmap) instead of NCGR (tiled)
        System.out.println("\n=== Render with NCBR (bitmap) ===");
        Sprite2DResource resBitmap = new Sprite2DResource();
        mergeImport(resBitmap, ncbrFile, "CGR");
        mergeImport(resBitmap, nclrFile, "CLR");
        mergeImport(resBitmap, ncerFile, "CER");
        mergeImport(resBitmap, nanrFile, "ANR");
        mergeImport(resBitmap, nmcrFile, "MCR");
        mergeImport(resBitmap, nmarFile, "MAR");
        resBitmap.linkTileSheetsToCells();
        if (!resBitmap.tileSheets.isEmpty()) {
            Sprite2DTileSheet bts = resBitmap.tileSheets.get(0);
            System.out.println("  Bitmap tiles: count=" + bts.getTileCount()
                + " w=" + bts.getEffectiveTileWidth() + " h=" + bts.tileHeight);
        }
        if (!resBitmap.multiCells.isEmpty()) {
            BufferedImage bmpImg = SpriteRenderer.renderMultiCell(resBitmap.multiCells.get(0), resBitmap);
            if (bmpImg != null) {
                String path = OUTPUT_DIR + "/trainer_0_bitmap.png";
                ImageIO.write(bmpImg, "PNG", new File(path));
                System.out.println("  Bitmap render: " + bmpImg.getWidth() + "x" + bmpImg.getHeight() + " -> " + path);
            }
        }

        // Render animation frames by shifting tile indices through NCGR data
        // NCGR has 512 tiles, one frame uses ~100 tiles (OAM max idx=96+4=100)
        // So there should be ~5 animation frames stored sequentially
        System.out.println("\n=== Render animation frames by tile offset ===");
        int tilesPerFrame = 100; // from OAM analysis
        int totalTiles = 512;    // from NCGR
        int numFrames = totalTiles / tilesPerFrame;
        Sprite2DCell origCell = res.cells.get(0);
        for (int frame = 0; frame < numFrames; frame++) {
            int tileOffset = frame * tilesPerFrame;
            Sprite2DCell shiftedCell = new Sprite2DCell("frame_" + frame);
            for (Sprite2DOAM origOam : origCell.oams) {
                Sprite2DOAM oam = new Sprite2DOAM();
                oam.x = origOam.x;
                oam.y = origOam.y;
                oam.width = origOam.width;
                oam.height = origOam.height;
                oam.tileIndex = origOam.tileIndex + tileOffset;
                oam.paletteIndex = origOam.paletteIndex;
                oam.flipH = origOam.flipH;
                oam.flipV = origOam.flipV;
                oam.priority = origOam.priority;
                shiftedCell.addOAM(oam);
            }
            BufferedImage frameImg = SpriteRenderer.renderCell(
                shiftedCell, res.tileSheets.get(0), res.palettes.get(0), res.mappingMode);
            if (frameImg != null) {
                String path = OUTPUT_DIR + "/trainer_0_animframe_" + frame + ".png";
                ImageIO.write(frameImg, "PNG", new File(path));
                System.out.println("  Frame " + frame + " (tileOff=" + tileOffset + "): "
                    + frameImg.getWidth() + "x" + frameImg.getHeight() + " -> " + path);
            }
        }

        // Render all unique frames from NANR tick
        if (!res.multiCells.isEmpty() && !res.cellAnimations.isEmpty()) {
            Sprite2DCellAnimation anim = res.cellAnimations.get(0);
            int totalDuration = anim.getTotalDuration();
            System.out.println("\n=== Rendering " + totalDuration + " ticks ===");

            List<BufferedImage> frames = new ArrayList<>();
            BufferedImage prev = null;
            int tick = 0;
            while (tick < totalDuration) {
                BufferedImage img = SpriteRenderer.layoutMultiCell(
                    res.multiCells.get(0), res, (long) tick).image;
                // Check if this frame is visually different from previous
                boolean different = prev == null || !imagesEqual(prev, img);
                if (different) {
                    frames.add(img);
                    String path = OUTPUT_DIR + "/anim_frame_" + frames.size() + "_tick" + tick + ".png";
                    ImageIO.write(img, "PNG", new File(path));
                    System.out.println("  Tick " + tick + ": " + img.getWidth() + "x" + img.getHeight() + " (new frame " + frames.size() + ") -> " + path);
                }
                prev = img;
                tick++;
            }
            System.out.println("Total unique frames: " + frames.size() + " / " + totalDuration + " ticks");
        }
    }

    static boolean imagesEqual(BufferedImage a, BufferedImage b) {
        if (a.getWidth() != b.getWidth() || a.getHeight() != b.getHeight()) return false;
        for (int y = 0; y < a.getHeight(); y++) {
            for (int x = 0; x < a.getWidth(); x++) {
                if (a.getRGB(x, y) != b.getRGB(x, y)) return false;
            }
        }
        return true;
    }

    static FSFile getNarcFile(FSFile narcFile, int index) {
        try {
            byte[] data = DirectNARC.getData(narcFile, String.valueOf(index));
            if (data == null || data.length == 0) return null;
            FSFile f = new MemoryFile("narc_" + index, data);
            return decompressIfLZ(f);
        } catch (Exception e) { return null; }
    }

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
        } catch (IOException e) { }
        return file;
    }

    static void mergeImport(Sprite2DResource res, FSFile file, String type) {
        if (file == null) return;
        try {
            Sprite2DResource imported = null;
            switch (type) {
                case "CGR": imported = Gen5NGCS2DPlugin.CSNNS_CGR.importFile(file, null); break;
                case "CLR": imported = Gen5NGCS2DPlugin.CSNNS_CLR.importFile(file, null); break;
                case "CER": imported = Gen5NGCS2DPlugin.CSNNS_CER.importFile(file, null); break;
                case "ANR": imported = Gen5NGCS2DPlugin.CSNNS_ANR.importFile(file, null); break;
                case "MCR": imported = Gen5NGCS2DPlugin.CSNNS_MCR.importFile(file, null); break;
                case "MAR": imported = Gen5NGCS2DPlugin.CSNNS_MAR.importFile(file, null); break;
            }
            if (imported != null) res.merge(imported);
        } catch (Exception e) {
            System.out.println("  Import " + type + " failed: " + e);
        }
    }
}
