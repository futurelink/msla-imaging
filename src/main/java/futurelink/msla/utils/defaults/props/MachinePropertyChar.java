package futurelink.msla.utils.defaults.props;

import futurelink.msla.formats.MSLAException;
import lombok.Getter;

import java.io.Serializable;

@Getter
public class MachinePropertyChar extends MachineProperty {
    public MachinePropertyChar(String value) { super(!value.isEmpty() ? value.substring(0,1) : ""); }
    @Override public String getType() { return "character"; }
    @Override public String toString() { return "Char option [ default = " + getValue() + " ]"; }

    protected Serializable valueToType(String type, Object value) throws MSLAException {
        var strValue = String.valueOf(value);
        return switch (type) {
            case "short", "Short" -> strValue.isEmpty() ? null : (short) getValue().getBytes()[0];
            case "int", "Integer" -> strValue.isEmpty() ? null : (int) getValue().getBytes()[0];
            case "byte", "Byte" ->  strValue.isEmpty() ? null : getValue().getBytes()[0];
            default -> throw new MSLAException("Value of type '" + type + "' can't be converted to Character");
        };
    }

    @Override
    public <T> String rawToDisplay(Class<? extends T> rawType, Serializable rawValue) throws MSLAException {
        if (Byte.class.isAssignableFrom(rawType)) return rawValue == null ? null : Character.toString((Byte) rawValue);
        else if (Integer.class.isAssignableFrom(rawType)) return rawValue == null ? null : Character.toString((Integer) rawValue);
        else throw new MSLAException("Invalid option value: '" + rawValue + "' of raw type " + rawType.getSimpleName());
    }

    @Override
    public <T extends Serializable> T displayToRaw(Class<? extends T> rawType, Object optionValue) throws MSLAException {
        if (optionValue instanceof Character) return rawType.cast(optionValue);
        if (optionValue instanceof String) return rawType.cast((int) ((String) optionValue).getBytes()[0]);
        else throw new MSLAException("Option raw value " + optionValue + " is not a Character");
    }

}
