package ctrmap.formats.ntr.nitro2d.ncer;

import java.util.ArrayList;
import java.util.List;

/**
 * A cell composed of one or more OAM entries that together form a sprite.
 *
 * Each OAM entry describes a rectangular tile region at a relative position
 * within the cell. The bounding box fields are populated either from the
 * binary data (when the cell bank has extended info) or by calling
 * {@link #calculateBounds()}.
 */
public class NCERCell {

	/** The list of OAM entries that make up this cell. */
	public List<NCERCellOAM> oams;

	/** Bounding box maximum X (right edge, exclusive). */
	public int xMax;

	/** Bounding box maximum Y (bottom edge, exclusive). */
	public int yMax;

	/** Bounding box minimum X (left edge, inclusive). */
	public int xMin;

	/** Bounding box minimum Y (top edge, inclusive). */
	public int yMin;

	/**
	 * Cell attributes (uint16 at +0x02 within the cell entry). Stored
	 * verbatim for round-trip writes; NitroPaint passes this through as
	 * an opaque value.
	 */
	public int cellAttr;

	/**
	 * Optional per-cell CHAR VRAM transfer source address, in bytes, from
	 * the CEBK VRAM transfer sub-block. NitroPaint's {@code ChrGetChar}
	 * uses this as a byte-offset base: when a cell has a transfer entry,
	 * each OAM's tile index is interpreted as an offset past {@code srcAddr}
	 * (since {@code dstAddr} is always 0 for NCER transfers). Effectively
	 * the renderer adds {@code srcAddr / bytesPerTile} to each tile index
	 * before looking it up in the NCGR.
	 */
	public int vramTransferSrcAddr;

	/**
	 * Optional per-cell CHAR VRAM transfer window size, in bytes. Tile
	 * addresses that fall outside {@code [0, size)} bypass the remap and
	 * read directly from the NCGR (matching NitroPaint's fallback path).
	 */
	public int vramTransferSize;

	/** True when this cell has a populated VRAM transfer entry. */
	public boolean hasVramTransfer;

	public NCERCell() {
		oams = new ArrayList<>();
	}

	/**
	 * Adds an OAM entry to this cell.
	 *
	 * @param oam The OAM entry to add.
	 */
	public void addOAM(NCERCellOAM oam) {
		oams.add(oam);
	}

	/**
	 * Removes the OAM entry at the given index.
	 *
	 * @param index Index of the OAM entry to remove.
	 */
	public void removeOAM(int index) {
		oams.remove(index);
	}

	/**
	 * Gets the number of OAM entries in this cell.
	 *
	 * @return The OAM count.
	 */
	public int getOAMCount() {
		return oams.size();
	}

	/**
	 * Calculates the bounding box from the current OAM entries.
	 *
	 * The bounding box is the smallest rectangle enclosing all OAM regions,
	 * based on each OAM's position and shape dimensions.
	 */
	public void calculateBounds() {
		if (oams.isEmpty()) {
			xMin = 0;
			yMin = 0;
			xMax = 0;
			yMax = 0;
			return;
		}
		xMin = Integer.MAX_VALUE;
		yMin = Integer.MAX_VALUE;
		xMax = Integer.MIN_VALUE;
		yMax = Integer.MIN_VALUE;
		for (NCERCellOAM oam : oams) {
			if (oam.x < xMin) {
				xMin = oam.x;
			}
			if (oam.y < yMin) {
				yMin = oam.y;
			}
			int right = oam.x + oam.getWidth();
			int bottom = oam.y + oam.getHeight();
			if (right > xMax) {
				xMax = right;
			}
			if (bottom > yMax) {
				yMax = bottom;
			}
		}
	}
}
