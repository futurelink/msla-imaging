package io.msla.formats.chitubox.encrypted;

import io.msla.formats.MSLAException;
import io.msla.formats.MSLAFileGeneric;
import io.msla.formats.chitubox.CTBCrypto;
import io.msla.formats.chitubox.common.CTBCommonFileCodec;
import io.msla.formats.chitubox.encrypted.tables.CTBEncryptedFileHeader;
import io.msla.formats.chitubox.encrypted.tables.CTBEncryptedFileLayers;
import io.msla.formats.chitubox.encrypted.tables.CTBEncryptedFileSlicerSettings;
import io.msla.formats.chitubox.common.tables.*;
import io.msla.formats.iface.*;
import io.msla.formats.iface.options.MSLAOptionContainer;
import io.msla.formats.io.FileFieldsException;
import io.msla.utils.Size;
import lombok.Getter;

import javax.crypto.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.security.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
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
        PreviewLarge = new CTBFilePreview(Version, CTBFilePreview.Type.Large, true);
        PreviewSmall = new CTBFilePreview(Version, CTBFilePreview.Type.Small, true);
        MachineName = new CTBFileMachineName();
        Disclaimer = new CTBFileDisclaimer();
        Layers = new CTBEncryptedFileLayers(this);
        if (Version >= 5) Resin = new CTBFileResinParams(Version);
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
            throw new MSLAException("Can't read encrypted data", e);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new MSLAException("Can't decrypt file", e);
        }
    }

    public void writeEncryptedBlock(MSLAFileBlock block, OutputStream stream) throws MSLAException {
        try {
            var byteStream = new ByteArrayOutputStream();
            block.write(byteStream);
            stream.write(CTBCrypto.initCipher(Cipher.ENCRYPT_MODE).doFinal(byteStream.toByteArray()));
        } catch (IOException e) {
            throw new MSLAException("Can't write encrypted block", e);
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
            logger.fine("Read signature was " + Arrays.toString(signature));
            if (!Arrays.equals(signature, generateSignature()))
                throw new MSLAException("The file checksum does not match, malformed data.");
        } catch (IOException e) {
            throw new MSLAException("Can't check file signature, data read error.");
        }

        // Read disclaimer block
        if (SlicerSettings.getBlockFields().getDisclaimerOffset() > 0) {
            Disclaimer = new CTBFileDisclaimer();
            Disclaimer.read(stream, SlicerSettings.getBlockFields().getDisclaimerOffset());
        }

        // Read machine name
        if (SlicerSettings.getBlockFields().getMachineNameOffset() > 0) {
            MachineName.getBlockFields().setMachineNameSize(SlicerSettings.getBlockFields().getMachineNameSize());
            MachineName.read(stream, SlicerSettings.getBlockFields().getMachineNameOffset());
        }

        // Read large preview
        if (SlicerSettings.getBlockFields().getPreviewLargeOffset() > 0) {
            logger.fine("Reading large preview at " + SlicerSettings.getBlockFields().getPreviewLargeOffset());
            PreviewLarge = new CTBFilePreview(Header.getBlockFields().getVersion(), CTBFilePreview.Type.Large, true);
            PreviewLarge.read(stream, SlicerSettings.getBlockFields().getPreviewLargeOffset());
            var pixels = PreviewLarge.readImage(stream);
        }

        // Read small preview
        if (SlicerSettings.getBlockFields().getPreviewSmallOffset() > 0) {
            logger.fine("Reading small preview at " + SlicerSettings.getBlockFields().getPreviewSmallOffset());
            PreviewSmall = new CTBFilePreview(Header.getBlockFields().getVersion(), CTBFilePreview.Type.Small, true);
            PreviewSmall.read(stream, SlicerSettings.getBlockFields().getPreviewSmallOffset());
            var pixels = PreviewSmall.readImage(stream);
        }

        // Read resin settings
        if (Header.getBlockFields().getVersion() >= 5 && SlicerSettings.getBlockFields().getResinParametersOffset() > 0)
            Resin.read(stream, SlicerSettings.getBlockFields().getResinParametersOffset());

        // Read layers table
        Layers = new CTBEncryptedFileLayers(this);
        Layers.read(stream, SlicerSettings.getBlockFields().getLayersDefinitionOffset());
    }

    @Override public Class<? extends MSLALayerCodec<byte[]>> getCodec() { return CTBCommonFileCodec.class; }
    @Override public String getMachineName() { return MachineName.getBlockFields().getMachineName(); }
    @Override public Short getPreviewsNumber() { return 2; }
    @Override public MSLAPreview getPreview(int index) throws MSLAException { return (index == 0) ? PreviewLarge : PreviewSmall; }
    @Override public MSLAPreview getLargePreview() { return PreviewLarge; }
    @Override public void setPreview(int index, BufferedImage image) {
        if (index == 0) PreviewSmall.setImage(image); else PreviewLarge.setImage(image);
    }

    @Override
    public void reset(MSLAFileDefaults defaults) throws MSLAException {
        super.reset(defaults);
        defaults.setFields(Header.getBlockFields());
        defaults.setFields(MachineName.getBlockFields());
        defaults.setFields(SlicerSettings.getBlockFields());
        getLayers().setDefaults(defaults.getLayerDefaults());
    }

    @Override public boolean isMachineValid(MSLAFileDefaults defaults) {
        return defaults.getFileClass().equals(this.getClass()) &&
                ((getResolution() == null) || Objects.equals(defaults.getResolution(), getResolution()));
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
        var offset = 0;
        try {
            offset += Header.getDataLength();

            // Calculate slicer settings length
            logger.fine("Settings offset:" + offset);
            Header.getBlockFields().setSettingsOffset(offset);
            offset += SlicerSettings.getDataLength();

            // Larger preview length
            logger.fine("Larger preview offset:" + offset);
            SlicerSettings.getBlockFields().setPreviewLargeOffset(offset);
            PreviewLarge.getBlockFields().setImageOffset(offset + 16);
            offset += PreviewLarge.getDataLength();

            // Smaller preview length
            logger.fine("Smaller preview offset:" + offset);
            SlicerSettings.getBlockFields().setPreviewSmallOffset(offset);
            PreviewSmall.getBlockFields().setImageOffset(offset + 16);
            offset += PreviewSmall.getDataLength();

            // Calculate machine name length
            logger.fine("Machine name offset:" + offset);
            SlicerSettings.getBlockFields().setMachineNameSize(MachineName.getBlockFields().getMachineNameSize());
            SlicerSettings.getBlockFields().setMachineNameOffset(offset);
            offset += MachineName.getDataLength();

            // Calculate disclaimer length
            logger.fine("Disclaimer offset:" + offset);
            SlicerSettings.getBlockFields().setDisclaimerOffset(offset);
            SlicerSettings.getBlockFields().setDisclaimerLength(Disclaimer.getBlockFields().getDisclaimer().length());
            offset += Disclaimer.getDataLength();

            if (Resin != null) {
                logger.fine("Resin parameters offset:" + offset);
                SlicerSettings.getBlockFields().setResinParametersOffset(offset);
                offset += Resin.getDataLength();
            } else SlicerSettings.getBlockFields().setResinParametersOffset(0);

            // Precalculate necessary parameters
            SlicerSettings.getBlockFields().setLayersDefinitionOffset(offset);
            SlicerSettings.getBlockFields().setPrintTime(0);
            SlicerSettings.getBlockFields().setTotalHeightMillimeter(Layers.count() * SlicerSettings.getBlockFields().getLayerHeight());
            SlicerSettings.getBlockFields().setLayerCount(Layers.count());
            SlicerSettings.getBlockFields().setLastLayerIndex(Layers.count()-1);

            // Calculate layer definitions length
            offset += Layers.getDataLength();

            var layerDataOffset = offset; // First layer goes immediately after layer pointers table
            for (var i = 0; i < Layers.count(); i++) {
                var layerDef = Layers.get(i);
                Layers.getBlockFields().getLayerPointers().get(i).setLayerOffset(layerDataOffset);
                Layers.getBlockFields().getLayerPointers().get(i).setPageNumber(0);
                layerDataOffset = offset + layerDef.getDataLength();

                logger.fine("Offset of layer " + i + ":" + offset);
                layerDef.getBlockFields().setDataAddress(88 + offset);
                layerDef.getBlockFields().setPageNumber(0); // Set to 0, but for large(?) files should be something else
                layerDef.getBlockFields().setPositionZ(i * SlicerSettings.getBlockFields().getLayerHeight());
                offset += layerDef.getDataLength();
            }

            logger.fine("Calculated signature offset:" + offset);

            // Calculate signature and set pointer in header
            Header.getBlockFields().setSignatureOffset(offset);
            Header.getBlockFields().setSignatureSize(32);

            // Set header's settings size variable after everything was calculated
            Header.getBlockFields().setSettingsSize(SlicerSettings.getDataLength());
        } catch (FileFieldsException e) {
            throw new MSLAException("Error writing file fields", e);
        }

        /* Write blocks */
        Header.write(stream);
        writeEncryptedBlock(SlicerSettings, stream);
        PreviewLarge.write(stream);
        PreviewSmall.write(stream);
        MachineName.write(stream);
        Disclaimer.write(stream);
        if (Resin != null) Resin.write(stream);
        Layers.write(stream);
        for (var i = 0; i < Layers.count(); i++) {
            var layerDef = Layers.get(i);
            layerDef.write(stream);
        }

        var signature = generateSignature();
        logger.fine("Writing signature: " + Arrays.toString(signature));
        try {
            stream.write(signature);
        } catch(IOException e) {
            throw new MSLAException("Error writing signature", e);
        }
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
