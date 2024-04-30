package futurelink.msla.formats.creality.tables;

import futurelink.msla.formats.iface.MSLALayerEncodeReader;
import futurelink.msla.tools.BufferedImageInputStream;
import lombok.Getter;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Arrays;
import java.util.Objects;

@Getter
public class CXDLPFileLayer {
    @Getter
    public static class LayerLine {
        static public final byte CoordinateCount = 5;
        private final byte[] Coordinates = new byte[CoordinateCount];
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

        public LayerLine() {}

        public LayerLine(short startY, short endY, short startX, byte gray) {
            Coordinates[0] = (byte) ((startY >> 5) & 0xFF);
            Coordinates[1] = (byte) (((startY << 3) + (endY >> 10)) & 0xFF);
            Coordinates[2] = (byte) ((endY >> 2) & 0xFF);
            Coordinates[3] = (byte) (((endY << 6) + (startX >> 8)) & 0xFF);
            Coordinates[4] = (byte) startX;
            Gray = gray;
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
        public boolean equals(Object o) {
            if (this == o) return true;
            if ((o == null) || (getClass() != o.getClass())) return false;
            var layerLine = (LayerLine) o;
            return (Gray == layerLine.Gray) && Arrays.equals(Coordinates, layerLine.Coordinates);
        }

        @Override
        public int hashCode() {
            return 31 * Objects.hash(Gray) + Arrays.hashCode(Coordinates);
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
                    "}";
        }
    }

    // Used to position in a file when read
    Integer DataOffset = 0;
    Integer DataLength = 0;

    // Layer data
    Integer LayerArea = 0;
    Integer LineCount = 0;
    private LayerLine[] Lines = new LayerLine[0];

    private void addLine(LayerLine line) {
        Lines = Arrays.copyOf(Lines, LineCount+1);
        Lines[LineCount] = line;
        LineCount++;
    }

    private void readStream(int width, int height, InputStream data) throws IOException {
        var color = 0;
        int startY = 0, endY = 0, startX = 0;
        int pos = 0;
        while (data.available() > 0) {
            var t = data.read();
            if (color != t) {
                if (color != 0) {   // color change, break the line and start new
                    addLine(new CXDLPFileLayer.LayerLine((short) startY, (short) endY, (short) startX, (byte) color));
                } else {            // break the line, no start
                    startX = pos / height;
                    startY = pos % height;
                    endY = startY;
                }
            } else endY++;          // continue the line
            color = t;
            pos++;
        }
        DataLength = getLineCount() * 6 + 2;
    }

    public CXDLPFileLayer() {}

    public CXDLPFileLayer(int width, int height, InputStream data) throws IOException {
        readStream(width, height, data);
    }

    public CXDLPFileLayer(BufferedImage img) throws IOException {
        var st = new BufferedImageInputStream(img, MSLALayerEncodeReader.ReadDirection.READ_COLUMN);
        readStream(img.getWidth(), img.getHeight(), st);
    }

    public void writeData(OutputStream stream) throws IOException {
        var dos = new DataOutputStream(stream);
        dos.writeInt(LayerArea);
        dos.writeInt(LineCount);
        for (int i = 0; i < LineCount; i++) {
            dos.write(Lines[i].Coordinates);
            dos.writeByte(Lines[i].Gray);
        }
        dos.writeShort(0x0d0a);
    }

    @Override
    public String toString() {
        return "ChituBoxDLPFileLayer { " +
                "DataOffset=" + DataOffset +
                ", DataLength=" + DataLength +
                ", LayerArea=" + LayerArea +
                ", LineCount=" + LineCount +
                ", Lines=" + Arrays.toString(Lines) +
                '}';
    }
}
