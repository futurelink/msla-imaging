package io.msla.utils.defaults;

import io.msla.formats.MSLAException;
import io.msla.formats.iface.*;
import io.msla.formats.iface.options.MSLAOptionName;
import io.msla.utils.Size;
import io.msla.utils.defaults.props.MachineProperty;
import lombok.Getter;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Internal machine defaults class.
 */
@Getter
public class MachineFileDefaults implements MSLAFileDefaults {
    private static final Logger logger = Logger.getLogger(MachineFileDefaults.class.getName());
    private final String machineManufacturer;
    private final String machineName;
    private final String fileExtension;
    private final Class<? extends MSLAFile<?>> fileClass;
    private final MSLAFileProps fileProps = new MSLAFileProps();
    private final MachineOptions options = new MachineOptions();
    private final MachineLayerDefaults layerDefaults = new MachineLayerDefaults();

    public MachineFileDefaults(String manufacturer, String name, String extension, Class<? extends MSLAFile<?>> fileClass) {
        this.machineName = name;
        this.machineManufacturer = manufacturer;
        this.fileExtension = extension;
        this.fileClass = fileClass;
        this.fileProps.put("MachineName", new MachineProperty(manufacturer + " " + name));
    }

    @Override public final Float getPixelSize() {
        try {return fileProps.getFloat("PixelSize"); }
        catch (MSLAException e) { return null; }
    }

    @Override public final Size getResolution() {
        try {return Size.parseSize(fileProps.getString("Resolution")); }
        catch (MSLAException e) { return null; }
    }

    @Override public final String getMachineFullName() { return getMachineManufacturer() + " " + getMachineName(); }
    @Override public MSLADefaultsParams getFileOption(MSLAOptionName name) { return options.getOption(name); }
    @Override public boolean hasFileOption(MSLAOptionName name) { return options.getOption(name) != null; }

    @Override
    public <T extends Serializable> Serializable displayToRaw(
            MSLAOptionName name,
            T optionValue,
            Class<? extends T> rawType) throws MSLAException
    {
        if (optionValue == null) return null;
        if (rawType == null) return optionValue;
        if (!hasFileOption(name)) throw new MSLAException("No defaults defined for option '" + name + "', can't set value");
        return options.getOption(name).displayToRaw(rawType, optionValue);
    }

    @Override
    public <T> String rawToDisplay(MSLAOptionName name, Serializable optionValue) throws MSLAException {
        if (optionValue == null) return null;
        if (!hasFileOption(name)) throw new MSLAException("No defaults defined for option '" + name + "', can't get display value");
        return options.getOption(name).rawToDisplay(optionValue.getClass(), optionValue);
    }

    /**
     * Gets set of all fields in MSLAFileBlockFields marked as MSLAFileField
     * @param fields block fields obect
     */
    private Set<String> getBlockFieldsProperties(MSLAFileBlockFields fields) {
        var ret = new HashSet<String>();
        var props = fields.getClass().getDeclaredFields();
        for (var prop : props) {
            if (prop.isAnnotationPresent(MSLAFileField.class)) ret.add(prop.getName());
        }
        var methods = fields.getClass().getDeclaredMethods();
        for (var method : methods) {
            if (method.isAnnotationPresent(MSLAFileField.class)) ret.add(method.getName());
        }
        return ret;
    }

    public final void setFields(MSLAFileBlockFields fields) throws MSLAException {
        var instance = MachineDefaults.getInstance();

        // Set properties
        var blockProps = getBlockFieldsProperties(fields);
        for (var prop : blockProps) {
            if (fileProps.containsKey(prop) || "ResolutionX".equals(prop) || "ResolutionY".equals(prop)) {
                instance.setFieldDefault(fields, prop, fileProps.get(prop));
            }
        }

        // Set options
        var blockOptions = instance.getBlockFieldsOptions(fields);
        for (var option : blockOptions.keySet()) {
            var defaultOption = options.getOption(option);
            if (defaultOption != null) {
                logger.fine("Setting default option '" + option.getName() + "' to " + defaultOption);
                instance.setFieldDefault(fields, blockOptions.get(option), defaultOption);
            }
        }
    }

    @Override
    public MSLADefaultsParams getParameters(String blockName, MSLAOptionName fieldName) {
        return options.getOption(fieldName);
    }
}
