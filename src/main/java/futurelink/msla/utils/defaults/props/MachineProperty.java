package futurelink.msla.utils.defaults.props;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.iface.MSLADefaultsParams;
import futurelink.msla.utils.Size;
import lombok.Getter;

import java.io.Serializable;

@Getter
public class MachineProperty implements MSLADefaultsParams {
    private final String value;
    public MachineProperty(String value) {
        this.value = value;
    }
    public String getType() { return "generic"; }
    public final Integer getInt() throws MSLAException { return (Integer) valueToType(Integer.class.getSimpleName(), value); }
    public final Byte getByte() throws MSLAException { return (Byte) valueToType(Byte.class.getSimpleName(), value); }
    public final Short getShort() throws MSLAException { return (Short) valueToType(Short.class.getSimpleName(), value); }
    public final Long getLong() throws MSLAException { return (Long) valueToType(Long.class.getSimpleName(), value); }
    public final Float getFloat() throws MSLAException { return (Float) valueToType(Float.class.getSimpleName(), value); }
    public final Double getDouble() throws MSLAException { return (Double) valueToType(Double.class.getSimpleName(), value); }
    public final Boolean getBoolean() throws MSLAException { return (Boolean) valueToType(Boolean.class.getSimpleName(), value); }
    public final  String getString() { return value; }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Serializable> T displayToRaw(Class<? extends T> rawType, Object optionValue) throws MSLAException {
        if (Serializable.class.isAssignableFrom(rawType)) return (T) valueToType(rawType.getSimpleName(), optionValue);
        else throw new MSLAException("Cannot convert " + rawType.getSimpleName() + " to " + optionValue);
    }

    @Override
    public <T> String rawToDisplay(Class<? extends T> rawType, Serializable rawValue) throws MSLAException {
        return String.valueOf(rawValue);
    }

    public final Object getAsType(String type) throws MSLAException {
        return valueToType(type, value);
    }

    protected Serializable valueToType(String type, Object value) throws MSLAException {
        var strValue = String.valueOf(value);
        return switch (type) {
            case "short", "Short" -> Short.parseShort(strValue);
            case "int", "Integer" -> Integer.parseInt(strValue);
            case "long", "Long" -> Long.parseLong(strValue);
            case "float", "Float" -> Float.parseFloat(strValue);
            case "double", "Double" -> Double.parseDouble(strValue);
            case "byte", "Byte" ->  Byte.parseByte(strValue);
            case "boolean", "Boolean" -> Boolean.parseBoolean(strValue);
            case "Size" -> Size.parseSize(strValue);
            default -> strValue;
        };
    }

    @Override public String toString() { return "Generic option [ default = " + value + " ]"; }
}