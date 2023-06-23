package futurelink.msla.formats.anycubic.tables;

import com.google.common.io.LittleEndianDataInputStream;
import com.google.common.io.LittleEndianDataOutputStream;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * "MACHINE" section representation.
 */
public class PhotonWorkshopFileMachineTable extends PhotonWorkshopFileTable {
    public static class Fields {
        @Getter @Setter String MachineName = null;
        @Getter @Setter String LayerImageFormat = "pw0img";
        @Getter @Setter int MaxAntialiasingLevel = 16;
        @Getter @Setter int PropertyFields = 7;
        @Getter @Setter float DisplayWidth;
        @Getter @Setter float DisplayHeight;
        @Getter @Setter float MachineZ;
        @Getter @Setter int MaxFileVersion = 0x0206;
        @Getter @Setter int MachineBackground = 6506241;
        @Getter @Setter float PixelWidthUm;
        @Getter @Setter float PixelHeightUm;
        @Getter @Setter int Padding1;
        @Getter @Setter int Padding2;
        @Getter @Setter int Padding3;
        @Getter @Setter int Padding4;
        @Getter @Setter int Padding5;
        @Getter @Setter int Padding6;
        @Getter @Setter int Padding7;
        @Getter @Setter int Padding8;
        @Getter @Setter int DisplayCount = 1;
        @Getter @Setter int Padding9;
        @Getter @Setter short ResolutionX ;
        @Getter @Setter short ResolutionY;
        @Getter @Setter int Padding10;
        @Getter @Setter int Padding11;
        @Getter @Setter int Padding12;
        @Getter @Setter int Padding13;

        public static Fields copyOf(Fields source) {
            var f = new Fields();
            f.MachineName = source.MachineName;
            f.LayerImageFormat = source.LayerImageFormat;
            f.MaxAntialiasingLevel = source.MaxAntialiasingLevel; f.PropertyFields = source.PropertyFields;
            f.DisplayWidth = source.DisplayWidth; f.DisplayHeight = source.DisplayHeight;
            f.MachineZ = source.MachineZ; f.MaxFileVersion = source.MaxFileVersion;
            f.MachineBackground = source.MachineBackground;
            f.PixelWidthUm = source.PixelWidthUm; f.PixelHeightUm = source.PixelHeightUm;
            f.Padding1 = source.Padding1; f.Padding2 = source.Padding2; f.Padding3 = source.Padding3;
            f.Padding4 = source.Padding4; f.Padding5 = source.Padding5; f.Padding6 = source.Padding6;
            f.Padding7 = source.Padding7; f.Padding8 = source.Padding8; f.DisplayCount = source.DisplayCount;
            f.Padding9 = source.Padding9; f.ResolutionX = source.ResolutionX; f.ResolutionY = source.ResolutionY;
            f.Padding10 = source.Padding10; f.Padding11 = source.Padding11; f.Padding12 = source.Padding12;
            f.Padding13 = source.Padding13;
            return f;
        }
    }

    public static final String Name = "MACHINE";
    private final Fields fields;

    public PhotonWorkshopFileMachineTable() {
        fields = new Fields();
    }
    public PhotonWorkshopFileMachineTable(Fields defaults) {
        fields = Fields.copyOf(defaults);
    }

    @Override
    int calculateTableLength(byte versionMajor, byte versionMinor) {
        return 156; // for the time being make it constant
    }

    @Override
    public int calculateDataLength(byte versionMajor, byte versionMinor) {
        // Need to subtract 16 to data length because for some reason
        // the value of TableLength is 16 bytes greater than factual table length...
        return calculateTableLength(versionMajor, versionMinor) + MarkLength + 4 - 16;
    }

    @Override
    public void read(LittleEndianDataInputStream stream) throws IOException {
        int dataRead;
        var headerMark = stream.readNBytes(Name.length());
        if (!Arrays.equals(headerMark, Name.getBytes())) {
            throw new IOException("Machine mark not found! Corrupted data.");
        }
        stream.readNBytes(MarkLength - Name.length()); // Skip section name zeroes
        TableLength = stream.readInt();
        fields.MachineName = new String(stream.readNBytes(96), StandardCharsets.US_ASCII);
        fields.LayerImageFormat = new String(stream.readNBytes(16), StandardCharsets.US_ASCII);
        fields.MaxAntialiasingLevel = stream.readInt();
        fields.PropertyFields = stream.readInt();
        fields.DisplayWidth = stream.readFloat();
        fields.DisplayHeight = stream.readFloat();
        fields.MachineZ = stream.readFloat();
        fields.MaxFileVersion = stream.readInt();
        fields.MachineBackground = stream.readInt();
        dataRead = 156; // Assume we read 156 bytes

        if (TableLength >= 160) { fields.PixelWidthUm = stream.readFloat(); dataRead += 4; }
        if (TableLength >= 164) { fields.PixelHeightUm = stream.readFloat(); dataRead += 4; }
        if (TableLength >= 168) { fields.Padding1 = stream.readInt(); dataRead += 4; }
        if (TableLength >= 172) { fields.Padding2 = stream.readInt(); dataRead += 4; }
        if (TableLength >= 176) { fields.Padding3 = stream.readInt(); dataRead += 4; }
        if (TableLength >= 180) { fields.Padding4 = stream.readInt(); dataRead += 4; }
        if (TableLength >= 184) { fields.Padding5 = stream.readInt(); dataRead += 4; }
        if (TableLength >= 188) { fields.Padding6 = stream.readInt(); dataRead += 4; }
        if (TableLength >= 192) { fields.Padding7 = stream.readInt(); dataRead += 4; }
        if (TableLength >= 196) { fields.Padding8 = stream.readInt(); dataRead += 4; }
        if (TableLength >= 200) { fields.DisplayCount = stream.readInt(); dataRead += 4; }
        if (TableLength >= 204) { fields.Padding9 = stream.readInt(); dataRead += 4; }
        if (TableLength >= 206) { fields.ResolutionX = stream.readShort(); dataRead += 2; }
        if (TableLength >= 208) { fields.ResolutionY = stream.readShort(); dataRead += 2; }
        if (TableLength >= 212) { fields.Padding10 = stream.readInt(); dataRead += 4; }
        if (TableLength >= 216) { fields.Padding11 = stream.readInt(); dataRead += 4; }
        if (TableLength >= 220) { fields.Padding12 = stream.readInt(); dataRead += 4; }
        if (TableLength >= 224) { fields.Padding13 = stream.readInt(); dataRead += 4; }

        if (dataRead != TableLength)
            throw new IOException("Machine was not completely read out (" + dataRead + " of " + TableLength +
                    "), some extra data left unread");
    }

    @Override
    public void write(LittleEndianDataOutputStream stream, byte versionMajor, byte versionMinor) throws IOException {
        stream.write(Name.getBytes());
        stream.write(new byte[PhotonWorkshopFileTable.MarkLength - Name.length()]);
        TableLength = calculateTableLength(versionMajor, versionMinor);
        stream.writeInt(TableLength);

        stream.write(fields.MachineName.getBytes());
        stream.write(new byte[96 - fields.MachineName.length()]);
        stream.write(fields.LayerImageFormat.getBytes());
        stream.write(new byte[16 - fields.LayerImageFormat.length()]);

        stream.writeInt(fields.MaxAntialiasingLevel);
        stream.writeInt(fields.PropertyFields);
        stream.writeFloat(fields.DisplayWidth);
        stream.writeFloat(fields.DisplayHeight);
        stream.writeFloat(fields.MachineZ);
        stream.writeInt(fields.MaxFileVersion);
        stream.writeInt(fields.MachineBackground);

        if (TableLength >= 160) stream.writeFloat(fields.PixelWidthUm);
        if (TableLength >= 164) stream.writeFloat(fields.PixelHeightUm);
        if (TableLength >= 168) stream.writeInt(fields.Padding1);
        if (TableLength >= 172) stream.writeInt(fields.Padding2);
        if (TableLength >= 176) stream.writeInt(fields.Padding3);
        if (TableLength >= 180) stream.writeInt(fields.Padding4);
        if (TableLength >= 184) stream.writeInt(fields.Padding5);
        if (TableLength >= 188) stream.writeInt(fields.Padding6);
        if (TableLength >= 192) stream.writeInt(fields.Padding7);
        if (TableLength >= 196) stream.writeInt(fields.Padding8);
        if (TableLength >= 200) stream.writeInt(fields.DisplayCount);
        if (TableLength >= 204) stream.writeInt(fields.Padding9);
        if (TableLength >= 206) stream.writeShort(fields.ResolutionX);
        if (TableLength >= 208) stream.writeShort(fields.ResolutionY);
        if (TableLength >= 212) stream.writeInt(fields.Padding10);
        if (TableLength >= 216) stream.writeInt(fields.Padding11);
        if (TableLength >= 220) stream.writeInt(fields.Padding12);
        if (TableLength >= 224) stream.writeInt(fields.Padding13);
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
