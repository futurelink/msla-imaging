package futurelink.msla.formats.creality.tables;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.MSLALayerDecoders;
import futurelink.msla.formats.iface.MSLALayerEncodeReader;
import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.util.ArrayList;

public class CXDLPFileLayerDef extends CXDLPFileTable {
    @Getter @Setter private Integer LayerCount;
    private final ArrayList<CXDLPFileLayer> layers = new ArrayList<>();

    @Override
    public void read(FileInputStream iStream, int layerDataPosition) throws IOException {
        var dis = new DataInputStream(iStream);
        var currentPosition = layerDataPosition;
        for (int i = 0; i < LayerCount; i++) {
            var fc = iStream.getChannel();
            fc.position(currentPosition);
            var layer = new CXDLPFileLayer();
            layer.LayerArea = dis.readInt();
            layer.LineCount = dis.readInt();
            layer.DataLength = layer.LineCount * 6 + 8 + 2;
            layer.DataOffset = currentPosition;
            layers.add(layer);

            currentPosition += layer.DataLength;
        }
    }

    @Override
    public int getDataLength() {
        return 0;
    }

    @Override
    public void write(OutputStream stream) throws IOException {
        for (int i = 0; i < getLayerCount(); i++) {
            layers.get(i).writeData(stream);
        }
    }

    public void writeLayerAreas(OutputStream stream) throws IOException {
        var dos = new DataOutputStream(stream);
        for (int i = 0; i < getLayerCount(); i++) {
            dos.writeInt(layers.get(i).LayerArea);
        }
        stream.write(0x0d); stream.write(0x0a);
    }

    public final boolean encodeLayer(MSLALayerEncodeReader reader) throws IOException {
        var number = layers.size();
        LayerCount = number;

        var iStream = reader.read(number, MSLALayerEncodeReader.ReadDirection.READ_COLUMN);
        var iSize = iStream.available();
        reader.onStart(number);
        if (iStream.available() > 0) { // Encode pixel data into lines
            var layer = new CXDLPFileLayer(
                    reader.getResolution().getWidth(),
                    reader.getResolution().getHeight(),
                    iStream);
            layers.add(layer);
            reader.onFinish(number, iSize, layer.DataLength);
        } else
            reader.onError(number, "empty image");

        return true;
    }

    /**
     * Decode layer
     * --------------------------------------------------------------------------------------------------------
     * Layers stored as a set of lines of particular color, each line consists of 6 bytes - 5 bytes of geometry
     * and 1 byte of grey shade (0 - black, 0xff - white).
     */
    public boolean decodeLayer(FileInputStream iStream, int layer, MSLALayerDecoders decoders) throws MSLAException {
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
        System.out.println("CXDLP file position " + position +
                ", data length is " + encodedDataLength +
                ", expected data length " + decodedDataLength);
        return decoders.decode(layer, dis, encodedDataLength, decodedDataLength);
    }
}
