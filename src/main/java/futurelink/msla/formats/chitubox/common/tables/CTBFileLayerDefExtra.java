package futurelink.msla.formats.chitubox.common.tables;

import futurelink.msla.formats.iface.MSLAFileBlock;
import futurelink.msla.formats.iface.MSLAFileBlockFields;
import futurelink.msla.formats.iface.annotations.MSLAFileField;
import futurelink.msla.formats.iface.annotations.MSLAOption;
import futurelink.msla.formats.io.FileFieldsException;
import futurelink.msla.formats.io.FileFieldsIO;
import lombok.Getter;
import lombok.Setter;

@Getter
public class CTBFileLayerDefExtra implements MSLAFileBlock {
    private final Fields fileFields = new Fields();
    public static final int TABLE_SIZE = 48;

    @Getter
    @Setter
    @SuppressWarnings("unused")
    public static class Fields implements MSLAFileBlockFields {
        @MSLAFileField private Integer TotalSize;
        @MSLAFileField(order = 1) @MSLAOption(MSLAOption.LiftHeight) private Float LiftHeight;
        @MSLAFileField(order = 2) @MSLAOption(MSLAOption.LiftSpeed) private Float LiftSpeed;
        @MSLAFileField(order = 3) @MSLAOption("Lift height 2") private Float LiftHeight2;
        @MSLAFileField(order = 4) @MSLAOption("Lift speed 2") private Float LiftSpeed2;
        @MSLAFileField(order = 5) @MSLAOption(MSLAOption.RetractSpeed) private Float RetractSpeed;
        @MSLAFileField(order = 6) @MSLAOption("Retract height 2") private Float RetractHeight2;
        @MSLAFileField(order = 7) @MSLAOption("Retract speed 2") private Float RetractSpeed2;
        @MSLAFileField(order = 8) @MSLAOption("Rest time before lift") private Float RestTimeBeforeLift;
        @MSLAFileField(order = 9) @MSLAOption("Rest time after lift") private Float RestTimeAfterLift;
        @MSLAFileField(order = 10) @MSLAOption("Rest time after retract") private Float RestTimeAfterRetract;
        @MSLAFileField(order = 11) @MSLAOption("Light PWM") private Float LightPWM;
    }

    @Override public String getName() { return null; }
    @Override public int getDataLength() { return TABLE_SIZE; }
    @Override
    public int getDataFieldOffset(String fieldName) throws FileFieldsException {
        return FileFieldsIO.getBlockLength(this.getFileFields(), fieldName);
    }
    @Override public String toString() { return "{ " + fileFields.fieldsAsString(":", ", ") +" }"; }
}
