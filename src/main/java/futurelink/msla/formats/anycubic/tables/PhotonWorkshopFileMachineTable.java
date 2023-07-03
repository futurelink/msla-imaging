package futurelink.msla.formats.anycubic.tables;

import com.google.common.io.LittleEndianDataInputStream;
import com.google.common.io.LittleEndianDataOutputStream;
import futurelink.msla.formats.MSLAFileBlockFields;
import lombok.Getter;
import lombok.Setter;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * "MACHINE" section representation.
 */
public class PhotonWorkshopFileMachineTable extends PhotonWorkshopFileTable {

    public static class Fields implements MSLAFileBlockFields {
        @Getter @Setter String MachineName = null;
        @Getter @Setter String LayerImageFormat = "pw0img";
        @Getter @Setter Integer MaxAntialiasingLevel = 16;
        @Getter @Setter Integer PropertyFields = 7;
        @Getter @Setter Float DisplayWidth;
        @Getter @Setter Float DisplayHeight;
        @Getter @Setter Float MachineZ;
        @Getter @Setter Integer MaxFileVersion = 0x0206;
        @Getter @Setter Integer MachineBackground = 6506241;
        @Getter @Setter Float PixelWidthUm;
        @Getter @Setter Float PixelHeightUm;
        @Getter @Setter Integer Padding1;
        @Getter @Setter Integer Padding2;
        @Getter @Setter Integer Padding3;
        @Getter @Setter Integer Padding4;
        @Getter @Setter Integer Padding5;
        @Getter @Setter Integer Padding6;
        @Getter @Setter Integer Padding7;
        @Getter @Setter Integer Padding8;
        @Getter @Setter Integer DisplayCount = 1;
        @Getter @Setter Integer Padding9;
        @Getter @Setter Short ResolutionX;
        @Getter @Setter Short ResolutionY;
        @Getter @Setter Integer Padding10;
        @Getter @Setter Integer Padding11;
        @Getter @Setter Integer Padding12;
        @Getter @Setter Integer Padding13;

        public Fields() {}
        public Fields(Fields source) {
            MachineName = source.MachineName;
            LayerImageFormat = source.LayerImageFormat;
            MaxAntialiasingLevel = source.MaxAntialiasingLevel; PropertyFields = source.PropertyFields;
            DisplayWidth = source.DisplayWidth; DisplayHeight = source.DisplayHeight;
            MachineZ = source.MachineZ; MaxFileVersion = source.MaxFileVersion;
            MachineBackground = source.MachineBackground;
            PixelWidthUm = source.PixelWidthUm; PixelHeightUm = source.PixelHeightUm;
            Padding1 = source.Padding1; Padding2 = source.Padding2; Padding3 = source.Padding3;
            Padding4 = source.Padding4; Padding5 = source.Padding5; Padding6 = source.Padding6;
            Padding7 = source.Padding7; Padding8 = source.Padding8; DisplayCount = source.DisplayCount;
            Padding9 = source.Padding9; ResolutionX = source.ResolutionX; ResolutionY = source.ResolutionY;
            Padding10 = source.Padding10; Padding11 = source.Padding11; Padding12 = source.Padding12;
            Padding13 = source.Padding13;
        }
        //@Override
        //public int getDataLength() { return 0; }
    }

    public static final String Name = "MACHINE";
    private final Fields fields;

    public String getLayerImageFormat() { return fields.getLayerImageFormat(); }

    public PhotonWorkshopFileMachineTable(byte versionMajor, byte versionMinor) {
        super(versionMajor, versionMinor);
        fields = new Fields();
    }
    public PhotonWorkshopFileMachineTable(MSLAFileBlockFields defaults, byte versionMajor, byte versionMinor) {
        super(versionMajor, versionMinor);
        fields = new Fields((Fields) defaults);
    }

    @Override
    int calculateTableLength(byte versionMajor, byte versionMinor) {
        return 156; // for the time being make it constant
    }

    @Override
    public int getDataLength() {
        // Need to subtract 16 to data length because for some reason
        // the value of TableLength is 16 bytes greater than factual table length...
        return calculateTableLength(versionMajor, versionMinor) + MarkLength + 4 - 16;
    }

    @Override
    public void read(FileInputStream stream, int position) throws IOException {
        var fc = stream.getChannel(); fc.position(position);
        var dis = new LittleEndianDataInputStream(stream);
        int dataRead;
        var headerMark = stream.readNBytes(Name.length());
        if (!Arrays.equals(headerMark, Name.getBytes())) {
            throw new IOException("Machine mark not found! Corrupted data.");
        }
        stream.readNBytes(MarkLength - Name.length()); // Skip section name zeroes
        TableLength = dis.readInt();
        fields.MachineName = new String(dis.readNBytes(96), StandardCharsets.US_ASCII).trim();
        fields.LayerImageFormat = new String(dis.readNBytes(16), StandardCharsets.US_ASCII).trim();
        fields.MaxAntialiasingLevel = dis.readInt();
        fields.PropertyFields = dis.readInt();
        fields.DisplayWidth = dis.readFloat();
        fields.DisplayHeight = dis.readFloat();
        fields.MachineZ = dis.readFloat();
        fields.MaxFileVersion = dis.readInt();
        fields.MachineBackground = dis.readInt();
        dataRead = 156; // Assume we read 156 bytes

        if (TableLength >= 160) { fields.PixelWidthUm = dis.readFloat(); dataRead += 4; }
        if (TableLength >= 164) { fields.PixelHeightUm = dis.readFloat(); dataRead += 4; }
        if (TableLength >= 168) { fields.Padding1 = dis.readInt(); dataRead += 4; }
        if (TableLength >= 172) { fields.Padding2 = dis.readInt(); dataRead += 4; }
        if (TableLength >= 176) { fields.Padding3 = dis.readInt(); dataRead += 4; }
        if (TableLength >= 180) { fields.Padding4 = dis.readInt(); dataRead += 4; }
        if (TableLength >= 184) { fields.Padding5 = dis.readInt(); dataRead += 4; }
        if (TableLength >= 188) { fields.Padding6 = dis.readInt(); dataRead += 4; }
        if (TableLength >= 192) { fields.Padding7 = dis.readInt(); dataRead += 4; }
        if (TableLength >= 196) { fields.Padding8 = dis.readInt(); dataRead += 4; }
        if (TableLength >= 200) { fields.DisplayCount = dis.readInt(); dataRead += 4; }
        if (TableLength >= 204) { fields.Padding9 = dis.readInt(); dataRead += 4; }
        if (TableLength >= 206) { fields.ResolutionX = dis.readShort(); dataRead += 2; }
        if (TableLength >= 208) { fields.ResolutionY = dis.readShort(); dataRead += 2; }
        if (TableLength >= 212) { fields.Padding10 = dis.readInt(); dataRead += 4; }
        if (TableLength >= 216) { fields.Padding11 = dis.readInt(); dataRead += 4; }
        if (TableLength >= 220) { fields.Padding12 = dis.readInt(); dataRead += 4; }
        if (TableLength >= 224) { fields.Padding13 = dis.readInt(); dataRead += 4; }

        if (dataRead != TableLength)
            throw new IOException("Machine was not completely read out (" + dataRead + " of " + TableLength +
                    "), some extra data left unread");
    }

    @Override
    public void write(OutputStream stream) throws IOException {
        var dos = new LittleEndianDataOutputStream(stream);
        dos.write(Name.getBytes());
        dos.write(new byte[PhotonWorkshopFileTable.MarkLength - Name.length()]);
        TableLength = calculateTableLength(versionMajor, versionMinor);
        dos.writeInt(TableLength);

        dos.write(fields.MachineName.getBytes());
        dos.write(new byte[96 - fields.MachineName.length()]);
        dos.write(fields.LayerImageFormat.getBytes());
        dos.write(new byte[16 - fields.LayerImageFormat.length()]);

        dos.writeInt(fields.MaxAntialiasingLevel);
        dos.writeInt(fields.PropertyFields);
        dos.writeFloat(fields.DisplayWidth);
        dos.writeFloat(fields.DisplayHeight);
        dos.writeFloat(fields.MachineZ);
        dos.writeInt(fields.MaxFileVersion);
        dos.writeInt(fields.MachineBackground);

        if (TableLength >= 160) dos.writeFloat(fields.PixelWidthUm);
        if (TableLength >= 164) dos.writeFloat(fields.PixelHeightUm);
        if (TableLength >= 168) dos.writeInt(fields.Padding1);
        if (TableLength >= 172) dos.writeInt(fields.Padding2);
        if (TableLength >= 176) dos.writeInt(fields.Padding3);
        if (TableLength >= 180) dos.writeInt(fields.Padding4);
        if (TableLength >= 184) dos.writeInt(fields.Padding5);
        if (TableLength >= 188) dos.writeInt(fields.Padding6);
        if (TableLength >= 192) dos.writeInt(fields.Padding7);
        if (TableLength >= 196) dos.writeInt(fields.Padding8);
        if (TableLength >= 200) dos.writeInt(fields.DisplayCount);
        if (TableLength >= 204) dos.writeInt(fields.Padding9);
        if (TableLength >= 206) dos.writeShort(fields.ResolutionX);
        if (TableLength >= 208) dos.writeShort(fields.ResolutionY);
        if (TableLength >= 212) dos.writeInt(fields.Padding10);
        if (TableLength >= 216) dos.writeInt(fields.Padding11);
        if (TableLength >= 220) dos.writeInt(fields.Padding12);
        if (TableLength >= 224) dos.writeInt(fields.Padding13);
    }

    @Override
    public String toString() {
        String out = "-- Machine data --\n" +
                "Machine name: " + fields.MachineName + "\n" +
                "LayerImageFormat: " + fields.LayerImageFormat + "\n" +
                "MaxAntialiasingLevel: " + fields.MaxAntialiasingLevel + "\n" +
                "PropertyFields: " + fields.PropertyFields + "\n" +
                "DisplayWidth: " + fields.DisplayWidth + "\n" +
                "DisplayHeight: " + fields.DisplayHeight + "\n" +
                "MachineZ: " + fields.MachineZ + "\n" +
                "MaxFileVersion: " + fields.MaxFileVersion + "\n" +
                "MachineBackground: " + fields.MachineBackground + "\n";

        if (TableLength >= 160) { out += "PixelWidthUm: " + fields.PixelWidthUm + "\n"; }
        if (TableLength >= 164) { out += "PixelHeightUm: " + fields.PixelHeightUm + "\n"; }
        if (TableLength >= 168) { out += "Padding1: " + fields.Padding1 + "\n"; }
        if (TableLength >= 172) { out += "Padding2: " + fields.Padding2 + "\n"; }
        if (TableLength >= 176) { out += "Padding3: " + fields.Padding3 + "\n"; }
        if (TableLength >= 180) { out += "Padding4: " + fields.Padding4 + "\n"; }
        if (TableLength >= 184) { out += "Padding5: " + fields.Padding5 + "\n"; }
        if (TableLength >= 188) { out += "Padding6: " + fields.Padding6 + "\n"; }
        if (TableLength >= 192) { out += "Padding7: " + fields.Padding7 + "\n"; }
        if (TableLength >= 196) { out += "Padding8: " + fields.Padding8 + "\n"; }
        if (TableLength >= 200) { out += "DisplayCount: " + fields.DisplayCount + "\n"; }
        if (TableLength >= 204) { out += "Padding9: " + fields.Padding9 + "\n"; }
        if (TableLength >= 206) { out += "ResolutionX: " + fields.ResolutionX + "\n"; }
        if (TableLength >= 208) { out += "ResolutionY: " + fields.ResolutionY + "\n"; }
        if (TableLength >= 212) { out += "Padding10: " + fields.Padding10 + "\n"; }
        if (TableLength >= 216) { out += "Padding11: " + fields.Padding11 + "\n"; }
        if (TableLength >= 220) { out += "Padding12: " + fields.Padding12 + "\n"; }
        if (TableLength >= 224) { out += "Padding13: " + fields.Padding13 + "\n"; }

        return out;
    }
}
