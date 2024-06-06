package futurelink.msla.utils.defaults;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.iface.MSLADefaultsParams;
import futurelink.msla.utils.Size;
import lombok.Getter;
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
public class MachineOptionParams {

    @Getter
    public static class Param implements MSLADefaultsParams {
        private final String group;
        private final String defaultValue;
        public Param(String group, String value) {
            this.group = group;
            this.defaultValue = value;
        }
        public String getType() { return "generic"; }
        public Integer getInt() { return Integer.parseInt(defaultValue); }
        public Byte getByte() { return Byte.parseByte(defaultValue); }
        public Short getShort() { return Short.parseShort(defaultValue); }
        public Long getLong() { return Long.parseLong(defaultValue); }
        public Float getFloat() { return Float.parseFloat(defaultValue); }
        public Double getDouble() { return Double.parseDouble(defaultValue); }
        public String getString() { return defaultValue; }
        public Boolean getBoolean() { return Boolean.parseBoolean(defaultValue); }

        public final Object getAsType(String type) {
            return switch (type) {
                case "short", "Short" -> getShort();
                case "int", "Integer" -> getInt();
                case "long", "Long" -> getLong();
                case "float", "Float" -> getFloat();
                case "double", "Double" -> getDouble();
                case "byte", "Byte" -> getByte();
                case "boolean", "Boolean" -> getBoolean();
                case "Size" -> Size.parseSize(getDefaultValue());
                default -> getString();
            };
        }

        @Override public String toString() { return "Generic option [ default = " + defaultValue + " ]"; }
    }

    @Getter public static class ParamFloat extends Param {
        private final Float minValue;
        private final Float maxValue;
        public ParamFloat(String group, String value, Float minValue, Float maxValue) {
            super(group, value);
            this.minValue = minValue;
            this.maxValue = maxValue;
        }

        @Override public String getType() { return "float"; }
        @Override public Byte getByte() { return null; }
        @Override public Short getShort() { return null; }
        @Override public Integer getInt() { return null; }
        @Override public Long getLong() { return null; }
        @Override public String getString() { return String.valueOf(getDefaultValue()); }

        @Override
        public void checkValue(String value) throws MSLAException {
            try {
                var val = Float.parseFloat(value);
                if (val < minValue) throw new MSLAException("Value can't be less than " + minValue);
                if (val > maxValue) throw new MSLAException("Value can't be greater than " + maxValue);
            } catch (NumberFormatException e) {
                throw new MSLAException("Value can't be " + value);
            }
        }

        @Override
        public String toString() {
            return "Float option [ " + minValue + " > " + getDefaultValue() + " > " + maxValue + " ]";
        }
    }

    @Getter public static class ParamInt extends Param {
        private final Integer minValue;
        private final Integer maxValue;
        public ParamInt(String group, String value, Integer minValue, Integer maxValue) {
            super(group, value);
            this.minValue = minValue;
            this.maxValue = maxValue;
        }

        @Override public String getType() { return "integer"; }
        @Override public String getString() { return String.valueOf(getDefaultValue()); }

        @Override
        public void checkValue(String value) throws MSLAException {
            try {
                var val = Integer.parseInt(value);
                if (val < minValue) throw new MSLAException("Value can't be less than " + minValue);
                if (val > maxValue) throw new MSLAException("Value can't be greater than " + maxValue);
            } catch (NumberFormatException e) {
                throw new MSLAException("Value can't be " + value);
            }
        }

        @Override
        public String toString() {
            return "Int option [ " + minValue + " > " + getDefaultValue() + " > " + maxValue + " ]";
        }
    }

    @Getter public static class ParamChar extends Param {
        public ParamChar(String group, String value) { super(group, !value.isEmpty() ? value.substring(0,1) : ""); }
        @Override public String getType() { return "character"; }
        @Override public Integer getInt() { return getDefaultValue().isEmpty() ? null : (int) getDefaultValue().getBytes()[0]; }
        @Override public Byte getByte() { return getDefaultValue().isEmpty() ? null : getDefaultValue().getBytes()[0]; }
        @Override public Short getShort() { return getDefaultValue().isEmpty() ? null : (short) getDefaultValue().getBytes()[0]; }
        @Override public String toString() { return "Char option [ default = " + getDefaultValue() + " ]"; }
    }

    @Getter public static class ParamBoolean extends Param {
        private final String trueValue;
        private final String falseValue;
        public ParamBoolean(String group, String value, String trueValue, String falseValue) {
            super(group, value);
            this.trueValue = trueValue;
            this.falseValue = falseValue;
        }
        @Override public String getType() { return "boolean"; }
        @Override public String toString() {
            return "Boolean option [ default = " + getDefaultValue() + ", true = " + trueValue + ", false = " + falseValue + " ]"; }
    }

    private final HashMap<String, Param> options = new HashMap<>();
    public Set<String> getOptionKeys() { return options.keySet(); }
    public Param getOption(String option) { return options.get(option); }
    public int size() { return options.size(); }

    /**
     * Creates a parameter from XML element that describes it in printer defaults file.
     *
     * @param option XML element
     * @throws MSLAException is thrown if XML element is incorrect or missing required attributes
     */
    public void addFromXMLElement(Element option) throws MSLAException {
        var group = option.attributeValue("group");
        var name = option.attributeValue("name");
        var value = option.attributeValue("value");
        var type = option.attributeValue("type"); // Option type in XML description, to properly display option
        if (name == null) throw new MSLAException("Attribute 'name' is missing in option description");

        Param opt;
        if ("int".equals(type) || "float".equals(type)) {
            var minValue = option.attributeValue("minValue");
            if (minValue == null) throw new MSLAException("Attribute 'minValue' is required in option '" + name + "'");
            var maxValue = option.attributeValue("maxValue");
            if (maxValue == null) throw new MSLAException("Attribute 'maxValue' is required in option '" + name + "'");
            if ("int".equals(type)) {
                opt = new ParamInt(group, value, Integer.parseInt(minValue), Integer.parseInt(maxValue));
            } else {
                opt = new ParamFloat(group,value, Float.parseFloat(minValue), Float.parseFloat(maxValue));
            }
        } else if ("char".equals(type)) opt = new ParamChar(group, value);
        else if ("boolean".equals(type)) {
            var trueValue = option.attributeValue("trueValue");
            if (trueValue == null) throw new MSLAException("Attribute 'trueValue' is required in option '" + name + "'");
            var falseValue = option.attributeValue("falseValue");
            if (falseValue == null) throw new MSLAException("Attribute 'falseValue' is required in option '" + name + "'");
            opt = new ParamBoolean(group, value, trueValue, falseValue);
        }
        else if ("".equals(type) || type == null) opt = new Param(group, value);
        else throw new MSLAException("Option type " + type + " is unknown");
        options.put(name, opt);
    }
}
