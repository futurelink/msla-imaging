package futurelink.msla.formats.elegoo;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.MSLAFileGeneric;
import futurelink.msla.formats.MSLAOptionMapper;
import futurelink.msla.formats.elegoo.tables.GOOFileFooter;
import futurelink.msla.formats.elegoo.tables.GOOFileHeader;
import futurelink.msla.formats.elegoo.tables.GOOFileLayerDef;
import futurelink.msla.formats.iface.*;
import futurelink.msla.formats.utils.Size;

import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

public class GOOFile extends MSLAFileGeneric<byte[]> {
    private final GOOFileHeader Header;
    private final List<GOOFileLayerDef> LayersDef = new LinkedList<>();
    private final GOOFileFooter Footer = new GOOFileFooter();

    public GOOFile(MSLAFileDefaults defaults) throws MSLAException {
        super();
        Header = new GOOFileHeader(defaults);
    }

    @Override public Class<? extends MSLALayerCodec<byte[]>> getCodec() { return GOOFileCodec.class; }
    @Override public MSLAPreview getPreview() { return null; }
    @Override public void updatePreviewImage() throws MSLAException {}
    @Override public float getDPI() { return 0; }
    @Override public Size getResolution() { return Header.getResolution(); }
    @Override public float getPixelSizeUm() { return Header.getPixelSizeUm(); }
    @Override public int getLayerCount() { return LayersDef.size(); }

    @Override
    public void addLayer(
            MSLALayerEncodeReader reader,
            MSLALayerEncoder.Callback<byte[]> callback) throws MSLAException
    {
        addLayer(reader, callback, 0, 0, 0, 0);
    }

    @Override
    public void addLayer(
            MSLALayerEncodeReader reader,
            MSLALayerEncoder.Callback<byte[]> callback,
            float layerHeight, float exposureTime, float liftSpeed, float liftHeight) throws MSLAException
    {
        LayersDef.add(new GOOFileLayerDef());
    }

    @Override
    public boolean readLayer(MSLALayerDecoder<byte[]> decoders, int layer) throws MSLAException {
        return false;
    }

    @Override
    public void write(OutputStream stream) throws MSLAException {
        Header.write(stream);
        for (var layer : LayersDef) { layer.write(stream); }
        Footer.write(stream);
    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public MSLAOptionMapper options() {
        return null;
    }
}
