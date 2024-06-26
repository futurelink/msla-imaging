package io.msla.formats.iface;

import io.msla.formats.MSLAException;

/**
 * mSLA file layer interface.
 */
public interface MSLAFileLayer {
    MSLAFileBlockFields getBlockFields();
    void setDefaults(MSLALayerDefaults layerDefaults) throws MSLAException;
}
