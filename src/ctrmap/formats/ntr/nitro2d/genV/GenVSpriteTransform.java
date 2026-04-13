package ctrmap.formats.ntr.nitro2d.genV;

import ctrmap.formats.ntr.common.NTRDataIOStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import xstandard.fs.FSFile;

/**
 * Gen V (Black/White, B2W2) per-cell sprite transform/cropping data.
 *
 * Provides additional per-cell cropping information for Gen V battle sprites,
 * defining how each cell's graphics are cropped from the NCGR tile sheet.
 * This is a simple binary file and is NOT a standard NITRO resource.
 *
 * Each entry is 8 bytes containing source coordinates and crop dimensions.
 */
public class GenVSpriteTransform {

	/** The list of transform entries. */
	public List<TransformEntry> entries;

	public GenVSpriteTransform() {
		entries = new ArrayList<>();
	}

	/**
	 * Reads transform data from the given file.
	 *
	 * @param file The file to read.
	 */
	public GenVSpriteTransform(FSFile file) {
		this();
		try (NTRDataIOStream io = new NTRDataIOStream(file.getIO())) {
			int entryCount = io.getLength() / 8;
			for (int i = 0; i < entryCount; i++) {
				TransformEntry entry = new TransformEntry();
				entry.srcX = io.readShort() & 0xFFFF;
				entry.srcY = io.readShort() & 0xFFFF;
				entry.width = io.readShort() & 0xFFFF;
				entry.height = io.readShort() & 0xFFFF;
				entries.add(entry);
			}
		} catch (IOException ex) {
			Logger.getLogger(GenVSpriteTransform.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * Writes this transform data to the given file.
	 *
	 * @param file The file to write to.
	 */
	public void write(FSFile file) {
		try (NTRDataIOStream io = new NTRDataIOStream(file.getIO())) {
			for (TransformEntry entry : entries) {
				io.writeShort(entry.srcX);
				io.writeShort(entry.srcY);
				io.writeShort(entry.width);
				io.writeShort(entry.height);
			}
		} catch (IOException ex) {
			Logger.getLogger(GenVSpriteTransform.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * Gets the number of transform entries.
	 *
	 * @return The entry count.
	 */
	public int getEntryCount() {
		return entries.size();
	}

	/**
	 * Gets the transform entry at the given index.
	 *
	 * @param index The entry index.
	 * @return The transform entry at the given index.
	 */
	public TransformEntry getEntry(int index) {
		return entries.get(index);
	}

	/**
	 * A single transform/cropping entry defining a rectangular region within
	 * the NCGR tile sheet.
	 */
	public static class TransformEntry {

		/** Source X coordinate in the tile sheet (pixel coordinates). */
		public int srcX;

		/** Source Y coordinate in the tile sheet (pixel coordinates). */
		public int srcY;

		/** Crop width in pixels. */
		public int width;

		/** Crop height in pixels. */
		public int height;

		public TransformEntry() {
		}
	}
}
