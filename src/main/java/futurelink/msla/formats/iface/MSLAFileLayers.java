package futurelink.msla.formats.iface;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.MSLAOptionMapper;

import java.util.Map;

public interface MSLAFileLayers<T, L> {
    /**
     * Returns file layers count.
     */
    int count();

    /**
     * Returns a layer.
     * @param index is a layer number
     * @return value of T
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

    MSLAOptionMapper options(int layerNumber);
}
