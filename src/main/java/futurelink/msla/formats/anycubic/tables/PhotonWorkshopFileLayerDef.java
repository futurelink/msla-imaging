package futurelink.msla.formats.anycubic.tables;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.iface.MSLAFileBlockFields;
import futurelink.msla.formats.iface.MSLAFileLayer;
import futurelink.msla.formats.iface.MSLALayerDefaults;
import futurelink.msla.formats.iface.annotations.MSLAFileField;
import futurelink.msla.formats.iface.annotations.MSLAOption;
import lombok.Getter;
import lombok.Setter;

@Getter
public class PhotonWorkshopFileLayerDef implements MSLAFileBlockFields, MSLAFileLayer {
    @MSLAFileField @Setter private Integer DataAddress = 0;
    @MSLAFileField(order = 1) @Setter private Integer DataLength = 0;
    @MSLAFileField(order = 2) @MSLAOption(MSLAOption.LiftHeight) @Setter private Float LiftHeight = 0.0f;
    @MSLAFileField(order = 3) @MSLAOption(MSLAOption.LiftSpeed) @Setter private Float LiftSpeed = 0.0f;
    @MSLAFileField(order = 4) @MSLAOption(MSLAOption.ExposureTime) @Setter private Float ExposureTime = 0.0f;
    @MSLAFileField(order = 5) @MSLAOption(MSLAOption.LayerHeight) @Setter private Float LayerHeight = 0.0f;
    @MSLAFileField(order = 6) @Setter private Integer NonZeroPixelCount = 0;
    @MSLAFileField(order = 7) private final Integer Padding1 = 0;

    @Override
    public String toString() {
        return "Data at " + DataAddress + ", length " + DataLength + ", time " + ExposureTime + ", pixels: " + NonZeroPixelCount;
    }

    @Override public MSLAFileBlockFields getFileFields() { return this; }

    @Override
    public void setDefaults(MSLALayerDefaults layerDefaults) throws MSLAException {

    }
}
