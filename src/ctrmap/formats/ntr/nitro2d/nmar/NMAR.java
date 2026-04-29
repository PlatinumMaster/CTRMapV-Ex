package ctrmap.formats.ntr.nitro2d.nmar;

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
 * NITRO Multi-Cell Animation Resource (NMAR).
 *
 * <p>Always contains a {@link NMARAnimBank} (ABNK) and may optionally contain
 * {@link NNS2DLabelBlock} (LABL) and {@link NNS2DUserExBlock} (UEXT).</p>
 *
 * <p>File magic: "RAMN".</p>
 */
public class NMAR extends NNS2DResource {

	public static final String MAGIC = "RAMN";
	public static final int VERSION = 0x0100;
	public static final ExtensionFilter EXTENSION_FILTER = new ExtensionFilter("NITRO Multi-Cell Animation Resource", "*.nmar");

	public NMARAnimBank animBank;
	public NNS2DLabelBlock labelBlock;
	public NNS2DUserExBlock userExBlock;

	public NMAR() {
		animBank = new NMARAnimBank();
	}

	public NMAR(FSFile file) {
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
					if (NMARAnimBank.MAGIC.equals(blockMagic)) {
						animBank = new NMARAnimBank(io);
					} else if (NNS2DLabelBlock.MAGIC.equals(blockMagic)) {
						labelBlock = new NNS2DLabelBlock(io);
					} else if (NNS2DUserExBlock.MAGIC.equals(blockMagic)) {
						userExBlock = new NNS2DUserExBlock(io);
					}
				} catch (Exception blockEx) {
					// Same defensive pattern as NCER/NMCR: a single malformed
					// optional block should not abort the rest of the parse.
					Logger.getLogger(NMAR.class.getName()).log(Level.WARNING,
						"Skipping malformed " + blockMagic + " block in NMAR", blockEx);
				}
			}
		} catch (IOException ex) {
			Logger.getLogger(NMAR.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public NMARAnimation getAnimation(int index) {
		return animBank.animations.get(index);
	}

	public int getAnimationCount() {
		return animBank.getAnimationCount();
	}

	@Override
	protected void write(NTRDataIOStream io) throws IOException {
		int blockCount = 1;
		if (labelBlock != null) blockCount++;
		if (userExBlock != null) blockCount++;
		int headerEnd = writeHeader(io, MAGIC, VERSION, blockCount);
		animBank.write(io);
		if (labelBlock != null) labelBlock.write(io);
		if (userExBlock != null) userExBlock.write(io);
		patchHeader(io, headerEnd, null);
	}
}
