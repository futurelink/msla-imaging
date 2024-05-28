package futurelink.msla.formats.utils;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.MSLAOptionMapper;
import futurelink.msla.formats.iface.*;
import futurelink.msla.formats.iface.annotations.MSLAOption;
import futurelink.msla.formats.iface.annotations.MSLAOptionContainer;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class LayerOptionMapper extends MSLAOptionMapper {
    private final Logger logger = Logger.getLogger(FileOptionMapper.class.getName());

    private final MSLAFileBlockFields layer;
    private final MSLALayerDefaults defaults;
    private final HashMap<String, Option> optionsMap;

    public LayerOptionMapper(MSLAFileBlockFields layer, MSLALayerDefaults defaults) {
        this.layer = layer;
        this.optionsMap = new HashMap<>();
        this.defaults = defaults;
        this.enumerateOptions(layer, new LinkedList<>());
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
            // Field is options container
            if (MSLAFileBlock.class.isAssignableFrom(f.getType()) || MSLAFileBlockFields.class.isAssignableFrom(f.getType())) {
                if (f.getAnnotation(MSLAOptionContainer.class) != null) {
                    try {
                        logger.fine("Getting options for container '" + f.getName() + "'");
                        MSLAFileBlockFields fieldsInternal;
                        f.setAccessible(true);
                        if (MSLAFileBlock.class.isAssignableFrom(f.getType())) {
                            fieldsInternal = ((MSLAFileBlock) f.get(fields)).getFileFields();
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
                var optionTitle = optionName.isEmpty() ? f.getName() : optionName;
                var opt = new Option(f.getName(), f.getType(), path);
                if (defaults != null) {
                    var blockName = path.isEmpty() ? null : path.get(path.size()-1);
                    opt.setParameters(defaults.getParameters(blockName, f.getName()));
                }
                optionsMap.put(optionTitle, opt);
            }
        }
    }

    @Override
    public Class<?> getType(String optionName) {
        if (!this.optionsMap.containsKey(optionName)) return null;
        return this.optionsMap.get(optionName).getType();
    }

    @Override
    public MSLADefaultsParams getParameters(String option) {
        if (this.optionsMap.get(option) == null) return null;
        return this.optionsMap.get(option).getParameters();
    }

    @Override
    public boolean hasOption(String optionName, Class<? extends Serializable> aClass) {
        if (!this.optionsMap.containsKey(optionName)) return false;
        if (aClass != null) return this.optionsMap.get(optionName).getType().isAssignableFrom(aClass);
        return true;
    }

    @Override public Boolean isEditable() { return defaults != null; }
    @Override public Set<String> available() { return optionsMap.keySet(); }

    @Override
    protected void populateOption(String optionName, Serializable value) throws MSLAException {
        if (!isEditable()) throw new MSLAException("Options are not editable because defaults were not specified");
        var option = optionsMap.get(optionName);
        try {
            if ("".equals(option.getLocation().get(0))) {
                // Option is in root layer definition object
                var optionField = layer.getClass().getDeclaredField(option.getName());
                optionField.setAccessible(true);
                optionField.set(layer, value);
                optionField.setAccessible(false);
            } else{
                // Option is inside another container
                logger.info("Option is inside '" + option.getLocation() + "'");
                var optionContainer = getOptionContainer(layer, option.getLocation());
                if (optionContainer != null) {
                    if (optionContainer instanceof MSLAFileBlock)
                        optionContainer = ((MSLAFileBlock) optionContainer).getFileFields();
                    var optionField = optionContainer.getClass().getDeclaredField(option.getName());
                    optionField.setAccessible(true);
                    optionField.set(optionContainer, option.getType().cast(value));
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
    protected Serializable fetchOption(String optionName) throws MSLAException {
        var option = optionsMap.get(optionName);
        try {
            if ("".equals(option.getLocation().get(0))) {
                // Option is in root layer definition object
                var optionField = layer.getClass().getDeclaredField(option.getName());
                optionField.setAccessible(true);
                var value = optionField.get(layer);
                optionField.setAccessible(false);
                return (Serializable) value;
            } else {
                // Option is inside another container
                logger.info("Option is inside '" + option.getLocation() + "'");
                var optionContainer = getOptionContainer(layer, option.getLocation());
                if (optionContainer != null) {
                    if (optionContainer instanceof MSLAFileBlock)
                        optionContainer = ((MSLAFileBlock) optionContainer).getFileFields();
                    var optionField = optionContainer.getClass().getDeclaredField(option.getName());
                    optionField.setAccessible(true);
                    var value = optionField.get(optionContainer);
                    optionField.setAccessible(false);
                    return (Serializable) value;
                } else {
                    throw new MSLAException("Option can't be set");
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new MSLAException("Can't set option", e);
        }
    }

    private MSLAFileBlockFields getOptionContainer(MSLAFileBlockFields container, List<String> location)
            throws MSLAException
    {
        if (container == null) throw new MSLAException("Container can't be null");
        var containerName = location.get(location.size() - 1);
        try {
            var optionContainerField = layer.getClass().getDeclaredField(containerName);
            optionContainerField.setAccessible(true);
            var optionContainer = optionContainerField.get(layer);
            optionContainerField.setAccessible(false);
            if (optionContainer != null) {
                if (optionContainer instanceof MSLAFileBlock)
                    optionContainer = ((MSLAFileBlock) optionContainer).getFileFields();
                return(MSLAFileBlockFields) optionContainer;
            } else {
                return null;
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new MSLAException("Can't get internal container '" + containerName + "'");
        }
    }
}
