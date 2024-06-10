package futurelink.msla.formats.creality.tables;

import futurelink.msla.formats.iface.*;
import futurelink.msla.formats.iface.MSLAFileField;
import futurelink.msla.formats.iface.options.MSLAOption;
import futurelink.msla.formats.iface.options.MSLAOptionName;
import lombok.Getter;
import lombok.experimental.Delegate;

@Getter
public class CXDLPFileSliceInfo extends CXDLPFileTable {
    @Delegate private final Fields blockFields = new Fields();

    @SuppressWarnings("unused")
    @Getter
    public static class Fields implements MSLAFileBlockFields {
        @MSLAFileField()
        private Integer DisplayWidthLength = 0;
        @MSLAFileField(order = 1, lengthAt = "DisplayWidthLength", charset = "UTF-16BE")
        private String DisplayWidth;
        @MSLAFileField(order = 2) private Integer DisplayHeightLength = 0;
        @MSLAFileField(order = 3, lengthAt = "DisplayHeightLength", charset = "UTF-16BE")
        private String DisplayHeight;
        @MSLAFileField(order = 4) private Integer LayerHeightLength = 8;
        @MSLAFileField(order = 5, lengthAt = "LayerHeightLength", charset = "UTF-16BE")
        @MSLAOption(value = MSLAOptionName.LayerHeight) private String LayerHeight = "0.05";
        @MSLAFileField(order = 6) @MSLAOption(MSLAOptionName.NormalLayersExposureTime) private Short ExposureTime;
        @MSLAFileField(order = 7) @MSLAOption(MSLAOptionName.NormalLayersWaitBeforeCure) private final Short WaitTimeBeforeCure = 1;   // 1 as minimum or it won't print!
        @MSLAFileField(order = 8) @MSLAOption(MSLAOptionName.BottomLayersExposureTime) private Short BottomExposureTime;
        @MSLAFileField(order = 9) @MSLAOption(MSLAOptionName.BottomLayersCount) private Short BottomLayersCount;
        @MSLAFileField(order = 10) @MSLAOption(MSLAOptionName.BottomLayersLiftHeight) private Short BottomLiftHeight;
        @MSLAFileField(order = 11) @MSLAOption(MSLAOptionName.BottomLayersLiftSpeed) private Short BottomLiftSpeed;
        @MSLAFileField(order = 12) @MSLAOption(MSLAOptionName.NormalLayersLiftHeight) private Short LiftHeight;
        @MSLAFileField(order = 13) @MSLAOption(MSLAOptionName.NormalLayersLiftSpeed) private Short LiftSpeed;
        @MSLAFileField(order = 14) @MSLAOption(MSLAOptionName.RetractSpeed) private Short RetractSpeed;
        @MSLAFileField(order = 15) @MSLAOption(MSLAOptionName.BottomLayersLightPWM) private final Short BottomLightPWM = 255;
        @MSLAFileField(order = 16) @MSLAOption(MSLAOptionName.NormalLayersLightPWM) private final Short LightPWM = 255;

        public Fields() {}
        public int getDataLength() { return DisplayWidthLength + DisplayHeightLength + LayerHeightLength + 22 + 12; }

        public void setDisplayHeight(String displayHeight) {
            DisplayHeight = displayHeight;
            DisplayHeightLength = displayHeight.length() * 2;
        }

        public void setDisplayWidth(String displayWidth) {
            DisplayWidth = displayWidth;
            DisplayWidthLength = displayWidth.length() * 2;
        }

        public void setLayerHeight(String layerHeight) {
            LayerHeight = layerHeight;
            LayerHeightLength = layerHeight.length() * 2;
        }
    }

    public CXDLPFileSliceInfo(MSLAFileProps initialProps) {
        if (initialProps != null) {
            blockFields.setDisplayWidth(initialProps.getFloat("DisplayWidth").toString());
            blockFields.setDisplayHeight(initialProps.getFloat("DisplayHeight").toString());
        }
    }

    @Override public String getName() { return "SliceInfo"; }
    @Override public int getDataLength() { return blockFields.getDataLength(); }
    @Override public String toString() { return fieldsAsString(" = ", "\n"); }
}
