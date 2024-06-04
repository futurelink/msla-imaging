package futurelink.msla.formats.chitubox;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.MSLAFileGeneric;
import futurelink.msla.formats.chitubox.tables.*;
import futurelink.msla.formats.iface.*;
import futurelink.msla.formats.iface.annotations.MSLAOptionContainer;
import futurelink.msla.formats.io.FileFieldsException;
import futurelink.msla.utils.Size;
import lombok.Getter;

import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.logging.Logger;

public class CTBFile extends MSLAFileGeneric<byte[]> {
    private final Logger logger = Logger.getLogger(CTBFile.class.getName());

    /* File sections */
    @Getter @MSLAOptionContainer private CTBFileHeader Header = null;
    @Getter @MSLAOptionContainer private CTBFilePrintParams PrintParams = null;
    @Getter @MSLAOptionContainer private CTBFileSlicerInfo SlicerInfo = null;
    private CTBFileMachineName MachineName = null;
    private CTBFileDisclaimer Disclaimer = null;
    @Getter private CTBFilePrintParamsV4 PrintParamsV4 = null;
    private CTBFileResinParams ResinParams = null;
    private final CTBFilePreview PreviewSmall = new CTBFilePreview(CTBFilePreview.Type.Small);
    private final CTBFilePreview PreviewLarge = new CTBFilePreview(CTBFilePreview.Type.Large);
    @Getter private CTBFileLayers Layers = null;

    public CTBFile(Byte Version) throws MSLAException {
        super();
        if (Version == null || Version <= 0)
            throw new MSLAException("File defaults do not have a version number.");

        Header = new CTBFileHeader(Version);
        if (Header.getFileFields().getVersion() <= 0)
            throw new MSLAException("The MSLA file does not have a version number.");

        MachineName = new CTBFileMachineName();
        PrintParams = new CTBFilePrintParams(Version);
        SlicerInfo = new CTBFileSlicerInfo(Version);
        Layers = new CTBFileLayers(this);

        // Version 4 or later data
        if (Header.getFileFields().getVersion() >= 4) {
            PrintParamsV4 = new CTBFilePrintParamsV4(Version);
            Disclaimer = new CTBFileDisclaimer();

            // Version 5 or later data
            if (Header.getFileFields().getVersion() >= 5) ResinParams = new CTBFileResinParams(Version);
        }
    }

    public CTBFile(DataInputStream stream) throws IOException, MSLAException {
        super();
        read(stream);
    }

    @Override public Class<? extends MSLALayerCodec<byte[]>> getCodec() { return CTBFileCodec.class; }
    @Override public MSLAPreview getPreview(int index) {
        if (index == 0) return PreviewLarge;
        else return PreviewSmall;
    }
    @Override public MSLAPreview getLargePreview() throws MSLAException { return PreviewLarge; }
    @Override public float getDPI() { return 0; }
    @Override public Size getResolution() { return Header.getFileFields().getResolution(); }
    @Override public float getPixelSizeUm() { return 0; }

    @Override
    public boolean isMachineValid(MSLAFileDefaults defaults) {
        return defaults.getFileClass().equals(this.getClass()) &&
                ((getResolution() == null) || defaults.getResolution().equals(getResolution()));
    }

    @Override
    public void addLayer(
            MSLALayerEncodeReader reader,
            MSLALayerEncoder.Callback<byte[]> callback) throws MSLAException
    {
        var params = new HashMap<String, Object>();
        params.put("EncryptionKey", Header.getFileFields().getEncryptionKey());
        Layers.add(getEncodersPool(), reader, params, callback);
    }

    @Override public boolean readLayer(MSLALayerDecodeWriter writer, int layer) throws MSLAException {
        var layerData = new CTBFileCodec.Input(Layers.get(layer).getFileFields().getData());
        var params = new HashMap<String, Object>();
        params.put("EncryptionKey", Header.getFileFields().getEncryptionKey());
        return getDecodersPool().decode(layer, writer, layerData, params);
    }

    /* Internal read method */
    @SuppressWarnings("unused")
    private void read(DataInputStream stream) throws MSLAException {
        Header = new CTBFileHeader(0); // Version is going to be read from file
        Header.read(stream, 0);
        if (Header.getFileFields().getVersion() == null)
            throw new MSLAException("Malformed file, Version is not identified");

        logger.info("CTB file version " + Header.getFileFields().getVersion());

        PrintParams = new CTBFilePrintParams(Header.getFileFields().getVersion());
        SlicerInfo = new CTBFileSlicerInfo(Header.getFileFields().getVersion());
        MachineName = new CTBFileMachineName();

        if (Header.getFileFields().getPrintParametersOffset() > 0)
            PrintParams.read(stream, Header.getFileFields().getPrintParametersOffset());
        else throw new MSLAException("Malformed file, PrintParameters section offset is missing");

        if (Header.getFileFields().getSlicerOffset() > 0)
            SlicerInfo.read(stream, Header.getFileFields().getSlicerOffset());
        else throw new MSLAException("Malformed file, SlicerOffset section offset is missing");

        MachineName.getFileFields().setMachineNameSize(SlicerInfo.getFileFields().getMachineNameSize());
        MachineName.read(stream, SlicerInfo.getFileFields().getMachineNameOffset());

        logger.info("File is for machine '" + MachineName.getFileFields().getMachineName() + "'");

        // Read version 4 or later data
        if (Header.getFileFields().getVersion() >= 4) {
            logger.info("Reading print parameters for version 4 or later");
            if (SlicerInfo.getFileFields().getPrintParametersV4Offset() == 0)
                throw new MSLAException("Malformed file, PrintParametersV4 section offset is missing");

            PrintParamsV4 = new CTBFilePrintParamsV4(Header.getFileFields().getVersion());
            PrintParamsV4.read(stream, SlicerInfo.getFileFields().getPrintParametersV4Offset());

            logger.info("Reading disclaimer");
            Disclaimer = new CTBFileDisclaimer();
            Disclaimer.read(stream, PrintParamsV4.getFileFields().getDisclaimerOffset());

            // Read version 5 or later resin settings
            if (Header.getFileFields().getVersion() >= 5 && PrintParamsV4.getFileFields().getResinParametersOffset() > 0) {
                logger.info("Reading resin parameters for version 5 or later");
                ResinParams = new CTBFileResinParams(Header.getFileFields().getVersion());
                ResinParams.read(stream, PrintParamsV4.getFileFields().getResinParametersOffset());
            }
        }

        // Read large preview
        logger.info("Reading large preview");
        if (Header.getFileFields().getPreviewLargeOffset() > 0) {
            PreviewLarge.read(stream, Header.getFileFields().getPreviewLargeOffset());
            var pixels = PreviewLarge.readImage(stream);
        }

        // Read small preview
        logger.info("Reading small preview");
        if (Header.getFileFields().getPreviewSmallOffset() > 0) {
            PreviewSmall.read(stream, Header.getFileFields().getPreviewSmallOffset());
            var pixels = PreviewSmall.readImage(stream);
        }

        Layers = new CTBFileLayers(this);
        Layers.read(stream, Header.getFileFields().getLayersDefinitionOffset());
    }

    @Override public void write(OutputStream stream) throws MSLAException {
        /* Pre-calculate block internal offsets */
        var offset = 0;
        try {
            offset += Header.getDataLength();
            Header.getFileFields().setPreviewLargeOffset(offset);
            offset += PreviewLarge.getDataLength();
            Header.getFileFields().setPreviewSmallOffset(offset);
            offset += PreviewSmall.getDataLength();
            Header.getFileFields().setPrintParametersOffset(offset);
            Header.getFileFields().setPrintParametersSize(PrintParams.getDataLength());
            offset += Header.getFileFields().getPrintParametersSize();
            Header.getFileFields().setSlicerOffset(offset);
            Header.getFileFields().setSlicerSize(SlicerInfo.getDataLength());
            offset += Header.getFileFields().getSlicerSize();

            PreviewLarge.getFileFields().setImageOffset(Header.getFileFields().getPreviewLargeOffset() + 32);
            PreviewSmall.getFileFields().setImageOffset(Header.getFileFields().getPreviewSmallOffset() + 32);

            SlicerInfo.getFileFields().setVersion(Header.getFileFields().getVersion());
            SlicerInfo.getFileFields().setMachineNameSize(MachineName.getFileFields().getMachineNameSize());
            SlicerInfo.getFileFields().setMachineNameOffset(offset);
            offset += MachineName.getDataLength();

            // For version 4 and greater
            if (PrintParamsV4 != null) {
                // Calculate disclaimer offset and size
                PrintParamsV4.getFileFields().setDisclaimerOffset(offset);
                PrintParamsV4.getFileFields().setDisclaimerLength(Disclaimer.getFileFields().getDisclaimer().length());
                offset += Disclaimer.getDataLength();
                SlicerInfo.getFileFields().setPrintParametersV4Offset(offset);
                PrintParamsV4.getFileFields().setLastLayerIndex(Layers.count()-1);
                offset += PrintParamsV4.getDataLength();
                if (ResinParams != null) {
                    PrintParamsV4.getFileFields().setResinParametersOffset(offset);
                    offset += ResinParams.getDataLength();
                } else PrintParamsV4.getFileFields().setResinParametersOffset(0);
            } else {
                SlicerInfo.getFileFields().setPrintParametersV4Offset(0);
            }

            Header.getFileFields().setLayersDefinitionOffset(offset);
            Header.getFileFields().setTotalHeightMillimeter(Layers.count() * Header.getFileFields().getLayerHeightMillimeter());
            Header.getFileFields().setLayerCount(Layers.count());
            Header.getFileFields().setPrintTime(0);
        } catch (FileFieldsException e) {
            throw new MSLAException("Error writing file fields", e);
        }

        /* Write blocks */
        Header.write(stream);
        PreviewLarge.write(stream);
        PreviewSmall.write(stream);
        PrintParams.write(stream);
        SlicerInfo.write(stream);
        MachineName.write(stream);
        if (PrintParamsV4 != null) {
            Disclaimer.write(stream);
            PrintParamsV4.write(stream);
        }
        if (ResinParams != null) ResinParams.write(stream);

        try {
            // Write brief layer definitions
            var wholeBriefLayerDefSize = Layers.count() * CTBFileLayerDef.BRIEF_TABLE_SIZE;
            for (var i = 0; i < Layers.count(); i++) {
                var def = Layers.get(i).getFileFields();
                def.setDataAddress(wholeBriefLayerDefSize +
                        offset + CTBFileLayerDef.BRIEF_TABLE_SIZE +
                        ((def.getExtra() != null) ? CTBFileLayerDefExtra.TABLE_SIZE : 0)
                );
                def.setDataSize(def.getData().length);
                def.getParent().setBriefMode(false); // Calculate data length for the whole block with Data & Extra
                var dataLength = def.getParent().getDataLength();
                var extra = def.getParent().getFileFields().getExtra();
                if (extra != null) extra.getFileFields().setTotalSize(dataLength);
                def.getParent().setBriefMode(true); // Write just in brief mode w/o Data & Extra
                def.getParent().write(stream);
                def.getParent().setBriefMode(false);
                offset += dataLength;
            }

            // Write whole layer definitions
            for (var i = 0; i < Layers.count(); i++) {
                var def = Layers.get(i).getFileFields();
                def.getParent().setBriefMode(false);
                def.getParent().write(stream);
            }
        } catch (FileFieldsException e) {
            throw new MSLAException("Error writing file fields", e);
        }
    }

    @Override public boolean isValid() { return (Header != null) && (SlicerInfo != null) && (PrintParams != null); }

    @Override
    public String getMachineName() {
        return MachineName.getFileFields().getMachineName();
    }

    @Override
    public void setPreview(int index, BufferedImage image) {
        if (index == 0) PreviewLarge.setImage(image);
        else PreviewSmall.setImage(image);
    }

    @Override
    public void reset(MSLAFileDefaults defaults) throws MSLAException {
        super.reset(defaults);
        if (isMachineValid(defaults)) {
            defaults.setFields(Header.getName(), Header.getFileFields());
            defaults.setFields(SlicerInfo.getName(), SlicerInfo.getFileFields());
            defaults.setFields(MachineName.getName(), MachineName.getFileFields());
            defaults.setFields(PrintParams.getName(), PrintParams.getFileFields());
            defaults.setFields(PrintParamsV4.getName(), PrintParamsV4.getFileFields());
            getLayers().setDefaults(defaults.getLayerDefaults());
        } else throw new MSLAException("Defaults of '" + defaults.getMachineFullName() + "' not applicable to this file");
    }

    @Override
    public String toString() {
        return "----- Header -----\n" + Header + "\n" +
                "----- Slicer info ----\n" + SlicerInfo + "\n" +
                MachineName + "\n" +
                ((Disclaimer != null) ? Disclaimer + "\n" : "") +
                "----- Printer params ----\n" + PrintParams + "\n" +
                ((PrintParamsV4 != null) ? "----- Printer params V4 ----\n" + PrintParamsV4 + "\n" : "") +
                ((ResinParams != null) ? "----- Resin params V4 ----\n" + ResinParams + "\n" : "") +
                "----- Small preview ----\n" + PreviewSmall + "\n" +
                "----- Larger preview ----\n" + PreviewLarge + "\n" +
                Layers;
    }
}
