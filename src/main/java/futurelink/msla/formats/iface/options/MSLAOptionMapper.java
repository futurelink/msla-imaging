package futurelink.msla.formats.iface.options;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.iface.MSLADefaults;
import futurelink.msla.formats.iface.MSLADefaultsParams;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

public interface MSLAOptionMapper {
    /**
     * Gets option type.
     * Returns option property type from the internal file format structure.
     *
     * @param option option name
     */
    Class<?> getType(MSLAOptionName option);

    /**
     * Gets defaults object.
     */
    MSLADefaults getDefaults();

    /**
     * Assigns defaults object to option mapper.
     */
    void setDefaults(MSLADefaults defaults);

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
    MSLADefaultsParams getParameters(MSLAOptionName option);

    /**
     * Return option group name
     * @param option option name
     */
    MSLAOptionGroup getGroup(MSLAOptionName option);

    /**
     * Returns all groups for all options available in a file.
     */
    List<MSLAOptionGroup> getGroups();

    /**
     * Returns true if mapper has option with specified name.
     * @param option option name
     * @return true if option was mapped, otherwise false
     */
    boolean hasOption(MSLAOptionName option);

    /**
     * Must be implemented in order to be able to populate option value.
     * @param option option name
     * @param value option value
     */
    void populateOption(MSLAOptionName option, Serializable value) throws MSLAException;

    /**
     * Must be implemented in order to be able to get option value by name.
     * @param option option name
     */
    Serializable fetchOption(MSLAOptionName option) throws MSLAException;

    /**
     * Returns true if options are editable, otherwise false.
     */
    Boolean isEditable();

    /**
     * Lists all available options
     */
    Set<MSLAOptionName> available();

    /**
     * Gets option value as String.
     * @param option option name
     */
    String get(MSLAOptionName option) throws MSLAException;

    void set(MSLAOptionName option, String value) throws MSLAException;
    void set(MSLAOptionName option, Float value) throws MSLAException;
    void set(MSLAOptionName option, Integer value) throws MSLAException;
    void set(MSLAOptionName option, Long value) throws MSLAException;
    void set(MSLAOptionName option, Double value) throws MSLAException;
    void set(MSLAOptionName option, Short value) throws MSLAException;
    void set(MSLAOptionName option, Byte value) throws MSLAException;


}
