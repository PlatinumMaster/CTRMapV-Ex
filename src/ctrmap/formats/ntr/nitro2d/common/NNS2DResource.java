package ctrmap.formats.ntr.nitro2d.common;

import ctrmap.formats.ntr.common.NTRDataIOStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import xstandard.fs.FSFile;

/**
 * Base class for NITRO 2D resources (NCLR, NCGR, NCER, NANR, NMCR, NMAR).
 *
 * <p>Unlike the 3D NITRO formats (NSBMD, NSBTX), 2D NITRO formats do NOT have
 * an offset table after the file header. The blocks are laid out sequentially
 * starting immediately after the 16-byte file header, and each block has its
 * own header containing its size so they can be iterated.</p>
 */
public class NNS2DResource {

	/**
	 * Fixed file header size (no offset table).
	 */
	public static final int HEADER_SIZE = 0x10;

	protected int signature;
	protected int byteOrder;
	protected int version;
	protected int fileSize;
	protected int headerSize;
	protected int blockCount;

	/**
	 * Reads the 16-byte NITRO 2D file header (magic, BOM, version, fileSize,
	 * headerSize, blockCount). No offset table follows.
	 */
	protected final void readBase(NTRDataIOStream io) throws IOException {
		signature = io.readInt();
		byteOrder = io.readUnsignedShort();
		version = io.readUnsignedShort();
		fileSize = io.readInt();
		headerSize = io.readUnsignedShort();
		blockCount = io.readUnsignedShort();
	}

	/**
	 * Seeks to the start of the block at the specified index. Blocks are
	 * discovered by iterating from the end of the file header, reading each
	 * block's magic + blockSize header and jumping forward by blockSize.
	 *
	 * @param io    The data stream.
	 * @param index The zero-based block index.
	 * @return True if the block was found and the stream was seeked to its
	 *         start; false if the index is out of range.
	 */
	protected final boolean seekBlock(NTRDataIOStream io, int index) throws IOException {
		if (index < 0 || index >= blockCount) {
			return false;
		}
		int pos = headerSize;
		for (int i = 0; i < index; i++) {
			io.seek(pos + 4); // skip magic
			int blockSize = io.readInt();
			if (blockSize <= 0) {
				return false;
			}
			pos += blockSize;
		}
		io.seek(pos);
		return true;
	}

	public void write(FSFile file) {
		try (NTRDataIOStream io = new NTRDataIOStream(file.getIO())) {
			write(io);
		} catch (IOException ex) {
			Logger.getLogger(NNS2DResource.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	protected void write(NTRDataIOStream io) throws IOException {
		// Subclasses override
	}

	/**
	 * Writes the standard NITRO 2D file header (16 bytes total, no offset table).
	 *
	 * @param io         Output stream.
	 * @param magic      4-byte magic string (e.g. "RLCN").
	 * @param version    File version (typically 0x0100).
	 * @param blockCount Number of data blocks.
	 * @return The position after the header (start of the first block).
	 */
	protected static int writeHeader(NTRDataIOStream io, String magic, int version, int blockCount) throws IOException {
		io.writeStringUnterminated(magic);
		io.writeShort(0xFEFF);            // BOM (little-endian)
		io.writeShort(version);
		io.writeInt(0);                   // fileSize placeholder (patched later)
		io.writeShort(HEADER_SIZE);       // headerSize = 16
		io.writeShort(blockCount);
		return io.getPosition();          // position after the header (= HEADER_SIZE)
	}

	/**
	 * Patches the file size field in the header after writing all blocks.
	 * NITRO 2D formats do not have an offset table, so only the file size
	 * needs patching.
	 *
	 * @param io                Output stream.
	 * @param headerEndPos      The position returned by {@link #writeHeader}
	 *                          (unused for 2D, kept for API symmetry).
	 * @param blockOffsets      The offsets of each block (unused for 2D,
	 *                          kept for API symmetry with the 3D counterpart).
	 */
	protected static void patchHeader(NTRDataIOStream io, int headerEndPos, int[] blockOffsets) throws IOException {
		int fileSize = io.getLength();
		int current = io.getPosition();
		io.seek(8); // fileSize field is at byte 8
		io.writeInt(fileSize);
		io.seek(current);
	}
}
