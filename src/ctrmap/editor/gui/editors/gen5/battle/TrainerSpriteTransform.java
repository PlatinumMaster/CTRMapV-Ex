package ctrmap.editor.gui.editors.gen5.battle;

import xstandard.fs.FSFile;

/**
 * Parser for the BW2 trainer sprite transformation file (file 6 in the
 * 8-file-per-class trainer NARC).
 *
 * This file works alongside the NCER to fully define cells. It provides
 * per-cell bounding dimensions and positioning that the renderer needs
 * to correctly assemble body parts from the tiled NCGR and drive
 * VRAM-transfer-based animation.
 *
 * Binary layout:
 *   Offset 0x00: uint32 LE  cellCount
 *   Offset 0x04: uint32 LE  reserved (0)
 *   Offset 0x08: uint32 LE  reserved (0)
 *   Offset 0x0C: CellRecord[0]  (48 bytes)
 *   Offset 0x3C: CellRecord[1]  (48 bytes)
 *   ...
 *
 * CellRecord (48 bytes):
 *   +0x00  int32  spriteX   (fixed 8.8, divide by 0x100)
 *   +0x04  int32  spriteY   (fixed 8.8, divide by 0x100)
 *   +0x08  int32  width     (fixed 16.12, divide by 0x1000)
 *   +0x0C  int32  height    (fixed 16.12, divide by 0x1000)
 *   +0x10  int32  cellX     (fixed 16.12, divide by 0x1000)
 *   +0x14  int32  cellY     (fixed 16.12, divide by 0x1000)
 *   +0x18  24 bytes sub-cell / padding (zeros for most trainers)
 *
 * Reference: FrostFalcon's FrostsGen5Editor RigCellsFile class.
 */
public class TrainerSpriteTransform {

    private static final int HEADER_SIZE = 12;
    private static final int RECORD_SIZE = 48;

    public final int cellCount;
    public final CellTransform[] cells;

    public static class CellTransform {
        /** Sprite anchor X in pixels (fixed 8.8). */
        public final float spriteX;
        /** Sprite anchor Y in pixels (fixed 8.8). */
        public final float spriteY;
        /** Cell width in pixels (fixed 16.12). */
        public final float width;
        /** Cell height in pixels (fixed 16.12). */
        public final float height;
        /** Cell rendering offset X in pixels (fixed 16.12). */
        public final float cellX;
        /** Cell rendering offset Y in pixels (fixed 16.12). */
        public final float cellY;

        CellTransform(float spriteX, float spriteY, float width, float height,
                       float cellX, float cellY) {
            this.spriteX = spriteX;
            this.spriteY = spriteY;
            this.width = width;
            this.height = height;
            this.cellX = cellX;
            this.cellY = cellY;
        }

        /** Cell width rounded to the nearest integer pixel count. */
        public int getWidthPixels() {
            return Math.max(8, Math.round(width));
        }

        /** Cell height rounded to the nearest integer pixel count. */
        public int getHeightPixels() {
            return Math.max(8, Math.round(height));
        }

        /**
         * Number of 8x8 tiles that one animation frame occupies,
         * derived from the cell's pixel dimensions.
         */
        public int getTilesPerFrame() {
            return (getWidthPixels() / 8) * (getHeightPixels() / 8);
        }
    }

    /**
     * Parses a transformation file from the given FSFile.
     * Returns an empty transform (cellCount=0) if the file is null or too small.
     */
    public TrainerSpriteTransform(FSFile file) {
        if (file == null) {
            cellCount = 0;
            cells = new CellTransform[0];
            return;
        }
        byte[] data = file.getBytes();
        if (data == null || data.length < HEADER_SIZE) {
            cellCount = 0;
            cells = new CellTransform[0];
            return;
        }

        int count = readInt32LE(data, 0);
        // Sanity: file must be large enough for the declared count
        int expected = HEADER_SIZE + count * RECORD_SIZE;
        if (count < 0 || data.length < expected) {
            count = Math.max(0, (data.length - HEADER_SIZE) / RECORD_SIZE);
        }

        this.cellCount = count;
        this.cells = new CellTransform[count];

        for (int i = 0; i < count; i++) {
            int base = HEADER_SIZE + i * RECORD_SIZE;
            float spriteX = readInt32LE(data, base)      / 256.0f;
            float spriteY = readInt32LE(data, base + 4)   / 256.0f;
            float w       = readInt32LE(data, base + 8)   / 4096.0f;
            float h       = readInt32LE(data, base + 12)  / 4096.0f;
            float cx      = readInt32LE(data, base + 16)  / 4096.0f;
            float cy      = readInt32LE(data, base + 20)  / 4096.0f;
            cells[i] = new CellTransform(spriteX, spriteY, w, h, cx, cy);
        }
    }

    private static int readInt32LE(byte[] data, int offset) {
        return (data[offset] & 0xFF)
             | ((data[offset + 1] & 0xFF) << 8)
             | ((data[offset + 2] & 0xFF) << 16)
             | ((data[offset + 3] & 0xFF) << 24);
    }
}
