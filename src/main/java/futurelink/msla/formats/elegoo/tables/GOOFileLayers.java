package futurelink.msla.formats.elegoo.tables;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.iface.MSLAFileLayers;
import futurelink.msla.formats.iface.MSLALayerEncodeReader;
import futurelink.msla.formats.iface.MSLALayerEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GOOFileLayers implements MSLAFileLayers<GOOFileLayerDef, byte[]> {
    private final List<GOOFileLayerDef> Layers = new ArrayList<>();

    @Override public int count() { return Layers.size(); }
    @Override public GOOFileLayerDef get(int index) { return Layers.get(index); }
    @Override public GOOFileLayerDef allocate() {
        var layer = new GOOFileLayerDef();
        Layers.add(layer);
        return layer;
    }
    @Override public void add(MSLALayerEncoder<byte[]> encoder,
                              MSLALayerEncodeReader reader,
                              Map<String, Object> params,
                              MSLALayerEncoder.Callback<byte[]> callback) throws MSLAException {
        var layer = new GOOFileLayerDef();
        var layerNumber = count();
        Layers.add(layer);
        encoder.encode(layerNumber, reader, params, callback);
    }
}
