package futurelink.msla.formats.iface;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.chitubox.tables.CTBFileLayerDef;

import java.util.Map;

public interface MSLAFileLayers<T, L> {
    /**
     * Returns file layers count.
     */
    int count();

    /**
     * Returns a layer.
     * @param index
     * @return
     */
    T get(int index);

    /**
     * Allocates a new layer with no data.
     */
    T allocate() throws MSLAException;

    /**
     * Adds layer definition.
     */
    //void add(T layerDef);
    void add(MSLALayerEncoder<L> encoder,
             MSLALayerEncodeReader reader,
             Map<String, Object> params,
             MSLALayerEncoder.Callback<L> callback)
            throws MSLAException;
}
