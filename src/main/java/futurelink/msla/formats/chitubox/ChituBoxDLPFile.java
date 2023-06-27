package futurelink.msla.formats.chitubox;

import futurelink.msla.formats.*;
import futurelink.msla.formats.chitubox.tables.*;
import futurelink.msla.formats.utils.Size;

import java.io.*;

public class ChituBoxDLPFile implements MSLAFile {
    private int layerDataPosition = 0;
    private final ChituBoxDLPFileHeader header = new ChituBoxDLPFileHeader();
    private final ChituBoxDLPFileSliceInfo slicerInfo = new ChituBoxDLPFileSliceInfo();
    private final ChituBoxDLPFileSliceInfoV3 slicerInfoV3 = new ChituBoxDLPFileSliceInfoV3();
    private final ChituBoxDLPFilePreviews previews = new ChituBoxDLPFilePreviews();
    private final MSLAOptionMapper optionMapper = new ChituBoxDLPFileOptionMapper();
    private final ChituBoxDLPFileLayerDef layers = new ChituBoxDLPFileLayerDef();

    public ChituBoxDLPFile(FileInputStream stream) throws IOException {
        var position = 0;
        header.read(stream, position); position += header.getDataLength();
        previews.read(stream, position); position += previews.getDataLength();
        slicerInfo.read(stream, position); position += slicerInfo.getDataLength();

        // Skip layer areas (don't know what's their purpose)
        stream.skipNBytes(header.getLayerCount() * 4 + 2); position += header.getLayerCount() * 4 + 2;
        if (header.getVersion() >= 3) slicerInfoV3.read(stream, position); position += slicerInfoV3.getDataLength();
        layerDataPosition  = position;

        // Scan layer data and get layer data lengths and offsets
        // ------------------------------------------------------
        layers.setLayerCount((int) header.getLayerCount());
        layers.read(stream, layerDataPosition);
    }

    private boolean hasOption(String option, Class<?> type) {
        return false;
    }

    /**
     * Codec is not needed for Chitubox DLP format. The data structure is fairly straightforward,
     * so it decodes when it's read.
     * @return null
     */
    @Override
    public MSLAFileCodec getCodec() {
        return null;
    }

    @Override
    public MSLAPreview getPreview() {
        return previews.getPreview(0);
    }

    @Override
    public void updatePreviewImage() throws IOException {}

    @Override
    public float getDPI() {
        return 0;
    }

    @Override
    public void addLayer(MSLAEncodeReader reader) throws IOException {

    }

    @Override
    public void addLayer(MSLAEncodeReader reader, float layerHeight, float exposureTime, float liftSpeed, float liftHeight) throws IOException {

    }

    @Override
    public void readLayer(FileInputStream iStream, int layer, MSLADecodeWriter writer) throws IOException {
        layers.decodeLayer(iStream, layer, writer);
    }

    @Override
    public void read(FileInputStream iStream) throws IOException {

    }

    @Override
    public void write(OutputStream stream) throws IOException {

    }

    @Override
    public Size getResolution() { return header.getResolution(); }
    @Override
    public float getPixelSizeUm() { return 0; }
    @Override
    public int getLayerCount() { return header.getLayerCount(); }
    @Override
    public boolean isValid() { return (header != null) && (slicerInfo != null); }

    @Override
    public void setOption(String option, Serializable value) throws IOException {
        optionMapper.setOption(option, value);
    }

    @Override
    public String toString() {
        return header.toString() + slicerInfo + slicerInfoV3 + previews;
    }
}
