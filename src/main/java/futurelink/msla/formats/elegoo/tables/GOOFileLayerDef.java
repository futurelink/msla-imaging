package futurelink.msla.formats.elegoo.tables;

import futurelink.msla.formats.MSLAOptionMapper;
import futurelink.msla.formats.iface.MSLAFileBlockFields;
import futurelink.msla.formats.iface.MSLAFileLayer;
import futurelink.msla.formats.iface.MSLALayerDefaults;
import futurelink.msla.formats.iface.annotations.MSLAFileField;
import futurelink.msla.formats.iface.annotations.MSLAOption;
import futurelink.msla.formats.utils.LayerOptionMapper;
import lombok.Getter;


public class GOOFileLayerDef extends GOOFileTable implements MSLAFileLayer {
    private final MSLAOptionMapper optionMapper;
    @Getter private final Fields fileFields = new Fields();

    @SuppressWarnings("unused")
    static public class Fields implements MSLAFileBlockFields {
        @MSLAFileField short Pause;
        @MSLAFileField(order = 1) float PausePositionZ;
        @MSLAFileField(order = 2) float PositionZ;
        @MSLAFileField(order = 3) @MSLAOption(MSLAOption.ExposureTime) float ExposureTime;
        @MSLAFileField(order = 4) @MSLAOption("Light off delay") float LightOffDelay;
        @MSLAFileField(order = 5) @MSLAOption(MSLAOption.WaitAfterCure) float WaitTimeAfterCure;
        @MSLAFileField(order = 6) @MSLAOption(MSLAOption.WaitAfterLift) float WaitTimeAfterLift;
        @MSLAFileField(order = 7) @MSLAOption(MSLAOption.WaitBeforeCure) float WaitTimeBeforeCure;
        @MSLAFileField(order = 8) @MSLAOption(MSLAOption.LiftHeight) float LiftHeight;
        @MSLAFileField(order = 9) @MSLAOption(MSLAOption.LiftSpeed) float LiftSpeed;
        @MSLAFileField(order = 10) @MSLAOption("Lift height 2") float LiftHeight2;
        @MSLAFileField(order = 11) @MSLAOption("Lift speed 2") float LiftSpeed2;
        @MSLAFileField(order = 12) @MSLAOption(MSLAOption.RetractHeight) float RetractHeight;
        @MSLAFileField(order = 13) @MSLAOption(MSLAOption.RetractSpeed) float RetractSpeed;
        @MSLAFileField(order = 14) @MSLAOption("Retract height 2") float RetractHeight2;
        @MSLAFileField(order = 15) @MSLAOption("Retract speed 2") float RetractSpeed2;
        @MSLAFileField(order = 16) @MSLAOption(MSLAOption.LightPWM) short LightPWM;
        @MSLAFileField(order = 17, length = 2) public byte[] Delimiter1 = new byte[]{ 0x0d, 0x0a };
        @MSLAFileField(order = 18) int DataLength = 0;
        @MSLAFileField(order = 19, lengthAt = "DataLength") @Getter byte[] Data;
        @MSLAFileField(order = 20, length = 2) public byte[] Delimiter2 = new byte[]{ 0x0d, 0x0a };
    }

    public GOOFileLayerDef(MSLALayerDefaults layerDefaults) {
        super();
        optionMapper = new LayerOptionMapper(fileFields, layerDefaults);
    }

    @Override public MSLAOptionMapper options() { return optionMapper; }
    @Override public int getDataLength() { return 72 + fileFields.DataLength; }
    @Override public String toString() { return fileFields.fieldsAsString(" = ", "\n"); }

}
