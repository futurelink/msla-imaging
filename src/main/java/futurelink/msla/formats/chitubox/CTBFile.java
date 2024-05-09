package futurelink.msla.formats.chitubox;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.MSLAFileGeneric;
import futurelink.msla.formats.MSLAOptionMapper;
import futurelink.msla.formats.chitubox.tables.*;
import futurelink.msla.formats.iface.*;
import futurelink.msla.formats.utils.Size;

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
    private CTBFilePrintParamsV4 paramsV4 = null;
    private CTBFileResinParams resinParams = null;
    private final CTBFilePreview previewSmall = new CTBFilePreview();
    private final CTBFilePreview previewLarge = new CTBFilePreview();
    private final ArrayList<CTBFileLayerDef> layerDef = new ArrayList<>();
    private ArrayList<CTBFileLayerDefExtra> layerDefExtra = null;

    public CTBFile(MSLAFileDefaults defaults) throws MSLAException {
        super();
        optionMapper = new CTBOptionMapper(this);

        header = new CTBFileHeader(defaults);
        if (header.getFields().getVersion() <= 0)
            throw new MSLAException("The MSLA file does not have a version number.");

        params = new CTBFilePrintParams(defaults);
        slicerInfo = new CTBFileSlicerInfo(defaults);

        if (header.getFields().getVersion() >= 3) layerDefExtra = new ArrayList<>();

        // Version 4 or later data
        if (header.getFields().getVersion() >= 4) {
            paramsV4 = new CTBFilePrintParamsV4(defaults);

            // Version 5 or later data
            if (header.getFields().getVersion() >= 5) resinParams = new CTBFileResinParams();
        }
    }

    public CTBFile(FileInputStream stream) throws IOException, MSLAException {
        super();
        this.stream = stream;
        optionMapper = new CTBOptionMapper(this);

        header = new CTBFileHeader();
        params = new CTBFilePrintParams();
        slicerInfo = new CTBFileSlicerInfo();

        header.read(stream, 0);

        if (header.getFields().getPrintParametersOffset() > 0)
            params.read(stream, header.getFields().getPrintParametersOffset());
        else throw new MSLAException("Malformed file, PrintParameters section offset is missing");

        if (header.getFields().getSlicerOffset() > 0)
            slicerInfo.read(stream, header.getFields().getSlicerOffset());
        else throw new MSLAException("Malformed file, SlicerOffset section offset is missing");

        if (header.getFields().getVersion() >= 3) layerDefExtra = new ArrayList<>();

        // Read version 4 or later data
        if (header.getFields().getVersion() >= 4) {
            if (slicerInfo.getFields().getPrintParametersV4Offset() == 0)
                throw new MSLAException("Malformed file, PrintParametersV4 section offset is missing");
            paramsV4 = new CTBFilePrintParamsV4();
            paramsV4.read(stream, slicerInfo.getFields().getPrintParametersV4Offset());

            // Read version 5 or later resin settings
            if (header.getFields().getVersion() >= 5 && paramsV4.getFields().getResinParametersOffset() > 0) {
                resinParams = new CTBFileResinParams();
                resinParams.read(stream, paramsV4.getFields().getResinParametersOffset());
            }
        }

        // Read small preview
        if (header.getFields().getPreviewLargeOffset() > 0) {
            previewLarge.read(stream, header.getFields().getPreviewLargeOffset());
            var pixels = previewLarge.readImage(stream);
        }

        // Read large preview
        if (header.getFields().getPreviewSmallOffset() > 0) {
            previewSmall.read(stream, header.getFields().getPreviewSmallOffset());
            var pixels = previewSmall.readImage(stream);
        }

        // Read layer definitions
        if (header.getFields().getLayerCount() > 0) {
            var layerDefOffset = header.getFields().getLayersDefinitionOffset();
            int expectedDataAddress = layerDefOffset
                    + header.getFields().getLayerCount() * CTBFileLayerDef.TABLE_SIZE
                    + 84; // Don't know why it's 84 here
            for (int i = 0; i < header.getFields().getLayerCount(); i++) {
                var layer = new CTBFileLayerDef();
                layerDef.add(layer);
                layer.read(stream, layerDefOffset);
                layerDefOffset += CTBFileLayerDef.TABLE_SIZE;

                // Validation. Check if layer def was read correctly, starting from 2nd layer
                if (expectedDataAddress > 0 && layer.getFields().getDataAddress() != expectedDataAddress)
                    throw new MSLAException(
                            "Error reading layer definition at layer " + i +
                                    ": expected data address was " + expectedDataAddress +
                                    " but actual was " + layer.getFields().getDataAddress());

                // Read version 3 and later, extra layer def data is located BEFORE(!!) layer data
                // but pointer points to layer data, not to the data before it.
                if (layerDefExtra != null) {
                    var layerExtra = new CTBFileLayerDefExtra();
                    layerDefExtra.add(layerExtra);
                    layerExtra.read(stream, layer.getFields().getDataAddress() - CTBFileLayerDefExtra.TABLE_SIZE);

                    // Validation. Check if TotalSize matches
                    var expectedTotalSize = layer.getFields().getTableSize() + layer.getFields().getDataSize();
                    if (layerExtra.getFields().getTotalSize() != expectedTotalSize)
                        throw new MSLAException("Error reading extra layer definition at layer " + i +
                                " TotalSize was " + layerExtra.getFields().getTotalSize() +
                                " but expected value is " + expectedTotalSize);
                }

                // Next layer def data address
                expectedDataAddress = layer.getFields().getDataAddress() +
                        layer.getFields().getDataSize() +
                        layer.getFields().getTableSize();
            }
        }
    }

    @Override public Class<? extends MSLALayerCodec<byte[]>> getCodec() { return CTBFileCodec.class; }
    @Override public MSLAPreview getPreview() { return previewLarge; }
    @Override public float getDPI() { return 0; }
    @Override public Size getResolution() { return header.getFields().getResolution(); }
    @Override public float getPixelSizeUm() { return 0; }
    @Override public int getLayerCount() { return header.getFields().getLayerCount(); }

    @Override
    public void addLayer(
            MSLALayerEncodeReader reader,
            MSLALayerEncoder.Callback<byte[]> callback) throws MSLAException
    {

    }

    @Override
    public void addLayer(
            MSLALayerEncodeReader reader, MSLALayerEncoder.Callback<byte[]> callback,
            float layerHeight, float exposureTime, float liftSpeed, float liftHeight) throws MSLAException
    {

    }

    @Override public boolean readLayer(MSLALayerDecodeWriter writer, int layer) throws MSLAException {
        var layerDataPosition = layerDef.get(layer).getFields().getDataAddress();
        var layerDataLength = layerDef.get(layer).getFields().getDataSize();
        try {
            var ch = stream.getChannel();
            ch.position(layerDataPosition);
            var layerData = new CTBFileCodec.Input(stream.readNBytes(layerDataLength));
            var params = new HashMap<String, Object>();
            params.put("EncryptionKey", header.getFields().getEncryptionKey());
            return getDecodersPool().decode(layer, writer, layerData, params);
        } catch (IOException e) {
            throw new MSLAException("Error reading layer " + layer, e);
        }
    }

    @Override public void write(OutputStream stream) throws MSLAException {}
    @Override public boolean isValid() { return (header != null) && (slicerInfo != null) && (params != null); }
    @Override public MSLAOptionMapper options() { return null; }

    @Override
    public String toString() {
        return "----- Header -----\n" + header + "\n" +
                "----- Slicer info ----\n" + slicerInfo + "\n" +
                "----- Printer params ----\n" + params + "\n" +
                ((paramsV4 != null) ? "----- Printer params V4 ----\n" + paramsV4 + "\n" : "") +
                ((resinParams != null) ? "----- Resin params V4 ----\n" + resinParams + "\n" : "") +
                "----- Small preview ----\n" + previewSmall + "\n" +
                "----- Larger preview ----\n" + previewLarge + "\n";
    }
}
