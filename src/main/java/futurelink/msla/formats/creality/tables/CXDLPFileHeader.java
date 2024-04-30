package futurelink.msla.formats.creality.tables;

import futurelink.msla.formats.iface.MSLAFileBlockFields;
import futurelink.msla.formats.utils.Size;
import lombok.Getter;
import lombok.experimental.Delegate;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class CXDLPFileHeader extends CXDLPFileTable {
    public static class Fields implements MSLAFileBlockFields {
        public static final byte HEADER_SIZE = 9; // CXSW3DV2
        public static  final String HEADER_VALUE = "CXSW3DV2";
        private final String HEADER_VALUE_GENERIC = "CXSW3D";
        private final byte DEFAULT_VERSION = 3;

        @Getter private int HeaderSize = HEADER_SIZE;
        @Getter private String HeaderValue = HEADER_VALUE;
        @Getter private short Version = DEFAULT_VERSION;
        @Getter private int PrinterModelSize = 6;
        @Getter private byte[] PrinterModelArray;
        @Getter private short LayerCount;
        @Getter private Size Resolution;
        public int getDataLength() { return HeaderSize + PrinterModelSize + 16 + 64; }

        public Fields() {
            PrinterModelArray = new byte[PrinterModelSize];
        }

        public Fields(Size resolution, String printerModel) {
            Resolution = new Size(resolution);
            PrinterModelSize = printerModel.length()+1;
            PrinterModelArray = Arrays.copyOf(printerModel.getBytes(), PrinterModelSize);
        }

        public Fields(Fields defaults) {
            HeaderSize = defaults.HeaderSize;
            HeaderValue = defaults.HeaderValue;
            Version = defaults.getVersion();
            PrinterModelSize = defaults.PrinterModelSize;
            PrinterModelArray = defaults.PrinterModelArray;
            LayerCount = 0;
            Resolution = new Size(defaults.getResolution());
        }

        public void setPrinterModel(String model) {
            PrinterModelSize = model.length();
            PrinterModelArray = model.getBytes();
        }
    }

    @Delegate private Fields fields = new Fields();;

    public CXDLPFileHeader() {}

    public CXDLPFileHeader(MSLAFileBlockFields defaults) {
        fields = new Fields((Fields) defaults);
    }

    public void setLayerCount(short count) {
        fields.LayerCount = count;
    }

    @Override
    public void read(FileInputStream stream, int position) throws IOException {
        var dis = new DataInputStream(stream);
        fields.HeaderSize = dis.readInt();
        fields.HeaderValue = new String(stream.readNBytes(fields.HeaderSize), StandardCharsets.US_ASCII).trim();
        fields.Version = dis.readShort();
        fields.PrinterModelSize = dis.readInt();
        fields.PrinterModelArray = dis.readNBytes(fields.PrinterModelSize);
        fields.LayerCount = dis.readShort();
        fields.Resolution = new Size(dis.readShort(), dis.readShort());
        dis.readNBytes(64);
    }

    @Override
    public void write(OutputStream stream) throws IOException {
        var dos = new DataOutputStream(stream);
        dos.writeInt(fields.HeaderSize);
        dos.write(fields.HeaderValue.getBytes()); dos.write(0);
        dos.writeShort(fields.Version);
        dos.writeInt(fields.PrinterModelSize);
        dos.write(fields.PrinterModelArray);
        dos.writeShort(fields.LayerCount);
        dos.writeShort(fields.Resolution.getWidth());
        dos.writeShort(fields.Resolution.getHeight());
        for (int i = 0; i < 64; i++) dos.write(0); // Offset zeroes
    }

    @Override
    public String toString() {
        return "-- Header --\n" +
                "HeaderSize: " + fields.HeaderSize + "\n" +
                "HeaderValue: " + fields.HeaderValue + "\n" +
                "Version: " + fields.Version + "\n" +
                "PrinterModelSize: " + fields.PrinterModelSize + "\n" +
                "PrinterModelArray: " + new String(fields.PrinterModelArray).trim() + "\n" +
                "LayerCount: " + fields.LayerCount + "\n" +
                "Resolution: " + fields.Resolution + "\n";
    }
}
