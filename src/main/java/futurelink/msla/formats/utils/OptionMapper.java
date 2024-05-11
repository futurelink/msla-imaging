package futurelink.msla.formats.utils;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.MSLAOptionMapper;
import futurelink.msla.formats.iface.MSLAFile;
import futurelink.msla.formats.iface.MSLAFileBlock;
import futurelink.msla.formats.iface.annotations.MSLAOptionContainer;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Logger;

public class OptionMapper extends MSLAOptionMapper {
    private final Logger logger = Logger.getLogger(OptionMapper.class.getName());
    private final MSLAFile<?> file;
    private final HashMap<String, Option> optionsMap;
    private final HashMap<String, String> optionsNames;

    public OptionMapper(MSLAFile<?> file) {
        this.file = file;
        this.optionsMap = new HashMap<>();
        this.optionsNames = new HashMap<>();
        enumerateFileOptions();

    }

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
                        fileBlock.getOptions().forEach((name, type) -> this.optionsMap.put(name, new Option(type, blockPropertyName)));
                        this.optionsNames.putAll(fileBlock.getOptionNames());
                    }
                }
            }
            logger.info("Available options: " + optionsMap);
            logger.info("Available option names: " + optionsNames);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * File options operations
     */

    @Override
    public Class<?> getType(String option) {
        if (!this.optionsNames.containsKey(option)) return null;
        return this.optionsMap.get(this.optionsNames.get(option)).getOptionClass();
    }

    @Override
    public boolean hasOption(String option, Class<? extends Serializable> aClass) {
        if (!this.optionsNames.containsKey(option)) return false;
        if (aClass != null) return this.optionsMap.get(this.optionsNames.get(option)).getOptionClass().isAssignableFrom(aClass);
        return true;
    }

    @Override
    public void populateOption(String option, Serializable value) throws MSLAException {
        try {
            var optionPropName = this.optionsNames.get(option);
            var cls = this.optionsMap.get(optionPropName).getOptionClass();
            var fileBlock = getOptionFileBlock(option);
            if (fileBlock.getFileFields() != null) {
                var fields = fileBlock.getFileFields();
                logger.info("Setting '" + optionPropName + "' of " + fields.getClass());
                try {
                    var setter = fields.getClass().getDeclaredMethod("set" + optionPropName, cls);
                    setter.invoke(fields, cls.cast(value));
                } catch (NoSuchMethodException ignored) {
                    var f = fields.getClass().getDeclaredField(optionPropName);
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
    public Serializable fetchOption(String option) throws MSLAException {
        try {
            if (!this.optionsNames.containsKey(option)) throw new MSLAException("Option '" + option + "' is not known");
            var optionPropName = this.optionsNames.get(option);
            var cls = this.optionsMap.get(optionPropName).getOptionClass();
            var fileBlock = getOptionFileBlock(option);
            if (fileBlock.getFileFields() != null) {
                var fields = fileBlock.getFileFields();
                logger.info("Getting '" + optionPropName + "' of " + fields.getClass());
                try {
                    var getter = fields.getClass().getDeclaredMethod("get" + optionPropName);
                    return (Serializable) cls.cast(getter.invoke(fields));
                } catch (NoSuchMethodException ignored) {
                    var f = fields.getClass().getDeclaredField(optionPropName);
                    f.setAccessible(true);
                    var value = f.get(fields);
                    f.setAccessible(false);
                    return (Serializable) cls.cast(value);
                }
            } else throw new MSLAException("Can't fetch option, no file block fields instantiated");
        } catch (NoSuchFieldException | InvocationTargetException | IllegalAccessException e) {
            throw new MSLAException("Error fetching option", e);
        }
    }

    @Override
    public boolean hasLayerOption(String option, Class<? extends Serializable> aClass) {
        return false;
    }

    /*
     * Layer options operations
     */

    @Override public Set<String> getLayerOptions() { return Set.of(); }
    @Override public void populateLayerOption(String option, int layer, Serializable value) {}
    @Override public Serializable fetchLayerOption(String option, int layer) { return null; }
    @Override  public Set<String> getOptions() { return optionsNames.keySet(); }

    /**
     * Gets the block where option is located
     */
    private MSLAFileBlock getOptionFileBlock(String option) throws MSLAException {
        try {
            var optionPropName = this.optionsNames.get(option);
            var loc = this.optionsMap.get(optionPropName).getLocation();
            var fileClass = file.getClass();
            var blockPropertyName = fileClass.getDeclaredField(loc).getName();
            var blockAnnotation = fileClass.getDeclaredField(loc).getAnnotation(MSLAOptionContainer.class);
            if (blockAnnotation != null) {
                var getterMethod = fileClass.getDeclaredMethod("get" + blockPropertyName);
                return ((MSLAFileBlock) getterMethod.invoke(file));
            } else throw new MSLAException("File block is not configured as @MSLAOptionContainer");
        } catch (NoSuchFieldException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new MSLAException(e.getMessage());
        }
    }
}
