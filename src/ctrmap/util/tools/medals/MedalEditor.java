package ctrmap.util.tools.medals;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import ctrmap.formats.ntr.common.NTRDataIOStream;
import xstandard.fs.FSFile;

public class MedalEditor {
    public static class MedalFile {
        public int medalProgress;
        public int recordID;
        public int recordType;
        public int type;
        public int hintSel;
        public int hintStage;

        private List<Byte> newLeByteArray(int num, int size) {
            byte[] arr = ByteBuffer.allocate(size).order(ByteOrder.LITTLE_ENDIAN).putInt(num).array();
            return IntStream.range(0, arr.length).mapToObj(i -> arr[i]).collect(Collectors.toList());
        }

        public MedalFile(int medalProgress, int recordID, int recordType, int type, int hintSel, int hintStage) {
            this.medalProgress = medalProgress;
            this.recordID = recordID;
            this.recordType = recordType;
            this.type = type;
            this.hintSel = hintSel;
            this.hintStage = hintStage;
        }

        public MedalFile(FSFile file) {
            try (NTRDataIOStream io = new NTRDataIOStream(file)) {
                io.seek(0);
                medalProgress = io.readInt();
                recordID = io.readInt();
                recordType = io.read();
                type = io.read();
                hintSel = io.read();
                hintStage = io.read();

            } catch (IOException ex) {
                Logger.getLogger(MedalFile.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        public MedalFile() {
            this.medalProgress = 0;
            this.recordID = 0x2710;
            this.recordType = 1;
            this.type = 0;
            this.hintSel = 0;
            this.hintStage = 0;
        }

        public List<Byte> toBytes() {
            List<Byte> bytes = new ArrayList<Byte>();

            bytes.addAll(newLeByteArray(medalProgress, 4));
            bytes.addAll(newLeByteArray(recordID, 4));

            bytes.add((byte) (recordType));
            bytes.add((byte) (type));
            bytes.add((byte) (hintSel));
            bytes.add((byte) (hintStage));

            return bytes;
        }

        public boolean equals(MedalFile comp) {
            return this.medalProgress == comp.medalProgress
            && this.recordID == comp.recordID
            && this.recordType == comp.recordType
            && this.type == comp.type
            && this.hintSel == comp.hintSel
            && this.hintStage == comp.hintStage;
        }
    };

    public static void writeMedal(FSFile file, MedalFile medalFile) {
        file.touch(); // Create if not already existing

        try (NTRDataIOStream io = new NTRDataIOStream(file)) {
            io.writeBytes(medalFile.toBytes());
        } catch (IOException ex) {
            Logger.getLogger(VMedals.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static MedalFile readMedal(FSFile file) {
        return new MedalFile(file);
    }

    public static void deleteMedal(FSFile file) {
        file.delete();
    }
}
