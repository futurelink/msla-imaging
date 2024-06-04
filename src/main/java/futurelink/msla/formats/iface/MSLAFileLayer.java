package futurelink.msla.formats.iface;

import futurelink.msla.formats.MSLAException;

public interface MSLAFileLayer {
    MSLAFileBlockFields getFileFields();
    void setDefaults(MSLALayerDefaults layerDefaults) throws MSLAException;
}
