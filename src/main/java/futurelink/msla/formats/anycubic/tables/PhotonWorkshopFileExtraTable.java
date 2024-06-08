package futurelink.msla.formats.anycubic.tables;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.iface.MSLAFileBlockFields;
import futurelink.msla.formats.iface.annotations.MSLAFileField;
import futurelink.msla.formats.iface.annotations.MSLAOption;
import lombok.Getter;
import lombok.Setter;

import java.io.OutputStream;

/**
 * "EXTRA" section representation.
 */
@Getter @Setter
public class PhotonWorkshopFileExtraTable extends PhotonWorkshopFileTable {
    private final Fields blockFields;

    @SuppressWarnings("unused")
    @Getter
    public static class Fields implements MSLAFileBlockFields {
        private final PhotonWorkshopFileTable parent;

        @MSLAFileField(length = MarkLength, dontCount = true) private String Name() { return "EXTRA"; }
        // Validation setter checks for what's been read from file
        // and throws an exception when that is something unexpected.
        private void setName(String name) throws MSLAException {
            if (!"EXTRA".equals(name)) throw new MSLAException("Table name '" + name + "' is invalid");
        }
        @MSLAFileField(order = 1, dontCount = true) private Integer TableLength() { return parent.calculateTableLength(); }
        private void setTableLength(Integer length) { parent.TableLength = length; }
        @MSLAFileField(order = 2) @MSLAOption("Bottom layers gradient") Integer BottomGradientSteps = 2;
        @MSLAFileField(order = 3) @MSLAOption("Bottom layers lift height 1") Float BottomLiftHeight1 = 2.0f;
        @MSLAFileField(order = 4) @MSLAOption("Bottom layers lift speed 1") Float BottomLiftSpeed1 = 1.0f;
        @MSLAFileField(order = 5) @MSLAOption("Bottom layers retract speed 1") Float BottomRetractSpeed1 = 2.0f;
        @MSLAFileField(order = 6) @MSLAOption("Bottom layers lift height 2") Float BottomLiftHeight2 = 6.0f;
        @MSLAFileField(order = 7) @MSLAOption("Bottom layers lift speed 2") Float BottomLiftSpeed2 = 4.0f;
        @MSLAFileField(order = 8) @MSLAOption("Bottom layers retract speed 2") Float BottomRetractSpeed2 = 4.0f;

        @MSLAFileField(order = 9) @MSLAOption("Normal layers gradient") Integer NormalGradientSteps = 2;
        @MSLAFileField(order = 10) @MSLAOption("Normal layers lift height 1") Float NormalLiftHeight1 = 2.0f;
        @MSLAFileField(order = 11) @MSLAOption("Normal layers lift speed 1") Float NormalLiftSpeed1 = 2.0f;
        @MSLAFileField(order = 12) @MSLAOption("Normal layers retract speed 1") Float NormalRetractSpeed1 = 2.0f;
        @MSLAFileField(order = 13) @MSLAOption("Normal layers lift height 2") Float NormalLiftHeight2 = 6.0f;
        @MSLAFileField(order = 14) @MSLAOption("Normal layers lift speed 2") Float NormalLiftSpeed2 = 4.0f;
        @MSLAFileField(order = 15) @MSLAOption("Normal layers retract speed 2") Float NormalRetractSpeed2 = 4.0f;

        public Fields(PhotonWorkshopFileTable parent) { this.parent = parent; }
    }

    public PhotonWorkshopFileExtraTable(byte versionMajor, byte versionMinor) {
        super(versionMajor, versionMinor);
        Name = "Extra";
        TableLength = 24; // Constant that doesn't mean anything...
        blockFields = new Fields(this);
    }

    @Override
    int calculateTableLength() {
        return TableLength;
    }

    @Override
    public final void write(OutputStream stream) throws MSLAException {
        TableLength = calculateTableLength();
        super.write(stream);
    }

    @Override
    public String toString() { return "-- Extra table --" + "\n" + blockFields.fieldsAsString(" = ", "\n"); }
}
