package futurelink.msla.formats.chitubox.tables;

import futurelink.msla.formats.MSLADecodeWriter;
import futurelink.msla.formats.MSLAFileBlock;
import lombok.Getter;
import lombok.Setter;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class ChituBoxDLPFileLayerDef implements MSLAFileBlock {
    @Getter @Setter private Integer LayerCount;
    private final ArrayList<ChituboxDLPFileLayer> layers = new ArrayList<>();

    @Override
    public void read(FileInputStream iStream, int layerDataPosition) throws IOException {
        var dis = new DataInputStream(iStream);
        var currentPosition = layerDataPosition;
        for (int i = 0; i < LayerCount; i++) {
            var fc = iStream.getChannel();
            fc.position(currentPosition);
            var layer = new ChituboxDLPFileLayer();
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
        throw new RuntimeException("Chitubox format writing is not implemented yet");
    }

    /**
     * Decode layer
     * --------------------------------------------------------------------------------------------------------
     * Layers stored as a set of lines of particular color, each line consists of 6 bytes - 5 bytes of geometry
     * and 1 byte of grey shade (0 - black, 0xff - white).
     */
    public void decodeLayer(FileInputStream iStream, int layer, MSLADecodeWriter writer) throws IOException {
        var position = layers.get(layer).getDataOffset();
        var dis = new DataInputStream(iStream);
        var fc = iStream.getChannel();
        fc.position(position);

        // Layer: 4 bytes + 4 bytes line count + (6 * line count) bytes
        writer.onStart(layer);
        dis.readInt();
        var lineCount = dis.readInt();
        var pos = 0;
        var count = 0;
        var pixelsCount = 0;
        for (int i = 0; i < lineCount; i++) {
            var line = ChituboxDLPFileLayer.LayerLine.fromByteArray(dis.readNBytes(6));
            count = line.getLength();
            pos = line.getStartX() + line.getStartY() * writer.getLayerResolution().getWidth();
            writer.stripe(layer, line.getGray(), pos, count, true);
            pixelsCount += count;
        }
        dis.readShort(); // 0x0d 0x0a - page break
        writer.onFinish(layer, pixelsCount);
    }
}
