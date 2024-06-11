package futurelink.msla.formats.anycubic.tables;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.iface.MSLAFileBlockFields;
import futurelink.msla.formats.iface.MSLAFileField;
import futurelink.msla.formats.iface.options.MSLAOption;
import futurelink.msla.formats.iface.options.MSLAOptionName;
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

        @MSLAFileField(order = 2) @MSLAOption(MSLAOptionName.BottomLayersGradient) private Integer BottomGradientSteps;
        @MSLAFileField(order = 3) @MSLAOption(MSLAOptionName.BottomLayersLiftHeight1) private Float BottomLiftHeight1;
        @MSLAFileField(order = 4) @MSLAOption(MSLAOptionName.BottomLayersLiftSpeed1) private Float BottomLiftSpeed1;
        @MSLAFileField(order = 5) @MSLAOption(MSLAOptionName.BottomLayersRetractSpeed1) private Float BottomRetractSpeed1;
        @MSLAFileField(order = 6) @MSLAOption(MSLAOptionName.BottomLayersLiftHeight2) private Float BottomLiftHeight2;
        @MSLAFileField(order = 7) @MSLAOption(MSLAOptionName.BottomLayersLiftSpeed2) private Float BottomLiftSpeed2;
        @MSLAFileField(order = 8) @MSLAOption(MSLAOptionName.BottomLayersRetractSpeed2) private Float BottomRetractSpeed2;

        @MSLAFileField(order = 9) @MSLAOption(MSLAOptionName.NormalLayersGradient) private Integer NormalGradientSteps;
        @MSLAFileField(order = 10) @MSLAOption(MSLAOptionName.NormalLayersLiftHeight1) private Float NormalLiftHeight1;
        @MSLAFileField(order = 11) @MSLAOption(MSLAOptionName.NormalLayersLiftSpeed1) private Float NormalLiftSpeed1;
        @MSLAFileField(order = 12) @MSLAOption(MSLAOptionName.NormalLayersRetractSpeed1) private Float NormalRetractSpeed1;
        @MSLAFileField(order = 13) @MSLAOption(MSLAOptionName.NormalLayersLiftHeight2) private Float NormalLiftHeight2;
        @MSLAFileField(order = 14) @MSLAOption(MSLAOptionName.NormalLayersLiftSpeed2) private Float NormalLiftSpeed2;
        @MSLAFileField(order = 15) @MSLAOption(MSLAOptionName.NormalLayersRetractSpeed2) Float NormalRetractSpeed2;

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
