package futurelink.msla.formats.chitubox.common.tables;

import futurelink.msla.formats.iface.MSLAFileBlockFields;
import futurelink.msla.formats.iface.MSLAFileField;
import futurelink.msla.formats.iface.options.MSLAOption;
import futurelink.msla.formats.iface.options.MSLAOptionName;
import futurelink.msla.formats.io.FileFieldsException;
import futurelink.msla.formats.io.FileFieldsIO;
import lombok.Getter;
import lombok.Setter;

@Getter
public class CTBFileSlicerInfo extends CTBFileBlock {
    private final String OPTIONS_SECTION_NAME = "SlicerInfo";
    private final Fields blockFields;

    @SuppressWarnings("unused")
    public static class Fields implements MSLAFileBlockFields {
        private final CTBFileBlock parent;
        @MSLAFileField @MSLAOption(MSLAOptionName.BottomLayersLiftHeight2) @Getter private Float BottomLiftHeight2;
        @MSLAFileField(order = 1) @MSLAOption(MSLAOptionName.BottomLayersLiftSpeed2) @Getter private Float BottomLiftSpeed2;
        @MSLAFileField(order = 2) @MSLAOption(MSLAOptionName.NormalLayersLiftHeight2) @Getter private Float LiftHeight2;
        @MSLAFileField(order = 3) @MSLAOption(MSLAOptionName.NormalLayersLiftSpeed2) @Getter private Float LiftSpeed2;
        @MSLAFileField(order = 4) @MSLAOption(MSLAOptionName.NormalLayersRetractHeight2) @Getter private Float RetractHeight2;
        @MSLAFileField(order = 5) @MSLAOption(MSLAOptionName.NormalLayersRetractSpeed2) @Getter private Float RetractSpeed2;
        @MSLAFileField(order = 6) @MSLAOption(MSLAOptionName.WaitAfterLift) @Getter private Float RestTimeAfterLift;
        @MSLAFileField(order = 7) @Setter @Getter private Integer MachineNameOffset;
        @MSLAFileField(order = 8) @Setter @Getter private Integer MachineNameSize;
        @MSLAFileField(order = 9) @MSLAOption(MSLAOptionName.Antialias) @Getter private Byte AntiAliasFlag;
        @MSLAFileField(order = 10) private final Short Padding = 0;
        @MSLAFileField(order = 11) @MSLAOption(MSLAOptionName.LayerSettings) @Getter private Byte PerLayerSettings;
        @MSLAFileField(order = 12) private final Integer ModifiedTimestampMinutes = 0; // TODO fill in with current time
        @MSLAFileField(order = 13) @MSLAOption(MSLAOptionName.AntialiasLevel) @Getter private Integer AntiAliasLevel = 1;
        @MSLAFileField(order = 14) @Getter private Integer SoftwareVersion;
        @MSLAFileField(order = 15) @MSLAOption(MSLAOptionName.WaitAfterRetract) @Getter private Float RestTimeAfterRetract;
        @MSLAFileField(order = 16) @MSLAOption(MSLAOptionName.WaitAfterLift2) @Getter private Float RestTimeAfterLift2;
        @MSLAFileField(order = 17) @MSLAOption(MSLAOptionName.TransitionLayersCount) @Getter @Setter private Integer TransitionLayerCount; // CTB not all printers
        @MSLAFileField(order = 18) @Getter @Setter private Integer PrintParametersV4Offset; // V4 Only
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
        blockFields = new Fields(this);
    }

    @Override public String getName() { return OPTIONS_SECTION_NAME; }
    @Override public int getDataLength() throws FileFieldsException { return FileFieldsIO.getBlockLength(this); }
    @Override
    public int getDataFieldOffset(String fieldName) throws FileFieldsException {
        return FileFieldsIO.getBlockLength(this.getBlockFields(), fieldName);
    }
    @Override public String toString() { return blockFields.fieldsAsString(" = ", "\n"); }
}
