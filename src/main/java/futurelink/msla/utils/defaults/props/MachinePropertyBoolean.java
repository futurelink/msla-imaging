package futurelink.msla.utils.defaults.props;

import lombok.Getter;

@Getter
public class MachinePropertyBoolean extends MachineProperty {
    private final String trueValue;
    private final String falseValue;
    public MachinePropertyBoolean(String value, String trueValue, String falseValue) {
        super(value);
        this.trueValue = trueValue;
        this.falseValue = falseValue;
    }
    @Override public String getType() { return "boolean"; }
    @Override public String toString() {
        return "Boolean option [ default = " + getValue() + ", true = " + trueValue + ", false = " + falseValue + " ]"; }
}