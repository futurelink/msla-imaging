package futurelink.msla.formats.anycubic.tables;

import com.google.common.io.LittleEndianDataInputStream;
import com.google.common.io.LittleEndianDataOutputStream;
import futurelink.msla.formats.MSLAEncodeReader;
import lombok.Getter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * "LAYERDEF" section representation.
 */
public class PhotonWorkshopFileLayerDefTable extends PhotonWorkshopFileTable {
    public static final String Name = "LAYERDEF";
    public static class PhotonWorkshopFileLayerDef {
        public int DataAddress;
        public int DataLength;
        public float LiftHeight;
        public float LiftSpeed;
        public float ExposureTime;
        public float LayerHeight;
        public int NonZeroPixelCount;
        public int Padding1;

        @Override
        public String toString() {
            return "Data at " + DataAddress + ", length " + DataLength + ", time " + ExposureTime + ", pixels: " + NonZeroPixelCount;
        }
    }

    @Getter private int LayerCount;
    private final ArrayList<PhotonWorkshopFileLayerDef> layers = new ArrayList<>();
    private final ArrayList<byte[]> layerData = new ArrayList<>();

    public void addLayer(PhotonWorkshopFileLayerDef def, MSLAEncodeReader reader) {
        var number = layers.size();
        layers.add(def);
        layerData.add(null);
        LayerCount = layers.size();
        reader.onStart(number);
        new Thread(() -> {
            try {
                var input = reader.read(number);
                var iSize = input.available();
                var output = new ByteArrayOutputStream();
                var oSize = reader.getCodec().Encode(input, output);
                if (output.size() > 0) {
                    layers.get(number).DataLength = oSize;
                    layerData.set(number, output.toByteArray());
                } else reader.onError(number, "empty image");
                reader.onFinish(number, iSize, oSize);
            } catch (IOException e) {
                reader.onError(number, "Encoder error " + e.getMessage());
            }
        }).start();
    }

    public void addLayer(PhotonWorkshopFileLayerDef def, byte[] data) throws IOException {
        if ((data != null) && (def != null)) {
            if (data.length != def.DataLength) throw new IOException("DataLength in layer definition does not match data size");
            layers.add(def);
            layerData.add(data);
            LayerCount = layers.size();
        }
    }

    public void addLayer(PhotonWorkshopFileLayerDef def, InputStream stream) throws IOException {
        if ((stream != null) && (def != null)) {
            layers.add(def);
            layerData.add(stream.readAllBytes());
            LayerCount = layers.size();
        }
    }

    public final PhotonWorkshopFileLayerDef getLayer(int i) {
        return layers.get(i);
    }

    public final  byte[] getLayerData(int i) {
        return layerData.get(i);
    }

    @Override
    int calculateTableLength(byte versionMajor, byte versionMinor) {
        return 4 + getLayerCount() * 32;
    }

    @Override
    public void read(LittleEndianDataInputStream stream) throws IOException {
        int dataRead = 0;
        var headerMark = stream.readNBytes(Name.length());
        if (!Arrays.equals(headerMark, Name.getBytes())) {
            throw new IOException("Layer definition mark not found! Corrupted data.");
        }
        stream.readNBytes(MarkLength - Name.length()); // Skip section name zeroes
        TableLength = stream.readInt();
        LayerCount = stream.readInt();
        dataRead += 4;

        while (layers.size() < LayerCount) {
            var layer = new PhotonWorkshopFileLayerDef();
            layer.DataAddress = stream.readInt();
            layer.DataLength = stream.readInt();
            layer.LiftHeight = stream.readFloat();
            layer.LiftSpeed = stream.readFloat();
            layer.ExposureTime = stream.readFloat();
            layer.LayerHeight = stream.readFloat();
            layer.NonZeroPixelCount = stream.readInt();
            layer.Padding1 = stream.readInt();
            layers.add(layer);
            dataRead += 32;
        }

        if (dataRead != TableLength)
            throw new IOException("Layer definition was not completely read out (" + dataRead + " of " + TableLength +
                    "), some extra data left unread.");
    }

    @Override
    public void write(LittleEndianDataOutputStream stream, byte versionMajor, byte versionMinor) throws IOException {
        stream.write(Name.getBytes());
        stream.write(new byte[PhotonWorkshopFileTable.MarkLength - Name.length()]);
        TableLength = calculateTableLength(versionMajor, versionMinor);

        stream.writeInt(TableLength);   // Pre-calculate table length
        stream.writeInt(LayerCount);    // Pre-calculate layer count
        for (var i = 0; i < LayerCount; i++) {
            var layer = layers.get(i);
            stream.writeInt(layer.DataAddress);
            stream.writeInt(layer.DataLength);
            stream.writeFloat(layer.LiftHeight);
            stream.writeFloat(layer.LiftSpeed);
            stream.writeFloat(layer.ExposureTime);
            stream.writeFloat(layer.LayerHeight);
            stream.writeInt(layer.NonZeroPixelCount);
            stream.writeInt(layer.Padding1);
        }
    }

    @Override
    public String toString() {
        var b = new StringBuilder();
        b.append("-- Layer definition data --\n");
        b.append("TableLength: ").append(TableLength).append("\n");
        b.append("Layers count: ").append(layers.size()).append("\n");
        for (var layer : layers) {
            b.append("-> ").append(layer).append("\n");
        }

        return b.toString();
    }
}

