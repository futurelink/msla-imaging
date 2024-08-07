package io.msla.formats.chitubox.encrypted.tables;

import io.msla.formats.MSLAException;
import io.msla.formats.chitubox.common.tables.CTBFileBlock;
import io.msla.formats.chitubox.CTBCrypto;
import io.msla.formats.iface.MSLAFileBlockFields;
import io.msla.formats.iface.MSLAFileLayer;
import io.msla.formats.iface.MSLALayerDefaults;
import io.msla.formats.iface.MSLAFileField;
import io.msla.formats.iface.options.MSLAOption;
import io.msla.formats.iface.options.MSLAOptionName;
import io.msla.formats.io.FileFieldsException;
import io.msla.formats.io.FileFieldsIO;
import lombok.Getter;
import lombok.Setter;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import java.util.Arrays;

@Getter
public class CTBEncryptedFileLayerDef extends CTBFileBlock implements MSLAFileLayer {
    private final Fields blockFields;

    @SuppressWarnings("unused")
    public static class Fields implements MSLAFileBlockFields {
        @MSLAFileField private final Integer TableSize = 88;
        @MSLAFileField(order = 1) @Getter @Setter private Float PositionZ;
        @MSLAFileField(order = 2) @Getter @Setter @MSLAOption(MSLAOptionName.LayerExposureTime) private Float ExposureTime;
        @MSLAFileField(order = 3) @Getter @Setter @MSLAOption(MSLAOptionName.LayerLightOffDelay) private Float LightOffDelay;
        @MSLAFileField(order = 4) @Getter @Setter private Integer DataAddress;
        @MSLAFileField(order = 5) @Getter @Setter private Integer PageNumber;
        @MSLAFileField(order = 6) @Getter @Setter private Integer DataSize;
        @MSLAFileField(order = 7) private Integer Unknown = 0;
        @MSLAFileField(order = 8) @Getter @Setter private Integer EncryptedDataOffset = 0;
        @MSLAFileField(order = 9) @Getter @Setter private Integer EncryptedDataLength = 0;

        /* Same as 'Extra' in non-encrypted file format */
        @MSLAFileField(order = 10) @Getter @Setter @MSLAOption(MSLAOptionName.LayerLiftHeight) private Float LiftHeight;
        @MSLAFileField(order = 11) @Getter @Setter @MSLAOption(MSLAOptionName.LayerLiftSpeed) private Float LiftSpeed;
        @MSLAFileField(order = 12) @Getter @Setter @MSLAOption(MSLAOptionName.LayerLiftHeight2) private Float LiftHeight2;
        @MSLAFileField(order = 13) @Getter @Setter @MSLAOption(MSLAOptionName.LayerLiftSpeed2) private Float LiftSpeed2;
        @MSLAFileField(order = 14) @Getter @Setter @MSLAOption(MSLAOptionName.LayerRetractSpeed) private Float RetractSpeed;
        @MSLAFileField(order = 15) @Getter @Setter @MSLAOption(MSLAOptionName.LayerRetractHeight2) private Float RetractHeight2;
        @MSLAFileField(order = 16) @Getter @Setter @MSLAOption(MSLAOptionName.LayerRetractSpeed2) private Float RetractSpeed2;
        @MSLAFileField(order = 17) @Getter @Setter @MSLAOption(MSLAOptionName.LayerWaitBeforeLift) private Float RestTimeBeforeLift;
        @MSLAFileField(order = 18) @Getter @Setter @MSLAOption(MSLAOptionName.LayerWaitAfterLift) private Float RestTimeAfterLift;
        @MSLAFileField(order = 19) @Getter @Setter @MSLAOption(MSLAOptionName.LayerWaitAfterRetract) private Float RestTimeAfterRetract;
        @MSLAFileField(order = 20) @Getter @Setter @MSLAOption(MSLAOptionName.LayerLightPWM) private Float LightPWM;
        @MSLAFileField(order = 21) private final Integer Unknown2 = 0;
        @MSLAFileField(order = 22, lengthAt = "DataSize", offsetAt = "DataAddress") @Getter private byte[] Data;
        public void setData(byte[] data) {
            this.Data = data;
            this.DataSize = data.length;
        }
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
        if (layerDefaults == null) return;
        layerDefaults.setFields(getBlockFields());
    }

    @Override
    public void afterRead() throws MSLAException {
        if (getBlockFields().EncryptedDataLength > 0)  {
            try {
                var byteBuffer = Arrays.copyOfRange(
                        getBlockFields().Data,
                        getBlockFields().EncryptedDataOffset,
                        getBlockFields().EncryptedDataOffset + getBlockFields().EncryptedDataLength);
                var decryptedLayerData = CTBCrypto.initCipher(Cipher.DECRYPT_MODE).doFinal(byteBuffer);
            } catch (BadPaddingException | IllegalBlockSizeException e) {
                throw new MSLAException("Error decrypting layer data", e);
            }
        }
    }

    @Override
    public String toString() { return "{ " + blockFields.fieldsAsString(":", ", ") + " }"; }
}
