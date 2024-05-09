package futurelink.msla.formats.elegoo;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.MSLAFileGeneric;
import futurelink.msla.formats.MSLAOptionMapper;
import futurelink.msla.formats.elegoo.tables.GOOFileFooter;
import futurelink.msla.formats.elegoo.tables.GOOFileHeader;
import futurelink.msla.formats.elegoo.tables.GOOFileLayerDef;
import futurelink.msla.formats.iface.*;
import futurelink.msla.formats.utils.Size;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

public class GOOFile extends MSLAFileGeneric<byte[]> {
    private final GOOFileHeader header;
    private final List<GOOFileLayerDef> layersDef;
    private final GOOFileFooter footer = new GOOFileFooter();
    private final GOOFileOptionMapper optionMapper;

    public GOOFile(MSLAFileDefaults defaults) throws MSLAException {
        super();
        header = new GOOFileHeader(defaults);
        layersDef =  new LinkedList<>();
        optionMapper = new GOOFileOptionMapper(this);
    }

    public GOOFile(FileInputStream stream) throws IOException, MSLAException {
        super();
        header = new GOOFileHeader();
        layersDef =  new LinkedList<>();
        readTables(stream);
        optionMapper = new GOOFileOptionMapper(this);
    }

    private void readTables(FileInputStream input) throws MSLAException {
        var pos = header.read(input, 0);
        if (pos != header.getLayerDefAddress()) throw new MSLAException("Invalid layer definition at position " + pos);

        var layerOffset = 0L;
        for (var i = 0; i < header.getLayerCount(); i++) {
            var layer = new GOOFileLayerDef();
            pos += layer.read(input, pos);
            layerOffset += layer.getDataLength();
            // Check if stream position is still ok while reading layers
            if (header.getLayerDefAddress() + layerOffset != pos)
                throw new MSLAException("Invalid layer " + layer + " definition at position " + pos);
            layersDef.add(layer);
        }
    }

    @Override public Class<? extends MSLALayerCodec<byte[]>> getCodec() { return GOOFileCodec.class; }
    @Override public MSLAPreview getPreview() { return header.getSmallPreview(); }
    @Override public float getDPI() { return 0; }
    @Override public Size getResolution() { return header.getResolution(); }
    @Override public float getPixelSizeUm() { return header.getPixelSizeUm(); }
    @Override public int getLayerCount() { return layersDef.size(); }

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
        var layer = new GOOFileLayerDef();
        var layerNumber = layersDef.size();
        layersDef.add(layer);
        getEncodersPool().encode(layerNumber, reader, null, callback);
    }

    @Override
    public boolean readLayer(MSLALayerDecodeWriter writer, int layer) throws MSLAException {
        var input = new GOOFileCodec.Input(layersDef.get(layer).getFields().getData());
        return getDecodersPool().decode(layer, writer, input, null);
    }

    @Override
    public void write(OutputStream stream) throws MSLAException {
        header.write(stream);
        for (var layer : layersDef) { layer.write(stream); }
        footer.write(stream);
    }

    @Override
    public boolean isValid() {
        return (header != null && layersDef != null);
    }

    @Override
    public MSLAOptionMapper options() {
        return null;
    }

    @Override
    public String toString() {
        return header.toString();
                //layersDef.toString() + "\n" +
                //footer.toString();
    }
}
