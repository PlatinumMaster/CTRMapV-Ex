package ctrmap.formats.ntr.nitro2d.nanr;

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
 * NITRO Cell Animation Resource (NANR).
 *
 * <p>Always contains a {@link NANRAnimBank} (ABNK) and may optionally
 * contain {@link NNS2DLabelBlock} (LABL) and {@link NNS2DUserExBlock}
 * (UEXT).</p>
 *
 * <p>File magic: "RNAN".</p>
 */
public class NANR extends NNS2DResource {

	public static final String MAGIC = "RNAN";
	public static final int VERSION = 0x0100;
	public static final ExtensionFilter EXTENSION_FILTER = new ExtensionFilter("NITRO Cell Animation Resource", "*.nanr");

	public NANRAnimBank animBank;
	public NNS2DLabelBlock labelBlock;
	public NNS2DUserExBlock userExBlock;

	public NANR() {
		animBank = new NANRAnimBank();
	}

	public NANR(FSFile file) {
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
				if (NANRAnimBank.MAGIC.equals(blockMagic)) {
					animBank = new NANRAnimBank(io);
				} else if (NNS2DLabelBlock.MAGIC.equals(blockMagic)) {
					labelBlock = new NNS2DLabelBlock(io);
				} else if (NNS2DUserExBlock.MAGIC.equals(blockMagic)) {
					userExBlock = new NNS2DUserExBlock(io);
				}
			}
		} catch (IOException ex) {
			Logger.getLogger(NANR.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public NANRAnimation getAnimation(int index) {
		if (index >= 0 && index < animBank.animations.size()) {
			return animBank.animations.get(index);
		}
		return null;
	}

	public int getAnimationCount() {
		return animBank.animations.size();
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
