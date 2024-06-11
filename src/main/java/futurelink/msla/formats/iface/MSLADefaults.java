package futurelink.msla.formats.iface;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.iface.options.MSLAOptionName;

/**
 * Common defaults interface for both file and layer defaults.
 */
public interface MSLADefaults {
    void setFields(MSLAFileBlockFields fields) throws MSLAException;
    MSLADefaultsParams getParameters(String blockName, MSLAOptionName fieldName);
}
