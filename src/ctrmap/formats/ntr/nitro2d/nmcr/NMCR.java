package ctrmap.formats.ntr.nitro2d.nmcr;

import ctrmap.formats.ntr.common.NTRDataIOStream;
import ctrmap.formats.ntr.nitro2d.common.NNS2DLabelBlock;
import ctrmap.formats.ntr.nitro2d.common.NNS2DResource;
import ctrmap.formats.ntr.nitro2d.common.NNS2DUserExBlock;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import xstandard.fs.FSFile;
import xstandard.gui.file.ExtensionFilter;

/**
 * NITRO Multi-Cell Resource (NMCR).
 *
 * <p>Always contains a {@link NMCRMultiCellBank} (MCBK) and may optionally
 * contain {@link NNS2DLabelBlock} (LABL) and {@link NNS2DUserExBlock} (UEXT).</p>
 *
 * <p>File magic: "RCMN".</p>
 */
public class NMCR extends NNS2DResource {

	public static final String MAGIC = "RCMN";
	public static final int VERSION = 0x0100;
	public static final ExtensionFilter EXTENSION_FILTER = new ExtensionFilter("NITRO Multi-Cell Resource", "*.nmcr");

	public NMCRMultiCellBank multiCellBank;
	public NNS2DLabelBlock labelBlock;
	public NNS2DUserExBlock userExBlock;

	public NMCR() {
		multiCellBank = new NMCRMultiCellBank();
	}

	public NMCR(FSFile file) {
		this();
		try (NTRDataIOStream io = new NTRDataIOStream(file.getIO())) {
			readBase(io);
			for (int i = 0; i < blockCount; i++) {
				if (!seekBlock(io, i)) {
					break;
				}
				int savedPos = io.getPosition();
				String blockMagic = io.readPaddedString(4);
				io.seek(savedPos);
				try {
					if (NMCRMultiCellBank.MAGIC.equals(blockMagic)) {
						multiCellBank = new NMCRMultiCellBank(io);
					} else if (NNS2DLabelBlock.MAGIC.equals(blockMagic)) {
						labelBlock = new NNS2DLabelBlock(io);
					} else if (NNS2DUserExBlock.MAGIC.equals(blockMagic)) {
						userExBlock = new NNS2DUserExBlock(io);
					}
				} catch (Exception blockEx) {
					// One bad optional block (typically LABL/UEXT, but also a
					// malformed MCBK with unexpected layout) should not take
					// down the whole NMCR. Log and try the next block so the
					// caller can still see whatever was parseable.
					Logger.getLogger(NMCR.class.getName()).log(Level.WARNING,
						"Skipping malformed " + blockMagic + " block in NMCR", blockEx);
				}
			}
		} catch (IOException ex) {
			Logger.getLogger(NMCR.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public NMCRMultiCell getMultiCell(int index) {
		return multiCellBank.multiCells.get(index);
	}

	public int getMultiCellCount() {
		return multiCellBank.getMultiCellCount();
	}

	@Override
	protected void write(NTRDataIOStream io) throws IOException {
		int blockCount = 1;
		if (labelBlock != null) blockCount++;
		if (userExBlock != null) blockCount++;
		int headerEnd = writeHeader(io, MAGIC, VERSION, blockCount);
		multiCellBank.write(io);
		if (labelBlock != null) labelBlock.write(io);
		if (userExBlock != null) userExBlock.write(io);
		patchHeader(io, headerEnd, null);
	}
}
