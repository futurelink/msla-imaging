package futurelink.msla.formats.chitubox.encrypted;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.MSLAFileGeneric;
import futurelink.msla.formats.chitubox.encrypted.tables.CTBEncryptedFileHeader;
import futurelink.msla.formats.chitubox.encrypted.tables.CTBEncryptedFileResin;
import futurelink.msla.formats.chitubox.encrypted.tables.CTBEncryptedFileSlicerSettings;
import futurelink.msla.formats.chitubox.tables.*;
import futurelink.msla.formats.iface.*;
import futurelink.msla.formats.iface.annotations.MSLAOptionContainer;
import futurelink.msla.utils.Size;
import lombok.Getter;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.*;
import java.util.Arrays;
import java.util.Base64;
import java.util.logging.Logger;

public class CTBEncryptedFile extends MSLAFileGeneric<byte[]> {
    private final Logger logger = Logger.getLogger(CTBEncryptedFile.class.getName());

    @Getter @MSLAOptionContainer private CTBEncryptedFileHeader Header = null;
    @Getter @MSLAOptionContainer private CTBEncryptedFileSlicerSettings SlicerSettings = null;
    @Getter private CTBFilePreview PreviewSmall;
    @Getter private CTBFilePreview PreviewLarge;
    @Getter @MSLAOptionContainer private CTBEncryptedFileResin Resin = null;
    @Getter private CTBFileDisclaimer Disclaimer = new CTBFileDisclaimer();
    private CTBFileMachineName MachineName = new CTBFileMachineName();

    private final Byte HASH_LENGTH = 32;
    private final Integer LAYER_XOR_KEY = 0xEFBEADDE;

    private final String Secret0 = "HDgSAB0BEiE/AgpPAhwhM1QAAUwHPT8HTywEGiEjVAoBDwEsJgAKC0wVPDoRTwkDATg3AE9HQhAhNF1VZWYk" +
            "MHYVHQpMEjI3HQEcGFM7OQBPHwkBOD8AGwoIUyAlER1PCBIhN1QKAQ8BLCYABgACX3U6GwwEH191NRsBHBgBND8aHENMATAlAB0GDwc8ORo" +
            "cQ0weOjgbHwAAGi83AAYAAlM0OBBPAQMdeCURARwJUyU5GAYMBRYmdgAHDhhTJSQRGQoCByZ2GxsHCQEmdhIdAAFTNiQRDhsJUzQ4EE8DCR" +
            "IxexIAHRsSJzJUHAAABiE/GwEcTBInOQEBC0wHMDUcAQAAHDIvWmU8GQMlOQYbBgIUdSIcBhxMFTw6EU8JAwE4NwBPBh9TNHYHGwocXjc3F" +
            "wRPChwndkcrTxgWNj4aAAMDFCx2FQELTBU6JFQbBwlTNjkZAhoCGiEvVAAZCQE0OhhBTz8HPDoYQ08NHTF2HQFPDhY9NxgJTwMVdSMHCh0f" +
            "UyIzVA4DABwidgAATx4WNDJYTxwNBTB2FQELTB40OB0fGgASITNUGwcJUzM/GApPChwndgYKGQUWInpUHQoPHCMzBk8LDQc0dhUBC0wXMCI" +
            "RDBtMAyc5FgMKAQB1IhtPAg0YMHYNABpMEDogER0KCFMzJBsCTwEaJiIVBAofUzQ4EE8KHgE6JAdBZTwfMDcHCkNMHjQ9EU8WAwYndgcHBg" +
            "oHdTAGAAJMBz0/B08fHhwxIxcbHEwSOzJUBwoAA3UiHApPXzd1IhEMBwIcOTkTFk8LHHUwGx0YDQExdhUBC0wcJTMaTk8/BiUmGx0bTBwlM" +
            "xpCHAMGJzURTxwDHyAiHQABH191IhwOG0wENC9UGApMEDQ4VAwdCRIhM1QNChgHMCRUHx0DFyA1ABxPChwndgAHCkwQOjgHGgIJASZ4";

    private final String Secret1 = "0FuOM3HePRrlTyLd31v9lKtdZDqdfr+vQgPzENhSKuo=";
    private final String Secret2 = "DwEKBQULBgcIBgoMDA0JDw==";

    // Encoded using XOR key 'UVtools'
    // private final String Secret1 = "hQ36XB6yTk+zO02ysyiowt8yC1buK+nbLWyfY40EXoU=";
    // private final String Secret2 = "Wld+ampndVJecmVjYH5cWQ==";

    public CTBEncryptedFile(Integer Version) throws MSLAException {
        super();
        if (Version == null || Version <= 0)
            throw new MSLAException("File defaults do not have a version number.");

        Header = new CTBEncryptedFileHeader(Version);
        if (Header.getFileFields().getVersion() <= 0)
            throw new MSLAException("The mSLA file does not have a version number.");

        SlicerSettings = new CTBEncryptedFileSlicerSettings(Version);
        MachineName = new CTBFileMachineName();
        Disclaimer = new CTBFileDisclaimer();

        /*MachineName = new CTBFileMachineName();
        PrintParams = new CTBFilePrintParams(Version);
        SlicerInfo = new CTBFileSlicerInfo(Version);
        Layers = new CTBFileLayers(this);

        // Version 4 or later data
        if (Header.getFileFields().getVersion() >= 4) {
            PrintParamsV4 = new CTBFilePrintParamsV4(Version);
            Disclaimer = new CTBFileDisclaimer();

            // Version 5 or later data
            if (Header.getFileFields().getVersion() >= 5) ResinParams = new CTBFileResinParams(Version);
        }*/
    }

    public CTBEncryptedFile(DataInputStream stream) throws IOException, MSLAException {
        super();
        read(stream);
    }

    private Cipher initCipher(int mode) throws MSLAException {
        try {
            byte[] decodedKey = Base64.getDecoder().decode(Secret1);
            logger.fine("AES key size is " + decodedKey.length * Byte.SIZE); // Must be 256

            byte[] decodedIV = Base64.getDecoder().decode(Secret2);
            logger.fine("IV size is " + decodedIV.length * Byte.SIZE); // Must be 16

            SecretKey key = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
            IvParameterSpec iv = new IvParameterSpec(decodedIV);
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(mode, key, iv);
            return cipher;
        } catch (NoSuchPaddingException | NoSuchAlgorithmException |
                 InvalidKeyException | InvalidAlgorithmParameterException e) {
            throw new MSLAException("Can't init crypt cipher", e);
        }
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
            return new ByteArrayInputStream(initCipher(Cipher.DECRYPT_MODE).doFinal(encryptedBlock));
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

    public static byte[] XORCipher(byte[] text, byte[] key) {
        var output = new byte[text.length];
        for (int i = 0; i < text.length; i++) output[i] = (byte)(text[i] ^ key[i % key.length]);
        return output;
    }

    private byte[] generateSignature() throws MSLAException {
        var checksum = SlicerSettings.getFileFields().getChecksumValue();
        if (checksum == 0)
            throw new MSLAException("Can't check file signature, checksum is zero.");
        try {
            var checksumHash = MessageDigest.getInstance("SHA-256").digest(LongToBytes(checksum));
            var encryptedHash = initCipher(Cipher.ENCRYPT_MODE).doFinal(checksumHash);
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
        Header = new CTBEncryptedFileHeader(0); // Version is going to be read from file
        Header.read(stream, 0);
        if (Header.getFileFields().getVersion() == null)
            throw new MSLAException("Malformed file, Version is not identified");

        logger.info("CTB file version " + Header.getFileFields().getVersion());

        SlicerSettings = new CTBEncryptedFileSlicerSettings(Header.getFileFields().getVersion());
        SlicerSettings.read(new DataInputStream(readEncryptedBlock(
                stream,
                Header.getFileFields().getSettingsOffset(),
                Header.getFileFields().getSettingsSize()
        )), 0);

        /* Validate signature */
        try {
            // Read signature
            stream.reset();
            stream.skipBytes(Header.getFileFields().getSignatureOffset());
            var signature = stream.readNBytes(Header.getFileFields().getSignatureSize());
            logger.fine("Signature is " + Arrays.toString(signature));
            if (!Arrays.equals(signature, generateSignature()))
                throw new MSLAException("The file checksum does not match, malformed data.");
        } catch (IOException e) {
            throw new MSLAException("Can't check file signature, data read error.");
        }

        // Read disclaimer block
        if (SlicerSettings.getFileFields().getDisclaimerOffset() > 0) {
            Disclaimer = new CTBFileDisclaimer();
            Disclaimer.read(stream, SlicerSettings.getFileFields().getDisclaimerOffset());
            logger.info(Disclaimer.getFileFields().getDisclaimer());
        }

        // Read large preview
        if (SlicerSettings.getFileFields().getPreviewLargeOffset() > 0) {
            logger.info("Reading large preview");
            PreviewLarge = new CTBFilePreview(Header.getFileFields().getVersion(), CTBFilePreview.Type.Large);
            PreviewLarge.read(stream, SlicerSettings.getFileFields().getPreviewLargeOffset());
            var pixels = PreviewLarge.readImage(stream);
        }

        // Read small preview
        if (SlicerSettings.getFileFields().getPreviewSmallOffset() > 0) {
            logger.info("Reading small preview");
            PreviewSmall = new CTBFilePreview(Header.getFileFields().getVersion(), CTBFilePreview.Type.Small);
            PreviewSmall.read(stream, SlicerSettings.getFileFields().getPreviewSmallOffset());
            var pixels = PreviewSmall.readImage(stream);
        }

        if (Header.getFileFields().getVersion() >= 5 && SlicerSettings.getFileFields().getResinParametersAddress() > 0)
            Resin.read(stream, SlicerSettings.getFileFields().getResinParametersAddress());

        if (SlicerSettings.getFileFields().getMachineNameOffset() > 0) {
            MachineName.getFileFields().setMachineNameSize(SlicerSettings.getFileFields().getMachineNameSize());
            MachineName.read(stream, SlicerSettings.getFileFields().getMachineNameOffset());
        }

        /*
        Layers = new CTBFileLayers(this);
        Layers.read(stream, Header.getFileFields().getLayersDefinitionOffset());
        */
    }

    @Override public Class<? extends MSLALayerCodec<byte[]>> getCodec() { return null; }
    @Override public String getMachineName() { return MachineName.getFileFields().getMachineName(); }
    @Override public MSLAPreview getPreview(int index) throws MSLAException { return (index == 0) ? PreviewSmall : PreviewLarge; }
    @Override public MSLAPreview getLargePreview() throws MSLAException { return PreviewLarge; }
    @Override public void setPreview(int index, BufferedImage image) throws MSLAException {
        if (index == 0) PreviewSmall.setImage(image); else PreviewLarge.setImage(image);
    }
    @Override public boolean isMachineValid(MSLAFileDefaults defaults) { return false; }
    @Override public float getDPI() { return 0; }
    @Override public Size getResolution() { return null; }
    @Override public float getPixelSizeUm() { return 0; }
    @Override public boolean isValid() { return Header != null && SlicerSettings != null; }

    @Override
    public MSLAFileLayers<? extends MSLAFileLayer, ?> getLayers() {
        return null;
    }

    @Override
    public void addLayer(MSLALayerEncodeReader reader, MSLALayerEncoder.Callback<byte[]> callback) throws MSLAException {

    }

    @Override
    public boolean readLayer(MSLALayerDecodeWriter writer, int layer) throws MSLAException {
        return false;
    }

    @Override
    public void write(OutputStream stream) throws MSLAException {

    }
}
