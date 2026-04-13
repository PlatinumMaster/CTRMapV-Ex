package ctrmap.formats.ntr.nitro2d.nmar;

import ctrmap.formats.ntr.common.NTRDataIOStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * ABNK (Animation Bank) data block within an NMAR file.
 *
 * <p>NMAR uses the same ABNK structure as NANR, but the cell index in each
 * frame's property block is interpreted as a multi-cell index.</p>
 *
 * <p>Block magic in file: "KNBA" (ABNK reversed). Container layout:</p>
 *
 * <pre>
 * 0x00 numberSequences           uint16
 * 0x02 numberFrames              uint16
 * 0x04 offsetDataSequences       uint32
 * 0x08 offsetDataFrame           uint32
 * 0x0C offsetDataFrameProperties uint32
 * 0x10 unknown0                  uint32
 * 0x14 unknown1                  uint32
 * </pre>
 */
public class NMARAnimBank {

	public static final String MAGIC = "KNBA";

	public int unknown0Header;
	public int unknown1Header;

	public List<NMARAnimation> animations;

	public NMARAnimBank() {
		animations = new ArrayList<>();
	}

	public NMARAnimBank(NTRDataIOStream io) throws IOException {
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

		int[] seqFrameCount = new int[numberSequences];
		int[] seqOffsetFrame = new int[numberSequences];

		io.seek(sequenceTableStart);
		for (int i = 0; i < numberSequences; i++) {
			NMARAnimation anim = new NMARAnimation();
			seqFrameCount[i] = io.readInt();
			anim.animationType = io.readUnsignedShort();
			anim.cellType = io.readUnsignedShort();
			anim.loopMode = io.readInt();
			seqOffsetFrame[i] = io.readInt();
			animations.add(anim);
		}

		for (int i = 0; i < numberSequences; i++) {
			NMARAnimation anim = animations.get(i);
			int frameCount = seqFrameCount[i];

			io.seek(frameTableStart + seqOffsetFrame[i]);
			int[] propOffsets = new int[frameCount];
			int[] durations = new int[frameCount];

			for (int f = 0; f < frameCount; f++) {
				propOffsets[f] = io.readInt();
				durations[f] = io.readUnsignedShort();
				io.readShort(); // 0xBEEF
			}

			for (int f = 0; f < frameCount; f++) {
				NMARFrame frame = new NMARFrame();
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

		int numberSequences = animations.size();
		int totalFrames = 0;
		for (NMARAnimation a : animations) {
			totalFrames += a.getFrameCount();
		}

		int offsetDataSequences = 0x1C;
		int offsetDataFrame = offsetDataSequences + numberSequences * 16;
		int offsetDataFrameProperties = offsetDataFrame + totalFrames * 8;

		io.writeShort(numberSequences);
		io.writeShort(totalFrames);
		io.writeInt(offsetDataSequences);
		io.writeInt(offsetDataFrame);
		io.writeInt(offsetDataFrameProperties);
		io.writeInt(unknown0Header);
		io.writeInt(unknown1Header);

		int currentFrameOffset = 0;
		for (NMARAnimation anim : animations) {
			io.writeInt(anim.getFrameCount());
			io.writeShort(anim.animationType);
			io.writeShort(anim.cellType);
			io.writeInt(anim.loopMode);
			io.writeInt(currentFrameOffset);
			currentFrameOffset += anim.getFrameCount() * 8;
		}

		int currentPropOffset = 0;
		for (NMARAnimation anim : animations) {
			int propSize = NMARFrame.getPropertySize(anim.animationType);
			for (NMARFrame f : anim.frames) {
				io.writeInt(currentPropOffset);
				io.writeShort(f.duration);
				io.writeShort(NMARFrame.BEEF);
				currentPropOffset += propSize;
				if (anim.animationType == NMARAnimation.ANIM_TYPE_CELL) {
					currentPropOffset += 2; // type 0 padding
				}
			}
		}

		for (NMARAnimation anim : animations) {
			for (NMARFrame f : anim.frames) {
				f.writeFrameProperties(io, anim.animationType);
				if (anim.animationType == NMARAnimation.ANIM_TYPE_CELL) {
					io.writeShort(0);
				}
			}
		}

		while ((io.getPosition() - blockStart) % 4 != 0) {
			io.writeByte((byte) 0);
		}

		int blockEnd = io.getPosition();
		io.seek(blockSizePos);
		io.writeInt(blockEnd - blockStart);
		io.seek(blockEnd);
	}

	public int getAnimationCount() {
		return animations.size();
	}
}
