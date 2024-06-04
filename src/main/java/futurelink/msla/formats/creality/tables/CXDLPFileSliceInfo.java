package futurelink.msla.formats.creality.tables;

import futurelink.msla.formats.iface.*;
import futurelink.msla.formats.iface.annotations.MSLAFileField;
import futurelink.msla.formats.iface.annotations.MSLAOption;
import lombok.Getter;
import lombok.experimental.Delegate;

@Getter
public class CXDLPFileSliceInfo extends CXDLPFileTable {
    @Delegate private final Fields fileFields;

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
        @MSLAOption(value = MSLAOption.LayerHeight, type=String.class) private String LayerHeight = "0.05";
        @MSLAFileField(order = 6) @MSLAOption(MSLAOption.ExposureTime) private Short ExposureTime;
        @MSLAFileField(order = 7) @MSLAOption(MSLAOption.WaitBeforeCure) private final Short WaitTimeBeforeCure = 1;   // 1 as minimum or it won't print!
        @MSLAFileField(order = 8) @MSLAOption(MSLAOption.BottomExposureTime) private Short BottomExposureTime;
        @MSLAFileField(order = 9) @MSLAOption(MSLAOption.BottomLayersCount) private Short BottomLayersCount;
        @MSLAFileField(order = 10) @MSLAOption(MSLAOption.BottomLiftHeight) private Short BottomLiftHeight;
        @MSLAFileField(order = 11) @MSLAOption(MSLAOption.BottomLiftSpeed) private Short BottomLiftSpeed;
        @MSLAFileField(order = 12) @MSLAOption(MSLAOption.LiftHeight) private Short LiftHeight;
        @MSLAFileField(order = 13) @MSLAOption(MSLAOption.LiftSpeed) private Short LiftSpeed;
        @MSLAFileField(order = 14) @MSLAOption(MSLAOption.RetractSpeed) private Short RetractSpeed;
        @MSLAFileField(order = 15) @MSLAOption("Bottom layers PWM") private final Short BottomLightPWM = 255;
        @MSLAFileField(order = 16) @MSLAOption("Normal layers PWM") private final Short LightPWM = 255;

        public Fields() {}

        public int getDataLength() { return DisplayWidthLength + DisplayHeightLength + LayerHeightLength + 22 + 12; }

        @SuppressWarnings("unused")
        public void setDisplayHeight(String displayHeight) {
            DisplayHeight = displayHeight;
            DisplayHeightLength = displayHeight.length() * 2;
        }

        @SuppressWarnings("unused")
        public void setDisplayWidth(String displayWidth) {
            DisplayWidth = displayWidth;
            DisplayWidthLength = displayWidth.length() * 2;
        }

        @SuppressWarnings("unused")
        public void setLayerHeight(String layerHeight) {
            LayerHeight = layerHeight;
            LayerHeightLength = layerHeight.length() * 2;
        }
    }

    public CXDLPFileSliceInfo() {
        fileFields = new Fields();
    }

    @Override public String getName() { return "SliceInfo"; }
    @Override public int getDataLength() { return fileFields.getDataLength(); }
    @Override public String toString() { return fieldsAsString(" = ", "\n"); }
}
