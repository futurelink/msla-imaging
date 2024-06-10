package futurelink.msla.utils.defaults.props;

import futurelink.msla.formats.iface.MSLADefaultsParams;
import futurelink.msla.utils.Size;
import lombok.Getter;

@Getter
public class MachineProperty implements MSLADefaultsParams {
    private final String value;
    public MachineProperty(String value) {
        this.value = value;
    }
    public String getType() { return "generic"; }
    public Integer getInt() { return Integer.parseInt(value); }
    public Byte getByte() { return Byte.parseByte(value); }
    public Short getShort() { return Short.parseShort(value); }
    public Long getLong() { return Long.parseLong(value); }
    public Float getFloat() { return Float.parseFloat(value); }
    public Double getDouble() { return Double.parseDouble(value); }
    public String getString() { return value; }
    public Boolean getBoolean() { return Boolean.parseBoolean(value); }

    public final Object getAsType(String type) {
        return switch (type) {
            case "short", "Short" -> getShort();
            case "int", "Integer" -> getInt();
            case "long", "Long" -> getLong();
            case "float", "Float" -> getFloat();
            case "double", "Double" -> getDouble();
            case "byte", "Byte" -> getByte();
            case "boolean", "Boolean" -> getBoolean();
            case "Size" -> Size.parseSize(getValue());
            default -> getString();
        };
    }

    @Override public String toString() { return "Generic option [ default = " + value + " ]"; }
}