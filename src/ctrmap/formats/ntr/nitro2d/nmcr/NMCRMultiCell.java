package ctrmap.formats.ntr.nitro2d.nmcr;

import java.util.ArrayList;
import java.util.List;

/**
 * A multi-cell entry within an NMCR file.
 *
 * <p>The on-disk MultiCell entry is 8 bytes:</p>
 * <pre>
 * 0x00 numberDisplayedCells uint16
 * 0x02 numberLoadedCells    uint16
 * 0x04 offsetData           uint32
 * </pre>
 *
 * <p>The data referenced by {@code offsetData} is a list of MultiCell
 * Properties (see {@link NMCRMultiCellProperty}).</p>
 */
public class NMCRMultiCell {

	/** Number of visible cells in this multi-cell. */
	public int numberDisplayedCells;

	/** Number of cells loaded into the multi-cell buffer. */
	public int numberLoadedCells;

	/** The list of properties (one per node) that compose this multi-cell. */
	public List<NMCRMultiCellProperty> properties;

	public NMCRMultiCell() {
		properties = new ArrayList<>();
	}

	public int getPropertyCount() {
		return properties.size();
	}
}
