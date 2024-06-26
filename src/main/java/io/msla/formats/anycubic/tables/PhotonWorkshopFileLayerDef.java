package io.msla.formats.anycubic.tables;

import io.msla.formats.MSLAException;
import io.msla.formats.iface.MSLAFileBlockFields;
import io.msla.formats.iface.MSLAFileLayer;
import io.msla.formats.iface.MSLALayerDefaults;
import io.msla.formats.iface.MSLAFileField;
import io.msla.formats.iface.options.MSLAOption;
import io.msla.formats.iface.options.MSLAOptionName;
import lombok.Getter;
import lombok.Setter;

/**
 * "LAYERDEF" section's single layer representation.
 */
@Getter
public class PhotonWorkshopFileLayerDef implements MSLAFileBlockFields, MSLAFileLayer {
    @MSLAFileField @Setter private Integer DataAddress = 0;
    @MSLAFileField(order = 1) @Setter private Integer DataLength = 0;
    @MSLAFileField(order = 2) @MSLAOption(MSLAOptionName.LayerLiftHeight) @Setter private Float LiftHeight;
    @MSLAFileField(order = 3) @MSLAOption(MSLAOptionName.LayerLiftSpeed) @Setter private Float LiftSpeed;
    @MSLAFileField(order = 4) @MSLAOption(MSLAOptionName.LayerExposureTime) @Setter private Float ExposureTime;
    @MSLAFileField(order = 5) @MSLAOption(MSLAOptionName.LayerHeight) @Setter private Float LayerHeight;
    @MSLAFileField(order = 6) @Setter private Integer NonZeroPixelCount = 0;
    @MSLAFileField(order = 7) private final Integer Padding1 = 0;

    @Override
    public String toString() {
        return "Data at " + DataAddress + ", length " + DataLength + ", time " + ExposureTime + ", pixels: " + NonZeroPixelCount;
    }

    @Override public MSLAFileBlockFields getBlockFields() { return this; }

    @Override
    public void setDefaults(MSLALayerDefaults layerDefaults) throws MSLAException {
        if (layerDefaults != null) layerDefaults.setFields(this);
    }
}
