package futurelink.msla.formats.creality.tables;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.iface.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;

@MSLAOptionContainer(className = CXDLPFileSliceInfoV3.Fields.class)
@Getter
public class CXDLPFileSliceInfoV3 extends CXDLPFileTable {
    @Delegate private final Fields fields;
    @Getter
    public static class Fields implements MSLAFileBlockFields {
        @MSLAFileField() private Integer SoftwareNameLength = 0;
        @MSLAFileField(order = 1, lengthAt = "SoftwareNameLength") @MSLAOption(type = String.class) private String SoftwareName;
        @MSLAFileField(order = 2) private Integer MaterialNameLength = 0;
        @MSLAFileField(order = 3, lengthAt = "MaterialNameLength") @MSLAOption(type = String.class) private String MaterialName;
        @MSLAFileField(order = 4) @MSLAOption @Setter private Byte DistortionCompensationEnabled = 0;
        @MSLAFileField(order = 5) @MSLAOption @Setter private Integer DistortionCompensationThickness = 600;
        @MSLAFileField(order = 6) @MSLAOption @Setter private Integer DistortionCompensationFocalLength = 300000;
        @MSLAFileField(order = 7) @MSLAOption @Setter private Byte XYAxisProfileCompensationEnabled = 1;
        @MSLAFileField(order = 8) @MSLAOption @Setter private Short XYAxisProfileCompensationValue = 0;
        @MSLAFileField(order = 9) @MSLAOption @Setter private Byte ZPenetrationCompensationEnabled = 0;
        @MSLAFileField(order = 10) @MSLAOption @Setter private Short ZPenetrationCompensationLevel = 1000;
        @MSLAFileField(order = 11) @MSLAOption @Setter private Byte AntiAliasEnabled = 1;
        @MSLAFileField(order = 12) @MSLAOption private Byte AntiAliasGreyMinValue = 1;
        @MSLAFileField(order = 13) @MSLAOption private Byte AntiAliasGreyMaxValue = (byte) 0xff;
        @MSLAFileField(order = 14) @MSLAOption @Setter private Byte ImageBlurEnabled = 0;
        @MSLAFileField(order = 15) @MSLAOption @Setter private Byte ImageBlurLevel = 2;
        @MSLAFileField(order = 16, length = 2) private byte[] PageBreak = new byte[]{ 0x0d, 0x0a };

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
    public String toString() {
        return "-- SliceInfoV3 -- \n" +
                "SoftwareName: " + fields.SoftwareName + "\n" +
                "MaterialName: " + fields.MaterialName + "\n" +
                "DistortionCompensationEnabled: " + fields.DistortionCompensationEnabled + "\n" +
                "DistortionCompensationThickness: " + fields.DistortionCompensationThickness + "\n" +
                "DistortionCompensationFocalLength: " + fields.DistortionCompensationFocalLength + "\n" +
                "XYAxisProfileCompensationEnabled: " + fields.XYAxisProfileCompensationEnabled + "\n" +
                "XYAxisProfileCompensationValue: " + fields.XYAxisProfileCompensationValue + "\n" +
                "ZPenetrationCompensationEnabled: " + fields.ZPenetrationCompensationEnabled + "\n" +
                "ZPenetrationCompensationLevel: " + fields.ZPenetrationCompensationLevel + "\n" +
                "AntiAliasEnabled: " + fields.AntiAliasEnabled + "\n" +
                "AntiAliasGreyMinValue: " + fields.AntiAliasGreyMinValue + "\n" +
                "AntiAliasGreyMaxValue: " + fields.AntiAliasGreyMaxValue + "\n" +
                "ImageBlurEnabled: " + fields.ImageBlurEnabled + "\n" +
                "ImageBlurLevel: " + fields.ImageBlurLevel + "\n";
    }
}
