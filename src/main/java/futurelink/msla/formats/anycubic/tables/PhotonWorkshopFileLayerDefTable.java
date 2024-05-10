package futurelink.msla.formats.anycubic.tables;

import futurelink.msla.formats.*;
import futurelink.msla.formats.anycubic.PhotonWorkshopCodec;
import futurelink.msla.formats.iface.*;
import futurelink.msla.formats.iface.annotations.MSLAFileField;
import futurelink.msla.formats.utils.FileFieldsException;
import futurelink.msla.formats.utils.FileFieldsIO;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * "LAYERDEF" section representation.
 */
@Getter
public class PhotonWorkshopFileLayerDefTable extends PhotonWorkshopFileTable {
    @Delegate private final Fields fileFields;

    @Getter
    public static class PhotonWorkshopFileLayerDef implements MSLAFileBlockFields {
        @MSLAFileField @Setter private Integer DataAddress = 0;
        @MSLAFileField(order = 1) private Integer DataLength = 0;
        @MSLAFileField(order = 2) @Setter private Float LiftHeight = 0.0f;
        @MSLAFileField(order = 3) @Setter private Float LiftSpeed = 0.0f;
        @MSLAFileField(order = 4) @Setter private Float ExposureTime = 0.0f;
        @MSLAFileField(order = 5) @Setter private Float LayerHeight = 0.0f;
        @MSLAFileField(order = 6) private Integer NonZeroPixelCount = 0;
        @MSLAFileField(order = 7) private final Integer Padding1 = 0;

        @Override
        public String toString() {
            return "Data at " + DataAddress + ", length " + DataLength + ", time " + ExposureTime + ", pixels: " + NonZeroPixelCount;
        }
    }

    @SuppressWarnings("unused")
    static class Fields implements MSLAFileBlockFields {
        private final PhotonWorkshopFileTable parent;

        @MSLAFileField(length = MarkLength, dontCount = true) private String Name() { return "LAYERDEF"; }
        // Validation setter checks for what's been read from file
        // and throws an exception when that is something unexpected.
        private void setName(String name) throws MSLAException {
            if (!"LAYERDEF".equals(name)) throw new MSLAException("Table name '" + name + "' is invalid");
        }
        @MSLAFileField(order = 1, dontCount = true) private Integer TableLength() { return parent.calculateTableLength(); }
        private void setTableLength(Integer length) { parent.TableLength = length; }
        @MSLAFileField(order = 2) @Getter private Integer LayerCount;
        @MSLAFileField(order = 3, lengthAt = "LayerCount") private final ArrayList<PhotonWorkshopFileLayerDef> Layers = new ArrayList<>();
        private final ArrayList<byte[]> layerData = new ArrayList<>();

        public Fields(PhotonWorkshopFileTable parent) { this.parent = parent; }
    }

    public PhotonWorkshopFileLayerDefTable(byte versionMajor, byte versionMinor) {
        super(versionMajor, versionMinor);
        this.fileFields = new Fields(this);
        this.fileFields.LayerCount = 0;
    }

    /**
     * Asynchronously encodes a layer.
     *
     * @param def PhotonWorkshopFileLayerDef instance
     * @param reader MSLAEncodeReader instance
     * @throws MSLAException if no codec defined or something went wrong
     */
    public final void encodeLayer(PhotonWorkshopFileLayerDef def,
                                  MSLALayerEncodeReader reader,
                                  MSLALayerEncoder<byte[]> encoders,
                                  MSLALayerEncoder.Callback<byte[]> callback) throws MSLAException {
        // Add empty layer
        var number = fileFields.Layers.size();
        fileFields.Layers.add(def);
        fileFields.layerData.add(null);
        fileFields.LayerCount = fileFields.Layers.size();

        var params = new HashMap<String, Object>();
        params.put("DecodedDataLength", reader.getSize());

        // Encode layer data
        encoders.encode(number, reader, params, (layerNumber, data) -> {
            fileFields.Layers.get(layerNumber).DataLength = data.sizeInBytes();
            fileFields.Layers.get(layerNumber).NonZeroPixelCount = data.pixels();
            fileFields.layerData.set(layerNumber, data.data());
            if (callback != null) callback.onFinish(layerNumber, data);
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
    public final boolean decodeLayer(
            int layer,
            DataInputStream stream,
            int decodedDataLength,
            MSLALayerDecoder<byte[]> decoders,
            MSLALayerDecodeWriter writer) throws MSLAException
    {
        try {
            var input = new PhotonWorkshopCodec.Input(stream.readNBytes(getLayer(layer).DataLength));
            var params = new HashMap<String, Object>();
            params.put("DecodedDataLength", decodedDataLength);
            return decoders.decode(layer, writer, input, params);
        } catch (IOException e) {
            throw new MSLAException("Error decoding layer data", e);
        }
    }

    /**
     * Adds raw encoded data layer.
     * @param def layer definition
     * @param data bytes
     */
    public final void addLayer(PhotonWorkshopFileLayerDef def, byte[] data) throws MSLAException {
        if ((data != null) && (def != null)) {
            if (data.length != def.DataLength) throw new MSLAException("DataLength in layer definition does not match data size");
            fileFields.Layers.add(def);
            fileFields.layerData.add(data);
            fileFields.LayerCount = fileFields.Layers.size();
        }
    }

    /**
     * Adds raw encoded data layer from InputStream.
     * @param def layer definition
     * @param stream stream
     */
    @SuppressWarnings("unused")
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
        return fileFields.Layers.get(i);
    }

    public final  byte[] getLayerData(int i) {
        return fileFields.layerData.get(i);
    }

    @Override
    public final int calculateTableLength() {
        return 4 + fileFields.getLayerCount() * 32;
    }

    @Override
    public long read(FileInputStream stream, long position) throws MSLAException {
        var dataRead = super.read(stream, position);
        if (dataRead != TableLength) throw new MSLAException(
                "LayerDef was not completely read out (" + dataRead + " of " + TableLength +
                        "), some extra data left unread"
        );
        return dataRead;
    }

    @Override
    public final void write(OutputStream stream) throws MSLAException {
        TableLength = calculateTableLength();
        super.write(stream);
    }

    @Override
    public String toString() {
        var b = new StringBuilder();
        b.append("-- Layer definition data --\n");
        b.append("TableLength: ").append(TableLength).append("\n");
        b.append("Layers count: ").append(fileFields.Layers.size()).append("\n");
        for (var layer : fileFields.Layers) {
            b.append("-> ").append(layer).append("\n");
        }

        return b.toString();
    }
}
