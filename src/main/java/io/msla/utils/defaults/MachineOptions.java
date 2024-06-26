package io.msla.utils.defaults;

import io.msla.formats.MSLAException;
import io.msla.formats.iface.options.MSLAOptionName;
import io.msla.utils.defaults.props.*;
import org.dom4j.Element;

import java.util.HashMap;
import java.util.Set;

/**
 * Printer options parameters utility class.
 * This class is used as a linkage between file internal structures and
 * printer defaults.
 * -
 * A parameter that is provided by this class describes a way how the option is being
 * transformed and processed in order to be stored in a file format in the end.
 * -
 * Basically, this class is important for building UI of the application that is
 * supposed to work with this library.
 */
public class MachineOptions {
    private final HashMap<MSLAOptionName, MachineProperty> options = new HashMap<>();
    public Set<MSLAOptionName> getOptionKeys() { return options.keySet(); }
    public MachineProperty getOption(MSLAOptionName option) { return options.get(option); }
    public int size() { return options.size(); }

    /**
     * Creates a parameter from XML element that describes it in printer defaults file.
     *
     * @param option XML element
     * @throws MSLAException is thrown if XML element is incorrect or missing required attributes
     */
    public void addFromXMLElement(Element option) throws MSLAException {
        var name = option.attributeValue("name");
        var value = option.attributeValue("value");
        var type = option.attributeValue("type"); // Option type in XML description, to properly display option
        if (name == null) throw new MSLAException("Attribute 'name' is missing in option description");

        MachineProperty opt;
        if ("int".equals(type) || "float".equals(type)) {
            var minValue = option.attributeValue("minValue");
            if (minValue == null) throw new MSLAException("Attribute 'minValue' is required in option '" + name + "'");
            var maxValue = option.attributeValue("maxValue");
            if (maxValue == null) throw new MSLAException("Attribute 'maxValue' is required in option '" + name + "'");
            if ("int".equals(type)) {
                opt = new MachinePropertyInt(value, Integer.parseInt(minValue), Integer.parseInt(maxValue));
            } else {
                opt = new MachinePropertyFloat(value, Float.parseFloat(minValue), Float.parseFloat(maxValue));
            }
        } else if ("char".equals(type)) opt = new MachinePropertyChar(value);
        else if ("boolean".equals(type)) {
            var trueValue = option.attributeValue("trueValue");
            if (trueValue == null) throw new MSLAException("Attribute 'trueValue' is required in option '" + name + "'");
            var falseValue = option.attributeValue("falseValue");
            if (falseValue == null) throw new MSLAException("Attribute 'falseValue' is required in option '" + name + "'");
            opt = new MachinePropertyBoolean(value, trueValue, falseValue);
        }
        else if ("".equals(type) || type == null) opt = new MachineProperty(value);
        else throw new MSLAException("Option type " + type + " is unknown");
        options.put(MSLAOptionName.valueOf(name), opt);
    }
}
