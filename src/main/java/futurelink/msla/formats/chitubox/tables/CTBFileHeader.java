package futurelink.msla.formats.chitubox.tables;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.iface.MSLAFileBlock;
import futurelink.msla.formats.iface.MSLAFileBlockFields;
import futurelink.msla.formats.iface.annotations.MSLAFileField;
import futurelink.msla.formats.iface.annotations.MSLAOption;
import futurelink.msla.formats.io.FileFieldsException;
import futurelink.msla.formats.io.FileFieldsIO;
import futurelink.msla.utils.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;
import java.util.Random;

@Getter
public class CTBFileHeader extends CTBFileBlock {
    private final String OPTIONS_SECTION_NAME = "Header";

    public static final int MAGIC_CBD_DLP = 0x12FD0019; // 318570521
    public static final int MAGIC_CTB = 0x12FD0086; // 318570630
    public static final int MAGIC_CTBv4 = 0x12FD0106; // 318570758
    public static final int MAGIC_CTBv4_GK_two = 0xFF220810; // 4280420368

    private static final Integer DEFAULT_VERSION = 5;
    private static final short DefaultLightPWM = 255;
    private static final short DefaultBottomLightPWM = 255;

    private final Fields fileFields;

    @Getter
    @SuppressWarnings("unused")
    static public class Fields implements MSLAFileBlockFields {
        private Size Resolution = null;
        private float PixelSizeUm;

        @MSLAFileField private Integer Magic;
        @MSLAFileField(order = 1) private Integer Version;
        public void setVersion(Integer version) throws MSLAException {
            // When file is being read - Magic is set first, so version should match,
            // oppositely, when file is created then version defines Magic.
            if ((Magic != null) && !Objects.equals(Magic, getMagicByVersion(version)))
                throw new MSLAException("Version is not valid for magic number");
            else {
                Version = version;
                Magic = getMagicByVersion(version);
            }
        }
        @MSLAFileField(order = 2) private Float BedSizeX;
        @MSLAFileField(order = 3) private Float BedSizeY;
        @MSLAFileField(order = 4) private Float BedSizeZ;
        @MSLAFileField(order = 5) private final Integer Unknown1 = 0;
        @MSLAFileField(order = 6) private final Integer Unknown2 = 0;
        @MSLAFileField(order = 7) private @Setter Float TotalHeightMillimeter = 0.0f;
        @MSLAFileField(order = 8) @MSLAOption(MSLAOption.LayerHeight) private Float LayerHeightMillimeter;
        @MSLAFileField(order = 9) @MSLAOption(MSLAOption.ExposureTime) private Float LayerExposureSeconds;
        @MSLAFileField(order = 10) @MSLAOption(MSLAOption.BottomExposureTime) private Float BottomExposureSeconds;
        @MSLAFileField(order = 11) private Float LightOffDelay;
        @MSLAFileField(order = 12) @MSLAOption(MSLAOption.BottomLayersCount) private final Integer BottomLayersCount = 1;
        @MSLAFileField(order = 13) private Integer ResolutionX() { return Resolution != null ? Resolution.getWidth() : 0; }
        private void setResolutionX(Integer width) { Resolution = new Size(width, ResolutionY()); }
        @MSLAFileField(order = 14) private Integer ResolutionY() { return  Resolution != null ? Resolution.getHeight() : 0; }
        private void setResolutionY(Integer height) { Resolution = new Size(ResolutionX(), height); }
        @MSLAFileField(order = 15) @Setter private Integer PreviewLargeOffset;
        @MSLAFileField(order = 16) @Setter private Integer LayersDefinitionOffset;
        @MSLAFileField(order = 17) @Setter private Integer LayerCount;
        @MSLAFileField(order = 18) @Setter private Integer PreviewSmallOffset;
        @MSLAFileField(order = 19) @Setter private Integer PrintTime;
        @MSLAFileField(order = 20) private final Integer ProjectorType = 1;
        @MSLAFileField(order = 21) @Setter private Integer PrintParametersOffset;
        @MSLAFileField(order = 22) @Setter private Integer PrintParametersSize;
        @MSLAFileField(order = 23) private Integer AntiAliasLevel;
        @MSLAFileField(order = 24) private final Short LightPWM = DefaultLightPWM;
        @MSLAFileField(order = 25) private final Short BottomLightPWM = DefaultBottomLightPWM;
        @MSLAFileField(order = 26) @Setter private Integer EncryptionKey;
        @MSLAFileField(order = 27) @Setter private Integer SlicerOffset;
        @MSLAFileField(order = 28) @Setter private Integer SlicerSize;

        private Integer getMagicByVersion(Integer version) {
            return switch (version) {
                case 1 -> MAGIC_CBD_DLP;
                case 2, 3 -> MAGIC_CTB;
                case 4, 5 -> MAGIC_CTBv4;
                default -> null;
            };
        }
    }

    public CTBFileHeader(int version) throws MSLAException {
        super(version);
        var r = new Random();
        fileFields = new Fields();
        fileFields.setVersion(version);
        fileFields.EncryptionKey = r.nextInt(Integer.MAX_VALUE);
    }

    @Override public String getName() { return OPTIONS_SECTION_NAME; }
    @Override public int getDataLength() throws FileFieldsException { return FileFieldsIO.getBlockLength(this); }
    @Override
    public int getDataFieldOffset(String fieldName) throws FileFieldsException {
        return FileFieldsIO.getBlockLength(this.getFileFields(), fieldName);
    }
    @Override public String toString() { return fileFields.fieldsAsString(" = ", "\n"); }
}
