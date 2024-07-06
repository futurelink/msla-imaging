package io.msla.utils.defaults;

import io.msla.formats.MSLAException;
import io.msla.formats.iface.*;
import io.msla.formats.iface.options.MSLAOption;
import io.msla.formats.iface.options.MSLAOptionName;
import io.msla.utils.defaults.props.MachineProperty;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.IOException;
import java.io.InputStream;
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
    private final HashMap<String, MSLAFileDefaults> printers = new HashMap<>();
    private static MachineDefaults instance;

    private MSLAFileDefaults currentDefaults = null;

    public static MachineDefaults getInstance() throws MSLAException {
        if (instance == null) { instance = new MachineDefaults(); }
        return instance;
    }

    HashMap<MSLAOptionName, String> getBlockFieldsOptions(MSLAFileBlockFields fields) {
        var ret = new HashMap<MSLAOptionName, String>();
        var props = fields.getClass().getDeclaredFields();
        for (var prop : props)
            if (prop.isAnnotationPresent(MSLAOption.class))
                ret.put(prop.getAnnotation(MSLAOption.class).value(), prop.getName());
        return ret;
    }

    void setFieldDefault(MSLAFileBlockFields fields, String fieldName, MSLADefaultsParams defaultOption)
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



    private MachineDefaults() throws MSLAException {
        readPrinterDefaultsFile();
    }

    /**
     * Rads printer defaults XML from input stream.
     * @param stream input data stream
     */
    void readPrinterDefaults(InputStream stream) throws MSLAException, DocumentException {
        var xmlReader = new SAXReader();
        try {
            var input = xmlReader.read(stream);
            var root = input.getRootElement();
            var it = root.elementIterator("printer");
            while (it.hasNext()) {
                this.currentDefaults = parsePrinter(it.next());
                printers.put(this.currentDefaults.getMachineFullName(), this.currentDefaults);
            }
        } catch (DocumentException e) {
            throw new MSLAException("Error reading printer defaults", e);
        }
    }

    /**
     * Reads printer defaults XML from internal settings file.
     */
    void readPrinterDefaultsFile() throws MSLAException {
        try(var resource = getClass().getClassLoader().getResourceAsStream("printer_defaults.xml")) {
            if (resource == null) throw new MSLAException("Printer defaults not found");
            readPrinterDefaults(resource);
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
                fileClass.getSimpleName().equals(printers.get(printerName).getFileClass().getSimpleName())
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

    /**
     * Parses file properties.
     */
    private void parseFileProperties(MSLAFileDefaults defaults, Element fileOptionsElement) {
        logger.fine("Getting file properties...");
        for (var it = fileOptionsElement.elementIterator("property"); it.hasNext();) {
            var option = it.next();

            var name = option.attributeValue("name");
            var value = option.attributeValue("value");
            logger.fine("Got file property '" + name + "' = '" + value + "'");
            defaults.getFileProps().put(name, new MachineProperty(value));
        }
    }

    private void parseFileOptions(MachineFileDefaults defaults, Element optionsBlockElement) throws MSLAException {
        logger.fine("Getting file option...");
        for (var option : optionsBlockElement.elements()) {
            try {
                var optionName = MSLAOptionName.valueOf(option.getName());
                var value = option.attributeValue("value");
                logger.fine("Got file option '" + optionName + "' = '" + value + "'");

                defaults.getOptions().addFromXMLElement(optionName, option);
            } catch (IllegalArgumentException e) {
                throw new MSLAException("Invalid file option '" + option.getName() + "'", e);
            }
        }
    }

    private void parseLayerOptions(MachineFileDefaults defaults, Element layerOptionsElement) throws MSLAException {
        logger.fine("Getting layer options...");
        layerOptionsElement.elements();
        for (var element : layerOptionsElement.elements()) {
            try {
                var optionName = MSLAOptionName.valueOf(element.getName());
                defaults.getLayerDefaults().getOptions().addFromXMLElement(optionName, element);
            } catch (IllegalArgumentException e) {
                throw new MSLAException("Invalid layer option '" + element.getName() + "'");
            }
        }
    }

    private MSLAFileDefaults parsePrinter(Element printerElement) throws MSLAException {
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
                var def = new MachineFileDefaults(
                        mandatoryProps.get("manufacturer"),
                        mandatoryProps.get("name"),
                        mandatoryProps.get("extension"),
                        (Class<? extends MSLAFile<?>>) fileClass
                );

                /*
                 * Populate mandatory properties
                 */
                def.getFileProps().put("DisplayWidth", new MachineProperty(mandatoryProps.get("displayWidth")));
                def.getFileProps().put("DisplayHeight", new MachineProperty(mandatoryProps.get("displayHeight")));
                def.getFileProps().put("MachineZ", new MachineProperty(mandatoryProps.get("machineZ")));
                def.getFileProps().put("Resolution", new MachineProperty(mandatoryProps.get("resolution")));
                def.getFileProps().put("PixelSize", new MachineProperty(mandatoryProps.get("pixelSize")));

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
