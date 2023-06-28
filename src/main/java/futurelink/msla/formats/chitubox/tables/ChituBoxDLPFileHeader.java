package futurelink.msla.formats.chitubox.tables;

import futurelink.msla.formats.MSLAFileBlock;
import futurelink.msla.formats.MSLAOption;
import futurelink.msla.formats.utils.Size;
import lombok.Getter;
import lombok.experimental.Delegate;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;

public class ChituBoxDLPFileHeader implements MSLAFileBlock {
    private static class Fields {
        public static final byte HEADER_SIZE = 9; // CXSW3DV2
        public static  final String HEADER_VALUE = "CXSW3DV2";
        private final String HEADER_VALUE_GENERIC = "CXSW3D";
        private final byte DEFAULT_VERSION = 3;

        @Getter private int HeaderSize = HEADER_SIZE;
        @Getter private String HeaderValue = HEADER_VALUE;
        @Getter private short Version = DEFAULT_VERSION;
        @Getter private int PrinterModelSize  = 6;
        @Getter private byte[] PrinterModelArray;
        @Getter private short LayerCount;
        @Getter private Size Resolution;
        @Getter private byte[] Offset = new byte[64];
    }

    @Delegate private final Fields fields = new Fields();

    @Override
    public int getDataLength() { return fields.HeaderSize + fields.PrinterModelSize + 16 + 64; }
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
        fields.Offset = dis.readNBytes(64);
    }

    @Override
    public void write(OutputStream stream) throws IOException {

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
                "Resolution: " + fields.Resolution + "\n" +
                "Offset: " + new String(fields.Offset).trim() + "\n";
    }
    public HashMap<String, Class<?>> getOptions() {
        var optionsMap = new HashMap<String, Class<?>>();
        Arrays.stream(Fields.class.getDeclaredFields())
                .filter((f) -> f.getAnnotation(MSLAOption.class) != null)
                .forEach((f) -> optionsMap.put(f.getName(), f.getType()));
        return optionsMap;
    }
}
