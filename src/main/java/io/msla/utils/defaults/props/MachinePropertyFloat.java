package io.msla.utils.defaults.props;

import io.msla.formats.MSLAException;
import lombok.Getter;

import java.io.Serializable;

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

    protected Serializable valueToType(String type, Object value) throws MSLAException {
        var strValue = String.valueOf(value);
        return switch (type) {
            case "float", "Float" -> Float.parseFloat(strValue);
            case "double", "Double" -> Double.parseDouble(strValue);
            case "int", "Integer" -> Integer.parseInt(strValue);
            case "String" -> strValue;
            default -> throw new MSLAException("Value '" + value + "' of type '" + type + "' can't be converted to Float");
        };
    }

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