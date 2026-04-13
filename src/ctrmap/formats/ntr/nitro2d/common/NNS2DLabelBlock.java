package ctrmap.formats.ntr.nitro2d.common;

import ctrmap.formats.ntr.common.NTRDataIOStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Optional LABL (Label) section common to several NITRO 2D files.
 *
 * <p>Block magic in file: "LBAL" (LABL reversed).</p>
 *
 * <p>The container is a uint32 offset table immediately followed by a packed
 * blob of UTF-8 NUL-terminated strings. Each offset is relative to the start
 * of the string blob (not the section). The number of labels is determined
 * by the smallest offset (= length of the offset table).</p>
 */
public class NNS2DLabelBlock {

	public static final String MAGIC = "LBAL";

	/** Ordered list of label strings. */
	public ArrayList<String> labels = new ArrayList<>();

	public NNS2DLabelBlock() {
	}

	public NNS2DLabelBlock(NTRDataIOStream io) throws IOException {
		int blockStart = io.getPosition();
		String magic = io.readPaddedString(4);
		if (!MAGIC.equals(magic)) {
			throw new IOException("Expected LABL (LBAL) magic, got: " + magic);
		}
		int blockSize = io.readInt();
		int containerStart = io.getPosition();
		int containerSize = blockSize - 8;

		labels = new ArrayList<>();

		// An empty LABL block (header only) is legal — some games ship them.
		if (containerSize < 4) {
			io.seek(blockStart + blockSize);
			return;
		}

		// First read offset table — we have to determine its length without
		// any metadata, by following the smallest offset (which marks the
		// start of the string blob). Be defensive: if the first offset is
		// nonsense (zero, negative, or beyond the container), bail out
		// instead of crashing.
		int firstOffset = io.readInt();
		if (firstOffset <= 0 || firstOffset > containerSize || (firstOffset & 3) != 0) {
			io.seek(blockStart + blockSize);
			return;
		}
		int labelCount = firstOffset / 4;

		int[] offsets = new int[labelCount];
		offsets[0] = firstOffset;
		for (int i = 1; i < labelCount; i++) {
			offsets[i] = io.readInt();
		}

		int stringBlobStart = containerStart + labelCount * 4;
		labels.ensureCapacity(labelCount);
		for (int i = 0; i < labelCount; i++) {
			int rel = offsets[i];
			if (rel < 0 || rel >= containerSize) {
				labels.add("");
				continue;
			}
			io.seek(stringBlobStart + rel);
			labels.add(readNullTerminatedString(io));
		}

		io.seek(blockStart + blockSize);
	}

	private static String readNullTerminatedString(NTRDataIOStream io) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int b;
		while ((b = io.readUnsignedByte()) != 0) {
			out.write(b);
		}
		return new String(out.toByteArray(), StandardCharsets.UTF_8);
	}

	public void write(NTRDataIOStream io) throws IOException {
		int blockStart = io.getPosition();
		io.writeStringUnterminated(MAGIC);
		int blockSizePos = io.getPosition();
		io.writeInt(0);

		int labelCount = labels.size();

		// Build string blob and offsets
		ByteArrayOutputStream blob = new ByteArrayOutputStream();
		int[] offsets = new int[labelCount];
		for (int i = 0; i < labelCount; i++) {
			offsets[i] = blob.size();
			byte[] strBytes = labels.get(i).getBytes(StandardCharsets.UTF_8);
			blob.write(strBytes, 0, strBytes.length);
			blob.write(0);
		}

		// Adjust offsets to be relative to start of strings (which is right
		// after the offset table)
		// They are already relative to the blob start since blob.size() is
		// measured from the start of the blob.
		for (int i = 0; i < labelCount; i++) {
			io.writeInt(offsets[i]);
		}
		io.write(blob.toByteArray());

		// Pad to 4-byte alignment
		while ((io.getPosition() - blockStart) % 4 != 0) {
			io.writeByte((byte) 0);
		}

		int blockEnd = io.getPosition();
		io.seek(blockSizePos);
		io.writeInt(blockEnd - blockStart);
		io.seek(blockEnd);
	}
}
