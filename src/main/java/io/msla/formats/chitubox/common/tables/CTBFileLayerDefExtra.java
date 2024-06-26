package io.msla.formats.chitubox.common.tables;

import io.msla.formats.iface.MSLAFileBlock;
import io.msla.formats.iface.MSLAFileBlockFields;
import io.msla.formats.iface.MSLAFileField;
import io.msla.formats.iface.options.MSLAOption;
import io.msla.formats.iface.options.MSLAOptionName;
import io.msla.formats.io.FileFieldsException;
import io.msla.formats.io.FileFieldsIO;
import lombok.Getter;
import lombok.Setter;

@Getter
public class CTBFileLayerDefExtra implements MSLAFileBlock {
    private final Fields blockFields = new Fields();
    public static final int TABLE_SIZE = 48;

    @Getter
    @Setter
    @SuppressWarnings("unused")
    public static class Fields implements MSLAFileBlockFields {
        @MSLAFileField private Integer TotalSize;
        @MSLAFileField(order = 1) @MSLAOption(MSLAOptionName.LayerLiftHeight) private Float LiftHeight;
        @MSLAFileField(order = 2) @MSLAOption(MSLAOptionName.LayerLiftSpeed) private Float LiftSpeed;
        @MSLAFileField(order = 3) @MSLAOption(MSLAOptionName.LayerLiftHeight2) private Float LiftHeight2;
        @MSLAFileField(order = 4) @MSLAOption(MSLAOptionName.LayerLiftSpeed2) private Float LiftSpeed2;
        @MSLAFileField(order = 5) @MSLAOption(MSLAOptionName.LayerRetractSpeed) private Float RetractSpeed;
        @MSLAFileField(order = 6) @MSLAOption(MSLAOptionName.LayerRetractHeight2) private Float RetractHeight2;
        @MSLAFileField(order = 7) @MSLAOption(MSLAOptionName.LayerRetractSpeed2) private Float RetractSpeed2;
        @MSLAFileField(order = 8) @MSLAOption(MSLAOptionName.LayerWaitBeforeLift) private Float RestTimeBeforeLift;
        @MSLAFileField(order = 9) @MSLAOption(MSLAOptionName.LayerWaitAfterLift) private Float RestTimeAfterLift;
        @MSLAFileField(order = 10) @MSLAOption(MSLAOptionName.LayerWaitAfterRetract) private Float RestTimeAfterRetract;
        @MSLAFileField(order = 11) @MSLAOption(MSLAOptionName.LayerLightPWM) private Float LightPWM;
    }

    @Override public String getName() { return null; }
    @Override public int getDataLength() { return TABLE_SIZE; }
    @Override
    public int getDataFieldOffset(String fieldName) throws FileFieldsException {
        return FileFieldsIO.getBlockLength(this.getBlockFields(), fieldName);
    }
    @Override public String toString() { return "{ " + blockFields.fieldsAsString(":", ", ") +" }"; }
}
