package futurelink.msla.formats.anycubic.tables;

import futurelink.msla.formats.*;
import futurelink.msla.formats.anycubic.PhotonWorkshopCodec;
import futurelink.msla.formats.iface.*;
import futurelink.msla.formats.iface.annotations.MSLAFileField;
import futurelink.msla.formats.iface.annotations.MSLAOption;
import futurelink.msla.formats.utils.LayerOptionMapper;
import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * "LAYERDEF" section representation.
 */

@Getter
public class PhotonWorkshopFileLayerDefTable extends PhotonWorkshopFileTable
        implements MSLAFileLayers<PhotonWorkshopFileLayerDefTable.PhotonWorkshopFileLayerDef, byte[]>
{
    private final Fields fileFields;

    @Getter
    public static class PhotonWorkshopFileLayerDef implements MSLAFileBlockFields {
        @MSLAFileField @Setter private Integer DataAddress = 0;
        @MSLAFileField(order = 1) private Integer DataLength = 0;
        @MSLAFileField(order = 2) @MSLAOption(MSLAOption.LiftHeight) @Setter private Float LiftHeight = 0.0f;
        @MSLAFileField(order = 3) @MSLAOption(MSLAOption.LiftSpeed) @Setter private Float LiftSpeed = 0.0f;
        @MSLAFileField(order = 4) @MSLAOption(MSLAOption.ExposureTime) @Setter private Float ExposureTime = 0.0f;
        @MSLAFileField(order = 5) @MSLAOption(MSLAOption.LayerHeight) @Setter private Float LayerHeight = 0.0f;
        @MSLAFileField(order = 6) private Integer NonZeroPixelCount = 0;
        @MSLAFileField(order = 7) private final Integer Padding1 = 0;

        @Override
        public String toString() {
            return "Data at " + DataAddress + ", length " + DataLength + ", time " + ExposureTime + ", pixels: " + NonZeroPixelCount;
        }
    }

    @SuppressWarnings("unused")
    static class Fields implements MSLAFileBlockFields, MSLAFileLayer {
        private final PhotonWorkshopFileTable parent;
        private final MSLAOptionMapper optionMapper;

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
        private final ArrayList<byte[]> LayerData = new ArrayList<>();

        public Fields(PhotonWorkshopFileTable parent, MSLALayerDefaults layerDefaults) {
            this.parent = parent;
            this.optionMapper = new LayerOptionMapper(this, layerDefaults);
        }
        @Override public MSLAOptionMapper options() { return optionMapper; }
    }

    public PhotonWorkshopFileLayerDefTable(byte versionMajor, byte versionMinor, MSLALayerDefaults layerDefaults) {
        super(versionMajor, versionMinor);
        this.fileFields = new Fields(this, layerDefaults);
        this.fileFields.LayerCount = 0;
    }

    @Override public MSLAOptionMapper options(int layerNumber) { return fileFields.options(); }

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
            var input = new PhotonWorkshopCodec.Input(stream.readNBytes(get(layer).DataLength));
            var params = new HashMap<String, Object>();
            params.put("DecodedDataLength", decodedDataLength);
            return decoders.decode(layer, writer, input, params);
        } catch (IOException e) {
            throw new MSLAException("Error decoding layer data", e);
        }
    }

    @Override
    public int count() {
        return fileFields.getLayerCount();
    }

    @Override
    public final PhotonWorkshopFileLayerDef get(int i) {
        return fileFields.Layers.get(i);
    }

    @Override
    public PhotonWorkshopFileLayerDef allocate() {
        var layer = new PhotonWorkshopFileLayerDef();
        fileFields.Layers.add(layer);
        fileFields.LayerData.add(null);
        fileFields.LayerCount = fileFields.Layers.size();
        return layer;
    }

    /**
     * Asynchronously encodes a layer.
     *
     * @param encoder encoder object
     * @param reader MSLAEncodeReader instance
     * @param params layer codec parameters (encryption key, expected data size etc.)
     * @throws MSLAException if no codec defined or something went wrong
     */
    @Override
    public void add(MSLALayerEncoder<byte[]> encoder,
                    MSLALayerEncodeReader reader,
                    Map<String, Object> params,
                    MSLALayerEncoder.Callback<byte[]> callback) throws MSLAException
    {
        var layerNumber = fileFields.Layers.size();
        allocate();

        // Encode layer data
        params.put("DecodedDataLength", reader.getSize());
        encoder.encode(layerNumber, reader, params, (layer, data) -> {
            fileFields.Layers.get(layerNumber).DataLength = data.sizeInBytes();
            fileFields.Layers.get(layerNumber).NonZeroPixelCount = data.pixels();
            fileFields.LayerData.set(layerNumber, data.data());
            if (callback != null) callback.onFinish(layerNumber, data);
        });
    }

    public final  byte[] getLayerData(int i) {
        return fileFields.LayerData.get(i);
    }

    @Override
    public final int calculateTableLength() {
        return 4 + fileFields.getLayerCount() * 32;
    }

    @Override
    public long read(DataInputStream stream, long position) throws MSLAException {
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
