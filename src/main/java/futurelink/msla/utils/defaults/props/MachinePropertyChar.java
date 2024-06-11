package futurelink.msla.utils.defaults.props;

import lombok.Getter;

@Getter
public class MachinePropertyChar extends MachineProperty {
    public MachinePropertyChar(String value) { super(!value.isEmpty() ? value.substring(0,1) : ""); }
    @Override public String getType() { return "character"; }
    @Override public Integer getInt() { return getValue().isEmpty() ? null : (int) getValue().getBytes()[0]; }
    @Override public Byte getByte() { return getValue().isEmpty() ? null : getValue().getBytes()[0]; }
    @Override public Short getShort() { return getValue().isEmpty() ? null : (short) getValue().getBytes()[0]; }
    @Override public String toString() { return "Char option [ default = " + getValue() + " ]"; }
}