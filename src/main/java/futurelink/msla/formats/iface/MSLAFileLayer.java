package futurelink.msla.formats.iface;

import futurelink.msla.formats.MSLAException;

public interface MSLAFileLayer {
    MSLAFileBlockFields getBlockFields();
    void setDefaults(MSLALayerDefaults layerDefaults) throws MSLAException;
}
