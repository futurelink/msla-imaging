package futurelink.msla.formats.creality;

import futurelink.msla.formats.*;
import futurelink.msla.formats.creality.tables.*;
import futurelink.msla.formats.iface.*;
import futurelink.msla.formats.utils.Size;

import java.io.*;
import java.util.List;

public class CXDLPFile extends MSLAFileGeneric<List<CXDLPFileLayerLine>> {
    private final FileInputStream iStream;
    private final CXDLPFileHeader header;
    final CXDLPFileSliceInfo sliceInfo;
    final CXDLPFileSliceInfoV3 sliceInfoV3;
    private final CXDLPFilePreviews previews = new CXDLPFilePreviews();
    private final MSLAOptionMapper optionMapper;
    private final CXDLPFileLayerDef layers = new CXDLPFileLayerDef();

    public CXDLPFile(MSLAFileDefaults defaults) throws MSLAException {
        iStream = null;
        header = new CXDLPFileHeader(defaults);
        sliceInfo = new CXDLPFileSliceInfo(defaults);
        sliceInfoV3 = new CXDLPFileSliceInfoV3(defaults);
        optionMapper = new CXDLPFileOptionMapper(this);
    }

    public CXDLPFile(FileInputStream stream) throws MSLAException {
        var position = 0;
        iStream = stream;
        header = new CXDLPFileHeader();
        sliceInfo = new CXDLPFileSliceInfo();
        sliceInfoV3 = new CXDLPFileSliceInfoV3();
        optionMapper = new CXDLPFileOptionMapper(this);
        header.read(iStream, position); position += header.getDataLength();
        previews.read(iStream, position); position += previews.getDataLength();
        sliceInfo.read(iStream, position); position += sliceInfo.getDataLength();

        // Skip layer areas (don't know what's their purpose)
        try { iStream.skipNBytes(header.getLayerCount() * 4 + 2); }
        catch (IOException e) { throw new MSLAException("Can't read file", e); }
        position += header.getLayerCount() * 4 + 2;
        if (header.getVersion() >= 3) sliceInfoV3.read(iStream, position); position += sliceInfoV3.getDataLength();

        // Scan layer data and get layer data lengths and offsets
        // ------------------------------------------------------
        for (int i = 0; i < header.getLayerCount(); i++) layers.allocateLayer();
        layers.read(iStream, position);
    }

    @Override
    public Class<? extends MSLALayerCodec<List<CXDLPFileLayerLine>>> getCodec() {
        return CXDLPLayerCodec.class;
    }

    @Override public MSLAPreview getPreview() {
        return previews.getPreview(0);
    }
    @Override public void updatePreviewImage() {}
    @Override public float getDPI() { return 0; }
    @Override public float getPixelSizeUm() { return header.getPixelSizeUm(); }

    @Override
    public void addLayer(MSLALayerEncodeReader reader,
                         MSLALayerEncoder.Callback<List<CXDLPFileLayerLine>> callback)
            throws MSLAException
    {
        addLayer(reader, callback, 0, 0, 0, 0);
    }

    @Override
    public void addLayer(MSLALayerEncodeReader reader,
                         MSLALayerEncoder.Callback<List<CXDLPFileLayerLine>> callback,
                         float layerHeight, float exposureTime, float liftSpeed, float liftHeight)
            throws MSLAException
    {
        var layer = layers.allocateLayer();
        header.setLayerCount(layers.getLayersCount().shortValue());
        layers.encodeLayer(layer, reader, getEncodersPool(), callback);
    }

    public final CXDLPFileLayer getLayer(int index) { return layers.get(index); }

    @Override
    public boolean readLayer(
            MSLALayerDecoder< List<CXDLPFileLayerLine>> decoders,
            int layer) throws MSLAException
    {
        return layers.decodeLayer(iStream, layer, decoders);
    }

    @Override
    public void write(OutputStream stream) throws MSLAException {
        if (header == null) throw new MSLAException("Header is empty, a file cannot be written");
        if (sliceInfo == null) throw new MSLAException("SliceInfo is empty, a file cannot be written");
        header.write(stream);
        previews.write(stream);
        sliceInfo.write(stream);
        layers.writeLayerAreas(stream);
        if (header.getVersion() >= 3) sliceInfoV3.write(stream);
        layers.write(stream);
    }

    @Override public Size getResolution() { return header.getResolution(); }
    @Override public int getLayerCount() { return header.getLayerCount(); }
    @Override public boolean isValid() { return (header != null) && (sliceInfo != null); }
    @Override public MSLAOptionMapper options() {
        return optionMapper;
    }
    @Override public String toString() {
        return header.toString() + sliceInfo + sliceInfoV3 + previews;
    }
}
