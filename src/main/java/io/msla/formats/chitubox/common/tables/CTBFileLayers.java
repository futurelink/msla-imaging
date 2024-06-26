package io.msla.formats.chitubox.common.tables;

import io.msla.formats.MSLAException;
import io.msla.formats.chitubox.common.CTBCommonFile;
import io.msla.formats.iface.*;
import io.msla.formats.io.FileFieldsException;
import lombok.Setter;

import java.io.DataInputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Logger;

public class CTBFileLayers extends CTBFileBlock implements MSLAFileLayers<CTBFileLayerDef, byte[]> {
    private final CTBCommonFile parent;
    private final Logger logger = Logger.getLogger(CTBFileLayers.class.getName());
    private final ArrayList<CTBFileLayerDef> LayerDefinition = new ArrayList<>();
    @Setter private MSLALayerDefaults layerDefaults;

    public CTBFileLayers(CTBCommonFile parent) {
        super(parent.getHeader().getBlockFields().getVersion());
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
        var layer = layerDef.getBlockFields();
        layer.setPositionZ((layerNumber + 1) * parent.getHeader().getBlockFields().getLayerHeightMillimeter());
        layer.setExposureTime(parent.getHeader().getBlockFields().getLayerExposureSeconds());
        layer.setLightOffSeconds(parent.getHeader().getBlockFields().getLightOffDelay());

        // Fill in layer overrides with defaults
        var extra = layer.getExtra();
        if (extra != null) {
            var extraFields = extra.getBlockFields();
            extraFields.setLiftHeight(parent.getPrintParams().getBlockFields().getLiftHeight());
            extraFields.setLiftSpeed(parent.getPrintParams().getBlockFields().getLiftSpeed());
            extraFields.setLiftHeight2(parent.getSlicerInfo().getBlockFields().getLiftHeight2());
            extraFields.setLiftSpeed2(parent.getSlicerInfo().getBlockFields().getLiftSpeed2());
            extraFields.setRetractSpeed(parent.getPrintParams().getBlockFields().getRetractSpeed());
            extraFields.setRetractHeight2(parent.getSlicerInfo().getBlockFields().getRetractHeight2());
            extraFields.setRetractSpeed2(parent.getSlicerInfo().getBlockFields().getRetractSpeed2());
            if (parent.getPrintParamsV4() != null) {
                extraFields.setRestTimeBeforeLift(parent.getPrintParamsV4().getBlockFields().getRestTimeBeforeLift());
                extraFields.setRestTimeAfterLift(parent.getPrintParamsV4().getBlockFields().getRestTimeAfterLift());
                extraFields.setRestTimeAfterRetract(parent.getPrintParamsV4().getBlockFields().getRestTimeAfterRetract());
            } else {
                extraFields.setRestTimeBeforeLift(0.0f);
                extraFields.setRestTimeAfterLift(parent.getSlicerInfo().getBlockFields().getRestTimeAfterLift());
                extraFields.setRestTimeAfterRetract(parent.getSlicerInfo().getBlockFields().getRestTimeAfterRetract());
            }
            extraFields.setLightPWM(parent.getHeader().getBlockFields().getLightPWM().floatValue());
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
            for (int i = 0; i < parent.getHeader().getBlockFields().getLayerCount(); i++) {
                var layerDef = allocate();
                layerDef.getBlockFields().getParent().setBriefMode(true);
                var len = layerDef.getBlockFields().getParent().read(stream, position);
                if (len != layerDef.getBlockFields().getParent().getDataLength())
                    throw new MSLAException("Error reading brief layer definition for layer " + i + ": data size mismatch");
                position += layerDef.getBlockFields().getParent().getDataLength();
                bytesRead += len;
                layerDef.getBlockFields().getParent().setBriefMode(false);
            }
        } catch (FileFieldsException e) {
            throw new MSLAException("Error reading brief layer definition", e);
        }

        // Read main layer data with definitions & extra layer settings
        logger.info("Reading layers");
        for (int i = 0; i < LayerDefinition.size(); i++) {
            var def = LayerDefinition.get(i);
            def.setBriefMode(false);
            var defOffset = def.getBlockFields().getDataAddress() - def.getBlockFields().getTableSize();
            var len = def.read(stream, defOffset);
            var expectedLen = def.getBlockFields().getDataSize() + def.getBlockFields().getTableSize();
            if (len != expectedLen)
                throw new MSLAException("Error reading layer " + i + ": data size mismatch: " + len + " vs " + expectedLen);
            bytesRead += len;
        }
        return bytesRead;
    }

    @Override public String getName() { return null; }
    @Override public int getDataLength() throws FileFieldsException { return 0; }
    @Override public int getDataFieldOffset(String fieldName) throws FileFieldsException { return 0; }
    @Override public MSLAFileBlockFields getBlockFields() {
        return null;
    }
}
