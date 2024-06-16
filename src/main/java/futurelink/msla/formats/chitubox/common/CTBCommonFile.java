package futurelink.msla.formats.chitubox.common;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.MSLAFileGeneric;
import futurelink.msla.formats.chitubox.common.tables.*;
import futurelink.msla.formats.iface.*;
import futurelink.msla.formats.iface.options.MSLAOptionContainer;
import futurelink.msla.formats.io.FileFieldsException;
import futurelink.msla.utils.Size;
import lombok.Getter;

import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.logging.Logger;

public class CTBCommonFile extends MSLAFileGeneric<byte[]> {
    private final Logger logger = Logger.getLogger(CTBCommonFile.class.getName());

    /* File sections */
    @Getter @MSLAOptionContainer private CTBFileHeader Header = null;
    @Getter @MSLAOptionContainer private CTBFilePrintParams PrintParams = null;
    @Getter @MSLAOptionContainer private CTBFileSlicerInfo SlicerInfo = null;
    private CTBFileMachineName MachineName = null;
    private CTBFileDisclaimer Disclaimer = null;
    @Getter private CTBFilePrintParamsV4 PrintParamsV4 = null;
    private CTBFileResinParams ResinParams = null;
    private CTBFilePreview PreviewSmall = null;
    private CTBFilePreview PreviewLarge = null;
    @Getter private CTBFileLayers Layers = null;

    public CTBCommonFile(MSLAFileProps initialProps) throws MSLAException {
        super(initialProps);
        var Version = (byte) initialProps.getByte("Version");
        if (Version <= 0) throw new MSLAException("File defaults do not have a version number.");
        Header = new CTBFileHeader(Version, initialProps);

        PreviewLarge = new CTBFilePreview(Header.getBlockFields().getVersion(), CTBFilePreview.Type.Large, false);
        PreviewSmall = new CTBFilePreview(Header.getBlockFields().getVersion(), CTBFilePreview.Type.Small, false);

        MachineName = new CTBFileMachineName();
        PrintParams = new CTBFilePrintParams(Version);
        SlicerInfo = new CTBFileSlicerInfo(Version);
        Layers = new CTBFileLayers(this);

        // Version 4 or later data
        if (Header.getBlockFields().getVersion() >= 4) {
            PrintParamsV4 = new CTBFilePrintParamsV4(Version);
            Disclaimer = new CTBFileDisclaimer();

            // Version 5 or later data
            if (Header.getBlockFields().getVersion() >= 5) ResinParams = new CTBFileResinParams(Version);
        }
    }

    public CTBCommonFile(DataInputStream stream) throws IOException, MSLAException {
        super(null);
        read(stream);
    }

    @Override public Class<? extends MSLALayerCodec<byte[]>> getCodec() { return CTBCommonFileCodec.class; }
    @Override public Short getPreviewsNumber() { return 2; }
    @Override public MSLAPreview getPreview(int index) {
        if (index == 0) return PreviewLarge;
        else return PreviewSmall;
    }
    @Override public MSLAPreview getLargePreview() { return PreviewLarge; }
    @Override public float getDPI() { return 0; }
    @Override public Size getResolution() { return Header.getBlockFields().getResolution(); }

    @Override
    public boolean isMachineValid(MSLAFileDefaults defaults) {
        try {
            return defaults.getFileClass().equals(this.getClass()) &&
                    ((getResolution() == null) || defaults.getResolution().equals(getResolution()));
        } catch (MSLAException e) {
            return false;
        }
    }

    @Override
    public void addLayer(
            MSLALayerEncodeReader reader,
            MSLALayerEncoder.Callback<byte[]> callback) throws MSLAException
    {
        var params = new HashMap<String, Object>();
        params.put("EncryptionKey", Header.getBlockFields().getEncryptionKey());
        Layers.add(getEncodersPool(), reader, params, callback);
    }

    @Override public boolean readLayer(MSLALayerDecodeWriter writer, int layer) throws MSLAException {
        var layerData = new CTBCommonFileCodec.Input(Layers.get(layer).getBlockFields().getData());
        var params = new HashMap<String, Object>();
        params.put("EncryptionKey", Header.getBlockFields().getEncryptionKey());
        return getDecodersPool().decode(layer, writer, layerData, params);
    }

    /* Internal read method */
    @SuppressWarnings("unused")
    private void read(DataInputStream stream) throws MSLAException {
        Header = new CTBFileHeader(0, null); // Version is going to be read from file
        Header.read(stream, 0);
        if (Header.getBlockFields().getVersion() == null)
            throw new MSLAException("Malformed file, Version is not identified");

        logger.info("CTB file version " + Header.getBlockFields().getVersion());

        PrintParams = new CTBFilePrintParams(Header.getBlockFields().getVersion());
        SlicerInfo = new CTBFileSlicerInfo(Header.getBlockFields().getVersion());
        MachineName = new CTBFileMachineName();

        if (Header.getBlockFields().getPrintParametersOffset() > 0)
            PrintParams.read(stream, Header.getBlockFields().getPrintParametersOffset());
        else throw new MSLAException("Malformed file, PrintParameters section offset is missing");

        if (Header.getBlockFields().getSlicerOffset() > 0)
            SlicerInfo.read(stream, Header.getBlockFields().getSlicerOffset());
        else throw new MSLAException("Malformed file, SlicerOffset section offset is missing");

        MachineName.getBlockFields().setMachineNameSize(SlicerInfo.getBlockFields().getMachineNameSize());
        MachineName.read(stream, SlicerInfo.getBlockFields().getMachineNameOffset());

        logger.info("File is for machine '" + MachineName.getBlockFields().getMachineName() + "'");

        // Read version 4 or later data
        if (Header.getBlockFields().getVersion() >= 4) {
            logger.info("Reading print parameters for version 4 or later");
            if (SlicerInfo.getBlockFields().getPrintParametersV4Offset() == 0)
                throw new MSLAException("Malformed file, PrintParametersV4 section offset is missing");

            PrintParamsV4 = new CTBFilePrintParamsV4(Header.getBlockFields().getVersion());
            PrintParamsV4.read(stream, SlicerInfo.getBlockFields().getPrintParametersV4Offset());

            logger.info("Reading disclaimer");
            Disclaimer = new CTBFileDisclaimer();
            Disclaimer.read(stream, PrintParamsV4.getBlockFields().getDisclaimerOffset());

            // Read version 5 or later resin settings
            if (Header.getBlockFields().getVersion() >= 5 && PrintParamsV4.getBlockFields().getResinParametersOffset() > 0) {
                logger.info("Reading resin parameters for version 5 or later");
                ResinParams = new CTBFileResinParams(Header.getBlockFields().getVersion());
                ResinParams.read(stream, PrintParamsV4.getBlockFields().getResinParametersOffset());
            }
        }

        // Read large preview
        if (Header.getBlockFields().getPreviewLargeOffset() > 0) {
            logger.info("Reading large preview");
            PreviewLarge = new CTBFilePreview(Header.getBlockFields().getVersion(), CTBFilePreview.Type.Large, false);
            PreviewLarge.read(stream, Header.getBlockFields().getPreviewLargeOffset());
            var pixels = PreviewLarge.readImage(stream);
        }

        // Read small preview
        if (Header.getBlockFields().getPreviewSmallOffset() > 0) {
            logger.info("Reading small preview");
            PreviewSmall = new CTBFilePreview(Header.getBlockFields().getVersion(), CTBFilePreview.Type.Small, false);
            PreviewSmall.read(stream, Header.getBlockFields().getPreviewSmallOffset());
            var pixels = PreviewSmall.readImage(stream);
        }

        Layers = new CTBFileLayers(this);
        Layers.read(stream, Header.getBlockFields().getLayersDefinitionOffset());
    }

    @Override public void write(OutputStream stream) throws MSLAException {
        /* Pre-calculate block internal offsets */
        var offset = 0;
        try {
            offset += Header.getDataLength();
            Header.getBlockFields().setPreviewLargeOffset(offset);
            offset += PreviewLarge.getDataLength();
            Header.getBlockFields().setPreviewSmallOffset(offset);
            offset += PreviewSmall.getDataLength();
            Header.getBlockFields().setPrintParametersOffset(offset);
            Header.getBlockFields().setPrintParametersSize(PrintParams.getDataLength());
            offset += Header.getBlockFields().getPrintParametersSize();
            Header.getBlockFields().setSlicerOffset(offset);
            Header.getBlockFields().setSlicerSize(SlicerInfo.getDataLength());
            offset += Header.getBlockFields().getSlicerSize();

            PreviewLarge.getBlockFields().setImageOffset(Header.getBlockFields().getPreviewLargeOffset() + 32);
            PreviewSmall.getBlockFields().setImageOffset(Header.getBlockFields().getPreviewSmallOffset() + 32);

            SlicerInfo.getBlockFields().setVersion(Header.getBlockFields().getVersion());
            SlicerInfo.getBlockFields().setMachineNameSize(MachineName.getBlockFields().getMachineNameSize());
            SlicerInfo.getBlockFields().setMachineNameOffset(offset);
            offset += MachineName.getDataLength();

            // For version 4 and greater
            if (PrintParamsV4 != null) {
                // Calculate disclaimer offset and size
                PrintParamsV4.getBlockFields().setDisclaimerOffset(offset);
                PrintParamsV4.getBlockFields().setDisclaimerLength(Disclaimer.getBlockFields().getDisclaimer().length());
                offset += Disclaimer.getDataLength();
                SlicerInfo.getBlockFields().setPrintParametersV4Offset(offset);
                PrintParamsV4.getBlockFields().setLastLayerIndex(Layers.count()-1);
                offset += PrintParamsV4.getDataLength();
                if (ResinParams != null) {
                    PrintParamsV4.getBlockFields().setResinParametersOffset(offset);
                    offset += ResinParams.getDataLength();
                } else PrintParamsV4.getBlockFields().setResinParametersOffset(0);
            } else {
                SlicerInfo.getBlockFields().setPrintParametersV4Offset(0);
            }

            Header.getBlockFields().setLayersDefinitionOffset(offset);
            Header.getBlockFields().setTotalHeightMillimeter(Layers.count() * Header.getBlockFields().getLayerHeightMillimeter());
            Header.getBlockFields().setLayerCount(Layers.count());
            Header.getBlockFields().setPrintTime(0);
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
                var def = Layers.get(i).getBlockFields();
                def.setDataAddress(wholeBriefLayerDefSize +
                        offset + CTBFileLayerDef.BRIEF_TABLE_SIZE +
                        ((def.getExtra() != null) ? CTBFileLayerDefExtra.TABLE_SIZE : 0)
                );
                def.setDataSize(def.getData().length);
                def.getParent().setBriefMode(false); // Calculate data length for the whole block with Data & Extra
                var dataLength = def.getParent().getDataLength();
                var extra = def.getParent().getBlockFields().getExtra();
                if (extra != null) extra.getBlockFields().setTotalSize(dataLength);
                def.getParent().setBriefMode(true); // Write just in brief mode w/o Data & Extra
                def.getParent().write(stream);
                def.getParent().setBriefMode(false);
                offset += dataLength;
            }

            // Write whole layer definitions
            for (var i = 0; i < Layers.count(); i++) {
                var def = Layers.get(i).getBlockFields();
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
        return MachineName.getBlockFields().getMachineName();
    }

    @Override
    public void setPreview(int index, BufferedImage image) {
        if (index == 0) PreviewLarge.setImage(image);
        else PreviewSmall.setImage(image);
    }

    @Override
    public void reset(MSLAFileDefaults defaults) throws MSLAException {
        super.reset(defaults);
        defaults.setFields(Header.getBlockFields());
        defaults.setFields(SlicerInfo.getBlockFields());
        defaults.setFields(MachineName.getBlockFields());
        defaults.setFields(PrintParams.getBlockFields());
        defaults.setFields(PrintParamsV4.getBlockFields());
        getLayers().setDefaults(defaults.getLayerDefaults());
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
