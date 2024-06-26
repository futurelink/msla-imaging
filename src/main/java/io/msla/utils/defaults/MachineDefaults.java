package io.msla.utils.defaults;

import io.msla.formats.MSLAException;
import io.msla.formats.iface.*;
import io.msla.formats.iface.MSLAFileField;
import io.msla.formats.iface.options.MSLAOption;
import io.msla.formats.iface.options.MSLAOptionName;
import io.msla.utils.Size;
import io.msla.utils.defaults.props.MachineProperty;
import lombok.Getter;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Printer defaults utility class.
 * Loads printer_defaults.xml file and creates settings for all supported machines.
 */
public class MachineDefaults {
    private static final Logger logger = Logger.getLogger(MachineDefaults.class.getName());
    private final HashMap<String, Defaults> printers = new HashMap<>();
    private static MachineDefaults instance;

    public static MachineDefaults getInstance() throws MSLAException {
        if (instance == null) { instance = new MachineDefaults(); }
        return instance;
    }

    private HashMap<MSLAOptionName, String> getBlockFieldsOptions(MSLAFileBlockFields fields) {
        var ret = new HashMap<MSLAOptionName, String>();
        var props = fields.getClass().getDeclaredFields();
        for (var prop : props)
            if (prop.isAnnotationPresent(MSLAOption.class))
                ret.put(prop.getAnnotation(MSLAOption.class).value(), prop.getName());
        return ret;
    }

    private void setFieldDefault(MSLAFileBlockFields fields, String fieldName, MSLADefaultsParams defaultOption)
            throws MSLAException
    {
        if (defaultOption == null) {
            logger.warning("Can't set option to '" + fieldName + "' as its value is null");
            return;
        }
        try {
            Field field = null;
            Class<?> type = null;
            boolean hasField = false;
            boolean hasProperty = false;
            try {
                field = fields.getClass().getDeclaredField(fieldName);
                type = field.getType();
                hasField = true;
                hasProperty = true;
            } catch (NoSuchFieldException ignored) {}

            Method method;
            try {
                if (!hasField) {
                    method = fields.getClass().getDeclaredMethod(fieldName);
                    type = method.getReturnType();
                    hasProperty = true;
                }
            } catch (NoSuchMethodException ignored) {}

            if (hasProperty) {
                Method setter = null;
                try {
                    setter = fields.getClass().getDeclaredMethod("set" + fieldName, type);
                } catch (NoSuchMethodException ignored) {}

                if (setter != null) {
                    logger.fine("Calling setter for default '" + defaultOption.getString() + "' " + fieldName + " of type " + type.getSimpleName());
                    try {
                        setter.setAccessible(true);
                        setter.invoke(fields, defaultOption.getAsType(type.getSimpleName()));
                    } catch (Exception e) {
                        throw new MSLAException("Option '" + fieldName + "' can't be set", e);
                    } finally {
                        setter.setAccessible(false);
                    }
                } else {
                    if (hasField) {
                        logger.fine("Setting default '" + defaultOption.getString() + "' " + fieldName + " of type " + type.getSimpleName());
                        try {
                            field.setAccessible(true);
                            field.set(fields, defaultOption.getAsType(type.getSimpleName()));
                        } catch (Exception e) {
                            throw new MSLAException("Option '" + fieldName + "' can't be set", e);
                        } finally {
                            field.setAccessible(false);
                        }
                    } else logger.warning("Field '" + fieldName + " can't be set as it is method without setter");
                }
            } else throw new MSLAException("Option '" + fieldName + "' has no associated property or method");
        } catch (SecurityException e) {
            throw new MSLAException("Option '" + fieldName + "' can not be set", e);
        }
    }

    /**
     * Internal machine defaults class.
     */
    public static class Defaults implements MSLAFileDefaults {
        @Getter private final String machineManufacturer;
        @Getter private final String machineName;
        @Getter private final String fileExtension;
        @Getter private final Class<? extends MSLAFile<?>> fileClass;
        @Getter private final MSLAFileProps fileProps = new MSLAFileProps();
        private final MachineOptions fileOptions = new MachineOptions();
        @Getter private LayerDefaults layerDefaults;

        public Defaults(String manufacturer, String name, String extension, Class<? extends MSLAFile<?>> fileClass) {
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
        @Override public MSLADefaultsParams getFileOption(MSLAOptionName name) { return fileOptions.getOption(name); }
        @Override public boolean hasFileOption(MSLAOptionName name) { return fileOptions.getOption(name) != null; }

        @Override
        public <T extends Serializable> Serializable displayToRaw(
                MSLAOptionName name,
                T optionValue,
                Class<? extends T> rawType) throws MSLAException
        {
            if (optionValue == null) return null;
            if (rawType == null) return optionValue;
            if (!hasFileOption(name)) throw new MSLAException("No defaults defined for option '" + name + "', can't set value");
            return fileOptions.getOption(name).displayToRaw(rawType, optionValue);
        }

        @Override
        public <T> String rawToDisplay(MSLAOptionName name, Serializable optionValue) throws MSLAException {
            if (optionValue == null) return null;
            if (!hasFileOption(name)) throw new MSLAException("No defaults defined for option '" + name + "', can't get display value");
            return fileOptions.getOption(name).rawToDisplay(optionValue.getClass(), optionValue);
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
                var defaultOption = fileOptions.getOption(option);
                if (defaultOption != null) {
                    logger.fine("Setting default option '" + option.getName() + "' to " + defaultOption);
                    instance.setFieldDefault(fields, blockOptions.get(option), defaultOption);
                }
            }
        }

        @Override
        public MSLADefaultsParams getParameters(String blockName, MSLAOptionName fieldName) {
            return fileOptions.getOption(fieldName);
        }
    }

    /**
     * Internal machine layer-specific defaults class.
     */
    public static class LayerDefaults implements MSLALayerDefaults {
        private final MachineOptions options = new MachineOptions();

        @Override
        public void setFields(MSLAFileBlockFields fields) throws MSLAException {
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

    private MachineDefaults() throws MSLAException {
        readPrinterDefaultsFile();
    }

    void readPrinterDefaultsFile() throws MSLAException {
        var xmlReader = new SAXReader();
        try(var resource = getClass().getClassLoader().getResourceAsStream("printer_defaults.xml")) {
            if (resource == null) throw new MSLAException("Printer defaults not found");
            var input = xmlReader.read(resource);
            var root = input.getRootElement();
            var it = root.elementIterator("printer");
            while (it.hasNext()) {
                var def = parsePrinter(it.next());
                printers.put(def.getMachineFullName(), def);
            }
        } catch (DocumentException | IOException e) {
            throw new MSLAException("Error reading printer description XML file", e);
        }
    }

    /**
     * Returns a set of machines that supports given file class.
     * @param fileClass an MSLAFile class
     */
    public final Set<String> getMachines(Class<? extends MSLAFile<?>> fileClass) {
        return printers.keySet().stream().filter(printerName ->
                fileClass.getSimpleName().equals(printers.get(printerName).fileClass.getSimpleName())
        ).collect(Collectors.toSet());
    }

    /**
     * Returns an optional of 'Defaults' object by given machine name.
     * @param name machine full name
     */
    public final Optional<MSLAFileDefaults> getMachineDefaults(String name) {
        return Optional.ofNullable(printers.get(name));
    }

    /**
     * Returns a set of machines that supports given file.
     * @param file an MSLAFile instance
     */
    public final List<MSLAFileDefaults> getMachineDefaults(MSLAFile<?> file) {
        return printers.keySet().stream()
                .filter(printerName -> file.isMachineValid(printers.get(printerName)))
                .map(printers::get)
                .collect(Collectors.toList());
    }

    /**
     * Returns 'Defaults' for all supported machines
     */
    public final List<MSLAFileDefaults> getMachineDefaults() {
        return printers.keySet().stream().map(printers::get).collect(Collectors.toList());
    }

    private void parseFileProperties(Defaults defaults, Element fileOptionsElement) {
        logger.fine("Getting file properties...");
        for (var it = fileOptionsElement.elementIterator("property"); it.hasNext();) {
            var option = it.next();

            var name = option.attributeValue("name");
            var value = option.attributeValue("value");
            logger.fine("Got file property '" + name + "' = '" + value + "'");
            defaults.fileProps.put(name, new MachineProperty(value));
        }
    }

    private void parseFileOptions(Defaults defaults, Element optionsBlockElement)
            throws MSLAException
    {
        logger.fine("Getting file option...");
        for (var it = optionsBlockElement.elementIterator("option"); it.hasNext();) {
            var option = it.next();

            // Just for logging
            var name = option.attributeValue("name");
            var value = option.attributeValue("value");
            logger.fine("Got file option '" + name + "' = '" + value + "'");

            defaults.fileOptions.addFromXMLElement(option);
        }
    }

    private void parseLayerOptions(Defaults defaults, Element layerOptionsElement) throws MSLAException {
        logger.fine("Getting layer options...");
        defaults.layerDefaults = new LayerDefaults();
        for (var it = layerOptionsElement.elementIterator("option"); it.hasNext();)
            defaults.layerDefaults.options.addFromXMLElement(it.next());
    }

    private Defaults parsePrinter(Element printerElement) throws MSLAException {
        var mandatoryPropNames = List.of(
                "name", "manufacturer", "extension", "resolution",
                "displayWidth", "displayHeight", "machineZ", "pixelSize");
        var mandatoryProps = new HashMap<String, String>();
        for (var prop : mandatoryPropNames) {
            var value = printerElement.attributeValue(prop);
            if (value == null) throw new MSLAException("Attribute " + prop +  " is not set, but required");
            mandatoryProps.put(prop, value);
        }

        var file = printerElement.attributeValue("file");
        if (file == null) throw new MSLAException("Printer file format is not set");
        logger.info("Loading defaults for '" + mandatoryProps.get("manufacturer") + " " + mandatoryProps.get("name") + "' (" + file + ")");

        try {
            var fileClass = getClass().getClassLoader().loadClass(file);
            if (MSLAFile.class.isAssignableFrom(fileClass)) {
                @SuppressWarnings("unchecked")
                var def = new Defaults(
                        mandatoryProps.get("manufacturer"),
                        mandatoryProps.get("name"),
                        mandatoryProps.get("extension"),
                        (Class<? extends MSLAFile<?>>) fileClass
                );

                /*
                 * Populate mandatory properties
                 */
                def.fileProps.put("DisplayWidth", new MachineProperty(mandatoryProps.get("displayWidth")));
                def.fileProps.put("DisplayHeight", new MachineProperty(mandatoryProps.get("displayHeight")));
                def.fileProps.put("MachineZ", new MachineProperty(mandatoryProps.get("machineZ")));
                def.fileProps.put("Resolution", new MachineProperty(mandatoryProps.get("resolution")));
                def.fileProps.put("PixelSize", new MachineProperty(mandatoryProps.get("pixelSize")));

                var elem = printerElement.elements("file_properties");
                if (elem.size() > 1) throw new MSLAException("Can't be more than one <file_properties> tag for printer '" +
                        mandatoryProps.get("name") + "'");
                else if (elem.size() == 1) parseFileProperties(def, elem.get(0));

                elem = printerElement.elements("file_options");
                if (elem.size() > 1) throw new MSLAException("Can't be more than one <file_options> for printer '" +
                        mandatoryProps.get("name") + "'");
                else if (elem.size() == 1) parseFileOptions(def, elem.get(0));

                elem = printerElement.elements("layer_options");
                if (elem.size() > 1) throw new MSLAException("Can't be more than one <layer_options> for printer '" +
                        mandatoryProps.get("name") + "'");
                else if (elem.size() == 1) parseLayerOptions(def, elem.get(0));

                return def;
            } else throw new MSLAException("File class is not an MSLAFile");
        } catch (ClassNotFoundException e) {
            throw new MSLAException("Can't find an mSLA file class '" + file + "'", e);
        }
    }
}
