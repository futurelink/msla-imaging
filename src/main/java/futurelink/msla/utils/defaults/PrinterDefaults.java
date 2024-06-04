package futurelink.msla.utils.defaults;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.iface.*;
import futurelink.msla.utils.Size;
import lombok.Getter;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Printer defaults utility class.
 * Loads printer_defaults.xml file and creates settings for all supported machines.
 */
public class PrinterDefaults {
    private static final Logger logger = Logger.getLogger(PrinterDefaults.class.getName());
    private final HashMap<String, Defaults> printers = new HashMap<>();
    public static PrinterDefaults instance;

    static {
        try { instance = new PrinterDefaults(); } catch (MSLAException e) { throw new RuntimeException(e); }
    }

    /**
     * Internal machine defaults class.
     */
    public static class Defaults implements MSLAFileDefaults {
        @Getter private final String machineManufacturer;
        @Getter private final String machineName;
        @Getter private final String fileExtension;
        @Getter private float pixelSizeUm;
        @Getter private Size resolution;
        @Getter private final MSLAFileProps fileProps = new MSLAFileProps();
        @Getter private final Class<? extends MSLAFile<?>> fileClass;
        @Getter private LayerDefaults layerDefaults;
        private final HashMap<String, PrinterOptionParams> options = new HashMap<>();

        public Defaults(String manufacturer, String name, String extension, Class<? extends MSLAFile<?>> fileClass) {
            this.machineName = name;
            this.machineManufacturer = manufacturer;
            this.fileExtension = extension;
            this.fileClass = fileClass;
        }

        @Override public final String getMachineFullName() { return getMachineManufacturer() + " " + getMachineName(); }

        public final PrinterOptionParams getOptionsBlock(String blockName) {
            if (blockName == null) throw new NullPointerException("Block name can't be null");
            return options.get(blockName);
        }

        public final void setFields(String blockName, MSLAFileBlockFields fields) throws MSLAException {
            var block = getOptionsBlock(blockName);
            if (block == null) throw new MSLAException("Options block '" + blockName + "' does not exist");
            for (var option : block.getOptionKeys()) {
                try {
                    var defaultOption = block.getOption(option);
                    var field = fields.getClass().getDeclaredField(option);
                    var type = field.getType();
                    Method setter = null;
                    try { setter = fields.getClass().getDeclaredMethod("set" + option, type); }
                    catch (NoSuchMethodException ignored) {}
                    if (setter != null) {
                        logger.fine("Calling setter for default '" + defaultOption.getDefaultValue() + "' " + option + " of type " + type.getSimpleName());
                        try {
                            setter.invoke(fields, defaultOption.getAsType(type.getSimpleName()));
                        } catch (Exception e) {
                            throw new MSLAException("Option " + blockName + ":" + option + " can't be set", e);
                        }
                    } else {
                        logger.fine("Setting default '" + defaultOption.getDefaultValue() + "' " + option + " of type " + type.getSimpleName());
                        try {
                            field.setAccessible(true);
                            field.set(fields, defaultOption.getAsType(type.getSimpleName()));
                        } catch (Exception e) {
                            throw new MSLAException("Option " + blockName + ":" + option + " can't be set", e);
                        } finally {
                            field.setAccessible(false);
                        }
                   }
                } catch (NoSuchFieldException e) {
                    throw new MSLAException("Option " + blockName + ":" + option + " is not supported", e);
                } catch (SecurityException e) {
                    throw new MSLAException("Option " + blockName + ":" + option + " can not be set", e);
                }
            }
        }

        @Override
        public MSLADefaultsParams getParameters(String blockName, String fieldName) {
            return options.get(blockName).getOption(fieldName);
        }
    }

    /**
     * Internal machine layer-specific defaults class.
     */
    public static class LayerDefaults implements MSLALayerDefaults {
        private final PrinterOptionParams options = new PrinterOptionParams();

        @Override
        public void setFields(String blockName, MSLAFileBlockFields fields) throws MSLAException {
            // TODO implement this
        }

        @Override
        public MSLADefaultsParams getParameters(String blockName, String fieldName) {
            return options.getOption(fieldName);
        }
    }

    private PrinterDefaults() throws MSLAException {
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

    public final Optional<Defaults> getPrinter(String name) { return Optional.ofNullable(printers.get(name)); }

    public final Set<String> getSupportedPrinters(Class<? extends MSLAFile<?>> cls) {
        return printers.keySet().stream().filter(printerName ->
                cls.getSimpleName().equals(printers.get(printerName).fileClass.getSimpleName())
        ).collect(Collectors.toSet());
    }

    public final List<MSLAFileDefaults> getSuitableDefaults(MSLAFile<?> file) {
        return printers.keySet().stream()
                .filter(printerName -> file.isMachineValid(printers.get(printerName)))
                .map(printers::get)
                .collect(Collectors.toList());
    }

    private void parseOptionsBlockElement(Defaults defaults, String optionsBlockName, Element optionsBlockElement)
            throws MSLAException
    {
        logger.fine("Getting options into '" + optionsBlockName + "'");
        var opts = new PrinterOptionParams();
        defaults.options.put(optionsBlockName, opts);
        for (var it = optionsBlockElement.elementIterator("option"); it.hasNext();) {
            var option = it.next();
            opts.addFromXMLElement(option);

            // Save special options, no matter where they were located,
            // otherwise process standard options.
            var name = option.attributeValue("name");
            var value = option.attributeValue("value");
            logger.fine("Got option '" + name + "' = '" + value + "'");
            if (name.equals("PixelSizeUm")) defaults.pixelSizeUm = Float.parseFloat(value);
            if (name.equals("Resolution")) defaults.resolution = Size.parseSize(value);
        }
    }

    private void parseLayerOptions(Defaults defaults, Element layerOptionsElement) throws MSLAException {
        defaults.layerDefaults = new LayerDefaults();
        for (var it = layerOptionsElement.elementIterator("option"); it.hasNext();) {
            defaults.layerDefaults.options.addFromXMLElement(it.next());
        }
    }

    private void parseFileOptions(Defaults defaults, Element fileOptionsElement) {
        for (var it = fileOptionsElement.elementIterator("option"); it.hasNext();) {
            var option = it.next();
            var name = option.attributeValue("name");
            var value = option.attributeValue("value");
            logger.fine("Got file option '" + name + "' = '" + value + "'");
            defaults.fileProps.put(name, value);
        }
    }

    private Defaults parsePrinter(Element printerElement) throws MSLAException {
        var name = printerElement.attributeValue("name");
        var manufacturer = printerElement.attributeValue("manufacturer");
        var extension = printerElement.attributeValue("extension");
        var file = printerElement.attributeValue("file");
        if (name == null) throw new MSLAException("Printer name is not set");
        if (manufacturer == null) throw new MSLAException("Printer manufacturer is not set");
        if (extension == null) throw new MSLAException("Printer file extension is not set");
        if (file == null) throw new MSLAException("Printer file format is not set");
        logger.info("Loading defaults for '" + manufacturer + " " + name + "' (" + file + ")");

        try {
            var fileClass = ClassLoader.getSystemClassLoader().loadClass(file);
            if (!MSLAFile.class.isAssignableFrom(fileClass)) throw new MSLAException("File class is not an MSLAFile");
            var def = new Defaults(manufacturer, name, extension, (Class<? extends MSLAFile<?>>) fileClass);
            var elem = printerElement.elements("file_options");
            if (elem.size() > 1) throw new MSLAException("Can't be more than one <file_options> tag for printer '" + name + "'");
            else if (elem.size() == 1) parseFileOptions(def, elem.get(0));

            elem = printerElement.elements("layer_options");
            if (elem.size() > 1) throw new MSLAException("Can't be more than one <layer_options> for printer '" + name + "'");
            else if (elem.size() == 1) parseLayerOptions(def, elem.get(0));

            for (var it = printerElement.elementIterator("options"); it.hasNext();) {
                var optionsBlockElement = it.next();
                var optionsBlockName = optionsBlockElement.attributeValue("name");
                if (optionsBlockName == null) throw new MSLAException("<options> must have name attribute");
                parseOptionsBlockElement(def, optionsBlockName, optionsBlockElement);
            }

            return def;
        } catch (ClassNotFoundException e) {
            throw new MSLAException("Can't find an mSLA file class '" + file + "'", e);
        }
    }
}
