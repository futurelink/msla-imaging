package futurelink.msla.formats.chitubox;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.MSLAFileGeneric;
import futurelink.msla.formats.MSLAOptionMapper;
import futurelink.msla.formats.chitubox.tables.*;
import futurelink.msla.formats.iface.*;
import futurelink.msla.formats.utils.FileFieldsException;
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
    private final MSLAOptionMapper optionMapper;

    /* File sections */
    @Getter private CTBFileHeader header = null;
    @Getter private CTBFilePrintParams printParams = null;
    @Getter private CTBFileSlicerInfo slicerInfo = null;
    private CTBFileMachineName machineName = null;
    private CTBFileDisclaimer disclaimer = null;
    private CTBFilePrintParamsV4 printParamsV4 = null;
    private CTBFileResinParams resinParams = null;
    private final CTBFilePreview previewSmall = new CTBFilePreview(CTBFilePreview.Type.Small);
    private final CTBFilePreview previewLarge = new CTBFilePreview(CTBFilePreview.Type.Large);
    private final ArrayList<CTBFileLayerDef> layerDefinition = new ArrayList<>();
    private final Logger logger = Logger.getLogger(CTBFile.class.getName());

    public CTBFile(MSLAFileDefaults defaults) throws MSLAException {
        super();
        var Version = defaults.getOptionByte(null, "Version");
        if (Version == null || Version <= 0)
            throw new MSLAException("File defaults do not have a version number.");

        header = new CTBFileHeader(Version, defaults);
        if (header.getFileFields().getVersion() <= 0)
            throw new MSLAException("The MSLA file does not have a version number.");

        machineName = new CTBFileMachineName(defaults);
        printParams = new CTBFilePrintParams(Version, defaults);
        slicerInfo = new CTBFileSlicerInfo(Version, defaults);

        // Version 4 or later data
        if (header.getFileFields().getVersion() >= 4) {
            printParamsV4 = new CTBFilePrintParamsV4(Version, defaults);
            disclaimer = new CTBFileDisclaimer();

            // Version 5 or later data
            if (header.getFileFields().getVersion() >= 5) resinParams = new CTBFileResinParams(Version);
        }

        optionMapper = new CTBOptionMapper(this);
    }

    public CTBFile(FileInputStream stream) throws IOException, MSLAException {
        super();
        optionMapper = new CTBOptionMapper(this);
        read(stream);
    }

    @Override public Class<? extends MSLALayerCodec<byte[]>> getCodec() { return CTBFileCodec.class; }
    @Override public MSLAPreview getPreview(int index) {
        if (index == 0) return previewLarge;
        else return previewSmall;
    }
    @Override public float getDPI() { return 0; }
    @Override public Size getResolution() { return header.getFileFields().getResolution(); }
    @Override public float getPixelSizeUm() { return 0; }
    @Override public int getLayerCount() { return header.getFileFields().getLayerCount(); }

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
        var layer = new CTBFileLayerDef(header.getFileFields().getVersion());
        var layerNumber = layerDefinition.size();
        layer.getFileFields().setPositionZ((layerNumber + 1) * header.getFileFields().getLayerHeightMillimeter());
        layer.getFileFields().setExposureTime(header.getFileFields().getLayerExposureSeconds());
        layer.getFileFields().setLightOffSeconds(printParams.getFileFields().getLightOffDelay());

        // Fill in layer overrides with defaults
        var extra = layer.getFileFields().getExtra();
        if (extra != null) {
            var extraFields = extra.getFileFields();
            extraFields.setLiftHeight(printParams.getFileFields().getLiftHeight());
            extraFields.setLiftSpeed(printParams.getFileFields().getLiftSpeed());
            extraFields.setLiftHeight2(slicerInfo.getFileFields().getLiftHeight2());
            extraFields.setLiftSpeed2(slicerInfo.getFileFields().getLiftSpeed2());
            extraFields.setRetractSpeed(printParams.getFileFields().getRetractSpeed());
            extraFields.setRetractHeight2(slicerInfo.getFileFields().getRetractHeight2());
            extraFields.setRetractSpeed2(slicerInfo.getFileFields().getRetractSpeed2());
            if (printParamsV4 != null) {
                extraFields.setRestTimeBeforeLift(printParamsV4.getFileFields().getRestTimeBeforeLift());
                extraFields.setRestTimeAfterLift(printParamsV4.getFileFields().getRestTimeAfterLift());
                extraFields.setRestTimeAfterRetract(printParamsV4.getFileFields().getRestTimeAfterRetract());
            } else {
                extraFields.setRestTimeBeforeLift(0.0f);
                extraFields.setRestTimeAfterLift(slicerInfo.getFileFields().getRestTimeAfterLift());
                extraFields.setRestTimeAfterRetract(slicerInfo.getFileFields().getRestTimeAfterRetract());
            }
            extraFields.setLightPWM(header.getFileFields().getLightPWM().floatValue());
        }
        layerDefinition.add(layer);

        var params = new HashMap<String, Object>();
        params.put("EncryptionKey", header.getFileFields().getEncryptionKey());
        getEncodersPool().encode(layerNumber, reader, params, (ln, data) -> {
            layer.getFileFields().setData(data.data());
            if (callback != null) callback.onFinish(ln, data);
        });
    }

    @Override public boolean readLayer(MSLALayerDecodeWriter writer, int layer) throws MSLAException {
        var layerData = new CTBFileCodec.Input(layerDefinition.get(layer).getFileFields().getData());
        var params = new HashMap<String, Object>();
        params.put("EncryptionKey", header.getFileFields().getEncryptionKey());
        return getDecodersPool().decode(layer, writer, layerData, params);
    }

    /* Internal read method */
    private void read(FileInputStream stream) throws MSLAException {
        header = new CTBFileHeader(0); // Version is going to be read from file
        header.read(stream, 0);
        if (header.getFileFields().getVersion() == null)
            throw new MSLAException("Malformed file, Version is not identified");

        logger.info("CTB file version " + header.getFileFields().getVersion());

        printParams = new CTBFilePrintParams(header.getFileFields().getVersion());
        slicerInfo = new CTBFileSlicerInfo(header.getFileFields().getVersion());
        machineName = new CTBFileMachineName();

        if (header.getFileFields().getPrintParametersOffset() > 0)
            printParams.read(stream, header.getFileFields().getPrintParametersOffset());
        else throw new MSLAException("Malformed file, PrintParameters section offset is missing");

        if (header.getFileFields().getSlicerOffset() > 0)
            slicerInfo.read(stream, header.getFileFields().getSlicerOffset());
        else throw new MSLAException("Malformed file, SlicerOffset section offset is missing");

        machineName.getFileFields().setMachineNameSize(slicerInfo.getFileFields().getMachineNameSize());
        machineName.read(stream, slicerInfo.getFileFields().getMachineNameOffset());

        logger.info("File is for machine '" + machineName.getFileFields().getMachineName() + "'");

        // Read version 4 or later data
        if (header.getFileFields().getVersion() >= 4) {
            logger.info("Reading print parameters for version 4 or later");
            if (slicerInfo.getFileFields().getPrintParametersV4Offset() == 0)
                throw new MSLAException("Malformed file, PrintParametersV4 section offset is missing");

            printParamsV4 = new CTBFilePrintParamsV4(header.getFileFields().getVersion());
            printParamsV4.read(stream, slicerInfo.getFileFields().getPrintParametersV4Offset());

            logger.info("Reading disclaimer");
            disclaimer = new CTBFileDisclaimer();
            disclaimer.read(stream, printParamsV4.getFileFields().getDisclaimerOffset());

            // Read version 5 or later resin settings
            if (header.getFileFields().getVersion() >= 5 && printParamsV4.getFileFields().getResinParametersOffset() > 0) {
                logger.info("Reading resin parameters for version 5 or later");
                resinParams = new CTBFileResinParams(header.getFileFields().getVersion());
                resinParams.read(stream, printParamsV4.getFileFields().getResinParametersOffset());
            }
        }

        // Read large preview
        logger.info("Reading large preview");
        if (header.getFileFields().getPreviewLargeOffset() > 0) {
            previewLarge.read(stream, header.getFileFields().getPreviewLargeOffset());
            var pixels = previewLarge.readImage(stream);
        }

        // Read small preview
        logger.info("Reading small preview");
        if (header.getFileFields().getPreviewSmallOffset() > 0) {
            previewSmall.read(stream, header.getFileFields().getPreviewSmallOffset());
            var pixels = previewSmall.readImage(stream);
        }

        // Read preliminary layer definitions
        try {
            logger.info("Reading preliminary layer definitions");
            var layerDefOffset = header.getFileFields().getLayersDefinitionOffset();
            for (int i = 0; i < header.getFileFields().getLayerCount(); i++) {
                var layerDef = new CTBFileLayerDef(header.getFileFields().getVersion());
                layerDef.setBriefMode(true);
                layerDefinition.add(layerDef);
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
        for (int i = 0; i < header.getFileFields().getLayerCount(); i++) {
            var def = layerDefinition.get(i);
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
            offset += header.getDataLength();
            header.getFileFields().setPreviewLargeOffset(offset);
            offset += previewLarge.getDataLength();
            header.getFileFields().setPreviewSmallOffset(offset);
            offset += previewSmall.getDataLength();
            header.getFileFields().setPrintParametersOffset(offset);
            header.getFileFields().setPrintParametersSize(printParams.getDataLength());
            offset += header.getFileFields().getPrintParametersSize();
            header.getFileFields().setSlicerOffset(offset);
            header.getFileFields().setSlicerSize(slicerInfo.getDataLength());
            offset += header.getFileFields().getSlicerSize();

            previewLarge.getFileFields().setImageOffset(header.getFileFields().getPreviewLargeOffset() + 32);
            previewSmall.getFileFields().setImageOffset(header.getFileFields().getPreviewSmallOffset() + 32);

            slicerInfo.getFileFields().setVersion(header.getFileFields().getVersion());
            slicerInfo.getFileFields().setMachineNameSize(machineName.getFileFields().getMachineNameSize());
            slicerInfo.getFileFields().setMachineNameOffset(offset);
            offset += machineName.getDataLength();

            // For version 4 and greater
            if (printParamsV4 != null) {
                // Calculate disclaimer offset and size
                printParamsV4.getFileFields().setDisclaimerOffset(offset);
                printParamsV4.getFileFields().setDisclaimerLength(disclaimer.getFileFields().getDisclaimer().length());
                offset += disclaimer.getDataLength();
                slicerInfo.getFileFields().setPrintParametersV4Offset(offset);
                printParamsV4.getFileFields().setLastLayerIndex(layerDefinition.size()-1);
                offset += printParamsV4.getDataLength();
                if (resinParams != null) {
                    printParamsV4.getFileFields().setResinParametersOffset(offset);
                    offset += resinParams.getDataLength();
                } else printParamsV4.getFileFields().setResinParametersOffset(0);
            } else {
                slicerInfo.getFileFields().setPrintParametersV4Offset(0);
            }

            header.getFileFields().setLayersDefinitionOffset(offset);
            header.getFileFields().setTotalHeightMillimeter(layerDefinition.size() * header.getFileFields().getLayerHeightMillimeter());
            header.getFileFields().setLayerCount(layerDefinition.size());
            header.getFileFields().setPrintTime(0);
        } catch (FileFieldsException e) {
            throw new MSLAException("Error writing file fields", e);
        }

        /* Write blocks */
        header.write(stream);
        previewLarge.write(stream);
        previewSmall.write(stream);
        printParams.write(stream);
        slicerInfo.write(stream);
        machineName.write(stream);
        if (printParamsV4 != null) {
            disclaimer.write(stream);
            printParamsV4.write(stream);
        }
        if (resinParams != null) resinParams.write(stream);

        try {
            // Write brief layer definitions
            var wholeBriefLayerDefSize = layerDefinition.size() * CTBFileLayerDef.BRIEF_TABLE_SIZE;
            for (var def : layerDefinition) {
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
            for (var def : layerDefinition) {
                def.setBriefMode(false);
                def.write(stream);
            }
        } catch (FileFieldsException e) {
            throw new MSLAException("Error writing file fields", e);
        }
    }

    @Override public boolean isValid() { return (header != null) && (slicerInfo != null) && (printParams != null); }
    @Override public MSLAOptionMapper options() { return optionMapper; }

    @Override
    public void setPreview(int index, BufferedImage image) {
        if (index == 0) previewLarge.setImage(image);
        else previewSmall.setImage(image);
    }

    @Override
    public String toString() {
        return "----- Header -----\n" + header + "\n" +
                "----- Slicer info ----\n" + slicerInfo + "\n" +
                machineName + "\n" +
                ((disclaimer != null) ? disclaimer + "\n" : "") +
                "----- Printer params ----\n" + printParams + "\n" +
                ((printParamsV4 != null) ? "----- Printer params V4 ----\n" + printParamsV4 + "\n" : "") +
                ((resinParams != null) ? "----- Resin params V4 ----\n" + resinParams + "\n" : "") +
                "----- Small preview ----\n" + previewSmall + "\n" +
                "----- Larger preview ----\n" + previewLarge + "\n" +
                layerDefinition;
    }
}
