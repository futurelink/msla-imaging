package futurelink.msla.formats.creality.tables;

import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

@Getter
public class CXDLPFileLayerLine {
    static public final byte CoordinateCount = 5;
    private final byte[] Coordinates = new byte[CoordinateCount];
    private int Gray;

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

    public CXDLPFileLayerLine() {}

    public CXDLPFileLayerLine(short startY, short endY, short startX, int gray) {
        Coordinates[0] = (byte) ((startY >> 5) & 0xFF);
        Coordinates[1] = (byte) (((startY << 3) + (endY >> 10)) & 0xFF);
        Coordinates[2] = (byte) ((endY >> 2) & 0xFF);
        Coordinates[3] = (byte) (((endY << 6) + (startX >> 8)) & 0xFF);
        Coordinates[4] = (byte) startX;
        Gray = gray & 0xff;
    }

    public int getGray() { return Gray & 0xff; }

    public final byte[] bytes() {
        var bytes = Arrays.copyOf(Coordinates, 6);
        bytes[5] = (byte) (Gray & 0xff);
        return bytes;
    }

    public static CXDLPFileLayerLine fromByteArray(byte[] bytes) {
        var l = new CXDLPFileLayerLine();
        l.Coordinates[0] = bytes[0];
        l.Coordinates[1] = bytes[1];
        l.Coordinates[2] = bytes[2];
        l.Coordinates[3] = bytes[3];
        l.Coordinates[4] = bytes[4];
        l.Gray = bytes[5] & 0xff;
        return l;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if ((o == null) || (getClass() != o.getClass())) return false;
        var layerLine = (CXDLPFileLayerLine) o;
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