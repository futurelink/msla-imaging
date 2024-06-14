package futurelink.msla.formats.iface;

import com.fasterxml.jackson.annotation.JsonIgnore;
import futurelink.msla.formats.MSLAException;

import java.io.Serializable;

public interface MSLADefaultsParams {
    String getType();
    @JsonIgnore Boolean getBoolean() throws MSLAException;
    @JsonIgnore Short getShort() throws MSLAException;
    @JsonIgnore Integer getInt() throws MSLAException;
    @JsonIgnore Long getLong() throws MSLAException;
    @JsonIgnore Byte getByte() throws MSLAException;
    @JsonIgnore Float getFloat() throws MSLAException;
    @JsonIgnore Double getDouble() throws MSLAException;
    @JsonIgnore String getString() throws MSLAException;
    @JsonIgnore Object getAsType(String type) throws MSLAException;
    default void checkValue(String value) throws MSLAException {}

    <T extends Serializable> T displayToRaw(Class<? extends T> rawType, Object optionValue) throws MSLAException;
    <T> Object rawToDisplay(Class<? extends T> rawType, Serializable rawValue) throws MSLAException;
}
