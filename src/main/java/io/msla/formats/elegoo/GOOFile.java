package io.msla.formats.elegoo;

import io.msla.formats.MSLAException;
import io.msla.formats.MSLAFileGeneric;
import io.msla.formats.elegoo.tables.GOOFileFooter;
import io.msla.formats.elegoo.tables.GOOFileHeader;
import io.msla.formats.elegoo.tables.GOOFileLayers;
import io.msla.formats.iface.*;
import io.msla.formats.iface.options.MSLAOptionContainer;
import io.msla.formats.io.FileFieldsException;
import io.msla.utils.Size;
import lombok.Getter;

import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Objects;

/**
 * ELEGOO GOO file.
 */
public class GOOFile extends MSLAFileGeneric<byte[]> {
    @Getter @MSLAOptionContainer private final GOOFileHeader Header;
    @Getter private final GOOFileLayers Layers;
    private final GOOFileFooter Footer = new GOOFileFooter();

    public GOOFile(MSLAFileProps initialProps) throws MSLAException {
        super(initialProps);
        Header = new GOOFileHeader();
        Layers = new GOOFileLayers();
    }

    public GOOFile(DataInputStream stream) throws IOException, MSLAException {
        super(null);
        Header = new GOOFileHeader();
        Layers = new GOOFileLayers();
        readTables(stream);
    }

    private void readTables(DataInputStream input) throws MSLAException {
        var pos = Header.read(input, 0);
        if (pos != Header.getLayerDefAddress()) throw new MSLAException("Invalid layer definition at position " + pos);

        try {
            var layerOffset = 0L;
            for (var i = 0; i < Header.getLayerCount(); i++) {
                var layer = Layers.allocate();
                pos += layer.read(input, pos);
                layerOffset += layer.getDataLength();
                // Check if stream position is still ok while reading layers
                if (Header.getLayerDefAddress() + layerOffset != pos)
                    throw new MSLAException("Invalid layer " + layer + " definition at position " + pos);
            }
        } catch (FileFieldsException e) {
            throw new MSLAException("Can't read block", e);
        }
    }

    @Override public Class<? extends MSLALayerCodec<byte[]>> getCodec() { return GOOFileCodec.class; }
    @Override public Short getPreviewsNumber() { return 2; }
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
                ((getResolution() == null) || Objects.equals(defaults.getResolution(), getResolution()));

    }

    @Override
    public void reset(MSLAFileDefaults defaults) throws MSLAException {
        super.reset(defaults);
        defaults.setFields(Header.getBlockFields());
        getLayers().setDefaults(defaults.getLayerDefaults());
    }

    @Override public String getMachineName() { return Header.getMachineName(); }
    @Override public float getDPI() { return 0; }
    @Override public Size getResolution() { return Header.getResolution(); }

    @Override
    public void addLayer(
            MSLALayerEncodeReader reader,
            MSLALayerEncoder.Callback<byte[]> callback) throws MSLAException
    {
        Layers.add(getEncodersPool(), reader, new HashMap<>(), callback);
        Header.setLayerCount(Layers.count());
    }

    @Override
    public boolean readLayer(MSLALayerDecodeWriter writer, int layer) throws MSLAException {
        return Layers.readLayer(getDecodersPool(), writer, layer);
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