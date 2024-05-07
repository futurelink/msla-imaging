package futurelink.msla.formats.chitubox.tables;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.iface.MSLAFileBlock;
import futurelink.msla.formats.iface.MSLAFileBlockFields;
import futurelink.msla.formats.iface.MSLAFileDefaults;
import futurelink.msla.formats.iface.annotations.MSLAFileField;
import futurelink.msla.formats.utils.FileFieldsIO;
import lombok.Getter;

@Getter
public class CTBFileSlicerInfo implements MSLAFileBlock {
    private static final Byte PER_LAYER_SETTINGS_DISALLOW =     0;
    private static final Byte PER_LAYER_SETTINGS_CBD_DLP =   0x10;
    private static final Byte PER_LAYER_SETTINGS_CTBv2 =     0x20; // 15 for ctb v2 files and others (This disallow per layer settings)
    private static final Byte PER_LAYER_SETTINGS_CTBv3 =     0x30; // 536870927 for ctb v3 files (This allow per layer settings)
    private static final Byte PER_LAYER_SETTINGS_CTBv4 =     0x40; // 1073741839 for ctb v4 files (This allow per layer settings)
    private static final Byte PER_LAYER_SETTINGS_CTBv5 =     0x50; // 1073741839 for ctb v5 files (This allow per layer settings)

    private final Fields fields;

    @Getter
    @SuppressWarnings("unused")
    public static class Fields implements MSLAFileBlockFields {
        @MSLAFileField private Float BottomLiftHeight2;
        @MSLAFileField(order = 1) private Float BottomLiftSpeed2;
        @MSLAFileField(order = 2) private Float LiftHeight2;
        @MSLAFileField(order = 3) private Float LiftSpeed2;
        @MSLAFileField(order = 4) private Float RetractHeight2;
        @MSLAFileField(order = 5) private Float RetractSpeed2;
        @MSLAFileField(order = 6) private Float RestTimeAfterLift;
        @MSLAFileField(order = 7) private Integer MachineNameAddress;
        @MSLAFileField(order = 8) private final Integer MachineNameSize = 0;
        @MSLAFileField(order = 9) private final Byte AntiAliasFlag = 0x0F;
        @MSLAFileField(order = 10) private Short Padding;
        @MSLAFileField(order = 11) private Byte PerLayerSettings;
        @MSLAFileField(order = 12) private Integer ModifiedTimestampMinutes;
        @MSLAFileField(order = 13) private final Integer AntiAliasLevel = 1;
        @MSLAFileField(order = 14) private final Integer SoftwareVersion = 0x1090000; // ctb v3 = 0x1060300 (1.6.3) | ctb v4 = 0x1090000 (1.9.0) | ctb v5 = 0x2000000 (2.0.0)
        @MSLAFileField(order = 15) private Float RestTimeAfterRetract;
        @MSLAFileField(order = 16) private Float RestTimeAfterLift2;
        @MSLAFileField(order = 17) private Integer TransitionLayerCount; // CTB not all printers
        @MSLAFileField(order = 18) private Integer PrintParametersV4Offset; // V4 Only
        @MSLAFileField(order = 19) private Integer Padding2;
        @MSLAFileField(order = 20) private Integer Padding3;
        @MSLAFileField(order = 21, lengthAt = "MachineNameSize") public String MachineName = "";
    }

    public CTBFileSlicerInfo() {
        fields = new Fields();
    }

    public CTBFileSlicerInfo(MSLAFileDefaults defaults) throws MSLAException {
        this();
        defaults.setFields("SlicerInfo", fields);
    }

    @Override public FileFieldsIO.Endianness getEndianness() { return FileFieldsIO.Endianness.LittleEndian; }
    @Override public int getDataLength() { return 0; }
    @Override public String toString() { return fields.fieldsAsString(" = ", "\n"); }
}
