package futurelink.msla.formats;

import lombok.Getter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Generic option mapper.
 * Each mSLA printer has its own set of options and its own file format.
 * This mapper aims to manage all possible print options in one place.
 */
public abstract class MSLAOptionMapper {

    @Getter
    public static class Option {
        private final String name;
        private final Class<?> optionClass;
        private final List<String> location;
        public Option(String name, Class<?> optionsClass, List<String> location) {
            this.name = name;
            this.optionClass = optionsClass;
            this.location = new LinkedList<>(location);
        }

        @Override
        public String toString() {
            return "{ '" + name + "' of type " + this.optionClass.getName() + " at '" + this.location + "' }";
        }
    }

    /**
     * Gets option type.
     * @param option option name
     */
    abstract public Class<?> getType(String option);
    abstract protected boolean hasOption(String option, Class<? extends Serializable> aClass);
    abstract protected void populateOption(String option, Serializable value) throws MSLAException;
    abstract protected Serializable fetchOption(String option) throws MSLAException;

    /**
     * Lists all available options
     */
    abstract public Set<String> available();

    public final void set(String option, Serializable value) throws MSLAException {
        if (value == null) {
            if (hasOption(option, null)) populateOption(option, null);
            else throw new MSLAException("Option '" + option + "' is not available");
        } else {
            if (hasOption(option, value.getClass())) populateOption(option, value);
            else if (hasOption(option, null)) populateOption(option, value);
            else throw new MSLAException("Option '" + option + "' is not available");
        }
    }

    public final Serializable get(String option) throws MSLAException {
        if (!hasOption(option, null)) throw new MSLAException("Option '" + option + "' is not available");
        return fetchOption(option);
    }
}
