package futurelink.msla.formats.anycubic.tables;

import com.google.common.io.LittleEndianDataInputStream;
import com.google.common.io.LittleEndianDataOutputStream;
import futurelink.msla.formats.iface.MSLAOption;
import futurelink.msla.formats.iface.MSLAOptionContainer;
import lombok.Getter;
import lombok.Setter;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * "EXTRA" section representation.
 */
@MSLAOptionContainer(className = PhotonWorkshopFileExtraTable.class)
public class PhotonWorkshopFileExtraTable extends PhotonWorkshopFileTable {
    public static final String Name = "EXTRA";
    @MSLAOption @Getter @Setter Integer BottomLiftCount = 2;
    @MSLAOption @Getter @Setter Float BottomLiftHeight1 = 2.0f;
    @MSLAOption @Getter @Setter Float BottomLiftSpeed1 = 1.0f;
    @MSLAOption @Getter @Setter Float BottomRetractSpeed2 = 4.0f;
    @MSLAOption @Getter @Setter Float BottomLiftHeight2 = 6.0f;
    @MSLAOption @Getter @Setter Float BottomLiftSpeed2 = 4.0f;
    @MSLAOption @Getter @Setter Float BottomRetractSpeed1 = 2.0f;
    @MSLAOption @Getter @Setter Integer NormalLiftCount = 2;
    @MSLAOption @Getter @Setter Float LiftHeight1 = 2.0f;
    @MSLAOption @Getter @Setter Float LiftSpeed1 = 2.0f;
    @MSLAOption @Getter @Setter Float RetractSpeed2 = 4.0f;
    @MSLAOption @Getter @Setter Float LiftHeight2 = 6.0f;
    @MSLAOption @Getter @Setter Float LiftSpeed2 = 4.0f;
    @MSLAOption @Getter @Setter Float RetractSpeed1 = 2.0f;

    public PhotonWorkshopFileExtraTable(byte versionMajor, byte versionMinor) {
        super(versionMajor, versionMinor);
        TableLength = 24; // Constant that doesn't mean anything...
    }

    @Override
    int calculateTableLength(byte versionMajor, byte versionMinor) {
        return TableLength;
    }

    @Override
    public int getDataLength() {
        // 14 fields of 4 bytes + Mark length + 4 bytes for table length
        return 56 + MarkLength + 4;
    }

    @Override
    public void read(FileInputStream stream, int position) throws IOException {
        var fc = stream.getChannel(); fc.position(position);
        var dis = new LittleEndianDataInputStream(stream);

        var headerMark = dis.readNBytes(Name.length());
        if (!Arrays.equals(headerMark, Name.getBytes())) {
            throw new IOException("Extra definition mark not found at " + position + "! Corrupted data.");
        }
        dis.readNBytes(MarkLength - Name.length()); // Skip section name zeroes
        TableLength = dis.readInt();

        BottomLiftCount = dis.readInt();
        BottomLiftHeight1 = dis.readFloat();
        BottomLiftSpeed1 = dis.readFloat();
        BottomRetractSpeed2 = dis.readFloat();
        BottomLiftHeight2 = dis.readFloat();
        BottomLiftSpeed2 = dis.readFloat();
        BottomRetractSpeed1 = dis.readFloat();

        NormalLiftCount = dis.readInt();
        LiftHeight1 = dis.readFloat();
        LiftSpeed1 = dis.readFloat();
        RetractSpeed2 = dis.readFloat();
        LiftHeight2 = dis.readFloat();
        LiftSpeed2 = dis.readFloat();
        RetractSpeed1 = dis.readFloat();
    }

    @Override
    public final void write(OutputStream stream) throws IOException {
        var dos = new LittleEndianDataOutputStream(stream);
        dos.write(Name.getBytes());
        dos.write(new byte[PhotonWorkshopFileTable.MarkLength - Name.length()]);
        TableLength = calculateTableLength(versionMajor, versionMinor);
        dos.writeInt(TableLength);
        dos.writeInt(BottomLiftCount);
        dos.writeFloat(BottomLiftHeight1);
        dos.writeFloat(BottomLiftSpeed1);
        dos.writeFloat(BottomRetractSpeed2);
        dos.writeFloat(BottomLiftHeight2);
        dos.writeFloat(BottomLiftSpeed2);
        dos.writeFloat(BottomRetractSpeed1);
        dos.writeInt(NormalLiftCount);
        dos.writeFloat(LiftHeight1);
        dos.writeFloat(LiftSpeed1);
        dos.writeFloat(RetractSpeed2);
        dos.writeFloat(LiftHeight2);
        dos.writeFloat(LiftSpeed2);
        dos.writeFloat(RetractSpeed1);
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
