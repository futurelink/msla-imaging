package futurelink.msla.formats.chitubox.tables;

import futurelink.msla.formats.iface.MSLAFileBlockFields;
import futurelink.msla.formats.iface.annotations.MSLAFileField;
import futurelink.msla.formats.utils.FileFieldsException;
import futurelink.msla.formats.utils.FileFieldsIO;
import lombok.Getter;
import lombok.Setter;

@Getter
public class CTBFileLayerDef extends CTBFileBlock {
    public static final int BRIEF_TABLE_SIZE = 36;
    private final Fields fileFields;

    /**
     * Brief mode, which means a layer definition excludes
     * Extra fields and Data, so reed() and write() methods
     * work with just a part of all available fields.
     * Flag is true by default.
     */
    @Setter private boolean isBriefMode = true;

    @Getter
    @SuppressWarnings("unused")
    public static class Fields implements MSLAFileBlockFields {
        private final CTBFileLayerDef parent;
        @MSLAFileField(order = 1) @Setter private Float PositionZ;
        @MSLAFileField(order = 2) @Setter private Float ExposureTime;
        @MSLAFileField(order = 3) @Setter private Float LightOffSeconds;
        @MSLAFileField(order = 4) @Setter private Integer DataAddress;
        @MSLAFileField(order = 5) @Setter private Integer DataSize;
        @MSLAFileField(order = 6) private final Integer PageNumber = 0; // For files larger than 4Gb
        @MSLAFileField(order = 7) private Integer TableSize;
        @MSLAFileField(order = 8) private final Integer Unknown3 = 0;
        @MSLAFileField(order = 9) private final Integer Unknown4 = 0;
        @MSLAFileField(order = 10) private CTBFileLayerDefExtra Extra = null;
        @MSLAFileField(order = 11, lengthAt = "DataSize") @Setter private byte[] Data = new byte[0];

        public Fields(CTBFileLayerDef parent) {
            this.parent = parent;
        }

        @Override
        public boolean isFieldExcluded(String fieldName) {
            // In brief mode we exclude Extra and Data
            if (parent.isBriefMode && (fieldName.equals("Extra") || fieldName.equals("Data"))) return true;

            // In all modes we exclude Extra if version is less than 3rd
            return (parent.getVersion() < 3 && fieldName.equals("Extra"));
        }
    }

    public CTBFileLayerDef(int version) {
        super(version);
        fileFields = new Fields(this);
        fileFields.TableSize = BRIEF_TABLE_SIZE;
        if (version >= 3) {
            fileFields.Extra = new CTBFileLayerDefExtra();
            fileFields.TableSize += CTBFileLayerDefExtra.TABLE_SIZE;
        }
    }

    @Override public FileFieldsIO.Endianness getEndianness() { return FileFieldsIO.Endianness.LittleEndian; }
    @Override public int getDataLength() throws FileFieldsException {
        return (isBriefMode) ? BRIEF_TABLE_SIZE : FileFieldsIO.getBlockLength(this);
    }
    @Override
    public int getDataFieldOffset(String fieldName) throws FileFieldsException {
        return FileFieldsIO.getBlockLength(this.getFileFields(), fieldName);
    }
    @Override public String toString() { return "{ " + fileFields.fieldsAsString(":", ", ") + " }"; }
}
