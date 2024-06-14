package futurelink.msla.formats.iface;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.iface.options.MSLAOptionName;

import java.io.Serializable;

/**
 * Common defaults interface for both file and layer defaults.
 */
public interface MSLADefaults {
    void setFields(MSLAFileBlockFields fields) throws MSLAException;
    MSLADefaultsParams getParameters(String blockName, MSLAOptionName fieldName);

    <T extends Serializable> Serializable displayToRaw(MSLAOptionName name, T optionValue, Class<? extends T> rawType) throws MSLAException;
    <T> String rawToDisplay(MSLAOptionName name, Serializable optionValue) throws MSLAException;
}
