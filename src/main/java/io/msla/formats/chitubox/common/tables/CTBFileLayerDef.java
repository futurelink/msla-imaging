package io.msla.formats.chitubox.common.tables;

import io.msla.formats.MSLAException;
import io.msla.formats.iface.*;
import io.msla.formats.iface.MSLAFileField;
import io.msla.formats.iface.options.MSLAOption;
import io.msla.formats.iface.options.MSLAOptionContainer;
import io.msla.formats.iface.options.MSLAOptionName;
import io.msla.formats.io.FileFieldsException;
import io.msla.formats.io.FileFieldsIO;
import lombok.Getter;
import lombok.Setter;


public class CTBFileLayerDef extends CTBFileBlock implements MSLAFileLayer {
    public static final int BRIEF_TABLE_SIZE = 36;
    @Getter private final Fields blockFields;

    /**
     * Brief mode, which means a layer definition excludes
     * Extra fields and Data, so reed() and write() methods
     * work with just a part of all available fields.
     * Flag is true by default.
     */
    @Setter private boolean isBriefMode = false;


    @SuppressWarnings("unused")
    public static class Fields implements MSLAFileBlockFields {
        private final @Getter CTBFileLayerDef parent;
        @MSLAFileField @Getter @Setter private Float PositionZ;
        @MSLAFileField(order = 1) @MSLAOption(MSLAOptionName.NormalLayersExposureTime) @Getter @Setter private Float ExposureTime;
        @MSLAFileField(order = 2) @MSLAOption(MSLAOptionName.LightOffTime) @Getter @Setter private Float LightOffSeconds;
        @MSLAFileField(order = 3) @Setter @Getter private Integer DataAddress;
        @MSLAFileField(order = 4) @Setter @Getter private Integer DataSize;
        @MSLAFileField(order = 5) @Getter private  final Integer PageNumber = 0; // For files larger than 4Gb
        @MSLAFileField(order = 6) @Getter private Integer TableSize;
        @MSLAFileField(order = 7) private final Integer Unknown3 = 0;
        @MSLAFileField(order = 8) private final Integer Unknown4 = 0;
        @MSLAFileField(order = 9) @MSLAOptionContainer @Getter private CTBFileLayerDefExtra Extra = null;
        @MSLAFileField(order = 10, lengthAt = "DataSize") @Setter @Getter private byte[] Data = new byte[0];

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

    public CTBFileLayerDef(int version) throws MSLAException {
        super(version);
        if (version <= 0) throw new MSLAException("Can't allocate a layer, version is not set");
        blockFields = new Fields(this);
        blockFields.TableSize = BRIEF_TABLE_SIZE;
        if (version >= 3) {
            blockFields.Extra = new CTBFileLayerDefExtra();
            blockFields.TableSize += CTBFileLayerDefExtra.TABLE_SIZE;
        }
    }

    @Override public void setDefaults(MSLALayerDefaults layerDefaults) throws MSLAException {
        if (layerDefaults != null) layerDefaults.setFields(blockFields);
    }

    @Override public String getName() { return null; }
    @Override public int getDataLength() throws FileFieldsException {
        return (isBriefMode) ? BRIEF_TABLE_SIZE : FileFieldsIO.getBlockLength(this.blockFields);
    }
    @Override
    public int getDataFieldOffset(String fieldName) throws FileFieldsException {
        return FileFieldsIO.getBlockLength(this.getBlockFields(), fieldName);
    }
    @Override public String toString() { return "{ " + blockFields.fieldsAsString(":", ", ") + " }"; }
}
