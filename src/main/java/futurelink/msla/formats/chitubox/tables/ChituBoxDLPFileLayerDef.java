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
import java.util.Arrays;

public class ChituBoxDLPFileLayerDef implements MSLAFileBlock {
    public static class Layer {
        public static class LayerLine {
            static public final byte CoordinateCount = 5;
            @Getter
            private byte[] Coordinates = new byte[CoordinateCount];
            @Getter
            private byte Gray;

            public short getStartY() {
                var c1 = Byte.toUnsignedInt(Coordinates[0]) << 8;
                var c2 = Byte.toUnsignedInt(Coordinates[1]);
                return (short) ((((c1 + c2) & 0xffff) >> 3) & 0x1FFF);
            }

            public short getEndY() {
                var c1 = Byte.toUnsignedInt(Coordinates[1]) << 16;
                var c2 = Byte.toUnsignedInt(Coordinates[2]) << 8;
                var c3 = Byte.toUnsignedInt(Coordinates[3]);
                return (short) ((((c1 + c2 + c3) & 0xffffff) >> 6) & 0x1FFF);
            }

            public short getStartX() {
                var c1 = Byte.toUnsignedInt(Coordinates[3]) << 8;
                var c2 = Byte.toUnsignedInt(Coordinates[4]);
                return (short) (((c1 + c2) & 0xffff) & 0x3FFF);
            }

            public short getLength() {
                return (short) (getEndY() - getStartY());
            }

            public static byte[] GetBytes(short startY, short endY, short startX, byte gray) {
                var bytes = new byte[CoordinateCount + 1];
                bytes[0] = (byte) ((startY >> 5) & 0xFF);
                bytes[1] = (byte) (((startY << 3) + (endY >> 10)) & 0xFF);
                bytes[2] = (byte) ((endY >> 2) & 0xFF);
                bytes[3] = (byte) (((endY << 6) + (startX >> 8)) & 0xFF);
                bytes[4] = (byte) startX;
                bytes[5] = gray;
                return bytes;
            }

            public static LayerLine fromByteArray(byte[] bytes) {
                var l = new LayerLine();
                l.Coordinates[0] = bytes[0];
                l.Coordinates[1] = bytes[1];
                l.Coordinates[2] = bytes[2];
                l.Coordinates[3] = bytes[3];
                l.Coordinates[4] = bytes[4];
                l.Gray = bytes[5];
                return l;
            }

            @Override
            public String toString() {
                return "LayerLine {" +
                        "Coordinates = " + Arrays.toString(Coordinates) +
                        ", Gray = " + Gray +
                        ", StartY = " + getStartY() +
                        ", EndY = " + getEndY() +
                        ", StartX = " + getStartX() +
                        ", Length = " + getLength() +
                        "}\n";
            }
        }

        @Getter private Integer DataOffset;
        @Getter private Integer DataLength;

        @Getter private Integer LayerArea;
        @Getter private Integer LineCount;
        @Getter private LayerLine[] Lines;
    }

    @Getter @Setter private Integer LayerCount;
    private final ArrayList<Layer> layers = new ArrayList<>();

    @Override
    public void read(FileInputStream iStream, int layerDataPosition) throws IOException {
        var dis = new DataInputStream(iStream);
        var currentPosition = layerDataPosition;
        for (int i = 0; i < LayerCount; i++) {
            var fc = iStream.getChannel();
            fc.position(currentPosition);
            var layer = new Layer();
            layer.LayerArea = dis.readInt();
            layer.LineCount = dis.readInt();
            layer.DataLength = layer.LineCount * 6 + 8 + 2;
            layer.DataOffset = currentPosition;
            layers.add(layer);

            currentPosition += layer.DataLength;
            //System.out.println("Layer " + i + " lines " + layer.LineCount + " data length " + layer.DataLength);
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
        //System.out.println("Position: " + position);
        //System.out.println("Skipped field value: " + t);
        var lineCount = dis.readInt();
        //System.out.print("Line count: " + lineCount + "\n");
        var pos = 0;
        var count = 0;
        var pixelsCount = 0;
        var dataLen = 0;
        for (int i = 0; i < lineCount; i++) {
            var line = Layer.LayerLine.fromByteArray(dis.readNBytes(6));
            count = line.getLength();
            pos = line.getStartX() + line.getStartY() * writer.getLayerResolution().getWidth();
            writer.stripe(layer, line.getGray(), pos, count, true);
            pixelsCount += count;
            dataLen += 6;
        }
        //System.out.println("Pixels count: " + pixelsCount);
        //System.out.println("Bytes count: " + dataLen);
        dis.readShort(); // 0x0d 0x0a - page break
        writer.onFinish(layer, pixelsCount);
    }
}
