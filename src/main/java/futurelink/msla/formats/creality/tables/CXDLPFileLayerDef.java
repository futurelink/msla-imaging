package futurelink.msla.formats.creality.tables;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.iface.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class CXDLPFileLayerDef extends CXDLPFileTable implements MSLAFileLayers<CXDLPFileLayer, List<CXDLPFileLayerLine>> {
    private static final Logger logger = Logger.getLogger(CXDLPFileLayerDef.class.getName());
    private final ArrayList<CXDLPFileLayer> Layers = new ArrayList<>();

    @Override
    public final long read(DataInputStream iStream, long layerDataPosition) throws MSLAException {
        var dis = new DataInputStream(iStream);
        var currentPosition = layerDataPosition;
        try {
            for (var layer : Layers) {
                iStream.reset();
                iStream.skipNBytes(currentPosition);
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

        try {
            for (var layer : Layers) {
                var encodedDataLength = layer.DataLength;
                var decodedDataLength = layer.DataLength;
                var pos = layer.getDataOffset();
                iStream.reset();
                iStream.skipNBytes(pos);

                var bytesRead = 0;
                dis.readInt(); // Skip integer
                var lineCount = dis.readInt();
                bytesRead += 8;
                while (bytesRead < encodedDataLength - 2) { // Minus 2, because there's one extra 2 bytes at the end
                    layer.addLine(CXDLPFileLayerLine.fromByteArray(dis.readNBytes(6)));
                    bytesRead += 6;
                    lineCount--;
                }

                if (lineCount != 0)
                    throw new MSLAException("Error decoding data, not all " + lineCount + " lines were read");

                logger.finest("CXDLP file position " + pos +
                        ", data length is " + encodedDataLength +
                        ", expected data length " + decodedDataLength);
            }
        } catch (IOException e) {
            throw new MSLAException("Can't read layer data", e);
        }

        return currentPosition;
    }

    @Override
    public final void write(OutputStream stream) throws MSLAException {
        logger.finest("Writing " + Layers.size() + " layers into a stream");
        try {
            for (var layer : Layers) layer.writeData(stream);
        } catch (IOException e) {
            throw new MSLAException("Can't write layer definition", e);
        }
    }

    @Override public String getName() { return null; }
    @Override public final int getDataLength() { return 0; }
    @Override public MSLAFileBlockFields getBlockFields() { return null; }
    @Override public final int count() { return Layers.size(); }
    @Override public final CXDLPFileLayer get(int index) { return Layers.get(index); }
    @Override public boolean hasOptions() { return false; }

    @Override
    public void add(MSLALayerEncoder<List<CXDLPFileLayerLine>> encoder,
                    MSLALayerEncodeReader reader,
                    Map<String, Object> params,
                    MSLALayerEncoder.Callback<List<CXDLPFileLayerLine>> callback) throws MSLAException
    {
        var layerNumber = Layers.size();
        allocate();
        logger.finest("Encoding layer " + layerNumber + "...");
        reader.setReadDirection(MSLALayerEncodeReader.ReadDirection.READ_COLUMN);
        encoder.encode(layerNumber, reader, params, (layer, data) -> {
            Layers.get(layerNumber).DataLength = data.sizeInBytes();
            Layers.get(layerNumber).LayerArea = data.pixels();
            for (var line : data.data()) Layers.get(layerNumber).addLine(line);
            if (callback != null) callback.onFinish(layerNumber, data);
        });
    }

    @Override public void setDefaults(MSLALayerDefaults layerDefaults) {}

    @Override
    public final CXDLPFileLayer allocate() {
        var layer = new CXDLPFileLayer();
        Layers.add(layer);
        return layer;
    }

    public final void writeLayerAreas(OutputStream stream) throws MSLAException {
        var dos = new DataOutputStream(stream);
        try {
            for (var layer : Layers) { dos.writeInt(layer.LayerArea); }
            stream.write(0x0d); stream.write(0x0a);
        } catch (IOException e) {
            throw new MSLAException("Can't write layer areas", e);
        }
    }

}
