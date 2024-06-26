package io.msla.formats;

import io.msla.formats.iface.MSLADefaultsParams;
import io.msla.formats.iface.options.MSLAOptionMapper;
import io.msla.formats.iface.options.MSLAOptionName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * Generic option mapper.
 * Each mSLA printer has its own set of options and its own file format.
 * This mapper aims to manage all possible print options in one place.
 */
public abstract class OptionMapper implements MSLAOptionMapper {

    @Getter
    public static class Option {
        private final String name;
        private final Class<? extends Serializable> type;
        private final List<String> location;
        @Setter private MSLADefaultsParams parameters;
        @Setter private String group;
        public Option(String name, Class<? extends Serializable> type, List<String> location) {
            this.name = name;
            this.type = type;
            this.location = new LinkedList<>(location);
        }

        @Override
        public String toString() {
            return "{ '" + name + "' of type " + this.type.getName() + " at '" + this.location + "' }";
        }
    }

    public final void set(MSLAOptionName option, Float value) throws MSLAException { set(option, String.valueOf(value)); }
    public final void set(MSLAOptionName option, Integer value) throws MSLAException { set(option, String.valueOf(value)); }
    public final void set(MSLAOptionName option, Long value) throws MSLAException { set(option, String.valueOf(value)); }
    public final void set(MSLAOptionName option, Double value) throws MSLAException { set(option, String.valueOf(value)); }
    public final void set(MSLAOptionName option, Short value) throws MSLAException { set(option, String.valueOf(value)); }
    public final void set(MSLAOptionName option, Byte value) throws MSLAException { set(option, String.valueOf(value)); }
    public final void set(MSLAOptionName option, Boolean value) throws MSLAException { set(option, String.valueOf(value)); }

    /**
     * Sets option by name.
     * Throws MSLAException if mapper's options are not editable.
     * @param option option name
     * @param value option value
     */
    public void set(MSLAOptionName option, String value) throws MSLAException {
        if (!isEditable()) throw new MSLAException("Options are not editable in this mapper");
        if (!hasOption(option)) throw new MSLAException("Option '" + option +  "' does not exist!");

        // Check if provided value is correct
        if (getParameters(option) == null) throw new MSLAException("No parameters set for '" + option + "' although option exists");
        getParameters(option).checkValue(value);

        // Does nothing! Actual setting must be implemented in children classes.
    }

    /**
     * Gets option value by name.
     * @param option option name
     * @return option value
     */
    public String get(MSLAOptionName option) throws MSLAException {
        if (!hasOption(option)) throw new MSLAException("Option '" + option + "' is not available");
        return null; // Returns nothing - actual getting must be implemented in children classes.
    }
}
