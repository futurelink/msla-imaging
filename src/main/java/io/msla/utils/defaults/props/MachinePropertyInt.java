package io.msla.utils.defaults.props;

import io.msla.formats.MSLAException;
import lombok.Getter;

@Getter
public class MachinePropertyInt extends MachineProperty {
    private final Integer minValue;
    private final Integer maxValue;
    public MachinePropertyInt(String value, Integer minValue, Integer maxValue) {
        super(value);
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    @Override public String getType() { return "integer"; }

    @Override
    public void checkValue(String value) throws MSLAException {
        try {
            var val = Integer.parseInt(value);
            if (val < minValue) throw new MSLAException("Value can't be less than " + minValue);
            if (val > maxValue) throw new MSLAException("Value can't be greater than " + maxValue);
        } catch (NumberFormatException e) {
            throw new MSLAException("Value can't be " + value);
        }
    }

    @Override
    public String toString() {
        return "Int option [ " + minValue + " > " + getValue() + " > " + maxValue + " ]";
    }
}