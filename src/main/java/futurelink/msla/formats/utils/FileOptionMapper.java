package futurelink.msla.formats.utils;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.MSLAOptionMapper;
import futurelink.msla.formats.iface.*;
import futurelink.msla.formats.iface.annotations.MSLAOption;
import futurelink.msla.formats.iface.annotations.MSLAOptionContainer;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;
import java.util.logging.Logger;

public class FileOptionMapper extends MSLAOptionMapper {
    private final Logger logger = Logger.getLogger(FileOptionMapper.class.getName());
    private final MSLAFile<?> file;
    private final HashMap<String, Option> optionsMap;

    public FileOptionMapper(MSLAFile<?> file) {
        this.file = file;
        this.optionsMap = new HashMap<>();
        enumerateFileOptions();
    }

    /**
     * Flattens options map. Options are being obtained
     * from MSLAFile definition. Each property annotated as MSLAOptionContainer
     * is considered to be MSLAFileBlock or MSLAFileBlockFields that have
     * option fields inside.
     */
    private void enumerateFileOptions() {
        var fileClass = file.getClass();
        var fields = fileClass.getDeclaredFields();
        try {
            for (var field : fields) {
                if (MSLAFileBlock.class.isAssignableFrom(field.getType())) {
                    if (field.getAnnotation(MSLAOptionContainer.class) != null) {
                        var blockPropertyName = field.getName();
                        var getterMethod = fileClass.getDeclaredMethod("get" + blockPropertyName);
                        var fileBlock = ((MSLAFileBlock) getterMethod.invoke(file));
                        Arrays.stream(fileBlock.getFileFields().getClass().getDeclaredFields())
                                .filter((f) -> f.getAnnotation(MSLAOption.class) != null)
                                .forEach((f) -> {
                                    var optionName = f.getAnnotation(MSLAOption.class).value().isEmpty() ?
                                            f.getName() :
                                            f.getAnnotation(MSLAOption.class).value();
                                    var location = new LinkedList<String>();
                                    location.add(blockPropertyName);
                                    this.optionsMap.put(optionName, new Option(f.getName(), f.getType(), location));
                                });
                    }
                }
            }
            logger.info("Available options: " + optionsMap);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Class<?> getType(String option) {
        if (!this.optionsMap.containsKey(option)) return null;
        return this.optionsMap.get(option).getOptionClass();
    }

    @Override
    public boolean hasOption(String option, Class<? extends Serializable> aClass) {
        if (!this.optionsMap.containsKey(option)) return false;
        if (aClass != null) return this.optionsMap.get(option).getOptionClass().isAssignableFrom(aClass);
        return true;
    }

    @Override
    public void populateOption(String optionName, Serializable value) throws MSLAException {
        try {
            var option = this.optionsMap.get(optionName);
            var fileBlock = getOptionFileBlock(optionName);
            if (fileBlock.getFileFields() != null) {
                var fields = fileBlock.getFileFields();
                logger.info("Setting '" + option.getName() + "' of " + fields.getClass());
                try {
                    var setter = fields.getClass().getDeclaredMethod("set" + option.getName(), option.getOptionClass());
                    setter.invoke(fields, option.getOptionClass().cast(value));
                } catch (NoSuchMethodException ignored) {
                    var f = fields.getClass().getDeclaredField(option.getName());
                    f.setAccessible(true);
                    f.set(fields, value);
                    f.setAccessible(false);
                }
            } else throw new MSLAException("Can't fetch option, no file block fields instantiated");
        } catch (NoSuchFieldException | InvocationTargetException | IllegalAccessException e) {
            throw new MSLAException("Error populating option", e);
        }
    }

    @Override
    public Serializable fetchOption(String optionName) throws MSLAException {
        try {
            if (!this.optionsMap.containsKey(optionName)) throw new MSLAException("Option '" + optionName + "' is not known");
            var option = this.optionsMap.get(optionName);
            var fileBlock = getOptionFileBlock(optionName);
            if (fileBlock.getFileFields() != null) {
                var fields = fileBlock.getFileFields();
                logger.info("Getting '" + option.getName() + "' of " + fields.getClass());
                try {
                    var getter = fields.getClass().getDeclaredMethod("get" + option.getName());
                    return (Serializable) option.getOptionClass().cast(getter.invoke(fields));
                } catch (NoSuchMethodException ignored) {
                    var f = fields.getClass().getDeclaredField(option.getName());
                    f.setAccessible(true);
                    var value = f.get(fields);
                    f.setAccessible(false);
                    return (Serializable) option.getOptionClass().cast(value);
                }
            } else throw new MSLAException("Can't fetch option, no file block fields instantiated");
        } catch (NoSuchFieldException | InvocationTargetException | IllegalAccessException e) {
            throw new MSLAException("Error fetching option", e);
        }
    }

    @Override public Set<String> available() { return optionsMap.keySet(); }

    /**
     * Gets the block where option is located
     */
    private MSLAFileBlock getOptionFileBlock(String optionName) throws MSLAException {
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
