package futurelink.msla.formats.anycubic.tables;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.iface.MSLAFileBlockFields;
import futurelink.msla.formats.iface.MSLAFileDefaults;
import futurelink.msla.formats.iface.MSLAFileField;
import futurelink.msla.formats.iface.MSLAOptionContainer;
import futurelink.msla.formats.utils.FileFieldsReader;
import futurelink.msla.formats.utils.FileFieldsWriter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * "MACHINE" section representation.
 */
@MSLAOptionContainer(className = PhotonWorkshopFileMachineTable.Fields.class)
public class PhotonWorkshopFileMachineTable extends PhotonWorkshopFileTable {
    public static final String Name = "MACHINE";
    @Delegate private final Fields fields;

    @Getter @Setter
    @SuppressWarnings("unused")
    public static class Fields implements MSLAFileBlockFields {
        private final PhotonWorkshopFileTable parent;

        @MSLAFileField(length = MarkLength) private String Name() { return PhotonWorkshopFileMachineTable.Name; }
        // Validation setter checks for what's been read from file
        // and throws an exception when that is something unexpected.
        private void setName(String name) throws MSLAException {
            if (!PhotonWorkshopFileMachineTable.Name.equals(name))
                throw new MSLAException("Table name '" + name + "' is invalid");
        }
        @MSLAFileField(order = 1) private Integer TableLength() { return parent.calculateTableLength(); }
        private void setTableLength(Integer length) { parent.TableLength = length; }
        @MSLAFileField(order = 2, length = 96) private String MachineName = "";
        @MSLAFileField(order = 3, length = 16) private String LayerImageFormat = "pw0img";
        @MSLAFileField(order = 4) private Integer MaxAntialiasingLevel = 16;
        @MSLAFileField(order = 5) private Integer PropertyFields = 7;
        @MSLAFileField(order = 6) private Float DisplayWidth;
        @MSLAFileField(order = 7) private Float DisplayHeight;
        @MSLAFileField(order = 8) private Float MachineZ;
        @MSLAFileField(order = 9) private Integer MaxFileVersion = 0x0206;
        @MSLAFileField(order = 10) private Integer MachineBackground = 6506241;
        @MSLAFileField(order = 11) private Float PixelWidthUm;
        @MSLAFileField(order = 12) private Float PixelHeightUm;
        @MSLAFileField(order = 13) private Integer Padding1 = 0;
        @MSLAFileField(order = 14) private Integer Padding2 = 0;
        @MSLAFileField(order = 15) private Integer Padding3 = 0;
        @MSLAFileField(order = 16) private Integer Padding4 = 0;
        @MSLAFileField(order = 17) private Integer Padding5 = 0;
        @MSLAFileField(order = 18) private Integer Padding6 = 0;
        @MSLAFileField(order = 19) private Integer Padding7 = 0;
        @MSLAFileField(order = 20) private Integer Padding8 = 0;
        @MSLAFileField(order = 21) private Integer DisplayCount = 1;
        @MSLAFileField(order = 22) private Integer Padding9 = 0;
        @MSLAFileField(order = 23) private Short ResolutionX;
        @MSLAFileField(order = 24) private Short ResolutionY;
        @MSLAFileField(order = 25) private Integer Padding10 = 0;
        @MSLAFileField(order = 26) private Integer Padding11 = 0;
        @MSLAFileField(order = 27) private Integer Padding12 = 0;
        @MSLAFileField(order = 28) private Integer Padding13 = 0;

        public Fields(PhotonWorkshopFileTable parent) {
            this.parent = parent;
        }

        @Override
        public boolean isFieldExcluded(String fieldName) {
            return ((TableLength() < 224) && "Padding13".equals(fieldName)) ||
                    ((TableLength() < 220) && "Padding12".equals(fieldName)) ||
                    ((TableLength() < 216) && "Padding11".equals(fieldName)) ||
                    ((TableLength() < 212) && "Padding10".equals(fieldName)) ||
                    ((TableLength() < 208) && "ResolutionY".equals(fieldName)) ||
                    ((TableLength() < 206) && "ResolutionX".equals(fieldName)) ||
                    ((TableLength() < 204) && "Padding9".equals(fieldName)) ||
                    ((TableLength() < 200) && "DisplayCount".equals(fieldName)) ||
                    ((TableLength() < 196) && "Padding8".equals(fieldName)) ||
                    ((TableLength() < 192) && "Padding7".equals(fieldName)) ||
                    ((TableLength() < 188) && "Padding6".equals(fieldName)) ||
                    ((TableLength() < 184) && "Padding5".equals(fieldName)) ||
                    ((TableLength() < 180) && "Padding4".equals(fieldName)) ||
                    ((TableLength() < 176) && "Padding3".equals(fieldName)) ||
                    ((TableLength() < 172) && "Padding2".equals(fieldName)) ||
                    ((TableLength() < 168) && "Padding1".equals(fieldName)) ||
                    ((TableLength() < 164) && "PixelHeightUm".equals(fieldName)) ||
                    ((TableLength() < 160) && "PixelWidthUm".equals(fieldName));
        }
    }

    public String getLayerImageFormat() { return fields.getLayerImageFormat(); }

    public PhotonWorkshopFileMachineTable(byte versionMajor, byte versionMinor) {
        super(versionMajor, versionMinor);
        fields = new Fields(this);
    }
    public PhotonWorkshopFileMachineTable(
            MSLAFileDefaults defaults,
            byte versionMajor,
            byte versionMinor) throws MSLAException
    {
        this(versionMajor, versionMinor);
        defaults.setFields("Machine", fields);
    }

    @Override
    int calculateTableLength() {
        return 156; // for the time being make it constant
    }

    @Override
    public int getDataLength() {
        // Need to subtract 16 from data length because for some reason
        // the value of TableLength is 16 bytes greater than factual table length...
        return calculateTableLength() + MarkLength + 4 - 16;
    }

    @Override
    public void read(FileInputStream stream, int position) throws MSLAException {
        try {
            var reader = new FileFieldsReader(stream, FileFieldsReader.Endianness.LittleEndian);
            var dataRead = reader.read(fields);
            if (dataRead != TableLength) throw new MSLAException(
                    "Machine table was not completely read out (" + dataRead + " of " + TableLength +
                            "), some extra data left unread"
            );
        } catch (IOException e) {
            throw new MSLAException("Error reading Machine table", e);
        }
    }

    @Override
    public void write(OutputStream stream) throws MSLAException {
        TableLength = calculateTableLength();
        try {
            var writer = new FileFieldsWriter(stream, FileFieldsWriter.Endianness.LittleEndian);
            writer.write(fields);
            stream.flush();
        } catch (IOException e) {
            throw new MSLAException("Error writing Machine table", e);
        }
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
