package futurelink.msla.utils.defaults.props;

import futurelink.msla.formats.MSLAException;
import lombok.Getter;

import java.io.Serializable;

@Getter
public class MachinePropertyBoolean extends MachineProperty {
    private final String trueValue;
    private final String falseValue;
    public MachinePropertyBoolean(String value, String trueValue, String falseValue) {
        super(value);
        this.trueValue = trueValue;
        this.falseValue = falseValue;
    }

    @Override
    public <T> String rawToDisplay(Class<? extends T> rawType, Serializable rawValue) throws MSLAException {
        var val = String.valueOf(rawValue);
        if (val.equals(trueValue)) return "true";
        else if (val.equals(falseValue)) return "false";
        else throw new MSLAException("Invalid option value: " + rawValue + " can be '" + trueValue + "' or '" + falseValue + "'");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Serializable> T displayToRaw(Class<? extends T> rawType, Object optionValue) throws MSLAException {
        if (optionValue == null) return null;
        if (Boolean.class.isAssignableFrom(rawType)) {
            return (T) ((Boolean) optionValue ? trueValue : falseValue);
        } else if (optionValue instanceof String) {
            return (T) switch ((String) optionValue) {
                case "true" -> valueToType(rawType.getSimpleName(), trueValue);
                case "false" -> valueToType(rawType.getSimpleName(), falseValue);
                default -> throw new MSLAException("Invalid option value: " + optionValue);
            };
        } throw new MSLAException("Option raw value '" + optionValue + "' is not a Boolean");
    }

    @Override public String getType() { return "boolean"; }
    @Override public String toString() {
        return "Boolean option [ default = " + getValue() + ", true = " + trueValue + ", false = " + falseValue + " ]"; }
}