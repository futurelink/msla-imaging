package futurelink.msla.formats.anycubic.tables;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.iface.MSLAFileBlockFields;
import futurelink.msla.formats.iface.MSLAFileDefaults;
import futurelink.msla.formats.iface.annotations.MSLAFileField;
import futurelink.msla.formats.iface.annotations.MSLAOptionContainer;
import futurelink.msla.formats.utils.FileFieldsIO;
import futurelink.msla.formats.utils.FileFieldsReader;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * "MACHINE" section representation.
 */
@Getter
@MSLAOptionContainer(PhotonWorkshopFileMachineTable.Fields.class)
public class PhotonWorkshopFileMachineTable extends PhotonWorkshopFileTable {
    private static final String OPTIONS_SECTION_NAME = "Machine";
    @Delegate private final Fields fields;

    @Getter @Setter
    @SuppressWarnings("unused")
    public static class Fields implements MSLAFileBlockFields {
        private final PhotonWorkshopFileTable parent;

        @MSLAFileField(length = MarkLength) private String Name() { return "MACHINE"; }
        // Validation setter checks for what's been read from file
        // and throws an exception when that is something unexpected.
        private void setName(String name) throws MSLAException {
            if (!"MACHINE".equals(name)) throw new MSLAException("Table name '" + name + "' is invalid");
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
        defaults.setFields(OPTIONS_SECTION_NAME, fields);
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
    public long read(FileInputStream stream, long position) throws MSLAException {
        try {
            var reader = new FileFieldsReader(stream, FileFieldsIO.Endianness.LittleEndian);
            var dataRead = reader.read(this);
            if (dataRead != TableLength) throw new MSLAException(
                    "Machine table was not completely read out (" + dataRead + " of " + TableLength +
                            "), some extra data left unread"
            );
            return dataRead;
        } catch (IOException e) {
            throw new MSLAException("Error reading Machine table", e);
        }
    }

    @Override
    public void write(OutputStream stream) throws MSLAException {
        TableLength = calculateTableLength();
        super.write(stream);
    }

    @Override
    public String toString() { return "-- Machine data --\n" + fields.fieldsAsString(" = ", "\n"); }
}
