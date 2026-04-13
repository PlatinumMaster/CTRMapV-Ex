package ctrmap.formats.ntr.nitro2d.ncer;

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
 * NITRO Cell Resource (NCER).
 *
 * <p>Always contains a {@link NCERCellBank} (CEBK) and may optionally
 * contain a {@link NNS2DLabelBlock} (LABL) and {@link NNS2DUserExBlock}
 * (UEXT).</p>
 *
 * <p>File magic: "RECN".</p>
 */
public class NCER extends NNS2DResource {

	public static final String MAGIC = "RECN";
	public static final int VERSION = 0x0100;
	public static final ExtensionFilter EXTENSION_FILTER = new ExtensionFilter("NITRO Cell Resource", "*.ncer");

	public NCERCellBank cellBank;
	public NNS2DLabelBlock labelBlock;
	public NNS2DUserExBlock userExBlock;

	public NCER() {
		cellBank = new NCERCellBank();
	}

	public NCER(FSFile file) {
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
					if (NCERCellBank.MAGIC.equals(blockMagic)) {
						cellBank = new NCERCellBank(io);
					} else if (NNS2DLabelBlock.MAGIC.equals(blockMagic)) {
						labelBlock = new NNS2DLabelBlock(io);
					} else if (NNS2DUserExBlock.MAGIC.equals(blockMagic)) {
						userExBlock = new NNS2DUserExBlock(io);
					}
				} catch (Exception blockEx) {
					// One bad optional block (typically LABL/UEXT) shouldn't
					// take down the whole NCER. Log and continue with the
					// next block.
					Logger.getLogger(NCER.class.getName()).log(Level.WARNING,
						"Skipping malformed " + blockMagic + " block in NCER", blockEx);
				}
			}
		} catch (IOException ex) {
			Logger.getLogger(NCER.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public NCERCell getCell(int index) {
		return cellBank.cells.get(index);
	}

	public int getCellCount() {
		return cellBank.getCellCount();
	}

	@Override
	protected void write(NTRDataIOStream io) throws IOException {
		int blockCount = 1;
		if (labelBlock != null) blockCount++;
		if (userExBlock != null) blockCount++;
		int headerEnd = writeHeader(io, MAGIC, VERSION, blockCount);
		cellBank.write(io);
		if (labelBlock != null) labelBlock.write(io);
		if (userExBlock != null) userExBlock.write(io);
		patchHeader(io, headerEnd, null);
	}
}
