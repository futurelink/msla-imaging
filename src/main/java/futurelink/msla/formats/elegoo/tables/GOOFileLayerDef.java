package futurelink.msla.formats.elegoo.tables;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.iface.MSLAFileBlockFields;
import futurelink.msla.formats.iface.MSLAFileLayer;
import futurelink.msla.formats.iface.MSLALayerDefaults;
import futurelink.msla.formats.iface.MSLAFileField;
import futurelink.msla.formats.iface.options.MSLAOption;
import futurelink.msla.formats.iface.options.MSLAOptionName;
import lombok.Getter;
import lombok.Setter;


@Getter
public class GOOFileLayerDef extends GOOFileTable implements MSLAFileLayer {
    private final Fields blockFields = new Fields();

    @SuppressWarnings("unused")
    static public class Fields implements MSLAFileBlockFields {
        @MSLAFileField @Getter private Short Pause = 0;
        @MSLAFileField(order = 1) @Getter private Float PausePositionZ = 0.0f;
        @MSLAFileField(order = 2) @Getter private Float PositionZ = 0.0f;
        @MSLAFileField(order = 3) @MSLAOption(MSLAOptionName.LayerExposureTime) @Getter private Float ExposureTime;
        @MSLAFileField(order = 4) @MSLAOption(MSLAOptionName.LayerLightOffDelay) @Getter private Float LightOffDelay;
        @MSLAFileField(order = 5) @MSLAOption(MSLAOptionName.LayerWaitAfterCure) @Getter private Float WaitTimeAfterCure;
        @MSLAFileField(order = 6) @MSLAOption(MSLAOptionName.LayerWaitAfterLift) @Getter private Float WaitTimeAfterLift;
        @MSLAFileField(order = 7) @MSLAOption(MSLAOptionName.LayerWaitBeforeCure) @Getter private Float WaitTimeBeforeCure;
        @MSLAFileField(order = 8) @MSLAOption(MSLAOptionName.LayerLiftHeight) @Getter private Float LiftHeight;
        @MSLAFileField(order = 9) @MSLAOption(MSLAOptionName.LayerLiftSpeed) @Getter private Float LiftSpeed;
        @MSLAFileField(order = 10) @MSLAOption(MSLAOptionName.LayerLiftHeight2) @Getter private Float LiftHeight2;
        @MSLAFileField(order = 11) @MSLAOption(MSLAOptionName.LayerLiftSpeed2) @Getter private Float LiftSpeed2;
        @MSLAFileField(order = 12) @MSLAOption(MSLAOptionName.LayerRetractHeight) @Getter private Float RetractHeight;
        @MSLAFileField(order = 13) @MSLAOption(MSLAOptionName.LayerRetractSpeed) @Getter private Float RetractSpeed;
        @MSLAFileField(order = 14) @MSLAOption(MSLAOptionName.LayerRetractHeight2) @Getter private Float RetractHeight2;
        @MSLAFileField(order = 15) @MSLAOption(MSLAOptionName.LayerRetractSpeed2) @Getter private Float RetractSpeed2;
        @MSLAFileField(order = 16) @MSLAOption(MSLAOptionName.LayerLightPWM) @Getter private Short LightPWM;
        @MSLAFileField(order = 17, length = 2) private final byte[] Delimiter1 = new byte[] { 0x0d, 0x0a };
        @MSLAFileField(order = 18) @Setter @Getter private Integer DataLength = 0;
        @MSLAFileField(order = 19, lengthAt = "DataLength") @Getter @Setter private byte[] Data;
        @MSLAFileField(order = 20, length = 2) private final byte[] Delimiter2 = new byte[] { 0x0d, 0x0a };
    }

    @Override public void setDefaults(MSLALayerDefaults layerDefaults) throws MSLAException {
        if (layerDefaults != null) layerDefaults.setFields(blockFields);
    }

    @Override public String getName() { return null; }
    @Override public String toString() { return blockFields.fieldsAsString(" = ", "\n"); }

}
