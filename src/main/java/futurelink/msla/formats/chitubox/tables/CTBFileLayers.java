package futurelink.msla.formats.chitubox.tables;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.chitubox.CTBFile;
import futurelink.msla.formats.iface.*;
import futurelink.msla.formats.io.FileFieldsException;
import lombok.Setter;

import java.io.DataInputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Logger;

public class CTBFileLayers extends CTBFileBlock implements MSLAFileLayers<CTBFileLayerDef, byte[]> {
    private final CTBFile parent;
    private final Logger logger = Logger.getLogger(CTBFileLayers.class.getName());
    private final ArrayList<CTBFileLayerDef> LayerDefinition = new ArrayList<>();
    @Setter private MSLALayerDefaults layerDefaults;

    public CTBFileLayers(CTBFile parent) {
        super(parent.getHeader().getFileFields().getVersion());
        this.parent = parent;
    }

    @Override
    public void setDefaults(MSLALayerDefaults layerDefaults) throws MSLAException {
        this.layerDefaults = layerDefaults;
        for (CTBFileLayerDef def : LayerDefinition) {
            def.setDefaults(layerDefaults);
        }
    }

    @Override public boolean hasOptions() { return true; }
    @Override public int count() {
        return LayerDefinition.size();
    }
    @Override public CTBFileLayerDef get(int index) {
        return LayerDefinition.get(index);
    }

    @Override
    public CTBFileLayerDef allocate() throws MSLAException {
        var layer = new CTBFileLayerDef(getVersion());
        layer.setDefaults(layerDefaults);
        LayerDefinition.add(layer);
        return layer;
    }

    @Override
    public void add(MSLALayerEncoder<byte[]> encoder,
                    MSLALayerEncodeReader reader,
                    Map<String, Object> params,
                    MSLALayerEncoder.Callback<byte[]> callback) throws MSLAException
    {
        var layerNumber = count();
        var layerDef = allocate();
        var layer = layerDef.getFileFields();
        layer.setPositionZ((layerNumber + 1) * parent.getHeader().getFileFields().getLayerHeightMillimeter());
        layer.setExposureTime(parent.getHeader().getFileFields().getLayerExposureSeconds());
        layer.setLightOffSeconds(parent.getHeader().getFileFields().getLightOffDelay());

        // Fill in layer overrides with defaults
        var extra = layer.getExtra();
        if (extra != null) {
            var extraFields = extra.getFileFields();
            extraFields.setLiftHeight(parent.getPrintParams().getFileFields().getLiftHeight());
            extraFields.setLiftSpeed(parent.getPrintParams().getFileFields().getLiftSpeed());
            extraFields.setLiftHeight2(parent.getSlicerInfo().getFileFields().getLiftHeight2());
            extraFields.setLiftSpeed2(parent.getSlicerInfo().getFileFields().getLiftSpeed2());
            extraFields.setRetractSpeed(parent.getPrintParams().getFileFields().getRetractSpeed());
            extraFields.setRetractHeight2(parent.getSlicerInfo().getFileFields().getRetractHeight2());
            extraFields.setRetractSpeed2(parent.getSlicerInfo().getFileFields().getRetractSpeed2());
            if (parent.getPrintParamsV4() != null) {
                extraFields.setRestTimeBeforeLift(parent.getPrintParamsV4().getFileFields().getRestTimeBeforeLift());
                extraFields.setRestTimeAfterLift(parent.getPrintParamsV4().getFileFields().getRestTimeAfterLift());
                extraFields.setRestTimeAfterRetract(parent.getPrintParamsV4().getFileFields().getRestTimeAfterRetract());
            } else {
                extraFields.setRestTimeBeforeLift(0.0f);
                extraFields.setRestTimeAfterLift(parent.getSlicerInfo().getFileFields().getRestTimeAfterLift());
                extraFields.setRestTimeAfterRetract(parent.getSlicerInfo().getFileFields().getRestTimeAfterRetract());
            }
            extraFields.setLightPWM(parent.getHeader().getFileFields().getLightPWM().floatValue());
        }

        encoder.encode(layerNumber, reader, params, (ln, data) -> {
            layer.setData(data.data());
            if (callback != null) callback.onFinish(ln, data);
        });
    }

    @Override
    public long read(DataInputStream stream, long position) throws MSLAException {
        // Read preliminary layer definitions
        long bytesRead = 0;
        try {
            logger.info("Reading preliminary layer definitions");
            for (int i = 0; i < parent.getHeader().getFileFields().getLayerCount(); i++) {
                var layerDef = allocate();
                layerDef.getFileFields().getParent().setBriefMode(true);
                var len = layerDef.getFileFields().getParent().read(stream, position);
                if (len != layerDef.getFileFields().getParent().getDataLength())
                    throw new MSLAException("Error reading brief layer definition for layer " + i + ": data size mismatch");
                position += layerDef.getFileFields().getParent().getDataLength();
                bytesRead += len;
            }
        } catch (FileFieldsException e) {
            throw new MSLAException("Error reading brief layer definition", e);
        }

        // Read main layer data with definitions & extra layer settings
        logger.info("Reading layers");
        for (int i = 0; i < LayerDefinition.size(); i++) {
            var def = LayerDefinition.get(i);
            def.setBriefMode(false);
            var defOffset = def.getFileFields().getDataAddress() - def.getFileFields().getTableSize();
            var len = def.read(stream, defOffset);
            var expectedLen = def.getFileFields().getDataSize() + def.getFileFields().getTableSize();
            if (len != expectedLen)
                throw new MSLAException("Error reading layer " + i + ": data size mismatch: " + len + " vs " + expectedLen);
            bytesRead += len;
        }
        return bytesRead;
    }

    @Override public String getName() { return null; }
    @Override public int getDataLength() throws FileFieldsException { return 0; }
    @Override public int getDataFieldOffset(String fieldName) throws FileFieldsException { return 0; }
    @Override public MSLAFileBlockFields getFileFields() {
        return null;
    }
}
