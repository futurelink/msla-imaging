package io.msla.utils.defaults;

import io.msla.formats.MSLAException;
import io.msla.formats.iface.MSLADefaultsParams;
import io.msla.formats.iface.MSLAFileBlockFields;
import io.msla.formats.iface.MSLALayerDefaults;
import io.msla.formats.iface.options.MSLAOptionName;
import lombok.Getter;

import java.io.Serializable;
import java.util.logging.Logger;

/**
 * Internal machine layer-specific defaults class.
 */
@Getter
public class MachineLayerDefaults implements MSLALayerDefaults {
    private static final Logger logger = Logger.getLogger(MachineLayerDefaults.class.getName());
    private final MachineOptions options = new MachineOptions();

    @Override
    public void setFields(MSLAFileBlockFields fields) throws MSLAException {
        var instance = MachineDefaults.getInstance();
        var blockOptions = instance.getBlockFieldsOptions(fields);
        for (var option : blockOptions.keySet()) {
            var defaultOption = options.getOption(option);
            if (defaultOption != null) {
                logger.fine("Setting layer default option '" + option.getName() + "' to " + defaultOption);
                instance.setFieldDefault(fields, blockOptions.get(option), defaultOption);
            }
        }
    }

    @Override
    public MSLADefaultsParams getParameters(String blockName, MSLAOptionName optionName) {
        return options.getOption(optionName);
    }

    @Override
    public <T extends Serializable> Serializable displayToRaw(
            MSLAOptionName name,
            T optionValue,
            Class<? extends T> rawType) throws MSLAException
    {
        if (optionValue == null) return null;
        if (rawType == null) return optionValue;
        return options.getOption(name).displayToRaw(rawType, optionValue);
    }

    @Override
    public <T> String rawToDisplay(MSLAOptionName name, Serializable optionValue) throws MSLAException {
        if (optionValue == null) return null;
        return options.getOption(name).rawToDisplay(optionValue.getClass(), optionValue);
    }
}