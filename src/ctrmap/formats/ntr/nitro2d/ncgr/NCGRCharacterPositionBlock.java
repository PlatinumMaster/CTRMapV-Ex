package ctrmap.formats.ntr.nitro2d.ncgr;

import ctrmap.formats.ntr.common.NTRDataIOStream;
import java.io.IOException;

/**
 * Optional CPOS (Character Position) data block within an NCGR file.
 *
 * <p>Block magic in file: "SOPC" (CPOS reversed). Specifies the on-screen
 * position and size in tiles of the character data.</p>
 *
 * <pre>
 * 0x00 positionX        uint16   X position in tiles
 * 0x02 positionY        uint16   Y position in tiles
 * 0x04 widthInTiles     uint16   Width in tiles
 * 0x06 heightInTiles    uint16   Height in tiles
 * </pre>
 */
public class NCGRCharacterPositionBlock {

	public static final String MAGIC = "SOPC";

	public int positionX;
	public int positionY;
	public int tileWidth;
	public int tileHeight;

	public NCGRCharacterPositionBlock() {
	}

	public NCGRCharacterPositionBlock(NTRDataIOStream io) throws IOException {
		int blockStart = io.getPosition();
		String magic = io.readPaddedString(4);
		if (!MAGIC.equals(magic)) {
			throw new IOException("Expected CPOS (SOPC) magic, got: " + magic);
		}
		int blockSize = io.readInt();

		positionX = io.readUnsignedShort();
		positionY = io.readUnsignedShort();
		tileWidth = io.readUnsignedShort();
		tileHeight = io.readUnsignedShort();

		io.seek(blockStart + blockSize);
	}

	public void write(NTRDataIOStream io) throws IOException {
		int blockStart = io.getPosition();
		io.writeStringUnterminated(MAGIC);
		int blockSizePos = io.getPosition();
		io.writeInt(0);

		io.writeShort(positionX);
		io.writeShort(positionY);
		io.writeShort(tileWidth);
		io.writeShort(tileHeight);

		int blockEnd = io.getPosition();
		io.seek(blockSizePos);
		io.writeInt(blockEnd - blockStart);
		io.seek(blockEnd);
	}
}
