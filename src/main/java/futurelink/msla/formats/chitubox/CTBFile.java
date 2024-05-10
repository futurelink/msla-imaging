package futurelink.msla.formats.chitubox;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.MSLAFileGeneric;
import futurelink.msla.formats.MSLAOptionMapper;
import futurelink.msla.formats.chitubox.tables.*;
import futurelink.msla.formats.iface.*;
import futurelink.msla.formats.utils.FileFieldsException;
import futurelink.msla.formats.utils.Size;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class CTBFile extends MSLAFileGeneric<byte[]> {
    private FileInputStream stream = null;
    private final MSLAOptionMapper optionMapper;

    /* File sections */
    private final CTBFileHeader header;
    private final CTBFilePrintParams params;
    private final CTBFileSlicerInfo slicerInfo;
    private CTBFileMachineName machineName;
    private CTBFileDisclaimer disclaimer = null;
    private CTBFilePrintParamsV4 paramsV4 = null;
    private CTBFileResinParams resinParams = null;
    private final CTBFilePreview previewSmall = new CTBFilePreview(CTBFilePreview.Type.Small);
    private final CTBFilePreview previewLarge = new CTBFilePreview(CTBFilePreview.Type.Large);
    private final ArrayList<CTBFileLayerDef> layerDef = new ArrayList<>();
    private ArrayList<CTBFileLayerDefExtra> layerDefExtra = null;

    public CTBFile(MSLAFileDefaults defaults) throws MSLAException {
        super();
        optionMapper = new CTBOptionMapper(this);

        header = new CTBFileHeader(defaults);
        if (header.getFileFields().getVersion() <= 0)
            throw new MSLAException("The MSLA file does not have a version number.");

        machineName = new CTBFileMachineName(defaults);
        params = new CTBFilePrintParams(defaults);
        slicerInfo = new CTBFileSlicerInfo(defaults);

        if (header.getFileFields().getVersion() >= 3) layerDefExtra = new ArrayList<>();

        // Version 4 or later data
        if (header.getFileFields().getVersion() >= 4) {
            paramsV4 = new CTBFilePrintParamsV4(defaults);
            disclaimer = new CTBFileDisclaimer();

            // Version 5 or later data
            if (header.getFileFields().getVersion() >= 5) resinParams = new CTBFileResinParams();
        }
    }

    public CTBFile(FileInputStream stream) throws IOException, MSLAException {
        super();
        this.stream = stream;
        optionMapper = new CTBOptionMapper(this);

        header = new CTBFileHeader();
        params = new CTBFilePrintParams();
        slicerInfo = new CTBFileSlicerInfo();
        machineName = new CTBFileMachineName();

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
        var layer = new CTBFileLayerDef();
        var layerNumber = layerDef.size();
        layerDef.add(layer);

        var params = new HashMap<String, Object>();
        params.put("EncryptionKey", header.getFileFields().getEncryptionKey());
        getEncodersPool().encode(layerNumber, reader, params, callback);
    }

    @Override public boolean readLayer(MSLALayerDecodeWriter writer, int layer) throws MSLAException {
        var layerDataPosition = layerDef.get(layer).getFileFields().getDataAddress();
        var layerDataLength = layerDef.get(layer).getFileFields().getDataSize();
        try {
            var ch = stream.getChannel();
            ch.position(layerDataPosition);
            var layerData = new CTBFileCodec.Input(stream.readNBytes(layerDataLength));
            var params = new HashMap<String, Object>();
            params.put("EncryptionKey", header.getFileFields().getEncryptionKey());
            return getDecodersPool().decode(layer, writer, layerData, params);
        } catch (IOException e) {
            throw new MSLAException("Error reading layer " + layer, e);
        }
    }

    /* Internal read method */
    private void read(FileInputStream stream) throws MSLAException {
        header.read(stream, 0);

        if (header.getFileFields().getPrintParametersOffset() > 0)
            params.read(stream, header.getFileFields().getPrintParametersOffset());
        else throw new MSLAException("Malformed file, PrintParameters section offset is missing");

        if (header.getFileFields().getSlicerOffset() > 0)
            slicerInfo.read(stream, header.getFileFields().getSlicerOffset());
        else throw new MSLAException("Malformed file, SlicerOffset section offset is missing");

        machineName.getFileFields().setMachineNameSize(slicerInfo.getFileFields().getMachineNameSize());
        machineName.read(stream, slicerInfo.getFileFields().getMachineNameOffset());

        if (header.getFileFields().getVersion() >= 3) layerDefExtra = new ArrayList<>();

        // Read version 4 or later data
        if (header.getFileFields().getVersion() >= 4) {
            if (slicerInfo.getFileFields().getPrintParametersV4Offset() == 0)
                throw new MSLAException("Malformed file, PrintParametersV4 section offset is missing");

            paramsV4 = new CTBFilePrintParamsV4();
            paramsV4.read(stream, slicerInfo.getFileFields().getPrintParametersV4Offset());

            disclaimer = new CTBFileDisclaimer();
            disclaimer.read(stream, paramsV4.getFileFields().getDisclaimerOffset());

            // Read version 5 or later resin settings
            if (header.getFileFields().getVersion() >= 5 && paramsV4.getFileFields().getResinParametersOffset() > 0) {
                resinParams = new CTBFileResinParams();
                resinParams.read(stream, paramsV4.getFileFields().getResinParametersOffset());
            }
        }

        // Read small preview
        if (header.getFileFields().getPreviewLargeOffset() > 0) {
            previewLarge.read(stream, header.getFileFields().getPreviewLargeOffset());
            var pixels = previewLarge.readImage(stream);
        }

        // Read large preview
        if (header.getFileFields().getPreviewSmallOffset() > 0) {
            previewSmall.read(stream, header.getFileFields().getPreviewSmallOffset());
            var pixels = previewSmall.readImage(stream);
        }

        // Read layer definitions
        if (header.getFileFields().getLayerCount() > 0) {
            var layerDefOffset = header.getFileFields().getLayersDefinitionOffset();
            int expectedDataAddress = layerDefOffset
                    + header.getFileFields().getLayerCount() * CTBFileLayerDef.TABLE_SIZE
                    + 84; // Don't know why it's 84 here
            for (int i = 0; i < header.getFileFields().getLayerCount(); i++) {
                var layer = new CTBFileLayerDef();
                layerDef.add(layer);
                layer.read(stream, layerDefOffset);
                layerDefOffset += CTBFileLayerDef.TABLE_SIZE;

                // Validation. Check if layer def was read correctly, starting from 2nd layer
                if (expectedDataAddress > 0 && layer.getFileFields().getDataAddress() != expectedDataAddress)
                    throw new MSLAException(
                            "Error reading layer definition at layer " + i +
                                    ": expected data address was " + expectedDataAddress +
                                    " but actual was " + layer.getFileFields().getDataAddress());

                // Read version 3 and later, extra layer def data is located BEFORE(!!) layer data
                // but pointer points to layer data, not to the data before it.
                if (layerDefExtra != null) {
                    var layerExtra = new CTBFileLayerDefExtra();
                    layerDefExtra.add(layerExtra);
                    layerExtra.read(stream, layer.getFileFields().getDataAddress() - CTBFileLayerDefExtra.TABLE_SIZE);

                    // Validation. Check if TotalSize matches
                    var expectedTotalSize = layer.getFileFields().getTableSize() + layer.getFileFields().getDataSize();
                    if (layerExtra.getFileFields().getTotalSize() != expectedTotalSize)
                        throw new MSLAException("Error reading extra layer definition at layer " + i +
                                " TotalSize was " + layerExtra.getFileFields().getTotalSize() +
                                " but expected value is " + expectedTotalSize);
                }

                // Next layer def data address
                expectedDataAddress = layer.getFileFields().getDataAddress() +
                        layer.getFileFields().getDataSize() +
                        layer.getFileFields().getTableSize();
            }
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
            header.getFileFields().setPrintParametersSize(params.getDataLength());
            offset += header.getFileFields().getPrintParametersSize();
            header.getFileFields().setSlicerOffset(offset);
            header.getFileFields().setSlicerSize(slicerInfo.getDataLength());
            offset += header.getFileFields().getSlicerSize();

            previewLarge.getFileFields().setImageOffset(header.getFileFields().getPreviewLargeOffset() + 32);
            previewSmall.getFileFields().setImageOffset(header.getFileFields().getPreviewSmallOffset() + 32);

            slicerInfo.getFileFields().setMachineNameOffset(offset);
            slicerInfo.getFileFields().setMachineNameSize(machineName.getFileFields().getMachineNameSize());
            offset += slicerInfo.getDataLength() + machineName.getDataLength();

            // For version 4 and greater
            if (paramsV4 != null) {
                // Calculate disclaimer offset and size
                paramsV4.getFileFields().setDisclaimerOffset(offset);
                paramsV4.getFileFields().setDisclaimerLength(disclaimer.getFileFields().getDisclaimer().length());
                offset += disclaimer.getDataLength();
                slicerInfo.getFileFields().setPrintParametersV4Offset(offset);
                paramsV4.getFileFields().setLastLayerIndex(layerDef.size()-1);
                offset += paramsV4.getDataLength();
                if (resinParams != null) {
                    paramsV4.getFileFields().setResinParametersOffset(offset);
                    offset += resinParams.getDataLength();
                } else paramsV4.getFileFields().setResinParametersOffset(0);
            } else {
                slicerInfo.getFileFields().setPrintParametersV4Offset(0);
            }

            header.getFileFields().setLayersDefinitionOffset(offset);
            header.getFileFields().setLayerCount(layerDef.size());
            header.getFileFields().setPrintTime(0);

            System.out.println(this);
        } catch (FileFieldsException e) {
            throw new MSLAException("Error writing file fields", e);
        }

        /* Write blocks */
        header.write(stream);
        previewLarge.write(stream);
        previewSmall.write(stream);
        params.write(stream);
        slicerInfo.write(stream);
        machineName.write(stream);
        if (paramsV4 != null) {
            disclaimer.write(stream);
            paramsV4.write(stream);
        }
        if (resinParams != null) resinParams.write(stream);
        for (var def : layerDef) def.write(stream);
    }

    @Override public boolean isValid() { return (header != null) && (slicerInfo != null) && (params != null); }
    @Override public MSLAOptionMapper options() { return null; }

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
                "----- Printer params ----\n" + params + "\n" +
                ((paramsV4 != null) ? "----- Printer params V4 ----\n" + paramsV4 + "\n" : "") +
                ((resinParams != null) ? "----- Resin params V4 ----\n" + resinParams + "\n" : "") +
                "----- Small preview ----\n" + previewSmall + "\n" +
                "----- Larger preview ----\n" + previewLarge + "\n";
    }
}
