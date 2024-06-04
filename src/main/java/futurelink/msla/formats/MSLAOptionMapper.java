package futurelink.msla.formats;

import futurelink.msla.formats.iface.MSLADefaults;
import futurelink.msla.formats.iface.MSLADefaultsParams;
import lombok.Getter;
import lombok.Setter;

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
        private final Class<?> type;
        private final List<String> location;
        @Setter private MSLADefaultsParams parameters;
        public Option(String name, Class<?> type, List<String> location) {
            this.name = name;
            this.type = type;
            this.location = new LinkedList<>(location);
        }

        @Override
        public String toString() {
            return "{ '" + name + "' of type " + this.type.getName() + " at '" + this.location + "' }";
        }
    }

    /**
     * Gets option type.
     * Returns option property type from the internal file format structure.
     *
     * @param option option name
     */
    abstract public Class<?> getType(String option);

    /**
     * Gets defaults object.
     */
    abstract public MSLADefaults getDefaults();

    abstract public void setDefaults(MSLADefaults defaults);

    /**
     * Gets option default parameters.
     * Parameters identify how the options is going to be used. Unlike getType(), that returns type of internal
     * option property in a file format, parameter type describes what input the option expects.
     * -
     * For example: a file has property stored as Short, but actually it is boolean value. So it's necessary
     * to transform boolean into Short and there should be a transformation rule. Another example: a file has
     * currency character stored as Integer, but for end user that should be a character, say '$' which is stored
     * as value 39. That needs to be transformed as well.
     * -
     * Other than that, each device has its own limits, so if option is supposed to be set to, say Integer or Float,
     * then it can have MIN and MAX values.
     *
     * @param option option name
     */
    abstract public MSLADefaultsParams getParameters(String option);

    /**
     * Returns true if mapper has option with specified name.
     * @param option option name
     * @param aClass option type
     * @return true if option was mapped, otherwise false
     */
    abstract protected boolean hasOption(String option, Class<? extends Serializable> aClass);

    /**
     * Must be implemented in order to be able to populate option value.
     * @param option option name
     * @param value option value
     */
    abstract protected void populateOption(String option, Serializable value) throws MSLAException;

    /**
     * Must be implemented in order to be able to get option value by name.
     * @param option option name
     */
    abstract protected Serializable fetchOption(String option) throws MSLAException;

    /**
     * Returns true if options are editable, otherwise false.
     */
    abstract public Boolean isEditable();

    /**
     * Lists all available options
     */
    abstract public Set<String> available();

    /**
     * Sets option by name.
     * Throws MSLAException if mapper's options are not editable.
     * @param option option name
     * @param value option value
     */
    public final void set(String option, String value) throws MSLAException {
        if (!isEditable()) throw new MSLAException("Options are not editable in this mapper");
        if (!hasOption(option, null)) throw new MSLAException("Option '" + option +  "' does not exist!");

        // Check if provided value is correct
        if (getParameters(option) == null) throw new MSLAException("No parameters set for '" + option + "' although option exists");
        getParameters(option).checkValue(value);

        if (value == null) {
            populateOption(option, null);
        } else {
            var optionType = getType(option);
            if (optionType == Integer.class) populateOption(option, Integer.parseInt(value));
            else if (optionType == Float.class) populateOption(option, Float.parseFloat(value));
            else if (optionType == Double.class) populateOption(option, Double.parseDouble(value));
            else if (optionType == Short.class) populateOption(option, Short.parseShort(value));
            else if (optionType == Byte.class) populateOption(option, Byte.parseByte(value));
            else if (optionType == Character.class) populateOption(option, value.toCharArray()[0]);
        }
    }

    /**
     * Gets option value by name.
     * @param option option name
     * @return option value
     */
    public final String get(String option) throws MSLAException {
        if (!hasOption(option, null)) throw new MSLAException("Option '" + option + "' is not available");
        return fetchOption(option).toString();
    }
}
