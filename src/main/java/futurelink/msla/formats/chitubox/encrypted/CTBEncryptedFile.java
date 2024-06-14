package futurelink.msla.formats.chitubox.encrypted;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.MSLAFileGeneric;
import futurelink.msla.formats.chitubox.CTBCrypto;
import futurelink.msla.formats.chitubox.common.CTBCommonFileCodec;
import futurelink.msla.formats.chitubox.encrypted.tables.CTBEncryptedFileHeader;
import futurelink.msla.formats.chitubox.encrypted.tables.CTBEncryptedFileLayers;
import futurelink.msla.formats.chitubox.encrypted.tables.CTBEncryptedFileSlicerSettings;
import futurelink.msla.formats.chitubox.common.tables.*;
import futurelink.msla.formats.iface.*;
import futurelink.msla.formats.iface.options.MSLAOptionContainer;
import futurelink.msla.utils.Size;
import lombok.Getter;

import javax.crypto.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Logger;

public class CTBEncryptedFile extends MSLAFileGeneric<byte[]> {
    private final Logger logger = Logger.getLogger(CTBEncryptedFile.class.getName());

    @Getter @MSLAOptionContainer private CTBEncryptedFileHeader Header = null;
    @Getter @MSLAOptionContainer private CTBEncryptedFileSlicerSettings SlicerSettings = null;
    @Getter private CTBFilePreview PreviewSmall;
    @Getter private CTBFilePreview PreviewLarge;
    @Getter @MSLAOptionContainer private CTBFileResinParams Resin = null;
    @Getter private CTBFileDisclaimer Disclaimer = new CTBFileDisclaimer();
    private CTBFileMachineName MachineName = new CTBFileMachineName();
    @Getter private CTBEncryptedFileLayers Layers;

    private final Byte HASH_LENGTH = 32;

    public CTBEncryptedFile(MSLAFileProps initialProps) throws MSLAException {
        super(initialProps);
        var Version = (int) initialProps.getByte("Version");
        if (Version <= 0) throw new MSLAException("File defaults do not have a version number.");

        Header = new CTBEncryptedFileHeader(Version, initialProps);
        if (Header.getBlockFields().getVersion() <= 0)
            throw new MSLAException("The mSLA file does not have a version number.");

        SlicerSettings = new CTBEncryptedFileSlicerSettings(Version, initialProps);
        MachineName = new CTBFileMachineName();
        Disclaimer = new CTBFileDisclaimer();
        Layers = new CTBEncryptedFileLayers(this);
        if (Header.getBlockFields().getVersion() >= 5) Resin = new CTBFileResinParams(Version);
    }

    public CTBEncryptedFile(DataInputStream stream) throws IOException, MSLAException {
        super(null);
        read(stream);
    }

    public ByteArrayInputStream readEncryptedBlock(
            DataInputStream stream,
            Integer offset,
            Integer size) throws MSLAException
    {
        try {
            stream.reset();
            stream.skipBytes(offset);
            byte[] encryptedBlock = stream.readNBytes(size);
            return new ByteArrayInputStream(CTBCrypto.initCipher(Cipher.DECRYPT_MODE).doFinal(encryptedBlock));
        } catch (IOException e) {
            throw new MSLAException("Can't read file", e);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new MSLAException("Can't decrypt file", e);
        }
    }

    public final byte[] LongToBytes(Long value) {
        var data = new byte[8];
        for (int i = 0; i < 8; i++) data[i] = (byte) ((value >> (i * 8)) & 0xff);
        return data;
    }

    private byte[] generateSignature() throws MSLAException {
        var checksum = SlicerSettings.getBlockFields().getChecksumValue();
        if (checksum == 0)
            throw new MSLAException("Can't check file signature, checksum is zero.");
        try {
            var checksumHash = MessageDigest.getInstance("SHA-256").digest(LongToBytes(checksum));
            var encryptedHash = CTBCrypto.initCipher(Cipher.ENCRYPT_MODE).doFinal(checksumHash);
            logger.fine("Encrypted checksum hash is " + Arrays.toString(encryptedHash));
            return encryptedHash;
        } catch (NoSuchAlgorithmException e) {
            throw new MSLAException("Can't check file signature, SHA256 algorithm not available.");
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new MSLAException("Can't check file signature, AES encryption error.");
        }
    }

    /* Internal read method */
    private void read(DataInputStream stream) throws MSLAException {
        Header = new CTBEncryptedFileHeader(0, null); // Version is going to be read from file
        Header.read(stream, 0);
        if (Header.getBlockFields().getVersion() == null)
            throw new MSLAException("Malformed file, Version is not identified");

        logger.info("CTB encrypted file version " + Header.getBlockFields().getVersion());

        SlicerSettings = new CTBEncryptedFileSlicerSettings(Header.getBlockFields().getVersion(), null);
        SlicerSettings.read(new DataInputStream(readEncryptedBlock(
                stream,
                Header.getBlockFields().getSettingsOffset(),
                Header.getBlockFields().getSettingsSize()
        )), 0);

        /* Validate signature */
        try {
            // Read signature
            stream.reset();
            stream.skipBytes(Header.getBlockFields().getSignatureOffset());
            var signature = stream.readNBytes(Header.getBlockFields().getSignatureSize());
            logger.fine("Signature is " + Arrays.toString(signature));
            if (!Arrays.equals(signature, generateSignature()))
                throw new MSLAException("The file checksum does not match, malformed data.");
        } catch (IOException e) {
            throw new MSLAException("Can't check file signature, data read error.");
        }

        // Read disclaimer block
        if (SlicerSettings.getBlockFields().getDisclaimerOffset() > 0) {
            Disclaimer = new CTBFileDisclaimer();
            Disclaimer.read(stream, SlicerSettings.getBlockFields().getDisclaimerOffset());
            logger.info(Disclaimer.getBlockFields().getDisclaimer());
        }

        // Read machine name
        if (SlicerSettings.getBlockFields().getMachineNameOffset() > 0) {
            MachineName.getBlockFields().setMachineNameSize(SlicerSettings.getBlockFields().getMachineNameSize());
            MachineName.read(stream, SlicerSettings.getBlockFields().getMachineNameOffset());
        }

        // Read large preview
        if (SlicerSettings.getBlockFields().getPreviewLargeOffset() > 0) {
            logger.info("Reading large preview");
            PreviewLarge = new CTBFilePreview(Header.getBlockFields().getVersion(), CTBFilePreview.Type.Large);
            PreviewLarge.read(stream, SlicerSettings.getBlockFields().getPreviewLargeOffset());
            var pixels = PreviewLarge.readImage(stream);
        }

        // Read small preview
        if (SlicerSettings.getBlockFields().getPreviewSmallOffset() > 0) {
            logger.info("Reading small preview");
            PreviewSmall = new CTBFilePreview(Header.getBlockFields().getVersion(), CTBFilePreview.Type.Small);
            PreviewSmall.read(stream, SlicerSettings.getBlockFields().getPreviewSmallOffset());
            var pixels = PreviewSmall.readImage(stream);
        }

        // Read resin settings
        if (Header.getBlockFields().getVersion() >= 5 && SlicerSettings.getBlockFields().getResinParametersAddress() > 0)
            Resin.read(stream, SlicerSettings.getBlockFields().getResinParametersAddress());

        // Read layers table
        Layers = new CTBEncryptedFileLayers(this);
        Layers.read(stream, SlicerSettings.getBlockFields().getLayersDefinitionOffset());
    }

    @Override public Class<? extends MSLALayerCodec<byte[]>> getCodec() { return CTBCommonFileCodec.class; }
    @Override public String getMachineName() { return MachineName.getBlockFields().getMachineName(); }
    @Override public MSLAPreview getPreview(int index) throws MSLAException { return (index == 0) ? PreviewSmall : PreviewLarge; }
    @Override public MSLAPreview getLargePreview() { return PreviewLarge; }
    @Override public void setPreview(int index, BufferedImage image) {
        if (index == 0) PreviewSmall.setImage(image); else PreviewLarge.setImage(image);
    }
    @Override public boolean isMachineValid(MSLAFileDefaults defaults) {
        try {
            return defaults.getFileClass().equals(this.getClass()) &&
                    ((getResolution() == null) || defaults.getResolution().equals(getResolution()));
        } catch (MSLAException e) {
            return false;
        }
    }
    @Override public float getDPI() { return 0; }
    @Override public Size getResolution() { return SlicerSettings.getBlockFields().getResolution(); }
    @Override public boolean isValid() { return Header != null && SlicerSettings != null; }

    @Override
    public void addLayer(MSLALayerEncodeReader reader, MSLALayerEncoder.Callback<byte[]> callback) throws MSLAException {
        var params = new HashMap<String, Object>();
        params.put("EncryptionKey",SlicerSettings.getBlockFields().getEncryptionKey());
        Layers.add(getEncodersPool(), reader, params, callback);
    }

    @Override
    public boolean readLayer(MSLALayerDecodeWriter writer, int layer) throws MSLAException {
        var layerData = new CTBCommonFileCodec.Input(Layers.get(layer).getBlockFields().getData());
        var params = new HashMap<String, Object>();
        params.put("EncryptionKey", SlicerSettings.getBlockFields().getEncryptionKey());
        return getDecodersPool().decode(layer, writer, layerData, params);
    }

    @Override
    public void write(OutputStream stream) throws MSLAException {
        throw new MSLAException("Encrypted file write is not supported yet.");
    }

    @Override
    public String toString() {
        return "----- Header -----\n" + Header + "\n" +
                "----- Slicer settings ----\n" + SlicerSettings + "\n" +
                MachineName + "\n" +
                ((Disclaimer != null) ? Disclaimer + "\n" : "") +
                "----- Small preview ----\n" + PreviewSmall + "\n" +
                "----- Larger preview ----\n" + PreviewLarge;
    }
}
