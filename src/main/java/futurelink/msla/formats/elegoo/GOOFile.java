package futurelink.msla.formats.elegoo;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.MSLAFileGeneric;
import futurelink.msla.formats.elegoo.tables.GOOFileFooter;
import futurelink.msla.formats.elegoo.tables.GOOFileHeader;
import futurelink.msla.formats.elegoo.tables.GOOFileLayers;
import futurelink.msla.formats.iface.*;
import futurelink.msla.formats.iface.annotations.MSLAOptionContainer;
import futurelink.msla.utils.Size;
import lombok.Getter;

import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

public class GOOFile extends MSLAFileGeneric<byte[]> {
    @Getter @MSLAOptionContainer private final GOOFileHeader Header;
    @Getter private final GOOFileLayers Layers;
    private final GOOFileFooter Footer = new GOOFileFooter();

    public GOOFile() {
        super();
        Header = new GOOFileHeader();
        Layers = new GOOFileLayers();
    }

    public GOOFile(DataInputStream stream) throws IOException, MSLAException {
        this();
        readTables(stream);
    }

    private void readTables(DataInputStream input) throws MSLAException {
        var pos = Header.read(input, 0);
        if (pos != Header.getLayerDefAddress()) throw new MSLAException("Invalid layer definition at position " + pos);

        var layerOffset = 0L;
        for (var i = 0; i < Header.getLayerCount(); i++) {
            var layer = Layers.allocate();
            pos += layer.read(input, pos);
            layerOffset += layer.getDataLength();
            // Check if stream position is still ok while reading layers
            if (Header.getLayerDefAddress() + layerOffset != pos)
                throw new MSLAException("Invalid layer " + layer + " definition at position " + pos);
        }
    }

    @Override public Class<? extends MSLALayerCodec<byte[]>> getCodec() { return GOOFileCodec.class; }
    @Override public MSLAPreview getPreview(int index) {
        if (index == 0) return Header.getSmallPreview();
        else return Header.getBigPreview();
    }

    @Override public MSLAPreview getLargePreview() { return Header.getBigPreview(); }
    @Override public void setPreview(int index, BufferedImage image) {
        if (index == 0) Header.getSmallPreview().setImage(image);
        else Header.getBigPreview().setImage(image);
    }

    @Override
    public boolean isMachineValid(MSLAFileDefaults defaults) {
        return defaults.getFileClass().equals(this.getClass()) &&
                (getResolution() == null || defaults.getResolution().equals(getResolution()));
    }

    @Override
    public void reset(MSLAFileDefaults defaults) throws MSLAException {
        super.reset(defaults);
        if (isMachineValid(defaults)) {
            defaults.setFields(Header.getName(), Header.getFileFields());
            getLayers().setDefaults(defaults.getLayerDefaults());
        } else throw new MSLAException("Defaults of '" + defaults.getMachineFullName() + "' not applicable to this file");
    }

    @Override public String getMachineName() { return Header.getMachineName(); }
    @Override public float getDPI() { return 0; }
    @Override public Size getResolution() { return Header.getResolution(); }
    @Override public float getPixelSizeUm() { return Header.getPixelSizeUm(); }

    @Override
    public void addLayer(
            MSLALayerEncodeReader reader,
            MSLALayerEncoder.Callback<byte[]> callback) throws MSLAException
    {
        Layers.add(getEncodersPool(), reader, new HashMap<>(), callback);
    }

    @Override
    public boolean readLayer(MSLALayerDecodeWriter writer, int layer) throws MSLAException {
        var input = new GOOFileCodec.Input(Layers.get(layer).getFileFields().getData());
        return getDecodersPool().decode(layer, writer, input, null);
    }

    @Override
    public void write(OutputStream stream) throws MSLAException {
        Header.write(stream);
        for (var i = 0; i < Layers.count(); i++) { Layers.get(i).write(stream); }
        Footer.write(stream);
    }

    @Override public boolean isValid() {
        return Header != null;
    }

    @Override
    public String toString() {
        return Header.toString();
                //layersDef.toString() + "\n" +
                //footer.toString();
    }
}
