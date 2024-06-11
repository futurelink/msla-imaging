package futurelink.msla.formats.elegoo.tables;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.iface.MSLAFileBlockFields;
import futurelink.msla.formats.iface.MSLAFileLayer;
import futurelink.msla.formats.iface.MSLALayerDefaults;
import futurelink.msla.formats.iface.MSLAFileField;
import futurelink.msla.formats.iface.options.MSLAOption;
import futurelink.msla.formats.iface.options.MSLAOptionName;
import lombok.Getter;


@Getter
public class GOOFileLayerDef extends GOOFileTable implements MSLAFileLayer {
    private final Fields blockFields = new Fields();

    @SuppressWarnings("unused")
    static public class Fields implements MSLAFileBlockFields {
        @MSLAFileField short Pause;
        @MSLAFileField(order = 1) float PausePositionZ;
        @MSLAFileField(order = 2) float PositionZ;
        @MSLAFileField(order = 3) @MSLAOption(MSLAOptionName.NormalLayersExposureTime) float ExposureTime;
        @MSLAFileField(order = 4) @MSLAOption(MSLAOptionName.LightOffTime) float LightOffDelay;
        @MSLAFileField(order = 5) @MSLAOption(MSLAOptionName.NormalLayersWaitAfterCure) float WaitTimeAfterCure;
        @MSLAFileField(order = 6) @MSLAOption(MSLAOptionName.NormalLayersWaitAfterLift) float WaitTimeAfterLift;
        @MSLAFileField(order = 7) @MSLAOption(MSLAOptionName.NormalLayersWaitBeforeCure) float WaitTimeBeforeCure;
        @MSLAFileField(order = 8) @MSLAOption(MSLAOptionName.NormalLayersLiftHeight) float LiftHeight;
        @MSLAFileField(order = 9) @MSLAOption(MSLAOptionName.NormalLayersLiftSpeed) float LiftSpeed;
        @MSLAFileField(order = 10) @MSLAOption(MSLAOptionName.LiftHeight2) float LiftHeight2;
        @MSLAFileField(order = 11) @MSLAOption(MSLAOptionName.LiftSpeed2) float LiftSpeed2;
        @MSLAFileField(order = 12) @MSLAOption(MSLAOptionName.RetractHeight) float RetractHeight;
        @MSLAFileField(order = 13) @MSLAOption(MSLAOptionName.RetractSpeed) float RetractSpeed;
        @MSLAFileField(order = 14) @MSLAOption(MSLAOptionName.RetractHeight2) float RetractHeight2;
        @MSLAFileField(order = 15) @MSLAOption(MSLAOptionName.RetractSpeed2) float RetractSpeed2;
        @MSLAFileField(order = 16) @MSLAOption(MSLAOptionName.LightPWM) short LightPWM;
        @MSLAFileField(order = 17, length = 2) public byte[] Delimiter1 = new byte[]{ 0x0d, 0x0a };
        @MSLAFileField(order = 18) int DataLength = 0;
        @MSLAFileField(order = 19, lengthAt = "DataLength") @Getter byte[] Data;
        @MSLAFileField(order = 20, length = 2) public byte[] Delimiter2 = new byte[]{ 0x0d, 0x0a };
    }

    @Override public void setDefaults(MSLALayerDefaults layerDefaults) throws MSLAException {
        if (layerDefaults != null) layerDefaults.setFields(blockFields);
    }

    @Override public String getName() { return null; }
    @Override public int getDataLength() { return 72 + blockFields.DataLength; }
    @Override public String toString() { return blockFields.fieldsAsString(" = ", "\n"); }

}
