package ctrmap.formats.ntr.nitro2d.nanr;

import ctrmap.formats.ntr.common.NTRDataIOStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * ABNK (Animation Bank) data block within a NANR (or NMAR) file.
 *
 * <p>Block magic in file: "KNBA" (ABNK reversed). Container layout:</p>
 *
 * <pre>
 * 0x00 numberSequences           uint16
 * 0x02 numberFrames              uint16
 * 0x04 offsetDataSequences       uint32   Sequence table offset
 * 0x08 offsetDataFrame           uint32   Frame table offset
 * 0x0C offsetDataFrameProperties uint32   Property data offset
 * 0x10 unknown0                  uint32
 * 0x14 unknown1                  uint32
 * </pre>
 *
 * <p>Each Sequence entry is 16 bytes:</p>
 * <pre>
 * 0x00 numberFrames    uint32
 * 0x04 animationType   uint16   0=cell, 1=affine, 2=translate
 * 0x06 cellType        uint16   1=single cell, 2=multi-cell
 * 0x08 loopMode        uint32   1=stop, 2=repeat, 3=backward, 4=ping-pong
 * 0x0C offsetFrame     uint32
 * </pre>
 *
 * <p>Each Frame entry is 8 bytes:</p>
 * <pre>
 * 0x00 offsetProperties uint32
 * 0x04 durationInFrames uint16
 * 0x06 unknown0         int16    Always 0xBEEF
 * </pre>
 */
public class NANRAnimBank {

	public static final String MAGIC = "KNBA";

	public int unknown0Header;
	public int unknown1Header;

	/** The list of animations in this bank. */
	public List<NANRAnimation> animations;

	public NANRAnimBank() {
		animations = new ArrayList<>();
	}

	public NANRAnimBank(NTRDataIOStream io) throws IOException {
		animations = new ArrayList<>();

		int blockStart = io.getPosition();
		String magic = io.readPaddedString(4);
		if (!MAGIC.equals(magic)) {
			throw new IOException("Expected ABNK (KNBA) magic, got: " + magic);
		}
		int blockSize = io.readInt();
		int containerStart = io.getPosition();

		int numberSequences = io.readUnsignedShort();
		int numberFrames = io.readUnsignedShort();
		int offsetDataSequences = io.readInt();
		int offsetDataFrame = io.readInt();
		int offsetDataFrameProperties = io.readInt();
		unknown0Header = io.readInt();
		unknown1Header = io.readInt();

		int sequenceTableStart = containerStart + offsetDataSequences;
		int frameTableStart = containerStart + offsetDataFrame;
		int propertyTableStart = containerStart + offsetDataFrameProperties;

		// Read all sequences
		int[] seqFrameCount = new int[numberSequences];
		int[] seqOffsetFrame = new int[numberSequences];

		io.seek(sequenceTableStart);
		for (int i = 0; i < numberSequences; i++) {
			NANRAnimation anim = new NANRAnimation();
			seqFrameCount[i] = io.readInt();
			anim.animationType = io.readUnsignedShort();
			anim.cellType = io.readUnsignedShort();
			anim.loopMode = io.readInt();
			seqOffsetFrame[i] = io.readInt();
			animations.add(anim);
		}

		// For each animation, read its frame entries and frame properties
		for (int i = 0; i < numberSequences; i++) {
			NANRAnimation anim = animations.get(i);
			int frameCount = seqFrameCount[i];

			io.seek(frameTableStart + seqOffsetFrame[i]);
			int[] propOffsets = new int[frameCount];
			int[] durations = new int[frameCount];

			for (int f = 0; f < frameCount; f++) {
				propOffsets[f] = io.readInt();
				durations[f] = io.readUnsignedShort();
				io.readShort(); // 0xBEEF marker
			}

			for (int f = 0; f < frameCount; f++) {
				NANRFrame frame = new NANRFrame();
				frame.duration = durations[f];
				io.seek(propertyTableStart + propOffsets[f]);
				frame.readFrameProperties(io, anim.animationType);
				anim.frames.add(frame);
			}
		}

		io.seek(blockStart + blockSize);
	}

	public void write(NTRDataIOStream io) throws IOException {
		int blockStart = io.getPosition();
		io.writeStringUnterminated(MAGIC);
		int blockSizePos = io.getPosition();
		io.writeInt(0);

		int containerStart = io.getPosition();

		int numberSequences = animations.size();
		int totalFrames = 0;
		for (NANRAnimation a : animations) {
			totalFrames += a.getFrameCount();
		}

		// Layout offsets:
		// container header is 0x18 (24) bytes:
		//   u16 numSeq + u16 numFrames + 3x u32 offsets + 2x u32 unknown
		// sequence table follows, sequenceCount * 16 bytes
		// frame table follows, totalFrames * 8 bytes
		// property data follows, sum of property sizes per frame
		//
		// Previously hardcoded 0x1C — wrong. Real BW2 NANR files encode
		// 0x18 at this offset, and the 4-byte gap between the written
		// header (24 bytes) and the claimed offset (28 bytes) made the
		// reader seek 4 bytes into seq[0], producing garbage sequence
		// counts that walked the stream off the end of the buffer.
		int offsetDataSequences = 0x18;
		int offsetDataFrame = offsetDataSequences + numberSequences * 16;
		int offsetDataFrameProperties = offsetDataFrame + totalFrames * 8;

		io.writeShort(numberSequences);
		io.writeShort(totalFrames);
		io.writeInt(offsetDataSequences);
		io.writeInt(offsetDataFrame);
		io.writeInt(offsetDataFrameProperties);
		io.writeInt(unknown0Header);
		io.writeInt(unknown1Header);

		// Sequence table
		int currentFrameOffset = 0;
		for (NANRAnimation anim : animations) {
			io.writeInt(anim.getFrameCount());
			io.writeShort(anim.animationType);
			io.writeShort(anim.cellType);
			io.writeInt(anim.loopMode);
			io.writeInt(currentFrameOffset);
			currentFrameOffset += anim.getFrameCount() * 8;
		}

		// Frame table - assign property offsets per frame
		int currentPropOffset = 0;
		for (NANRAnimation anim : animations) {
			int propSize = NANRFrame.getPropertySize(anim.animationType);
			for (NANRFrame f : anim.frames) {
				io.writeInt(currentPropOffset);
				io.writeShort(f.duration);
				io.writeShort(NANRFrame.BEEF);
				currentPropOffset += propSize;
				// Property data is dword aligned for type 0 (which is 2 bytes)
				if (animationType_is_type0_pad(anim.animationType)) {
					currentPropOffset += 2; // pad to 4
				}
			}
		}

		// Property data
		for (NANRAnimation anim : animations) {
			for (NANRFrame f : anim.frames) {
				f.writeFrameProperties(io, anim.animationType);
				if (animationType_is_type0_pad(anim.animationType)) {
					io.writeShort(0);
				}
			}
		}

		int blockEnd = io.getPosition();
		// Pad to 4-byte alignment
		while ((io.getPosition() - blockStart) % 4 != 0) {
			io.writeByte((byte) 0);
		}
		blockEnd = io.getPosition();

		io.seek(blockSizePos);
		io.writeInt(blockEnd - blockStart);
		io.seek(blockEnd);
	}

	private static boolean animationType_is_type0_pad(int animationType) {
		// Type 0 is 2 bytes, so we always pad to 4 to keep dword alignment
		return animationType == NANRAnimation.ANIM_TYPE_CELL;
	}
}
