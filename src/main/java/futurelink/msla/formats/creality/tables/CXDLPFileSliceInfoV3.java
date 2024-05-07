package futurelink.msla.formats.creality.tables;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.iface.*;
import futurelink.msla.formats.iface.annotations.MSLAFileField;
import futurelink.msla.formats.iface.annotations.MSLAOption;
import futurelink.msla.formats.iface.annotations.MSLAOptionContainer;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;

@MSLAOptionContainer(CXDLPFileSliceInfoV3.Fields.class)
@Getter
public class CXDLPFileSliceInfoV3 extends CXDLPFileTable {
    @Delegate private final Fields fields;
    @Getter
    public static class Fields implements MSLAFileBlockFields {
        @MSLAFileField() private Integer SoftwareNameLength = 0;
        @MSLAFileField(order = 1, lengthAt = "SoftwareNameLength") private String SoftwareName;
        @MSLAFileField(order = 2) private Integer MaterialNameLength = 0;
        @MSLAFileField(order = 3, lengthAt = "MaterialNameLength") private String MaterialName;
        @MSLAFileField(order = 4) @MSLAOption("Distortion compensation")
        private final Byte DistortionCompensationEnabled = 0;
        @MSLAFileField(order = 5) @MSLAOption("Distortion compensation thickness")
        private final Integer DistortionCompensationThickness = 600;
        @MSLAFileField(order = 6) @MSLAOption("Distortion compensation focal length")
        private final Integer DistortionCompensationFocalLength = 300000;
        @MSLAFileField(order = 7) @MSLAOption("XY axis profile compensation")
        private final Byte XYAxisProfileCompensationEnabled = 1;
        @MSLAFileField(order = 8) @MSLAOption("XY axis profile compensation value")
        private final Short XYAxisProfileCompensationValue = 0;
        @MSLAFileField(order = 9) @MSLAOption("Z penetration compensation")
        private final Byte ZPenetrationCompensationEnabled = 0;
        @MSLAFileField(order = 10) @MSLAOption("Z penetration compensation level")
        private final Short ZPenetrationCompensationLevel = 1000;
        @MSLAFileField(order = 11) @MSLAOption(MSLAOption.Antialias) @Setter private Byte AntiAliasEnabled = 1;
        @MSLAFileField(order = 12) @MSLAOption("Antialias min value") private final Byte AntiAliasGreyMinValue = 1;
        @MSLAFileField(order = 13) @MSLAOption("Antialias max value") private final Byte AntiAliasGreyMaxValue = (byte) 0xff;
        @MSLAFileField(order = 14) @MSLAOption(MSLAOption.Blur) @Setter private Byte ImageBlurEnabled = 0;
        @MSLAFileField(order = 15) @MSLAOption(MSLAOption.BlurLevel) @Setter private Byte ImageBlurLevel = 2;
        @MSLAFileField(order = 16, length = 2) private final byte[] PageBreak = new byte[]{ 0x0d, 0x0a };

        public int getDataLength() { return (SoftwareName.length() + 1) + (MaterialName.length() + 1) + 28; }

        @SuppressWarnings("unused")
        private void setSoftwareName(String name) {
            SoftwareName = name;
            SoftwareNameLength = name.length() + 1;
        }

        @SuppressWarnings("unused")
        private void setMaterialName(String name) {
            MaterialName = name;
            MaterialNameLength = name.length() + 1;
        }
    }

    public CXDLPFileSliceInfoV3() {
        fields = new Fields();
        fields.setSoftwareName("v1.9.4");
        fields.setMaterialName("normal");
    }
    public CXDLPFileSliceInfoV3(MSLAFileDefaults defaults) throws MSLAException {
        this();
        defaults.setFields("SliceInfoV3", fields);
    }

    @Override
    public int getDataLength() { return fields.getDataLength() + 2; }

    @Override
    public String toString() { return fieldsAsString(" = ", "\n"); }
}
