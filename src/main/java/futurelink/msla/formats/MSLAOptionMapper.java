package futurelink.msla.formats;

import java.io.IOException;
import java.io.Serializable;

public abstract class MSLAOptionMapper {
    protected abstract boolean hasOption(String option, Class<? extends Serializable> aClass);
    protected abstract boolean hasLayerOption(String option, Class<? extends Serializable> aClass);
    protected abstract void populateOption(String option, Serializable value);
    protected abstract void populateLayerOption(String option, int layer, Serializable value);
    public void setOption(String option, Serializable value) throws IOException {
        if (value == null) {
            if (hasOption(option, null)) {
                populateOption(option, null);
            } else {
                throw new IOException("Option '" + option + "' is not available");
            }
        } else {
            if (hasOption(option, value.getClass())) {
                populateOption(option, value);
            } else {
                throw new IOException("Option '" + option + "' of type " + value.getClass() + " is not available");
            }
        }
    }

    public void setLayerOption(String option, int layer, Serializable value) throws IOException {
        if (value == null) {
            if (hasOption(option, null)) {
                populateLayerOption(option, layer, null);
            } else {
                throw new IOException("Layer option '" + option + "' is not available");
            }
        } else {
            if (hasOption(option, value.getClass())) {
                populateLayerOption(option, layer, value);
            } else {
                throw new IOException("Layer option '" + option + "' of type " + value.getClass() + " is not available");
            }
        }
    }
}
