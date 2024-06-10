package futurelink.msla.formats.chitubox.encrypted.tables;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.chitubox.encrypted.CTBEncryptedFile;
import futurelink.msla.formats.chitubox.common.tables.CTBFileBlock;
import futurelink.msla.formats.iface.*;
import futurelink.msla.formats.iface.MSLAFileField;
import futurelink.msla.formats.io.FileFieldsException;
import lombok.Getter;
import lombok.Setter;

import java.io.DataInputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Logger;

public class CTBEncryptedFileLayers extends CTBFileBlock implements MSLAFileLayers<CTBEncryptedFileLayerDef, byte[]> {
    @Getter private final Fields blockFields;
    private final Logger logger = Logger.getLogger(CTBEncryptedFileLayers.class.getName());
    private final CTBEncryptedFile parent;
    @Setter private MSLALayerDefaults layerDefaults;
    private final ArrayList<CTBEncryptedFileLayerDef> LayerDefinitions = new ArrayList<>();

    @SuppressWarnings("unused")
    public static class LayerPointerEntry implements MSLAFileBlockFields {
        @MSLAFileField @Getter private Integer LayerOffset;
        @MSLAFileField(order = 1) @Getter private Integer PageNumber;
        @MSLAFileField(order = 2) @Getter private final Integer LayerTableSize = 88; // always 0x58
        @MSLAFileField(order = 3) private final Integer Padding2 = 0; // 0

        @Override
        public String toString() { return "{ " + fieldsAsString(":", ", ") + " }"; }
    }

    @Getter
    @SuppressWarnings("unused")
    public static class Fields implements MSLAFileBlockFields {
        @Getter private Integer LayersCount;
        @MSLAFileField(lengthAt = "LayersCount") private ArrayList<LayerPointerEntry> LayerPointers;
    }

    public CTBEncryptedFileLayers(CTBEncryptedFile parent) {
        super(parent.getHeader().getVersion());
        this.parent = parent;
        this.blockFields = new Fields();
        this.blockFields.LayersCount = parent.getSlicerSettings().getBlockFields().getLayerCount();
    }

    @Override public String getName() { return "Layers"; }
    @Override public int getDataLength() throws FileFieldsException { return 0; }
    @Override public int getDataFieldOffset(String fieldName) throws FileFieldsException { return 0; }

    @Override public int count() { return getBlockFields().LayersCount; }
    @Override public CTBEncryptedFileLayerDef get(int index) {
        return LayerDefinitions.get(index);
    }
    @Override public CTBEncryptedFileLayerDef allocate() throws MSLAException {
        blockFields.LayerPointers.add(new LayerPointerEntry());
        blockFields.LayersCount++;

        var layer = new CTBEncryptedFileLayerDef();
        layer.setDefaults(layerDefaults);
        LayerDefinitions.add(layer);
        parent.getSlicerSettings().getBlockFields().setLayerCount(blockFields.LayersCount);
        return layer;
    }

    @Override
    public void add(
            MSLALayerEncoder<byte[]> encoder, MSLALayerEncodeReader reader,
            Map<String, Object> params,
            MSLALayerEncoder.Callback<byte[]> callback) throws MSLAException
    {
        var layerNumber = count();
        var layerDef = allocate();

        // Fill in layer overrides with defaults
        var layer = layerDef.getBlockFields();
        layer.setPositionZ((layerNumber + 1) * parent.getSlicerSettings().getBlockFields().getLayerHeight());
        layer.setExposureTime(parent.getSlicerSettings().getBlockFields().getExposureTime());
        layer.setLightOffDelay(parent.getSlicerSettings().getBlockFields().getLightOffDelay());
        layer.setLiftHeight(parent.getSlicerSettings().getBlockFields().getLiftHeight());
        layer.setLiftSpeed(parent.getSlicerSettings().getBlockFields().getLiftSpeed());
        layer.setLiftHeight2(parent.getSlicerSettings().getBlockFields().getLiftHeight2());
        layer.setLiftSpeed2(parent.getSlicerSettings().getBlockFields().getLiftSpeed2());
        layer.setRetractSpeed(parent.getSlicerSettings().getBlockFields().getRetractSpeed());
        layer.setRetractHeight2(parent.getSlicerSettings().getBlockFields().getRetractHeight2());
        layer.setRetractSpeed2(parent.getSlicerSettings().getBlockFields().getRetractSpeed2());
        layer.setRestTimeBeforeLift(parent.getSlicerSettings().getBlockFields().getRestTimeBeforeLift());
        layer.setRestTimeAfterLift(parent.getSlicerSettings().getBlockFields().getRestTimeAfterLift());
        layer.setRestTimeAfterRetract(parent.getSlicerSettings().getBlockFields().getRestTimeAfterRetract());
        layer.setLightPWM(parent.getSlicerSettings().getBlockFields().getLightPWM().floatValue());

        encoder.encode(layerNumber, reader, params, (ln, data) -> {
            layer.setData(data.data());
            if (callback != null) callback.onFinish(ln, data);
        });
    }

    @Override
    public long read(DataInputStream stream, long position) throws MSLAException {
        /*
         * Preliminary layers go one after another
         */
        logger.info("Reading preliminary layer definitions");
        long bytesRead = super.read(stream, position);

        /*
         * Read layers definition table. Layer definition has 88 byte header and
         * a layer data, that has length defined in header.
         */
        logger.info("Reading layer definitions & data");
        for (int i = 0; i < getBlockFields().LayersCount; i++) {
            var layer = new CTBEncryptedFileLayerDef();
            layer.read(stream, getBlockFields().getLayerPointers().get(i).LayerOffset);
            LayerDefinitions.add(layer);
        }

        return bytesRead;
    }

    @Override public boolean hasOptions() { return true; }
    @Override public void setDefaults(MSLALayerDefaults layerDefaults) throws MSLAException {
        this.layerDefaults = layerDefaults;
        for (var layer : LayerDefinitions) layer.setDefaults(layerDefaults);
    }
}
