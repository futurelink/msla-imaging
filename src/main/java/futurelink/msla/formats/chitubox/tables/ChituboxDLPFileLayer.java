package futurelink.msla.formats.chitubox.tables;

import lombok.Getter;

import java.util.Arrays;

public class ChituboxDLPFileLayer {
    static class LayerLine {
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

    @Getter Integer DataOffset;
    @Getter Integer DataLength;

    @Getter Integer LayerArea;
    @Getter Integer LineCount;
    @Getter private LayerLine[] Lines;
}
