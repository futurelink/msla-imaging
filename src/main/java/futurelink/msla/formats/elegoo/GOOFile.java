package futurelink.msla.formats.elegoo;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.MSLAFileGeneric;
import futurelink.msla.formats.MSLAOptionMapper;
import futurelink.msla.formats.elegoo.tables.GOOFileFooter;
import futurelink.msla.formats.elegoo.tables.GOOFileHeader;
import futurelink.msla.formats.elegoo.tables.GOOFileLayerDef;
import futurelink.msla.formats.iface.*;
import futurelink.msla.formats.iface.annotations.MSLAOptionContainer;
import futurelink.msla.formats.utils.OptionMapper;
import futurelink.msla.formats.utils.Size;
import lombok.Getter;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

public class GOOFile extends MSLAFileGeneric<byte[]> {
    @Getter private final MSLAOptionMapper options;

    @Getter @MSLAOptionContainer private final GOOFileHeader Header;
    private final List<GOOFileLayerDef> LayersDef;
    private final GOOFileFooter Footer = new GOOFileFooter();

    public GOOFile(MSLAFileDefaults defaults) throws MSLAException {
        super();
        Header = new GOOFileHeader(defaults);
        LayersDef =  new LinkedList<>();
        options = new OptionMapper(this);
    }

    public GOOFile(FileInputStream stream) throws IOException, MSLAException {
        super();
        Header = new GOOFileHeader();
        LayersDef =  new LinkedList<>();
        readTables(stream);
        options = new OptionMapper(this);
    }

    private void readTables(FileInputStream input) throws MSLAException {
        var pos = Header.read(input, 0);
        if (pos != Header.getLayerDefAddress()) throw new MSLAException("Invalid layer definition at position " + pos);

        var layerOffset = 0L;
        for (var i = 0; i < Header.getLayerCount(); i++) {
            var layer = new GOOFileLayerDef();
            pos += layer.read(input, pos);
            layerOffset += layer.getDataLength();
            // Check if stream position is still ok while reading layers
            if (Header.getLayerDefAddress() + layerOffset != pos)
                throw new MSLAException("Invalid layer " + layer + " definition at position " + pos);
            LayersDef.add(layer);
        }
    }

    @Override public Class<? extends MSLALayerCodec<byte[]>> getCodec() { return GOOFileCodec.class; }
    @Override public MSLAPreview getPreview(int index) {
        if (index == 0) return Header.getSmallPreview();
        else return Header.getBigPreview();
    }
    @Override public void setPreview(int index, BufferedImage image) {
        if (index == 0) Header.getSmallPreview().setImage(image);
        else Header.getBigPreview().setImage(image);
    }

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
        var layer = new GOOFileLayerDef();
        var layerNumber = LayersDef.size();
        LayersDef.add(layer);
        getEncodersPool().encode(layerNumber, reader, null, callback);
    }

    @Override
    public boolean readLayer(MSLALayerDecodeWriter writer, int layer) throws MSLAException {
        var input = new GOOFileCodec.Input(LayersDef.get(layer).getFileFields().getData());
        return getDecodersPool().decode(layer, writer, input, null);
    }

    @Override
    public void write(OutputStream stream) throws MSLAException {
        Header.write(stream);
        for (var layer : LayersDef) { layer.write(stream); }
        Footer.write(stream);
    }

    @Override public boolean isValid() {
        return (Header != null && LayersDef != null);
    }

    @Override
    public String toString() {
        return Header.toString();
                //layersDef.toString() + "\n" +
                //footer.toString();
    }
}
