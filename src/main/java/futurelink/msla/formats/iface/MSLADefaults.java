package futurelink.msla.formats.iface;

import futurelink.msla.formats.MSLAException;

public interface MSLADefaults {
    void setFields(String blockName, MSLAFileBlockFields fields) throws MSLAException;
    MSLADefaultsParams getParameters(String blockName, String fieldName);
}
