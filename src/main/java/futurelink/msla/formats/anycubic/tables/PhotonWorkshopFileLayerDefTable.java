package futurelink.msla.formats.anycubic.tables;

import com.google.common.io.LittleEndianDataInputStream;
import com.google.common.io.LittleEndianDataOutputStream;
import futurelink.msla.formats.*;
import futurelink.msla.formats.iface.MSLALayerEncodeReader;
import lombok.Getter;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * "LAYERDEF" section representation.
 */
public class PhotonWorkshopFileLayerDefTable extends PhotonWorkshopFileTable {
    public static final String Name = "LAYERDEF";
    public static class PhotonWorkshopFileLayerDef {
        public Integer DataAddress;
        public Integer DataLength;
        public Float LiftHeight = 0.0f;
        public Float LiftSpeed = 0.0f;
        public Float ExposureTime = 0.0f;
        public Float LayerHeight = 0.0f;
        public Integer NonZeroPixelCount = 0;
        public Integer Padding1 = 0;

        @Override
        public String toString() {
            return "Data at " + DataAddress + ", length " + DataLength + ", time " + ExposureTime + ", pixels: " + NonZeroPixelCount;
        }
    }

    @Getter private Integer LayerCount;
    private final ArrayList<PhotonWorkshopFileLayerDef> layers = new ArrayList<>();
    private final ArrayList<byte[]> layerData = new ArrayList<>();

    public PhotonWorkshopFileLayerDefTable(byte versionMajor, byte versionMinor) {
        super(versionMajor, versionMinor);
        this.LayerCount = 0;
    }

    /**
     * Asynchronously encodes a layer.
     *
     * @param def PhotonWorkshopFileLayerDef instance
     * @param reader MSLAEncodeReader instance
     * @return true if encode process successfully started, otherwise - false
     * @throws MSLAException if no codec defined or something went wrong
     */
    public final boolean encodeLayer(PhotonWorkshopFileLayerDef def,
                                     MSLALayerEncodeReader reader,
                                     MSLALayerEncoders encoders) throws MSLAException {
        if (reader.getCodec() == null) throw new MSLAException("No codec defined for layer data");
        if (encoders.isBusy()) return false;

        // Add empty layer
        var number = layers.size();
        layers.add(def);
        layerData.add(null);
        LayerCount = layers.size();

        // Encode layer data
        return encoders.encode(number, reader, (size, data) -> {
            layers.get(number).DataLength = size;
            layerData.set(number, data);
        });
    }

    /**
     * Asynchronously decodes a layer.
     *
     * @param stream input stream
     * @param layer layer number
     * @param decodedDataLength expected decoded data length
     * @param decoders decoders pool
     * @return true if encode process successfully started, otherwise - false
     */
    public final boolean decodeLayer(DataInputStream stream, int layer, int decodedDataLength, MSLALayerDecoders decoders)
            throws MSLAException {
        return decoders.decode(layer, stream, getLayer(layer).DataLength, decodedDataLength);
    }

    /**
     * Adds raw encoded data layer.
     * @param def layer definition
     * @param data bytes
     */
    public final void addLayer(PhotonWorkshopFileLayerDef def, byte[] data) throws MSLAException {
        if ((data != null) && (def != null)) {
            if (data.length != def.DataLength) throw new MSLAException("DataLength in layer definition does not match data size");
            layers.add(def);
            layerData.add(data);
            LayerCount = layers.size();
        }
    }

    /**
     * Adds raw encoded data layer from InputStream.
     * @param def layer definition
     * @param stream stream
     */
    public final void addLayer(PhotonWorkshopFileLayerDef def, InputStream stream) throws MSLAException {
        if ((stream != null) && (def != null)) {
            try {
                addLayer(def, stream.readAllBytes());
            } catch (IOException e) {
                throw new MSLAException("Error reading layer", e);
            }
        }
    }

    public final PhotonWorkshopFileLayerDef getLayer(int i) {
        return layers.get(i);
    }

    public final  byte[] getLayerData(int i) {
        return layerData.get(i);
    }

    @Override
    final int calculateTableLength(byte versionMajor, byte versionMinor) {
        return 4 + getLayerCount() * 32;
    }

    @Override
    public void read(FileInputStream stream, int position) throws IOException {
        var fc = stream.getChannel(); fc.position(position);
        var dis = new LittleEndianDataInputStream(stream);

        int dataRead = 0;
        var headerMark = stream.readNBytes(Name.length());
        if (!Arrays.equals(headerMark, Name.getBytes())) {
            throw new IOException("Layer definition mark not found! Corrupted data.");
        }
        dis.readNBytes(MarkLength - Name.length()); // Skip section name zeroes
        TableLength = dis.readInt();
        LayerCount = dis.readInt();
        dataRead += 4;

        while (layers.size() < LayerCount) {
            var layer = new PhotonWorkshopFileLayerDef();
            layer.DataAddress = dis.readInt();
            layer.DataLength = dis.readInt();
            layer.LiftHeight = dis.readFloat();
            layer.LiftSpeed = dis.readFloat();
            layer.ExposureTime = dis.readFloat();
            layer.LayerHeight = dis.readFloat();
            layer.NonZeroPixelCount = dis.readInt();
            layer.Padding1 = dis.readInt();
            layers.add(layer);
            dataRead += 32;
        }

        if (dataRead != TableLength)
            throw new IOException("Layer definition was not completely read out (" + dataRead + " of " + TableLength +
                    "), some extra data left unread.");
    }

    @Override
    public final void write(OutputStream stream) throws IOException {
        var dos = new LittleEndianDataOutputStream(stream);
        dos.write(Name.getBytes());
        dos.write(new byte[PhotonWorkshopFileTable.MarkLength - Name.length()]);
        TableLength = calculateTableLength(versionMajor, versionMinor);

        dos.writeInt(TableLength);   // Pre-calculate table length
        dos.writeInt(LayerCount);    // Pre-calculate layer count
        for (var i = 0; i < LayerCount; i++) {
            var layer = layers.get(i);
            dos.writeInt(layer.DataAddress);
            dos.writeInt(layer.DataLength);
            dos.writeFloat(layer.LiftHeight);
            dos.writeFloat(layer.LiftSpeed);
            dos.writeFloat(layer.ExposureTime);
            dos.writeFloat(layer.LayerHeight);
            dos.writeInt(layer.NonZeroPixelCount);
            dos.writeInt(layer.Padding1);
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
