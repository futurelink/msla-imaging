package futurelink.msla.formats.creality;

import futurelink.msla.formats.*;
import futurelink.msla.formats.creality.tables.*;
import futurelink.msla.formats.utils.Size;
import lombok.Getter;

import java.io.*;

public class CXDLPFile implements MSLAFile {
    private final FileInputStream iStream;
    @Getter private float pixelSizeUm;
    private final CXDLPFileHeader header;
    final CXDLPFileSliceInfo sliceInfo;
    final CXDLPFileSliceInfoV3 sliceInfoV3 = new CXDLPFileSliceInfoV3();
    private final CXDLPFilePreviews previews = new CXDLPFilePreviews();
    private final MSLAOptionMapper optionMapper;
    private final CXDLPFileLayerDef layers = new CXDLPFileLayerDef();

    public CXDLPFile(MSLAFileDefaults defaults) throws IOException {
        iStream = null;
        pixelSizeUm = defaults.getPixelSizeUm();
        header = new CXDLPFileHeader(defaults.getOptionBlock("Header"));
        sliceInfo = new CXDLPFileSliceInfo(defaults.getOptionBlock("SliceInfo"));
        optionMapper = new CXDLPFileOptionMapper(this);
    }

    public CXDLPFile(FileInputStream stream) throws IOException {
        var position = 0;
        iStream = stream;
        header = new CXDLPFileHeader();
        sliceInfo = new CXDLPFileSliceInfo();
        optionMapper = new CXDLPFileOptionMapper(this);
        header.read(iStream, position); position += header.getDataLength();
        previews.read(iStream, position); position += previews.getDataLength();
        sliceInfo.read(iStream, position); position += sliceInfo.getDataLength();

        // Skip layer areas (don't know what's their purpose)
        iStream.skipNBytes(header.getLayerCount() * 4 + 2); position += header.getLayerCount() * 4 + 2;
        if (header.getVersion() >= 3) sliceInfoV3.read(iStream, position); position += sliceInfoV3.getDataLength();

        // Scan layer data and get layer data lengths and offsets
        // ------------------------------------------------------
        layers.setLayerCount((int) header.getLayerCount());
        layers.read(iStream, position);
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
        throw new IOException("AddLayer defaults not supported yet");
    }

    @Override
    public void addLayer(MSLAEncodeReader reader, float layerHeight, float exposureTime,
                         float liftSpeed, float liftHeight) throws IOException {
        layers.encodeLayer(reader);
        header.setLayerCount(layers.getLayerCount().shortValue());
    }

    @Override
    public void readLayer(int layer, MSLADecodeWriter writer) throws IOException {
        layers.decodeLayer(iStream, layer, writer);
    }

    @Override
    public void write(OutputStream stream) throws IOException {
        if (header == null) throw new IOException("Header is empty, a file cannot be written");
        if (sliceInfo == null) throw new IOException("SliceInfo is empty, a file cannot be written");
        header.write(stream);
        previews.write(stream);
        sliceInfo.write(stream);
        layers.writeLayerAreas(stream);
        if (header.getVersion() >= 3) sliceInfoV3.write(stream);
        layers.write(stream);
    }

    @Override
    public Size getResolution() { return header.getResolution(); }
    @Override
    public int getLayerCount() { return header.getLayerCount(); }
    @Override
    public boolean isValid() { return (header != null) && (sliceInfo != null); }
    @Override
    public void setOption(String option, Serializable value) throws IOException {
        optionMapper.setOption(option, value);
    }
    @Override
    public String toString() {
        return header.toString() + sliceInfo + sliceInfoV3 + previews;
    }
}
