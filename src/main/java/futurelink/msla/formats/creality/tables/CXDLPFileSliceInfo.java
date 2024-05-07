package futurelink.msla.formats.creality.tables;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.iface.*;
import futurelink.msla.formats.iface.annotations.MSLAFileField;
import futurelink.msla.formats.iface.annotations.MSLAOption;
import futurelink.msla.formats.iface.annotations.MSLAOptionContainer;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;

@MSLAOptionContainer(CXDLPFileSliceInfo.Fields.class)
@Getter
public class CXDLPFileSliceInfo extends CXDLPFileTable {
    @Delegate private final Fields fields;
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
        @MSLAOption(type=String.class) private String LayerHeight = "0.05";
        @MSLAFileField(order = 6) @MSLAOption @Setter private Short ExposureTime;
        @MSLAFileField(order = 7) @MSLAOption @Setter private Short WaitTimeBeforeCure = 1;   // 1 as minimum or it won't print!
        @MSLAFileField(order = 8) @MSLAOption @Setter private Short BottomExposureTime;
        @MSLAFileField(order = 9) @MSLAOption @Setter private Short BottomLayersCount;
        @MSLAFileField(order = 10) @MSLAOption @Setter private Short BottomLiftHeight;
        @MSLAFileField(order = 11) @MSLAOption @Setter private Short BottomLiftSpeed;
        @MSLAFileField(order = 12) @MSLAOption @Setter private Short LiftHeight;
        @MSLAFileField(order = 13) @MSLAOption @Setter private Short LiftSpeed;
        @MSLAFileField(order = 14) @MSLAOption @Setter private Short RetractSpeed;
        @MSLAFileField(order = 15) @MSLAOption @Setter private Short BottomLightPWM = 255;
        @MSLAFileField(order = 16) @MSLAOption @Setter private Short LightPWM = 255;

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
        fields = new Fields();
    }
    public CXDLPFileSliceInfo(MSLAFileDefaults defaults) throws MSLAException {
        this();
        defaults.setFields("SliceInfo", fields);
    }

    @Override
    public int getDataLength() { return fields.getDataLength(); }

    @Override
    public String toString() {
        return "-- Slicer Info --\n" +
                "DisplayWidthLength: " + fields.DisplayWidthLength + "\n" +
                "DisplayWidth: " + fields.DisplayWidth + "\n" +
                "DisplayHeightLength: " + fields.DisplayHeightLength + "\n" +
                "DisplayHeight: " + fields.DisplayHeight + "\n" +
                "LayerHeightLength: " + fields.LayerHeightLength + "\n" +
                "LayerHeight: " + fields.LayerHeight + "\n" +
                "ExposureTime: " + fields.ExposureTime + "\n" +
                "WaitTimeBeforeCure: " + fields.WaitTimeBeforeCure + "\n" +
                "BottomExposureTime: " + fields.BottomExposureTime + "\n" +
                "BottomLayersCount: " + fields.BottomLayersCount + "\n" +
                "BottomLiftHeight: " + fields.BottomLiftHeight + "\n" +
                "BottomLiftSpeed: " + fields.BottomLiftSpeed + "\n" +
                "LiftHeight: " + fields.LiftHeight + "\n" +
                "LiftSpeed: " + fields.LiftSpeed + "\n" +
                "RetractSpeed: " + fields.RetractSpeed + "\n" +
                "BottomLightPWM: " + fields.BottomLightPWM + "\n" +
                "LightPWM: " + fields.LightPWM + "\n";
    }
}
