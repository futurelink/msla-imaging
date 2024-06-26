package io.msla.formats.elegoo.tables;

import io.msla.formats.MSLAException;
import io.msla.formats.elegoo.GOOFileCodec;
import io.msla.formats.iface.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ELEGOO GOO file format layers.
 */
public class GOOFileLayers implements MSLAFileLayers<GOOFileLayerDef, byte[]> {
    private final List<GOOFileLayerDef> Layers = new ArrayList<>();
    private MSLALayerDefaults layerDefaults;

    public GOOFileLayers() {}

    @Override public void setDefaults(MSLALayerDefaults layerDefaults) throws MSLAException {
        this.layerDefaults = layerDefaults;
        for (GOOFileLayerDef layer : Layers) {
            layer.setDefaults(layerDefaults);
        }
    }

    @Override public boolean hasOptions() { return true; }
    @Override public int count() { return Layers.size(); }
    @Override public GOOFileLayerDef get(int index) { return Layers.get(index); }
    @Override public GOOFileLayerDef allocate() throws MSLAException {
        var layer = new GOOFileLayerDef();
        layer.setDefaults(layerDefaults);
        Layers.add(layer);
        return layer;
    }

    @Override public void add(MSLALayerEncoder<byte[]> encoder,
                              MSLALayerEncodeReader reader,
                              Map<String, Object> params,
                              MSLALayerEncoder.Callback<byte[]> callback) throws MSLAException
    {
        var newLayer = new GOOFileLayerDef();
        var layerNumber = count();
        newLayer.setDefaults(layerDefaults);
        Layers.add(newLayer);
        encoder.encode(layerNumber, reader, params, (lay, data) -> {
            Layers.get(layerNumber).getBlockFields().setDataLength(data.sizeInBytes());
            Layers.get(layerNumber).getBlockFields().setData(data.data());
            if (callback != null) callback.onFinish(layerNumber, data);
        });
    }

    public boolean readLayer(MSLALayerDecoder<byte[]> decoders, MSLALayerDecodeWriter writer, int layer) throws MSLAException {
        var input = new GOOFileCodec.Input(get(layer).getBlockFields().getData());
        return decoders.decode(layer, writer, input, null);
    }
}
