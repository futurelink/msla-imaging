package futurelink.msla.formats.creality.tables;

import futurelink.msla.formats.iface.*;
import futurelink.msla.formats.iface.MSLAFileField;
import futurelink.msla.formats.iface.options.MSLAOption;
import futurelink.msla.formats.iface.options.MSLAOptionName;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;

@Getter
public class CXDLPFileSliceInfoV3 extends CXDLPFileTable {
    @Delegate private final Fields blockFields = new Fields();
    @Getter
    static class Fields implements MSLAFileBlockFields {
        @MSLAFileField() private Integer SoftwareNameLength = 0;
        @MSLAFileField(order = 1, lengthAt = "SoftwareNameLength") private String SoftwareName;
        @MSLAFileField(order = 2) private Integer MaterialNameLength = 0;
        @MSLAFileField(order = 3, lengthAt = "MaterialNameLength") private String MaterialName;
        @MSLAFileField(order = 4) @MSLAOption(MSLAOptionName.DistortionCompensation)
        private final Byte DistortionCompensationEnabled = 0;
        @MSLAFileField(order = 5) @MSLAOption(MSLAOptionName.DistortionCompensationThickness)
        private final Integer DistortionCompensationThickness = 600;
        @MSLAFileField(order = 6) @MSLAOption(MSLAOptionName.DistortionCompensationLength)
        private final Integer DistortionCompensationFocalLength = 300000;
        @MSLAFileField(order = 7) @MSLAOption(MSLAOptionName.XYAxisProfileCompensation)
        private final Byte XYAxisProfileCompensationEnabled = 1;
        @MSLAFileField(order = 8) @MSLAOption(MSLAOptionName.XYAxisProfileCompensationValue)
        private final Short XYAxisProfileCompensationValue = 0;
        @MSLAFileField(order = 9) @MSLAOption(MSLAOptionName.ZPenetrationCompensation)
        private final Byte ZPenetrationCompensationEnabled = 0;
        @MSLAFileField(order = 10) @MSLAOption(MSLAOptionName.ZPenetrationCompensationLevel)
        private final Short ZPenetrationCompensationLevel = 1000;
        @MSLAFileField(order = 11) @MSLAOption(MSLAOptionName.Antialias) @Setter private Byte AntiAliasEnabled = 1;
        @MSLAFileField(order = 12) private final Byte AntiAliasGreyMinValue = 1;
        @MSLAFileField(order = 13) private final Byte AntiAliasGreyMaxValue = (byte) 0xff;
        @MSLAFileField(order = 14) @MSLAOption(MSLAOptionName.ImageBlur) @Setter private Byte ImageBlurEnabled = 0;
        @MSLAFileField(order = 15) @MSLAOption(MSLAOptionName.ImageBlurLevel) @Setter private Byte ImageBlurLevel = 2;
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
        blockFields.setSoftwareName("v1.9.4");
        blockFields.setMaterialName("normal");
    }

    @Override public String getName() { return "SliceInfoV3"; }
    @Override public int getDataLength() { return blockFields.getDataLength() + 2; }
    @Override public String toString() { return fieldsAsString(" = ", "\n"); }
}
