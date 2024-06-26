package io.msla.formats.iface;

import io.msla.formats.MSLAException;

import java.util.Map;

/**
 * mSLA file layers array interface.
 */
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

    boolean hasOptions();

    void setDefaults(MSLALayerDefaults layerDefaults) throws MSLAException;
}
