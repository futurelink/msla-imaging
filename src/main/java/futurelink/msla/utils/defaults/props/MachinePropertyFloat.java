package futurelink.msla.utils.defaults.props;

import futurelink.msla.formats.MSLAException;
import lombok.Getter;

@Getter
public class MachinePropertyFloat extends MachineProperty {
    private final Float minValue;
    private final Float maxValue;
    public MachinePropertyFloat(String value, Float minValue, Float maxValue) {
        super(value);
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    @Override public String getType() { return "float"; }
    @Override public Byte getByte() { return null; }
    @Override public Short getShort() { return null; }
    @Override public Integer getInt() { return null; }
    @Override public Long getLong() { return null; }
    @Override public String getString() { return String.valueOf(getValue()); }

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
        return "Float option [ " + minValue + " > " + getValue() + " > " + maxValue + " ]";
    }
}