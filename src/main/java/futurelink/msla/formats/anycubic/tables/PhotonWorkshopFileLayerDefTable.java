package futurelink.msla.formats.anycubic.tables;

import futurelink.msla.formats.*;
import futurelink.msla.formats.anycubic.PhotonWorkshopCodec;
import futurelink.msla.formats.iface.*;
import futurelink.msla.formats.iface.annotations.MSLAFileField;
import lombok.Getter;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * "LAYERDEF" section representation.
 */

@Getter
public class PhotonWorkshopFileLayerDefTable extends PhotonWorkshopFileTable
        implements MSLAFileLayers<PhotonWorkshopFileLayerDef, byte[]>
{
    private final Fields fileFields;

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
        private final ArrayList<byte[]> LayerData = new ArrayList<>();

        public Fields(PhotonWorkshopFileTable parent) {
            this.parent = parent;
        }
    }

    public PhotonWorkshopFileLayerDefTable(byte versionMajor, byte versionMinor) {
        super(versionMajor, versionMinor);
        this.fileFields = new Fields(this);
        this.fileFields.LayerCount = 0;
    }

    @Override
    public void setDefaults(MSLALayerDefaults layerDefaults) {
        //this.getFileFields().setDefaults(layerDefaults);
    }

    public final byte[] getLayerData(int i) {
        return fileFields.LayerData.get(i);
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
            var input = new PhotonWorkshopCodec.Input(stream.readNBytes(get(layer).getDataLength()));
            var params = new HashMap<String, Object>();
            params.put("DecodedDataLength", decodedDataLength);
            return decoders.decode(layer, writer, input, params);
        } catch (IOException e) {
            throw new MSLAException("Error decoding layer data", e);
        }
    }

    @Override public int count() {
        return fileFields.getLayerCount();
    }
    @Override public final PhotonWorkshopFileLayerDef get(int i) {
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
            fileFields.Layers.get(layerNumber).setDataLength(data.sizeInBytes());
            fileFields.Layers.get(layerNumber).setNonZeroPixelCount(data.pixels());
            fileFields.LayerData.set(layerNumber, data.data());
            if (callback != null) callback.onFinish(layerNumber, data);
        });
    }

    @Override public boolean hasOptions() { return true; }
    @Override public final int calculateTableLength() {
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
