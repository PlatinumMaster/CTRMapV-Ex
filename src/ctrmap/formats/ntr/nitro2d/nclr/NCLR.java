package ctrmap.formats.ntr.nitro2d.nclr;

import ctrmap.formats.ntr.common.NTRDataIOStream;
import ctrmap.formats.ntr.common.gfx.GXColor;
import ctrmap.formats.ntr.nitro2d.common.NNS2DResource;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import xstandard.fs.FSFile;
import xstandard.gui.file.ExtensionFilter;

/**
 * NITRO Color Resource (NCLR).
 *
 * <p>Contains one or more BGR555 sub-palettes for use with NCGR tile graphics.
 * The file always contains a {@link NCLRPaletteBlock} (PLTT) and may
 * optionally contain a {@link NCLRPCMPBlock} (PCMP) which limits the active
 * palette count.</p>
 *
 * <p>File magic: "RLCN".</p>
 */
public class NCLR extends NNS2DResource {

	public static final String MAGIC = "RLCN";
	public static final int VERSION = 0x0100;
	public static final ExtensionFilter EXTENSION_FILTER = new ExtensionFilter("NITRO Color Resource", "*.nclr");

	public NCLRPaletteBlock paletteBlock;
	public NCLRPCMPBlock pcmpBlock;

	public NCLR() {
		paletteBlock = new NCLRPaletteBlock();
		pcmpBlock = null;
	}

	public NCLR(FSFile file) {
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
				if (NCLRPaletteBlock.MAGIC.equals(blockMagic)) {
					paletteBlock = new NCLRPaletteBlock(io);
				} else if (NCLRPCMPBlock.MAGIC.equals(blockMagic)) {
					pcmpBlock = new NCLRPCMPBlock(io);
				}
			}
		} catch (IOException ex) {
			Logger.getLogger(NCLR.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * Gets a color from the palette data.
	 *
	 * @param paletteIdx Sub-palette index.
	 * @param colorIdx   Color index within the sub-palette.
	 * @return The GXColor, or black if out of range.
	 */
	public GXColor getColor(int paletteIdx, int colorIdx) {
		int cpp = paletteBlock.getColorsPerPalette();
		int idx = paletteIdx * cpp + colorIdx;
		if (idx >= 0 && idx < paletteBlock.colors.length) {
			return new GXColor(paletteBlock.colors[idx] & 0xFFFF);
		}
		return new GXColor(0);
	}

	/**
	 * Sets a color in the palette data.
	 */
	public void setColor(int paletteIdx, int colorIdx, GXColor color) {
		int cpp = paletteBlock.getColorsPerPalette();
		int idx = paletteIdx * cpp + colorIdx;
		if (idx >= 0 && idx < paletteBlock.colors.length) {
			paletteBlock.colors[idx] = (short) color.getBits();
		}
	}

	/**
	 * Returns the number of sub-palettes (after PCMP override if present).
	 */
	public int getPaletteCount() {
		if (pcmpBlock != null && pcmpBlock.numberPalettes > 0) {
			return pcmpBlock.numberPalettes;
		}
		return paletteBlock.getPaletteCount();
	}

	/**
	 * Returns the number of colors per sub-palette (16 for 4bpp, 256 for 8bpp).
	 */
	public int getColorsPerPalette() {
		return paletteBlock.getColorsPerPalette();
	}

	@Override
	protected void write(NTRDataIOStream io) throws IOException {
		int blockCount = pcmpBlock != null ? 2 : 1;
		int headerEnd = writeHeader(io, MAGIC, VERSION, blockCount);
		paletteBlock.write(io);
		if (pcmpBlock != null) {
			pcmpBlock.write(io);
		}
		patchHeader(io, headerEnd, null);
	}
}
