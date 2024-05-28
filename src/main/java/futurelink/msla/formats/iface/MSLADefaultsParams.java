package futurelink.msla.formats.iface;

import futurelink.msla.formats.MSLAException;

public interface MSLADefaultsParams {
    String getType();
    Short getShort();
    Integer getInt();
    Long getLong();
    Byte getByte();
    Float getFloat();
    Double getDouble();
    String getString();
    Object getAsType(String type);
    default void checkValue(String value) throws MSLAException {}
}
