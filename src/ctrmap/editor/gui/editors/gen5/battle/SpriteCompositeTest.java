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
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Test: renders trainer sprites using the new bitmap-direct approach
 * and compares with the old tile-walk approach.
 */
public class SpriteCompositeTest {

    static final String TRAINER_NARC = "D:/Games/Modding/NDS/IREO_EX/data/a/0/7/2";
    static final String OUTPUT_DIR = "D:/Games/Modding/NDS/sprite_test_output";
    static final int PREVIEW_SIZE = 96;
    static final int[] TEST_CLASSES = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};

    public static void main(String[] args) throws Exception {
        new File(OUTPUT_DIR).mkdirs();
        FSFile trainerNarc = new DiskFile(TRAINER_NARC);

        for (int cls : TEST_CLASSES) {
            System.out.println("\n========== CLASS " + cls + " ==========");
            try {
                renderClass(trainerNarc, cls);
            } catch (Exception e) {
                System.out.println("ERROR: " + e);
                e.printStackTrace();
            }
        }
        System.out.println("\nDone. Check " + OUTPUT_DIR);
    }

    static void renderClass(FSFile trainerNarc, int cls) throws Exception {
        int base = cls * 8;
        FSFile ncgrFile = getNarcFile(trainerNarc, base + 1);
        FSFile nclrFile = getNarcFile(trainerNarc, base + 7);
        FSFile ncerFile = getNarcFile(trainerNarc, base + 2);
        FSFile nanrFile = getNarcFile(trainerNarc, base + 3);
        FSFile nmcrFile = getNarcFile(trainerNarc, base + 4);
        FSFile nmarFile = getNarcFile(trainerNarc, base + 5);

        if (ncgrFile == null || nclrFile == null) return;

        Sprite2DResource res = new Sprite2DResource();
        mergeImport(res, ncgrFile, "CGR");
        mergeImport(res, nclrFile, "CLR");
        mergeImport(res, ncerFile, "CER");
        mergeImport(res, nanrFile, "ANR");
        mergeImport(res, nmcrFile, "MCR");
        mergeImport(res, nmarFile, "MAR");
        res.linkTileSheetsToCells();

        if (res.cells.isEmpty() || res.tileSheets.isEmpty() || res.palettes.isEmpty()) return;

        Sprite2DTileSheet ts = res.tileSheets.get(0);
        Sprite2DPalette pal = res.palettes.get(0);
        Sprite2DCell cell = res.cells.get(0);

        System.out.println("  TileSheet: tiles=" + ts.getTileCount() + " gridW=" + ts.getEffectiveTileWidth()
            + " raster=" + ts.rasterLayout + " fmt=" + ts.format);

        // === Bitmap-direct render (NEW approach) ===
        BufferedImage bmpDirect = renderCellFromBitmap(cell, ts, pal);
        if (bmpDirect != null) {
            ImageIO.write(bmpDirect, "PNG",
                new File(OUTPUT_DIR + "/class" + cls + "_bitmap_direct.png"));
            System.out.println("  Bitmap-direct: " + bmpDirect.getWidth() + "x" + bmpDirect.getHeight());
        }

        // === Old NCGR-stride render for comparison ===
        BufferedImage ncgrStride = SpriteRenderer.renderCell(cell, ts, pal, res.mappingMode);
        if (ncgrStride != null && ncgrStride.getWidth() > 1) {
            ImageIO.write(ncgrStride, "PNG",
                new File(OUTPUT_DIR + "/class" + cls + "_ncgr_stride.png"));
            System.out.println("  NCGR-stride:   " + ncgrStride.getWidth() + "x" + ncgrStride.getHeight());
        }

        // === 96x96 preview from bitmap-direct ===
        if (bmpDirect != null) {
            ImageIO.write(placeInCanvas(bmpDirect, PREVIEW_SIZE, 1.0), "PNG",
                new File(OUTPUT_DIR + "/class" + cls + "_preview96.png"));
        }
    }

    /** Bitmap-direct renderer: uses OAM cell positions as bitmap coordinates. */
    static BufferedImage renderCellFromBitmap(
            Sprite2DCell cell, Sprite2DTileSheet ts, Sprite2DPalette pal) {
        if (cell == null || cell.oams.isEmpty()) return null;

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
        if (imgW <= 0 || imgH <= 0) return null;

        int gridW = Math.max(1, ts.getEffectiveTileWidth());
        int tileCount = ts.getTileCount();
        int colorsPerSubPal = pal.format;

        BufferedImage img = new BufferedImage(imgW, imgH, BufferedImage.TYPE_INT_ARGB);

        for (int oi = cell.oams.size() - 1; oi >= 0; oi--) {
            Sprite2DOAM oam = cell.oams.get(oi);
            int bmpX0 = oam.x - minX;
            int bmpY0 = oam.y - minY;

            for (int py = 0; py < oam.height; py++) {
                for (int px = 0; px < oam.width; px++) {
                    int srcX = bmpX0 + (oam.flipH ? (oam.width - 1 - px) : px);
                    int srcY = bmpY0 + (oam.flipV ? (oam.height - 1 - py) : py);

                    int tileIdx = (srcY / 8) * gridW + (srcX / 8);
                    if (tileIdx < 0 || tileIdx >= tileCount) continue;
                    int palIdx = ts.getPixel(tileIdx, srcX % 8, srcY % 8);
                    if (palIdx == 0) continue;

                    int absIdx = (colorsPerSubPal <= 16) ? oam.paletteIndex * 16 + palIdx : palIdx;
                    if (absIdx < 0 || absIdx >= pal.colors.length) continue;
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

    static BufferedImage placeInCanvas(BufferedImage src, int size, double breathScaleY) {
        int srcW = src.getWidth();
        int srcH = src.getHeight();
        double fitScale = 1.0;
        if (srcW > size || srcH > size) {
            fitScale = Math.min((double) size / srcW, (double) size / srcH);
        }
        int dstW = Math.max(1, (int) Math.round(srcW * fitScale));
        int dstH = Math.max(1, (int) Math.round(srcH * fitScale * breathScaleY));
        BufferedImage dst = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = dst.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(src, (size - dstW) / 2, size - dstH, dstW, dstH, null);
        g.dispose();
        return dst;
    }

    static FSFile getNarcFile(FSFile narcFile, int index) {
        try {
            byte[] data = DirectNARC.getData(narcFile, String.valueOf(index));
            if (data == null || data.length == 0) return null;
            return decompressIfLZ(new MemoryFile("narc_" + index, data));
        } catch (Exception e) { return null; }
    }

    static FSFile decompressIfLZ(FSFile file) {
        if (file == null) return null;
        try {
            byte[] raw = file.getBytes();
            if (raw == null || raw.length < 4) return file;
            int magic = raw[0] & 0xFF;
            if (magic == 0x10 || magic == 0x11) {
                return new MemoryFile(file.getName() + "_dec", LZ1X.decompress(new MemoryStream(raw)));
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
        } catch (Exception e) { System.out.println("  Import " + type + " failed: " + e); }
    }
}
