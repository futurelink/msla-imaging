package futurelink.msla.formats.anycubic.tables;

import com.google.common.collect.Table;
import com.google.common.io.LittleEndianDataInputStream;
import com.google.common.io.LittleEndianDataOutputStream;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.Arrays;

/**
 * "EXTRA" section representation.
 */
public class PhotonWorkshopFileExtraTable extends PhotonWorkshopFileTable {
    public static final String Name = "EXTRA";
    @Getter @Setter int BottomLiftCount = 2;
    @Getter @Setter float BottomLiftHeight1 = 2.0f;
    @Getter @Setter float BottomLiftSpeed1 = 1.0f;
    @Getter @Setter float BottomRetractSpeed2 = 4.0f;
    @Getter @Setter float BottomLiftHeight2 = 6.0f;
    @Getter @Setter float BottomLiftSpeed2 = 4.0f;
    @Getter @Setter float BottomRetractSpeed1 = 2.0f;
    @Getter @Setter int NormalLiftCount = 2;
    @Getter @Setter float LiftHeight1 = 2.0f;
    @Getter @Setter float LiftSpeed1 = 2.0f;
    @Getter @Setter float RetractSpeed2 = 4.0f;
    @Getter @Setter float LiftHeight2 = 6.0f;
    @Getter @Setter float LiftSpeed2 = 4.0f;
    @Getter @Setter float RetractSpeed1 = 2.0f;

    public PhotonWorkshopFileExtraTable() {
        TableLength = 24; // Constant that doesn't mean anything...
    }

    @Override
    int calculateTableLength(byte versionMajor, byte versionMinor) {
        return TableLength;
    }

    @Override
    public int calculateDataLength(byte versionMajor, byte versionMinor) {
        // 14 fields of 4 bytes + Mark length + 4 bytes for table length
        return 56 + MarkLength + 4;
    }

    @Override
    public void read(LittleEndianDataInputStream stream) throws IOException {
        var headerMark = stream.readNBytes(Name.length());
        if (!Arrays.equals(headerMark, Name.getBytes())) {
            throw new IOException("Extra definition mark not found! Corrupted data.");
        }
        stream.readNBytes(MarkLength - Name.length()); // Skip section name zeroes
        TableLength = stream.readInt();

        BottomLiftCount = stream.readInt();
        BottomLiftHeight1 = stream.readFloat();
        BottomLiftSpeed1 = stream.readFloat();
        BottomRetractSpeed2 = stream.readFloat();
        BottomLiftHeight2 = stream.readFloat();
        BottomLiftSpeed2 = stream.readFloat();
        BottomRetractSpeed1 = stream.readFloat();

        NormalLiftCount = stream.readInt();
        LiftHeight1 = stream.readFloat();
        LiftSpeed1 = stream.readFloat();
        RetractSpeed2 = stream.readFloat();
        LiftHeight2 = stream.readFloat();
        LiftSpeed2 = stream.readFloat();
        RetractSpeed1 = stream.readFloat();
    }

    @Override
    public void write(LittleEndianDataOutputStream stream, byte versionMajor, byte versionMinor) throws IOException {
        stream.write(Name.getBytes());
        stream.write(new byte[PhotonWorkshopFileTable.MarkLength - Name.length()]);
        TableLength = calculateTableLength(versionMajor, versionMinor);
        stream.writeInt(TableLength);
        stream.writeInt(BottomLiftCount);
        stream.writeFloat(BottomLiftHeight1);
        stream.writeFloat(BottomLiftSpeed1);
        stream.writeFloat(BottomRetractSpeed2);
        stream.writeFloat(BottomLiftHeight2);
        stream.writeFloat(BottomLiftSpeed2);
        stream.writeFloat(BottomRetractSpeed1);
        stream.writeInt(NormalLiftCount);
        stream.writeFloat(LiftHeight1);
        stream.writeFloat(LiftSpeed1);
        stream.writeFloat(RetractSpeed2);
        stream.writeFloat(LiftHeight2);
        stream.writeFloat(LiftSpeed2);
        stream.writeFloat(RetractSpeed1);
    }

    @Override
    public String toString() {
        return "-- Extra table --" + "\n" +
                "BottomLiftCount: " + BottomLiftCount + "\n" +
                "BottomLiftHeight1: " + BottomLiftHeight1 + "\n" +
                "BottomLiftSpeed1: " + BottomLiftSpeed1 + "\n" +
                "BottomRetractSpeed2: " + BottomRetractSpeed2 + "\n" +
                "BottomLiftHeight2: " + BottomLiftHeight2 + "\n" +
                "BottomLiftSpeed2: " + BottomLiftSpeed2 + "\n" +
                "BottomRetractSpeed1: " + BottomRetractSpeed1 + "\n" +
                "NormalLiftCount: " + NormalLiftCount + "\n" +
                "LiftHeight1: " + LiftHeight1 + "\n" +
                "LiftSpeed1: " + LiftSpeed1 + "\n" +
                "RetractSpeed2: " + RetractSpeed2 + "\n" +
                "LiftHeight2: " + LiftHeight2 + "\n" +
                "LiftSpeed2: " + LiftSpeed2 + "\n" +
                "RetractSpeed1: " + RetractSpeed1 + "\n";
    }
}
