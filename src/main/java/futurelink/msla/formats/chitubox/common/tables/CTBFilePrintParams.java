package futurelink.msla.formats.chitubox.common.tables;

import futurelink.msla.formats.iface.MSLAFileBlockFields;
import futurelink.msla.formats.iface.MSLAFileField;
import futurelink.msla.formats.iface.options.MSLAOption;
import futurelink.msla.formats.iface.options.MSLAOptionName;
import futurelink.msla.formats.io.FileFieldsException;
import futurelink.msla.formats.io.FileFieldsIO;
import lombok.Getter;
import lombok.Setter;

@Getter
public class CTBFilePrintParams extends CTBFileBlock {
    private final String OPTIONS_SECTION_NAME = "PrintParams";
    private final Fields blockFields;

    @Getter
    @SuppressWarnings("unused")
    public static class Fields implements MSLAFileBlockFields {
        @MSLAFileField @MSLAOption(MSLAOptionName.BottomLayersLiftHeight) private Float BottomLiftHeight;
        @MSLAFileField(order = 1) @MSLAOption(MSLAOptionName.BottomLayersLiftSpeed) private Float BottomLiftSpeed;
        @MSLAFileField(order = 2) @MSLAOption(MSLAOptionName.NormalLayersLiftHeight) private Float LiftHeight;
        @MSLAFileField(order = 3) @MSLAOption(MSLAOptionName.NormalLayersLiftSpeed) private Float LiftSpeed;
        @MSLAFileField(order = 4) @MSLAOption(MSLAOptionName.RetractSpeed) private Float RetractSpeed;
        @MSLAFileField(order = 5) @MSLAOption(MSLAOptionName.Volume) @Setter private Float VolumeMl = 0.0f;
        @MSLAFileField(order = 6) @MSLAOption(MSLAOptionName.Weight) @Setter private Float WeightG = 0.0f;
        @MSLAFileField(order = 7) @Setter private Float CostDollars = 0.0f;
        @MSLAFileField(order = 8) @MSLAOption(MSLAOptionName.BottomLayersLightOffDelay) private final Float BottomLightOffDelay = 0.0f;
        @MSLAFileField(order = 9) @MSLAOption(MSLAOptionName.NormalLayersLightOffDelay)  private final Float LightOffDelay = 0.0f;
        @MSLAFileField(order = 10) @MSLAOption(MSLAOptionName.BottomLayersCount) private Integer BottomLayerCount;
        @MSLAFileField(order = 11) private final Integer Padding1 = 0;
        @MSLAFileField(order = 12) private final Integer Padding2 = 0;
        @MSLAFileField(order = 13) private final Integer Padding3 = 0;
        @MSLAFileField(order = 14) private final Integer Padding4 = 0;
    }

    public CTBFilePrintParams(int version) {
        super(version);
        blockFields = new Fields();
    }

    @Override public String getName() { return OPTIONS_SECTION_NAME; }
    @Override public int getDataLength() throws FileFieldsException { return FileFieldsIO.getBlockLength(this); }
    @Override
    public int getDataFieldOffset(String fieldName) throws FileFieldsException {
        return FileFieldsIO.getBlockLength(this.getBlockFields(), fieldName);
    }
    @Override public String toString() { return blockFields.fieldsAsString(" = ", "\n"); }
}
