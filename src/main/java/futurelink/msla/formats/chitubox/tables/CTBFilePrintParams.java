package futurelink.msla.formats.chitubox.tables;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.iface.MSLAFileBlock;
import futurelink.msla.formats.iface.MSLAFileBlockFields;
import futurelink.msla.formats.iface.MSLAFileDefaults;
import futurelink.msla.formats.iface.annotations.MSLAFileField;
import futurelink.msla.formats.iface.annotations.MSLAOption;
import futurelink.msla.formats.utils.FileFieldsIO;
import lombok.Getter;

@Getter
public class CTBFilePrintParams implements MSLAFileBlock {
    private final String OPTIONS_SECTION_NAME = "PrintParams";
    private final Fields fields;

    @Getter
    @SuppressWarnings("unused")
    static class Fields implements MSLAFileBlockFields {
        @MSLAFileField @MSLAOption(MSLAOption.BottomLiftHeight) private Float BottomLiftHeight;
        @MSLAFileField(order = 1) @MSLAOption(MSLAOption.BottomLiftSpeed) private Float BottomLiftSpeed;
        @MSLAFileField(order = 2) @MSLAOption(MSLAOption.LiftHeight) private Float LiftHeight;
        @MSLAFileField(order = 3) @MSLAOption(MSLAOption.LiftSpeed) private Float LiftSpeed;
        @MSLAFileField(order = 4) @MSLAOption(MSLAOption.RetractSpeed) private Float RetractSpeed;
        @MSLAFileField(order = 5) @MSLAOption(MSLAOption.Volume) private Float VolumeMl;
        @MSLAFileField(order = 6) @MSLAOption(MSLAOption.Weight) private Float WeightG;
        @MSLAFileField(order = 7) private Float CostDollars;
        @MSLAFileField(order = 8) private Float BottomLightOffDelay;
        @MSLAFileField(order = 9)  private Float LightOffDelay;
        @MSLAFileField(order = 10) @MSLAOption(MSLAOption.BottomLayersCount) private Integer BottomLayerCount;
        @MSLAFileField(order = 11) private Integer Padding1;
        @MSLAFileField(order = 12) private Integer Padding2;
        @MSLAFileField(order = 13) private Integer Padding3;
        @MSLAFileField(order = 14) private Integer Padding4;
    }

    public CTBFilePrintParams() {
        fields = new Fields();
    }

    public CTBFilePrintParams(MSLAFileDefaults defaults) throws MSLAException {
        this();
        defaults.setFields(OPTIONS_SECTION_NAME, fields);
    }

    @Override public FileFieldsIO.Endianness getEndianness() { return FileFieldsIO.Endianness.LittleEndian; }
    @Override public int getDataLength() { return 0; }
    @Override public String toString() { return fields.fieldsAsString(" = ", "\n"); }
}
