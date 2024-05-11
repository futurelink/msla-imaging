package futurelink.msla.formats.chitubox;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.MSLAFileGeneric;
import futurelink.msla.formats.MSLAOptionMapper;
import futurelink.msla.formats.chitubox.tables.*;
import futurelink.msla.formats.iface.*;
import futurelink.msla.formats.iface.annotations.MSLAOptionContainer;
import futurelink.msla.formats.utils.FileFieldsException;
import futurelink.msla.formats.utils.OptionMapper;
import futurelink.msla.formats.utils.Size;
import lombok.Getter;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

public class CTBFile extends MSLAFileGeneric<byte[]> {
    private final Logger logger = Logger.getLogger(CTBFile.class.getName());
    @Getter private final MSLAOptionMapper options;

    /* File sections */
    @Getter @MSLAOptionContainer private CTBFileHeader Header = null;
    @Getter @MSLAOptionContainer private CTBFilePrintParams PrintParams = null;
    @Getter @MSLAOptionContainer private CTBFileSlicerInfo SlicerInfo = null;
    private CTBFileMachineName MachineName = null;
    private CTBFileDisclaimer Disclaimer = null;
    private CTBFilePrintParamsV4 PrintParamsV4 = null;
    private CTBFileResinParams ResinParams = null;
    private final CTBFilePreview PreviewSmall = new CTBFilePreview(CTBFilePreview.Type.Small);
    private final CTBFilePreview PreviewLarge = new CTBFilePreview(CTBFilePreview.Type.Large);
    private final ArrayList<CTBFileLayerDef> LayerDefinition = new ArrayList<>();

    public CTBFile(MSLAFileDefaults defaults) throws MSLAException {
        super();
        var Version = defaults.getOptionByte(null, "Version");
        if (Version == null || Version <= 0)
            throw new MSLAException("File defaults do not have a version number.");

        Header = new CTBFileHeader(Version, defaults);
        if (Header.getFileFields().getVersion() <= 0)
            throw new MSLAException("The MSLA file does not have a version number.");

        MachineName = new CTBFileMachineName(defaults);
        PrintParams = new CTBFilePrintParams(Version, defaults);
        SlicerInfo = new CTBFileSlicerInfo(Version, defaults);

        // Version 4 or later data
        if (Header.getFileFields().getVersion() >= 4) {
            PrintParamsV4 = new CTBFilePrintParamsV4(Version, defaults);
            Disclaimer = new CTBFileDisclaimer();

            // Version 5 or later data
            if (Header.getFileFields().getVersion() >= 5) ResinParams = new CTBFileResinParams(Version);
        }

        options = new OptionMapper(this);
    }

    public CTBFile(FileInputStream stream) throws IOException, MSLAException {
        super();
        read(stream);
        options = new OptionMapper(this);
    }

    @Override public Class<? extends MSLALayerCodec<byte[]>> getCodec() { return CTBFileCodec.class; }
    @Override public MSLAPreview getPreview(int index) {
        if (index == 0) return PreviewLarge;
        else return PreviewSmall;
    }
    @Override public float getDPI() { return 0; }
    @Override public Size getResolution() { return Header.getFileFields().getResolution(); }
    @Override public float getPixelSizeUm() { return 0; }
    @Override public int getLayerCount() { return Header.getFileFields().getLayerCount(); }

    @Override
    public void addLayer(
            MSLALayerEncodeReader reader,
            MSLALayerEncoder.Callback<byte[]> callback) throws MSLAException
    {
        addLayer(reader, callback, 0, 0, 0, 0);
    }

    @Override
    public void addLayer(
            MSLALayerEncodeReader reader, MSLALayerEncoder.Callback<byte[]> callback,
            float layerHeight, float exposureTime, float liftSpeed, float liftHeight) throws MSLAException
    {
        var layer = new CTBFileLayerDef(Header.getFileFields().getVersion());
        var layerNumber = LayerDefinition.size();
        layer.getFileFields().setPositionZ((layerNumber + 1) * Header.getFileFields().getLayerHeightMillimeter());
        layer.getFileFields().setExposureTime(Header.getFileFields().getLayerExposureSeconds());
        layer.getFileFields().setLightOffSeconds(PrintParams.getFileFields().getLightOffDelay());

        // Fill in layer overrides with defaults
        var extra = layer.getFileFields().getExtra();
        if (extra != null) {
            var extraFields = extra.getFileFields();
            extraFields.setLiftHeight(PrintParams.getFileFields().getLiftHeight());
            extraFields.setLiftSpeed(PrintParams.getFileFields().getLiftSpeed());
            extraFields.setLiftHeight2(SlicerInfo.getFileFields().getLiftHeight2());
            extraFields.setLiftSpeed2(SlicerInfo.getFileFields().getLiftSpeed2());
            extraFields.setRetractSpeed(PrintParams.getFileFields().getRetractSpeed());
            extraFields.setRetractHeight2(SlicerInfo.getFileFields().getRetractHeight2());
            extraFields.setRetractSpeed2(SlicerInfo.getFileFields().getRetractSpeed2());
            if (PrintParamsV4 != null) {
                extraFields.setRestTimeBeforeLift(PrintParamsV4.getFileFields().getRestTimeBeforeLift());
                extraFields.setRestTimeAfterLift(PrintParamsV4.getFileFields().getRestTimeAfterLift());
                extraFields.setRestTimeAfterRetract(PrintParamsV4.getFileFields().getRestTimeAfterRetract());
            } else {
                extraFields.setRestTimeBeforeLift(0.0f);
                extraFields.setRestTimeAfterLift(SlicerInfo.getFileFields().getRestTimeAfterLift());
                extraFields.setRestTimeAfterRetract(SlicerInfo.getFileFields().getRestTimeAfterRetract());
            }
            extraFields.setLightPWM(Header.getFileFields().getLightPWM().floatValue());
        }
        LayerDefinition.add(layer);

        var params = new HashMap<String, Object>();
        params.put("EncryptionKey", Header.getFileFields().getEncryptionKey());
        getEncodersPool().encode(layerNumber, reader, params, (ln, data) -> {
            layer.getFileFields().setData(data.data());
            if (callback != null) callback.onFinish(ln, data);
        });
    }

    @Override public boolean readLayer(MSLALayerDecodeWriter writer, int layer) throws MSLAException {
        var layerData = new CTBFileCodec.Input(LayerDefinition.get(layer).getFileFields().getData());
        var params = new HashMap<String, Object>();
        params.put("EncryptionKey", Header.getFileFields().getEncryptionKey());
        return getDecodersPool().decode(layer, writer, layerData, params);
    }

    /* Internal read method */
    private void read(FileInputStream stream) throws MSLAException {
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

        // Read preliminary layer definitions
        try {
            logger.info("Reading preliminary layer definitions");
            var layerDefOffset = Header.getFileFields().getLayersDefinitionOffset();
            for (int i = 0; i < Header.getFileFields().getLayerCount(); i++) {
                var layerDef = new CTBFileLayerDef(Header.getFileFields().getVersion());
                layerDef.setBriefMode(true);
                LayerDefinition.add(layerDef);
                var bytesRead = layerDef.read(stream, layerDefOffset);
                if (bytesRead != layerDef.getDataLength())
                    throw new MSLAException("Error reading brief layer definition for layer " + i + ": data size mismatch");
                layerDefOffset += layerDef.getDataLength();
            }
        } catch (FileFieldsException e) {
            throw new MSLAException("Error reading brief layer definition", e);
        }

        // Read main layer data with definitions & extra layer settings
        logger.info("Reading layers");
        for (int i = 0; i < Header.getFileFields().getLayerCount(); i++) {
            var def = LayerDefinition.get(i);
            def.setBriefMode(false);
            var defOffset = def.getFileFields().getDataAddress() - def.getFileFields().getTableSize();
            var bytesRead = def.read(stream, defOffset);
            if (bytesRead != def.getFileFields().getDataSize() + def.getFileFields().getTableSize())
                throw new MSLAException("Error reading layer " + i + ": data size mismatch");
        }
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
                PrintParamsV4.getFileFields().setLastLayerIndex(LayerDefinition.size()-1);
                offset += PrintParamsV4.getDataLength();
                if (ResinParams != null) {
                    PrintParamsV4.getFileFields().setResinParametersOffset(offset);
                    offset += ResinParams.getDataLength();
                } else PrintParamsV4.getFileFields().setResinParametersOffset(0);
            } else {
                SlicerInfo.getFileFields().setPrintParametersV4Offset(0);
            }

            Header.getFileFields().setLayersDefinitionOffset(offset);
            Header.getFileFields().setTotalHeightMillimeter(LayerDefinition.size() * Header.getFileFields().getLayerHeightMillimeter());
            Header.getFileFields().setLayerCount(LayerDefinition.size());
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
            var wholeBriefLayerDefSize = LayerDefinition.size() * CTBFileLayerDef.BRIEF_TABLE_SIZE;
            for (var def : LayerDefinition) {
                def.getFileFields().setDataAddress(wholeBriefLayerDefSize +
                        offset + CTBFileLayerDef.BRIEF_TABLE_SIZE +
                        ((def.getFileFields().getExtra() != null) ? CTBFileLayerDefExtra.TABLE_SIZE : 0)
                );
                def.getFileFields().setDataSize(def.getFileFields().getData().length);
                def.setBriefMode(false); // Calculate data length for the whole block with Data & Extra
                var dataLength = def.getDataLength();
                var extra = def.getFileFields().getExtra();
                if (extra != null) extra.getFileFields().setTotalSize(dataLength);
                def.setBriefMode(true); // Write just in brief mode w/o Data & Extra
                def.write(stream);
                def.setBriefMode(false);
                offset += dataLength;
            }

            // Write whole layer definitions
            for (var def : LayerDefinition) {
                def.setBriefMode(false);
                def.write(stream);
            }
        } catch (FileFieldsException e) {
            throw new MSLAException("Error writing file fields", e);
        }
    }

    @Override public boolean isValid() { return (Header != null) && (SlicerInfo != null) && (PrintParams != null); }

    @Override
    public void setPreview(int index, BufferedImage image) {
        if (index == 0) PreviewLarge.setImage(image);
        else PreviewSmall.setImage(image);
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
                LayerDefinition;
    }
}
