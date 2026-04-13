package ctrmap.formats.ntr.nitro2d.common;

import ctrmap.formats.ntr.common.NTRDataIOStream;
import java.io.IOException;

/**
 * Optional UEXT (User Extended) section common to several NITRO 2D files.
 *
 * <p>Block magic in file: "TXEU" (UEXT reversed). Container is a single
 * uint32 "extended" field believed to enable a multi-cell capable runtime
 * variant.</p>
 */
public class NNS2DUserExBlock {

	public static final String MAGIC = "TXEU";

	/** Single uint32 field whose meaning is "enable multicell runtime?". */
	public int extended;

	public NNS2DUserExBlock() {
	}

	public NNS2DUserExBlock(NTRDataIOStream io) throws IOException {
		int blockStart = io.getPosition();
		String magic = io.readPaddedString(4);
		if (!MAGIC.equals(magic)) {
			throw new IOException("Expected UEXT (TXEU) magic, got: " + magic);
		}
		int blockSize = io.readInt();
		extended = io.readInt();
		io.seek(blockStart + blockSize);
	}

	public void write(NTRDataIOStream io) throws IOException {
		int blockStart = io.getPosition();
		io.writeStringUnterminated(MAGIC);
		int blockSizePos = io.getPosition();
		io.writeInt(0);

		io.writeInt(extended);

		int blockEnd = io.getPosition();
		io.seek(blockSizePos);
		io.writeInt(blockEnd - blockStart);
		io.seek(blockEnd);
	}
}
