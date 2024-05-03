package futurelink.msla.formats.creality.tables;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.iface.MSLAFileBlockFields;
import futurelink.msla.formats.iface.MSLALayerDecoder;
import futurelink.msla.formats.iface.MSLALayerEncodeReader;
import futurelink.msla.formats.creality.CXDLPLayerCodec;
import futurelink.msla.formats.iface.MSLALayerEncoder;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class CXDLPFileLayerDef extends CXDLPFileTable {
    private static final Logger logger = Logger.getLogger(CXDLPFileLayerDef.class.getName());
    private final ArrayList<CXDLPFileLayer> layers = new ArrayList<>();

    @Override
    public MSLAFileBlockFields getFields() {
        return null;
    }

    @Override
    public final void read(FileInputStream iStream, int layerDataPosition) throws MSLAException {
        var dis = new DataInputStream(iStream);
        var currentPosition = layerDataPosition;
        try {
            for (var layer : layers) {
                var fc = iStream.getChannel();
                fc.position(currentPosition);
                layer.LayerArea = dis.readInt();
                var lineCount = dis.readInt();
                // Some integer (4 bytes) + number of lines (4 bytes) + 6 bytes per line + 0x0D0A at the end
                layer.DataLength = 8 + lineCount * 6 + 2;
                layer.DataOffset = currentPosition;
                currentPosition += layer.DataLength;
            }
        } catch (IOException e) {
            throw new MSLAException("Can't read layer definition", e);
        }
    }

    @Override
    public final int getDataLength() {
        return 0;
    }

    @Override
    public final void write(OutputStream stream) throws MSLAException {
        logger.finest("Writing " + layers.size() + " layers into a stream");
        try {
            for (var layer : layers) layer.writeData(stream);
        } catch (IOException e) {
            throw new MSLAException("Can't write layer definition", e);
        }
    }

    public final Integer getLayersCount() {
        return layers.size();
    }

    public final CXDLPFileLayer get(int index) {
        return layers.get(index);
    }

    public final CXDLPFileLayer allocateLayer() {
        var layer = new CXDLPFileLayer();
        layers.add(layer);
        return layer;
    }

    public final void writeLayerAreas(OutputStream stream) throws MSLAException {
        var dos = new DataOutputStream(stream);
        try {
            for (var layer : layers) { dos.writeInt(layer.LayerArea); }
            stream.write(0x0d); stream.write(0x0a);
        } catch (IOException e) {
            throw new MSLAException("Can't write layer areas", e);
        }
    }

    public final void encodeLayer(CXDLPFileLayer layer,
                                MSLALayerEncodeReader reader,
                                MSLALayerEncoder<List<CXDLPFileLayerLine>> encoders,
                                  MSLALayerEncoder.Callback<List<CXDLPFileLayerLine>> callback) throws MSLAException
    {
        logger.finest("Encoding layer " + layer + "...");
        reader.setReadDirection(MSLALayerEncodeReader.ReadDirection.READ_COLUMN);
        var number = layers.indexOf(layer);
        encoders.encode(number, reader, (layerNumber, data) -> {
            // When encoding is done then fill layer with lines
            for (var line : data.data()) layer.addLine(line);
            if (callback != null) callback.onFinish(layerNumber, data);
        });
    }

    /**
     * Decode layer
     * --------------------------------------------------------------------------------------------------------
     * Layers stored as a set of lines of particular color, each line consists of 6 bytes - 5 bytes of geometry
     * and 1 byte of grey shade (0 - black, 0xff - white).
     */
    public final boolean decodeLayer(
            FileInputStream iStream,
            int layer,
            MSLALayerDecoder<List<CXDLPFileLayerLine>> decoders) throws MSLAException
    {
        logger.finest("Decoding layer " + layer + "...");
        var position = layers.get(layer).getDataOffset();
        var decodedDataLength = layers.get(layer).DataLength;
        var dis = new DataInputStream(iStream);
        try {
            var fc = iStream.getChannel();
            fc.position(position);
        } catch (IOException e) {
            throw new MSLAException("Can't go to layer data position", e);
        }

        var encodedDataLength = layers.get(layer).DataLength;
        logger.finest("CXDLP file position " + position +
                ", data length is " + encodedDataLength +
                ", expected data length " + decodedDataLength);
        return decoders.decode(layer, new CXDLPLayerCodec.Input(dis, encodedDataLength), decodedDataLength);
    }
}
