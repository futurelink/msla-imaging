package futurelink.msla.formats.chitubox.tables;

import futurelink.msla.formats.iface.MSLAFileBlockFields;
import futurelink.msla.formats.iface.annotations.MSLAFileField;
import futurelink.msla.formats.iface.annotations.MSLAOption;
import futurelink.msla.formats.io.FileFieldsException;
import futurelink.msla.formats.io.FileFieldsIO;
import lombok.Getter;
import lombok.Setter;

@Getter
public class CTBFilePrintParams extends CTBFileBlock {
    private final String OPTIONS_SECTION_NAME = "PrintParams";
    private final Fields fileFields;

    @Getter
    @SuppressWarnings("unused")
    public static class Fields implements MSLAFileBlockFields {
        @MSLAFileField @MSLAOption(MSLAOption.BottomLiftHeight) private Float BottomLiftHeight;
        @MSLAFileField(order = 1) @MSLAOption(MSLAOption.BottomLiftSpeed) private Float BottomLiftSpeed;
        @MSLAFileField(order = 2) @MSLAOption(MSLAOption.LiftHeight) private Float LiftHeight;
        @MSLAFileField(order = 3) @MSLAOption(MSLAOption.LiftSpeed) private Float LiftSpeed;
        @MSLAFileField(order = 4) @MSLAOption(MSLAOption.RetractSpeed) private Float RetractSpeed;
        @MSLAFileField(order = 5) @MSLAOption(MSLAOption.Volume) @Setter private Float VolumeMl = 0.0f;
        @MSLAFileField(order = 6) @MSLAOption(MSLAOption.Weight) @Setter private Float WeightG = 0.0f;
        @MSLAFileField(order = 7) @Setter private Float CostDollars = 0.0f;
        @MSLAFileField(order = 8) @MSLAOption("Bottom layers light off delay") private final Float BottomLightOffDelay = 0.0f;
        @MSLAFileField(order = 9) @MSLAOption("Normal layers light off delay")  private final Float LightOffDelay = 0.0f;
        @MSLAFileField(order = 10) @MSLAOption(MSLAOption.BottomLayersCount) private Integer BottomLayerCount;
        @MSLAFileField(order = 11) private final Integer Padding1 = 0;
        @MSLAFileField(order = 12) private final Integer Padding2 = 0;
        @MSLAFileField(order = 13) private final Integer Padding3 = 0;
        @MSLAFileField(order = 14) private final Integer Padding4 = 0;
    }

    public CTBFilePrintParams(int version) {
        super(version);
        fileFields = new Fields();
    }

    @Override public String getName() { return OPTIONS_SECTION_NAME; }
    @Override public int getDataLength() throws FileFieldsException { return FileFieldsIO.getBlockLength(this); }
    @Override
    public int getDataFieldOffset(String fieldName) throws FileFieldsException {
        return FileFieldsIO.getBlockLength(this.getFileFields(), fieldName);
    }
    @Override public String toString() { return fileFields.fieldsAsString(" = ", "\n"); }
}
