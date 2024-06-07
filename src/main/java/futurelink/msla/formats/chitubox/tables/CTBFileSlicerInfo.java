package futurelink.msla.formats.chitubox.tables;

import futurelink.msla.formats.iface.MSLAFileBlockFields;
import futurelink.msla.formats.iface.annotations.MSLAFileField;
import futurelink.msla.formats.iface.annotations.MSLAOption;
import futurelink.msla.formats.io.FileFieldsException;
import futurelink.msla.formats.io.FileFieldsIO;
import lombok.Getter;
import lombok.Setter;

@Getter
public class CTBFileSlicerInfo extends CTBFileBlock {
    private final String OPTIONS_SECTION_NAME = "SlicerInfo";
    private final Fields fileFields;

    @Getter
    @SuppressWarnings("unused")
    public static class Fields implements MSLAFileBlockFields {
        private final CTBFileBlock parent;
        @MSLAFileField @MSLAOption private Float BottomLiftHeight2;
        @MSLAFileField(order = 1) @MSLAOption private Float BottomLiftSpeed2;
        @MSLAFileField(order = 2) @MSLAOption private Float LiftHeight2;
        @MSLAFileField(order = 3) @MSLAOption private Float LiftSpeed2;
        @MSLAFileField(order = 4) @MSLAOption private Float RetractHeight2;
        @MSLAFileField(order = 5) @MSLAOption private Float RetractSpeed2;
        @MSLAFileField(order = 6) @MSLAOption private Float RestTimeAfterLift;
        @MSLAFileField(order = 7) @Setter private Integer MachineNameOffset;
        @MSLAFileField(order = 8) @Setter private Integer MachineNameSize;
        @MSLAFileField(order = 9) private final Byte AntiAliasFlag = 0x0F;
        @MSLAFileField(order = 10) private final Short Padding = 0;
        @MSLAFileField(order = 11) @MSLAOption private Byte PerLayerSettings;
        @MSLAFileField(order = 12) private final Integer ModifiedTimestampMinutes = 0; // TODO fill in with current time
        @MSLAFileField(order = 13) private final Integer AntiAliasLevel = 1;
        @MSLAFileField(order = 14) private Integer SoftwareVersion;
        @MSLAFileField(order = 15) @MSLAOption private Float RestTimeAfterRetract;
        @MSLAFileField(order = 16) @MSLAOption private Float RestTimeAfterLift2;
        @MSLAFileField(order = 17) @MSLAOption @Setter private Integer TransitionLayerCount; // CTB not all printers
        @MSLAFileField(order = 18) @Setter private Integer PrintParametersV4Offset; // V4 Only
        @MSLAFileField(order = 19) private final Integer Padding2 = 0;
        @MSLAFileField(order = 20) private final Integer Padding3 = 0;

        public Fields(CTBFileBlock parent) {
            this.parent = parent;
        }

        public final void setVersion(int version) {
            this.SoftwareVersion = switch(version) {
                case 3 -> 0x1060300; // ctb v3 = 0x1060300 (1.6.3),
                case 4 -> 0x1090000; // ctb v4 = 0x1090000 (1.9.0),
                case 5 -> 0x2000000; // ctb v5 = 0x2000000 (2.0.0)
                default -> 0;
            };
        }

        // Per layer settings is 0 - disabled, 0x10 enabled for CBD_DLP (v1),
        // 0x20 for CTB v2, 0x30 for CTB v3, 0x40 for CTB v4 etc.
        public final void setPerLayerSettings(boolean enable) {
            this.PerLayerSettings = enable ? (byte) (parent.getVersion() * 0x10) : 0x00;
        }
    }

    public CTBFileSlicerInfo(int version) {
        super(version);
        fileFields = new Fields(this);
    }

    @Override public String getName() { return OPTIONS_SECTION_NAME; }
    @Override public int getDataLength() throws FileFieldsException { return FileFieldsIO.getBlockLength(this); }
    @Override
    public int getDataFieldOffset(String fieldName) throws FileFieldsException {
        return FileFieldsIO.getBlockLength(this.getFileFields(), fieldName);
    }
    @Override public String toString() { return fileFields.fieldsAsString(" = ", "\n"); }
}
