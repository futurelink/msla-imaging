package io.msla.utils.options;

import io.msla.formats.MSLAException;
import io.msla.formats.OptionMapper;
import io.msla.formats.iface.*;
import io.msla.formats.iface.options.MSLAOption;
import io.msla.formats.iface.options.MSLAOptionContainer;
import io.msla.formats.iface.options.MSLAOptionGroup;
import io.msla.formats.iface.options.MSLAOptionName;
import lombok.Getter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class LayerOptionMapper extends OptionMapper {
    private final Logger logger = Logger.getLogger(FileOptionMapper.class.getName());

    private final MSLAFile<?> file;
    private Integer layerNumber;
    @Getter private MSLALayerDefaults defaults;
    private final HashMap<MSLAOptionName, Option> optionsMap;

    public LayerOptionMapper(MSLAFile<?> file, MSLALayerDefaults defaults) throws MSLAException {
        if (file == null) throw new MSLAException("File is mandatory for option mapper");
        if (!file.getLayers().hasOptions()) throw new MSLAException("Layer options are not supported in this file format");
        this.file = file;
        this.defaults = defaults;
        this.optionsMap = new HashMap<>();
        setLayerNumber(0);
    }

    public void setLayerNumber(Integer layerNumber) throws MSLAException {
        if (layerNumber == null) throw new MSLAException("Layer number can't be null");
        if (layerNumber < 0) throw new MSLAException("Layer number can't be negative");
        if (layerNumber >= file.getLayers().count()) throw new MSLAException("Layer number is too large");
        this.layerNumber = layerNumber;
        this.enumerateOptions(file.getLayers().get(layerNumber).getBlockFields(), new LinkedList<>());
    }

    /**
     * Flattens layer options map.
     * This method retrieves all options in MSLAFileBlockFields that have type of MSLAFileBlock or
     * MSLAFileBlockFields marked as MSLAOption. If a property is marked as MSLAOptionContainer
     * then it's considered to be another level of options and this method is going to drill down
     * recursively.
     *
     * @param fields a block that contains fields
     * @param path path to a block (used as recursion accumulator)
     */
    private void enumerateOptions(MSLAFileBlockFields fields, List<String> path) {
        logger.fine("Getting options from " + fields.getClass().getName());
        for (var f : fields.getClass().getDeclaredFields()) {
            if (fields.isFieldExcluded(f.getName())) continue;

            // Field is options container
            if (MSLAFileBlock.class.isAssignableFrom(f.getType()) || MSLAFileBlockFields.class.isAssignableFrom(f.getType())) {
                if (f.getAnnotation(MSLAOptionContainer.class) != null) {
                    try {
                        logger.fine("Getting options for container '" + f.getName() + "'");
                        MSLAFileBlockFields fieldsInternal;
                        f.setAccessible(true);
                        if (MSLAFileBlock.class.isAssignableFrom(f.getType())) {
                            fieldsInternal = ((MSLAFileBlock) f.get(fields)).getBlockFields();
                        } else {
                            fieldsInternal = (MSLAFileBlockFields) f.get(fields);
                        }
                        f.setAccessible(false);
                        if (fieldsInternal != null) {
                            var newPath = new LinkedList<>(path);
                            newPath.add(f.getName());
                            enumerateOptions(fieldsInternal, newPath);
                        }
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            // Field is an option
            else if (f.getAnnotation(MSLAOption.class) != null) {
                var optionName = f.getAnnotation(MSLAOption.class).value();
                if (Serializable.class.isAssignableFrom(f.getType())) {
                    var opt = new Option(f.getName(), (Class<? extends Serializable>) f.getType(), path);
                    if (defaults != null) {
                        var blockName = path.isEmpty() ? null : path.get(path.size() - 1);
                        opt.setParameters(defaults.getParameters(blockName, optionName));
                    }
                    optionsMap.put(optionName, opt);
                } else logger.warning("Option '" + optionName + "' is not serializable");
            }
        }
    }

    @Override
    public Class<?> getType(MSLAOptionName optionName) {
        if (!this.optionsMap.containsKey(optionName)) return null;
        return this.optionsMap.get(optionName).getType();
    }

    @Override
    public void setDefaults(MSLADefaults defaults) {
        if (defaults instanceof MSLALayerDefaults)
            this.defaults = (MSLALayerDefaults) defaults;
        else throw new ClassCastException("Can't set defaults other than " + MSLALayerDefaults.class.getName());
    }

    @Override
    public MSLADefaultsParams getParameters(MSLAOptionName option) {
        if (this.optionsMap.get(option) == null) return null;
        return this.optionsMap.get(option).getParameters();
    }

    @Override public MSLAOptionGroup getGroup(MSLAOptionName option) {
        return null;
    }
    @Override public List<MSLAOptionGroup> getGroups() { return List.of(); }
    @Override public boolean hasOption(MSLAOptionName optionName) {
        return this.optionsMap.containsKey(optionName);
    }

    @Override public Boolean isEditable() { return defaults != null; }
    @Override public Set<MSLAOptionName> available() { return optionsMap.keySet(); }

    @Override
    public void set(MSLAOptionName optionName, String value) throws MSLAException {
        super.set(optionName, value);
        var option = optionsMap.get(optionName);
        var layer = (MSLAFileLayer) file.getLayers().get(layerNumber);
        try {
            if ("".equals(option.getLocation().get(0))) {
                // Option is in root layer definition object
                var optionField = layer.getClass().getDeclaredField(option.getName());
                optionField.setAccessible(true);
                optionField.set(layer, defaults.displayToRaw(optionName, value, option.getType()));
                optionField.setAccessible(false);
            } else{
                // Option is inside another container
                logger.info("Option '" + optionName + "' is inside '" + option.getLocation() + "'");
                var optionContainer = getOptionContainer(layer.getBlockFields(), option.getLocation());
                if (optionContainer != null) {
                    if (optionContainer instanceof MSLAFileBlock)
                        optionContainer = ((MSLAFileBlock) optionContainer).getBlockFields();
                    var optionField = optionContainer.getClass().getDeclaredField(option.getName());
                    optionField.setAccessible(true);
                    optionField.set(optionContainer, defaults.displayToRaw(optionName, value, option.getType()));
                    optionField.setAccessible(false);
                } else {
                    throw new MSLAException("Option can't be set");
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new MSLAException("Can't set option", e);
        }
    }

    @Override
    public String get(MSLAOptionName optionName) throws MSLAException {
        super.get(optionName); // Return value is ignored
        var option = optionsMap.get(optionName);
        var layer = (MSLAFileLayer) file.getLayers().get(layerNumber);
        try {
            if (option.getLocation().isEmpty() || "".equals(option.getLocation().get(0))) {
                // Option is in root layer definition object
                var fields = layer.getBlockFields();
                var optionField = fields.getClass().getDeclaredField(option.getName());
                optionField.setAccessible(true);
                var value = optionField.get(fields);
                optionField.setAccessible(false);
                return String.valueOf(value);
            } else {
                // Option is inside another container
                logger.fine("Option is inside '" + option.getLocation() + "' location");
                var optionContainer = getOptionContainer(layer.getBlockFields(), option.getLocation());
                if (optionContainer != null) {
                    if (optionContainer instanceof MSLAFileBlock)
                        optionContainer = ((MSLAFileBlock) optionContainer).getBlockFields();
                    var optionField = optionContainer.getClass().getDeclaredField(option.getName());
                    optionField.setAccessible(true);
                    var value = optionField.get(optionContainer);
                    optionField.setAccessible(false);
                    return String.valueOf(value);
                } else {
                    throw new MSLAException("Option can't be fetched");
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new MSLAException("Can't fetch option", e);
        }
    }

    private MSLAFileBlockFields getOptionContainer(MSLAFileBlockFields container, List<String> location)
            throws MSLAException
    {
        if (container == null) throw new MSLAException("Container can't be null");
        var containerName = location.get(location.size() - 1);
        var layer = file.getLayers().get(layerNumber).getBlockFields();
        try {
            var optionContainerField = layer.getClass().getDeclaredField(containerName);
            optionContainerField.setAccessible(true);
            var optionContainer = optionContainerField.get(layer);
            optionContainerField.setAccessible(false);
            if (optionContainer != null) {
                if (optionContainer instanceof MSLAFileBlock)
                    optionContainer = ((MSLAFileBlock) optionContainer).getBlockFields();
                return(MSLAFileBlockFields) optionContainer;
            } else {
                return null;
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new MSLAException("Can't get internal container '" + containerName + "'", e);
        }
    }
}
