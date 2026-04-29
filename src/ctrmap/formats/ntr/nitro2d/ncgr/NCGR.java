package ctrmap.formats.ntr.nitro2d.ncgr;

import ctrmap.formats.ntr.common.NTRDataIOStream;
import ctrmap.formats.ntr.nitro2d.common.NNS2DResource;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import xstandard.fs.FSFile;
import xstandard.gui.file.ExtensionFilter;

/**
 * NITRO Character Graphics Resource (NCGR).
 *
 * <p>Contains 8x8 indexed tile graphics for use with NDS 2D rendering.
 * Always contains a {@link NCGRCharacterBlock} (CHAR) and may optionally
 * contain a {@link NCGRCharacterPositionBlock} (CPOS).</p>
 *
 * <p>File magic: "RGCN".</p>
 */
public class NCGR extends NNS2DResource {

	public static final String MAGIC = "RGCN";
	public static final int VERSION = 0x0100;
	public static final ExtensionFilter EXTENSION_FILTER = new ExtensionFilter("NITRO Character Graphics Resource", "*.ncgr");

	public NCGRCharacterBlock characterBlock;
	public NCGRCharacterPositionBlock cposBlock;

	public NCGR() {
		characterBlock = new NCGRCharacterBlock();
		cposBlock = null;
	}

	public NCGR(FSFile file) {
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
				if (NCGRCharacterBlock.MAGIC.equals(blockMagic)) {
					characterBlock = new NCGRCharacterBlock(io);
				} else if (NCGRCharacterPositionBlock.MAGIC.equals(blockMagic)) {
					cposBlock = new NCGRCharacterPositionBlock(io);
				}
			}
		} catch (IOException ex) {
			Logger.getLogger(NCGR.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public int getPixel(int tileIdx, int x, int y) {
		return characterBlock.getPixel(tileIdx, x, y);
	}

	public void setPixel(int tileIdx, int x, int y, int paletteIdx) {
		characterBlock.setPixel(tileIdx, x, y, paletteIdx);
	}

	public int getTileCount() {
		return characterBlock.getTileCount();
	}

	public int getBytesPerTile() {
		return characterBlock.getBytesPerTile();
	}

	@Override
	protected void write(NTRDataIOStream io) throws IOException {
		int blockCount = cposBlock != null ? 2 : 1;
		int headerEnd = writeHeader(io, MAGIC, VERSION, blockCount);
		characterBlock.write(io);
		if (cposBlock != null) {
			cposBlock.write(io);
		}
		patchHeader(io, headerEnd, null);
	}
}
