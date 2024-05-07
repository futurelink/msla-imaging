package futurelink.msla.formats;

import java.io.Serializable;
import java.util.Set;

/**
 * Generic option mapper.
 * Each mSLA printer has its own set of options and its own file format.
 * This mapper aims to manage all possible print options in one place.
 */
public abstract class MSLAOptionMapper {
    abstract protected boolean hasOption(String option, Class<? extends Serializable> aClass);
    public abstract Class<?> getType(String option);
    abstract protected boolean hasLayerOption(String option, Class<? extends Serializable> aClass);
    abstract protected void populateOption(String option, Serializable value) throws MSLAException;
    abstract protected Serializable fetchOption(String option) throws MSLAException;
    abstract protected void populateLayerOption(String option, int layer, Serializable value);
    abstract protected Serializable fetchLayerOption(String option, int layer);
    abstract public Set<String> getAvailable();

    public final void set(String option, Serializable value) throws MSLAException {
        if (value == null) {
            if (!hasOption(option, null)) throw new MSLAException("Option '" + option + "' is not available");
            populateOption(option, null);
        } else {
            if (!hasOption(option, value.getClass())) {
                if (!hasOption(option, null)) {
                    throw new MSLAException("Option '" + option + " is not available at all");
                } else {
                    populateOption(option, value);
                }
            }
        }
    }

    public final Serializable get(String option) throws MSLAException {
        if (!hasOption(option, null)) throw new MSLAException("Option '" + option + "' is not available");
        return fetchOption(option);
    }

    public final void setLayerOption(String option, int layer, Serializable value) throws MSLAException {
        if (value == null) {
            if (hasOption(option, null)) {
                populateLayerOption(option, layer, null);
            } else {
                throw new MSLAException("Layer option '" + option + "' is not available");
            }
        } else {
            if (hasOption(option, value.getClass())) {
                populateLayerOption(option, layer, value);
            } else {
                throw new MSLAException("Layer option '" + option + "' of type " + value.getClass() + " is not available");
            }
        }
    }
}
