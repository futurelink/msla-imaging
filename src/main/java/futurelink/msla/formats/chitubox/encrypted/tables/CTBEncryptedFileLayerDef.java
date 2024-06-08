package futurelink.msla.formats.chitubox.encrypted.tables;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.chitubox.common.tables.CTBFileBlock;
import futurelink.msla.formats.iface.MSLAFileBlockFields;
import futurelink.msla.formats.iface.MSLAFileLayer;
import futurelink.msla.formats.iface.MSLALayerDefaults;
import futurelink.msla.formats.iface.annotations.MSLAFileField;
import futurelink.msla.formats.io.FileFieldsException;
import futurelink.msla.formats.io.FileFieldsIO;
import lombok.Getter;

@Getter
public class CTBEncryptedFileLayerDef extends CTBFileBlock implements MSLAFileLayer {
    private final Fields blockFields;

    @SuppressWarnings("unused")
    public static class Fields implements MSLAFileBlockFields {
        @MSLAFileField private final Integer TableSize = 88;
        @MSLAFileField(order = 1) @Getter private Float PositionZ;
        @MSLAFileField(order = 2) @Getter private Float ExposureTime;
        @MSLAFileField(order = 3) @Getter private Float LightOffDelay;
        @MSLAFileField(order = 4) @Getter private Integer DataAddress;
        @MSLAFileField(order = 5) @Getter private Integer PageNumber;
        @MSLAFileField(order = 6) @Getter private Integer DataSize;
        @MSLAFileField(order = 7) private Integer Unknown3;
        @MSLAFileField(order = 8) @Getter private Integer EncryptedDataOffset;
        @MSLAFileField(order = 9) @Getter private Integer EncryptedDataLength;

        /* Same as 'Extra' in non-encrypted file format */
        @MSLAFileField(order = 10) @Getter private Float LiftHeight;
        @MSLAFileField(order = 11) @Getter private Float LiftSpeed;
        @MSLAFileField(order = 12) @Getter private Float LiftHeight2;
        @MSLAFileField(order = 13) @Getter private Float LiftSpeed2;
        @MSLAFileField(order = 14) @Getter private Float RetractSpeed;
        @MSLAFileField(order = 15) @Getter private Float RetractHeight2;
        @MSLAFileField(order = 16) @Getter private Float RetractSpeed2;
        @MSLAFileField(order = 17) @Getter private Float RestTimeBeforeLift;
        @MSLAFileField(order = 18) @Getter private Float RestTimeAfterLift;
        @MSLAFileField(order = 19) @Getter private Float RestTimeAfterRetract;
        @MSLAFileField(order = 20) @Getter private Float LightPWM;
        @MSLAFileField(order = 21) private final Integer Unknown = 0;
        @MSLAFileField(order = 22, lengthAt = "DataSize", offsetAt = "DataAddress") private byte[] Data;
    }

    public CTBEncryptedFileLayerDef() { super(0); blockFields = new Fields(); }

    @Override public String getName() { return null; }

    @Override public int getDataLength() throws FileFieldsException {
        return FileFieldsIO.getBlockLength(getBlockFields());
    }

    @Override public int getDataFieldOffset(String fieldName) throws FileFieldsException {
        return FileFieldsIO.getBlockLength(this.getBlockFields(), fieldName);
    }

    @Override public void setDefaults(MSLALayerDefaults layerDefaults) throws MSLAException {
        layerDefaults.setFields(null, getBlockFields());
    }

    @Override
    public String toString() { return "{ " + blockFields.fieldsAsString(":", ", ") + " }"; }
}
