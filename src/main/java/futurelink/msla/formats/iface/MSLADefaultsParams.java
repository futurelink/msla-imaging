package futurelink.msla.formats.iface;

import com.fasterxml.jackson.annotation.JsonIgnore;
import futurelink.msla.formats.MSLAException;

public interface MSLADefaultsParams {
    String getType();
    @JsonIgnore Short getShort();
    @JsonIgnore Integer getInt();
    @JsonIgnore Long getLong();
    @JsonIgnore Byte getByte();
    @JsonIgnore Float getFloat();
    @JsonIgnore Double getDouble();
    @JsonIgnore String getString();
    @JsonIgnore Object getAsType(String type);
    default void checkValue(String value) throws MSLAException {}
}
