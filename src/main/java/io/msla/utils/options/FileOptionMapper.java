package io.msla.utils.options;

import io.msla.formats.MSLAException;
import io.msla.formats.OptionMapper;
import io.msla.formats.iface.*;
import io.msla.formats.iface.options.MSLAOption;
import io.msla.formats.iface.options.MSLAOptionContainer;
import io.msla.formats.iface.options.MSLAOptionGroup;
import io.msla.formats.iface.options.MSLAOptionName;
import io.msla.utils.defaults.MachineDefaults;
import lombok.Getter;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.logging.Logger;

public class FileOptionMapper extends OptionMapper {
    private final Logger logger = Logger.getLogger(FileOptionMapper.class.getName());

    private final MSLAFile<?> file;
    @Getter private MSLAFileDefaults defaults;
    private final OptionGroupsMapper optionGroupsMapper = OptionGroupsMapper.getInstance();
    private final HashMap<MSLAOptionName, Option> optionsMap;

    public FileOptionMapper(MSLAFile<?> file, MSLAFileDefaults defaults) throws MSLAException {
        this.file = file;
        this.defaults = defaults;
        this.optionsMap = new HashMap<>();
        enumerateOptions();
    }

    @Override
    public void setDefaults(MSLADefaults defaults) {
        if (defaults instanceof MSLALayerDefaults)
            this.defaults = (MSLAFileDefaults) defaults;
        else throw new ClassCastException("Can't set defaults other than " + MSLAFileDefaults.class.getName());
    }

    public List<MSLAFileDefaults> getMatchingDefaults() throws MSLAException {
        return MachineDefaults.getInstance().getMachineDefaults(file);
    }

    /**
     * Flattens options map. Options are being obtained
     * from MSLAFile definition. Each property annotated as MSLAOptionContainer
     * is considered to be MSLAFileBlock or MSLAFileBlockFields that have
     * option fields inside.
     */
    @SuppressWarnings("unchecked")
    private void enumerateOptions() throws MSLAException {
        var fileClass = file.getClass();
        var fields = fileClass.getDeclaredFields();
        this.optionsMap.clear();
        try {

            for (var field : fields) {
                if (MSLAFileBlock.class.isAssignableFrom(field.getType())) {
                    if (field.getAnnotation(MSLAOptionContainer.class) != null) {
                        var blockPropertyName = field.getName();
                        var getterMethod = fileClass.getDeclaredMethod("get" + blockPropertyName);
                        var fileBlock = ((MSLAFileBlock) getterMethod.invoke(file));
                        if (fileBlock != null) {
                            var blockFields = fileBlock.getBlockFields();
                            Arrays.stream(blockFields.getClass().getDeclaredFields())
                                    .filter((f) -> f.getAnnotation(MSLAOption.class) != null)
                                    .filter((f) -> !blockFields.isFieldExcluded(f.getName()))
                                    .forEach((f) -> {
                                        var optionName = f.getAnnotation(MSLAOption.class).value();
                                        var location = List.of(blockPropertyName); // TODO make hierarchy
                                        if (Serializable.class.isAssignableFrom(f.getType())) {
                                            var opt = new Option(f.getName(), (Class<? extends Serializable>) f.getType(), location);
                                            if (defaults != null)
                                                opt.setParameters(defaults.getParameters(blockPropertyName, optionName));
                                            this.optionsMap.put(optionName, opt);
                                        } else logger.warning("Option '" + optionName + "' is not Serializable");
                                    });
                        } else logger.info("Block '" + blockPropertyName + "' is not defined or created");
                    }
                }
            }
            logger.info("Available options: " + optionsMap);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new MSLAException("Can't enumerate options", e);
        }
    }

    @Override
    public Class<?> getType(MSLAOptionName option) {
        if (!this.optionsMap.containsKey(option)) return null;
        return this.optionsMap.get(option).getType();
    }

    @Override
    public MSLADefaultsParams getParameters(MSLAOptionName option) {
        if (this.optionsMap.get(option) == null) return null;
        return this.optionsMap.get(option).getParameters();
    }

    @Override
    public MSLAOptionGroup getGroup(MSLAOptionName option) {
        if (this.optionGroupsMapper == null) return null;
        return this.optionGroupsMapper.getGroup(option);
    }

    @Override
    public List<MSLAOptionGroup> getGroups() {
        if (this.optionGroupsMapper == null) return null;
        var groups = new ArrayList<MSLAOptionGroup>();
        for (var option : optionsMap.keySet()) {
            var grp = this.optionGroupsMapper.getGroup(option);
            if (!groups.contains(grp)) groups.add(grp);
        }
        return groups;
    }

    @Override
    public boolean hasOption(MSLAOptionName option) {
        return this.optionsMap.containsKey(option);
    }

    @Override
    public void set(MSLAOptionName optionName, String value) throws MSLAException {
        super.set(optionName, value);
        try {
            var option = this.optionsMap.get(optionName);
            var fileBlock = getOptionFileBlock(optionName);
            if (fileBlock.getBlockFields() != null) {
                var fields = fileBlock.getBlockFields();
                logger.info("Setting '" + option.getName() + "' in " + fields.getClass() + " of type " + option.getType());
                try {
                    var setter = fields.getClass().getDeclaredMethod("set" + option.getName(), option.getType());
                    var rawValue = defaults.displayToRaw(optionName, value, option.getType());
                    logger.info("Raw value is '" + rawValue + "' of " + rawValue.getClass());
                    setter.setAccessible(true);
                    setter.invoke(fields, rawValue);
                    setter.setAccessible(false);
                } catch (NoSuchMethodException ignored) {
                    var f = fields.getClass().getDeclaredField(option.getName());
                    f.setAccessible(true);
                    f.set(fields, defaults.displayToRaw(optionName, value, option.getType()));
                    f.setAccessible(false);
                }
            } else throw new MSLAException("Can't fetch option, no file block fields instantiated");
        } catch (NoSuchFieldException | InvocationTargetException | IllegalAccessException e) {
            throw new MSLAException("Error populating option", e);
        }
    }

    @Override
    public String get(MSLAOptionName optionName) throws MSLAException {
        super.get(optionName); // Return value is ignored
        try {
            if (!this.optionsMap.containsKey(optionName)) throw new MSLAException("Option '" + optionName + "' is not known");
            var option = this.optionsMap.get(optionName);
            var fileBlock = getOptionFileBlock(optionName);
            Object rawValue;
            if (fileBlock.getBlockFields() != null) {
                var fields = fileBlock.getBlockFields();
                logger.fine("Getting '" + option.getName() + "' in '" + fileBlock.getClass().getSimpleName() +"' of " + option.getType());
                try {
                    var getter = fields.getClass().getDeclaredMethod("get" + option.getName());
                    getter.setAccessible(true);
                    rawValue =  getter.invoke(fields);
                    getter.setAccessible(false);
                } catch (NoSuchMethodException ignored) {
                    var f = fields.getClass().getDeclaredField(option.getName());
                    f.setAccessible(true);
                    rawValue = f.get(fields);
                    f.setAccessible(false);
                }
                if (defaults != null && defaults.hasFileOption(optionName)) {
                    var value = defaults.rawToDisplay(optionName, (Serializable) rawValue); // Gets RAW option value
                    logger.fine("Raw option value '" + rawValue + "' is displayed as '" + value + "'");
                    return value;
                } else {
                    logger.warning("Raw option value '" + rawValue + "' is displayed it is, no option params defined");
                    return String.valueOf(rawValue);
                }
            } else throw new MSLAException("Can't fetch option, no file block fields instantiated");
        } catch (NoSuchFieldException | InvocationTargetException | IllegalAccessException | ClassCastException e) {
            throw new MSLAException("Error fetching option '" + optionName + "'", e);
        }
    }

    @Override public Boolean isEditable() { return defaults != null; }
    @Override public Set<MSLAOptionName> available() { return optionsMap.keySet(); }

    /**
     * Gets the block where option is located
     */
    private MSLAFileBlock getOptionFileBlock(MSLAOptionName optionName) throws MSLAException {
        try {
            var loc = this.optionsMap.get(optionName).getLocation();
            var fileClass = file.getClass();
            var blockPropertyName = fileClass.getDeclaredField(loc.get(0)).getName();
            var blockAnnotation = fileClass.getDeclaredField(loc.get(0)).getAnnotation(MSLAOptionContainer.class);
            if (blockAnnotation != null) {
                var getterMethod = fileClass.getDeclaredMethod("get" + blockPropertyName);
                return ((MSLAFileBlock) getterMethod.invoke(file));
            } else throw new MSLAException("File block is not configured as @MSLAOptionContainer");
        } catch (NoSuchFieldException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new MSLAException(e.getMessage());
        }
    }
}
