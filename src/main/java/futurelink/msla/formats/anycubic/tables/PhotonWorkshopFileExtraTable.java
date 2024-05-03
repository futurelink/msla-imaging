package futurelink.msla.formats.anycubic.tables;

import com.google.common.io.LittleEndianDataInputStream;
import com.google.common.io.LittleEndianDataOutputStream;
import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.iface.MSLAFileBlockFields;
import futurelink.msla.formats.iface.MSLAFileField;
import futurelink.msla.formats.iface.MSLAOption;
import futurelink.msla.formats.iface.MSLAOptionContainer;
import futurelink.msla.formats.utils.FileFieldsReader;
import futurelink.msla.formats.utils.FileFieldsWriter;
import lombok.Getter;
import lombok.Setter;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * "EXTRA" section representation.
 */
@MSLAOptionContainer(className = PhotonWorkshopFileExtraTable.Fields.class)
@Getter @Setter
public class PhotonWorkshopFileExtraTable extends PhotonWorkshopFileTable {
    public static final String Name = "EXTRA";
    private final Fields fields;

    @SuppressWarnings("unused")
    static class Fields implements MSLAFileBlockFields {
        private final PhotonWorkshopFileTable parent;

        @MSLAFileField(length = MarkLength, dontCount = true) private String Name() { return PhotonWorkshopFileExtraTable.Name; }
        // Validation setter checks for what's been read from file
        // and throws an exception when that is something unexpected.
        private void setName(String name) throws MSLAException {
            if (!PhotonWorkshopFileExtraTable.Name.equals(name))
                throw new MSLAException("Table name '" + name + "' is invalid");
        }
        @MSLAFileField(order = 1, dontCount = true) private Integer TableLength() { return parent.calculateTableLength(); }
        private void setTableLength(Integer length) { parent.TableLength = length; }
        @MSLAFileField(order = 2) @MSLAOption Integer BottomLiftCount = 2;
        @MSLAFileField(order = 3) @MSLAOption Float BottomLiftHeight1 = 2.0f;
        @MSLAFileField(order = 4) @MSLAOption Float BottomLiftSpeed1 = 1.0f;
        @MSLAFileField(order = 5) @MSLAOption Float BottomRetractSpeed2 = 4.0f;
        @MSLAFileField(order = 6) @MSLAOption Float BottomLiftHeight2 = 6.0f;
        @MSLAFileField(order = 7) @MSLAOption Float BottomLiftSpeed2 = 4.0f;
        @MSLAFileField(order = 8) @MSLAOption Float BottomRetractSpeed1 = 2.0f;
        @MSLAFileField(order = 9) @MSLAOption Integer NormalLiftCount = 2;
        @MSLAFileField(order = 10) @MSLAOption Float LiftHeight1 = 2.0f;
        @MSLAFileField(order = 11) @MSLAOption Float LiftSpeed1 = 2.0f;
        @MSLAFileField(order = 12) @MSLAOption Float RetractSpeed2 = 4.0f;
        @MSLAFileField(order = 13) @MSLAOption Float LiftHeight2 = 6.0f;
        @MSLAFileField(order = 14) @MSLAOption Float LiftSpeed2 = 4.0f;
        @MSLAFileField(order = 15) @MSLAOption Float RetractSpeed1 = 2.0f;

        public Fields(PhotonWorkshopFileTable parent) { this.parent = parent; }
    }

    public PhotonWorkshopFileExtraTable(byte versionMajor, byte versionMinor) {
        super(versionMajor, versionMinor);
        TableLength = 24; // Constant that doesn't mean anything...
        fields = new Fields(this);
    }

    @Override
    int calculateTableLength() {
        return TableLength;
    }

    @Override
    public int getDataLength() {
        // 14 fields of 4 bytes + Mark length + 4 bytes for table length
        return 56 + MarkLength + 4;
    }

    @Override
    public void read(FileInputStream stream, int position) throws MSLAException {
        try {
            var reader = new FileFieldsReader(stream, FileFieldsReader.Endianness.LittleEndian);
            reader.read(fields);
        } catch (IOException e) { throw new MSLAException("Error reading Extra table", e); }
    }

    @Override
    public final void write(OutputStream stream) throws MSLAException {
        TableLength = calculateTableLength();
        try {
            var writer = new FileFieldsWriter(stream, FileFieldsWriter.Endianness.LittleEndian);
            writer.write(fields);
            stream.flush();
        } catch (IOException e) {
            throw new MSLAException("Error writing Extra table", e);
        }
    }

    @Override
    public String toString() {
        return "-- Extra table --" + "\n" +
                "BottomLiftCount: " + fields.BottomLiftCount + "\n" +
                "BottomLiftHeight1: " + fields.BottomLiftHeight1 + "\n" +
                "BottomLiftSpeed1: " + fields.BottomLiftSpeed1 + "\n" +
                "BottomRetractSpeed2: " + fields.BottomRetractSpeed2 + "\n" +
                "BottomLiftHeight2: " + fields.BottomLiftHeight2 + "\n" +
                "BottomLiftSpeed2: " + fields.BottomLiftSpeed2 + "\n" +
                "BottomRetractSpeed1: " + fields.BottomRetractSpeed1 + "\n" +
                "NormalLiftCount: " + fields.NormalLiftCount + "\n" +
                "LiftHeight1: " + fields.LiftHeight1 + "\n" +
                "LiftSpeed1: " + fields.LiftSpeed1 + "\n" +
                "RetractSpeed2: " + fields.RetractSpeed2 + "\n" +
                "LiftHeight2: " + fields.LiftHeight2 + "\n" +
                "LiftSpeed2: " + fields.LiftSpeed2 + "\n" +
                "RetractSpeed1: " + fields.RetractSpeed1 + "\n";
    }
}
