package ctrmap.formats.pokemon.gen5.battle.trainer;

import java.io.IOException;
import xstandard.io.base.impl.ext.data.DataIOStream;

/**
 * A single record in the BW2 trainer-message map (ARC 090 file 0).
 *
 * <p>On-disk layout (4 bytes per record, little-endian):</p>
 * <pre>
 *   uint16 trainerId    — Trainer ID this message belongs to
 *   uint16 messageType  — Kind of dialogue (pre-battle, defeat, etc.)
 * </pre>
 *
 * <p>The record at index N of ARC 090 is parallel-indexed with line N of
 * {@code MSGDATA_SYSTEM} file 381 (the {@code TrainerDialogue} TextFile) —
 * so reading the string for this record is
 * {@code trainerDialogue.getLine(recordIndex)}.</p>
 *
 * <p>Observed {@code messageType} values in a vanilla BW2 ROM range 0..24,
 * with the most common being 0 / 1 / 2 (basic trainer pre-battle /
 * post-battle sets). See {@link MessageType} for known names.</p>
 */
public class WBTrainerMessage {

    /** Convenience names for observed {@code messageType} values. */
    public static final class MessageType {
        /** Pre-battle greeting ("You want to battle me?"). */
        public static final int TYPE_0 = 0;
        /** Post-defeat (trainer loses) dialogue. */
        public static final int TYPE_1 = 1;
        /** Post-battle greeting ("Good match!"). */
        public static final int TYPE_2 = 2;
        // Types 3, 5-10, 13-17, 19-20, 24 also appear; they map to
        // event-specific / gym-leader / scripted dialogues whose exact
        // semantics depend on the game's scripting layer.
        private MessageType() {}
    }

    /** Trainer ID (uint16 on disk). */
    public int trainerId;

    /** Message-type discriminator (uint16 on disk). */
    public int messageType;

    public WBTrainerMessage() {
    }

    public WBTrainerMessage(int trainerId, int messageType) {
        this.trainerId = trainerId;
        this.messageType = messageType;
    }

    public void Deserialize(DataIOStream in) throws IOException {
        trainerId = in.readUnsignedShort();
        messageType = in.readUnsignedShort();
    }

    public void Serialize(DataIOStream out) throws IOException {
        out.writeShort(trainerId);
        out.writeShort(messageType);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof WBTrainerMessage)) return false;
        WBTrainerMessage other = (WBTrainerMessage) o;
        return trainerId == other.trainerId && messageType == other.messageType;
    }

    @Override
    public int hashCode() {
        return (trainerId << 16) | (messageType & 0xFFFF);
    }

    @Override
    public String toString() {
        return "WBTrainerMessage{trainer=" + trainerId + ", type=" + messageType + "}";
    }
}
