package futurelink.msla.formats.chitubox.tables;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.iface.MSLAFileBlock;
import futurelink.msla.formats.iface.MSLAFileBlockFields;
import futurelink.msla.formats.iface.MSLAFileDefaults;
import futurelink.msla.formats.iface.annotations.MSLAFileField;
import futurelink.msla.formats.iface.annotations.MSLAOption;
import futurelink.msla.formats.utils.FileFieldsIO;
import futurelink.msla.formats.utils.Size;
import lombok.Getter;

import java.util.Objects;

@Getter
public class CTBFileHeader implements MSLAFileBlock {
    public static final int MAGIC_CBD_DLP = 0x12FD0019; // 318570521
    public static final int MAGIC_CTB = 0x12FD0086; // 318570630
    public static final int MAGIC_CTBv4 = 0x12FD0106; // 318570758
    public static final int MAGIC_CTBv4_GK_two = 0xFF220810; // 4280420368

    private static final Integer DEFAULT_VERSION = 5;
    private static final short DefaultLightPWM = 255;
    private static final short DefaultBottomLightPWM = 255;

    private final Fields fields;

    @Getter
    @SuppressWarnings("unused")
    static public class Fields implements MSLAFileBlockFields {
        private Size Resolution = new Size(0, 0);

        @MSLAFileField private Integer Magic;
        @MSLAFileField(order = 1) private Integer Version;
        public void setVersion(Integer version) throws MSLAException {
            if ((Magic != null) && !Objects.equals(Magic, getMagicByVersion(version)))
                throw new MSLAException("Version is not valid for magic number");
            else Version = version;
        }
        @MSLAFileField(order = 2) private Float BedSizeX;
        @MSLAFileField(order = 3) private Float BedSizeY;
        @MSLAFileField(order = 4) private Float BedSizeZ;
        @MSLAFileField(order = 5) private Integer Unknown1;
        @MSLAFileField(order = 6) private Integer Unknown2;
        @MSLAFileField(order = 7) private Float TotalHeightMillimeter;
        @MSLAFileField(order = 8) private Float LayerHeightMillimeter;
        @MSLAFileField(order = 9) @MSLAOption(MSLAOption.ExposureTime) private Float LayerExposureSeconds;
        @MSLAFileField(order = 10) @MSLAOption(MSLAOption.BottomExposureTime) private Float BottomExposureSeconds;
        @MSLAFileField(order = 11) private Float LightOffDelay;
        @MSLAFileField(order = 12) @MSLAOption(MSLAOption.BottomLayersCount) private final Integer BottomLayersCount = 1;
        @MSLAFileField(order = 13) private Integer ResolutionX() { return Resolution.getWidth(); }
        private void setResolutionX(short width) { Resolution = new Size(width, Resolution.getHeight()); }
        @MSLAFileField(order = 14) private Integer ResolutionY() { return Resolution.getHeight(); }
        private void setResolutionY(short height) { Resolution = new Size(Resolution.getWidth(), height); }
        @MSLAFileField(order = 15) private Integer PreviewLargeOffset;
        @MSLAFileField(order = 16) private Integer LayersDefinitionOffset;
        @MSLAFileField(order = 17) private Integer LayerCount;
        @MSLAFileField(order = 18) private Integer PreviewSmallOffset;
        @MSLAFileField(order = 19) private Integer PrintTime;
        @MSLAFileField(order = 20) private Integer ProjectorType;
        @MSLAFileField(order = 21) private Integer PrintParametersOffset;
        @MSLAFileField(order = 22) private Integer PrintParametersSize;
        @MSLAFileField(order = 23) private Integer AntiAliasLevel;
        @MSLAFileField(order = 24) private final Short LightPWM = DefaultLightPWM;
        @MSLAFileField(order = 25) private final Short BottomLightPWM = DefaultBottomLightPWM;
        @MSLAFileField(order = 26) private Integer EncryptionKey;
        @MSLAFileField(order = 27) private Integer SlicerOffset;
        @MSLAFileField(order = 28) private Integer SlicerSize;

        private Integer getMagicByVersion(Integer version) {
            return switch (version) {
                case 1 -> MAGIC_CBD_DLP;
                case 2, 3 -> MAGIC_CTB;
                case 4, 5 -> MAGIC_CTBv4;
                default -> null;
            };
        }
    }

    public CTBFileHeader() {
        fields = new Fields();
    }

    public CTBFileHeader(MSLAFileDefaults defaults) throws MSLAException {
        this();
        defaults.setFields("Header", fields);
    }

    @Override public int getDataLength() { return 0; }
    @Override public FileFieldsIO.Endianness getEndianness() { return FileFieldsIO.Endianness.LittleEndian; }
    @Override public String toString() { return fields.fieldsAsString(" = ", "\n"); }
}
