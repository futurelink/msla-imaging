package futurelink.msla.formats.utils;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.iface.MSLAFile;
import futurelink.msla.formats.iface.MSLAFileBlockFields;
import futurelink.msla.formats.iface.MSLAFileDefaults;
import lombok.Getter;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class PrinterDefaults {
    private static final Logger logger = Logger.getLogger(PrinterDefaults.class.getName());
    private final HashMap<String, Defaults> printers = new HashMap<>();
    public static PrinterDefaults instance;

    static {
        try { instance = new PrinterDefaults(); } catch (MSLAException e) { throw new RuntimeException(e); }
    }

    @Getter
    public static class DefaultOptions {
        private final HashMap<String, String> options = new HashMap<>();
    }

    public static class Defaults implements MSLAFileDefaults {
        @Getter private final String machineManufacturer;
        @Getter private final String machineName;
        @Getter private final String fileExtension;
        @Getter private final String fileHandler;
        @Getter private float pixelSizeUm;
        @Getter private Size resolution;
        @Getter private final HashMap<String, String> fileOptions = new HashMap<>();
        private final HashMap<String, DefaultOptions> options = new HashMap<>();

        public Defaults(String manufacturer, String name, String extension, String fileHandler) {
            this.machineName = name;
            this.machineManufacturer = manufacturer;
            this.fileExtension = extension;
            this.fileHandler = fileHandler;
        }

        public final String getMachineFullName() {
            return machineManufacturer + " " + machineName;
        }

        @Override
        public String getName() { return getMachineFullName(); }

        @Override public Integer getOptionInt(String name) {
            return Integer.parseInt(getOptionsBlock(null).get(name));
        }
        @Override public Byte getOptionByte(String blockName, String name) {
            return Byte.parseByte(getOptionsBlock(blockName).get(name));
        }
        @Override public Short getOptionShort(String name) {
            return Short.parseShort(getOptionsBlock(null).get(name));
        }
        @Override public String getOptionString(String name) {
            return getOptionsBlock(null).get(name);
        }

        public final HashMap<String, String> getOptionsBlock(String blockName) {
            if (blockName == null) return fileOptions;
            var block = options.get(blockName);
            return block != null ? block.getOptions() : new HashMap<>();
        }

        private Object transformToType(String val, String type) {
            return switch (type) {
                case "int", "Integer" -> Integer.parseInt(val);
                case "float", "Float" -> Float.parseFloat(val);
                case "double", "Double" -> Double.parseDouble(val);
                case "byte", "Byte" ->  Byte.parseByte(val);
                case "short", "Short" -> Short.parseShort(val);
                case "Size" -> Size.parseSize(val);
                default -> val;
            };
        }

        public final void setFields(String blockName, MSLAFileBlockFields fields) throws MSLAException {
            var block = getOptionsBlock(blockName);
            if (block == null) throw new MSLAException("Options block '" + blockName + "does not exist");
            for (var option : block.keySet()) {
                try {
                    var val = block.get(option);
                    var field = fields.getClass().getDeclaredField(option);
                    var type = field.getType();
                    Method setter = null;
                    try { setter = fields.getClass().getDeclaredMethod("set" + option, type); }
                    catch (NoSuchMethodException ignored) {}
                    if (setter != null) {
                        logger.fine("Calling setter for default " + option + " of type " + type);
                        setter.invoke(fields, transformToType(val, type.getSimpleName()));
                    } else {
                        field.setAccessible(true);
                        logger.fine("Setting default " + option + " of type " + type);
                        field.set(fields, transformToType(val, type.getSimpleName()));
                        field.setAccessible(false);
                   }
                } catch (NoSuchFieldException e) {
                    throw new MSLAException("Option " + blockName + ":" + option + " is not supported", e);
                } catch (SecurityException | IllegalAccessException | InvocationTargetException e) {
                    throw new MSLAException("Option " + blockName + ":" + option + " can not be set", e);
                }
            }
        }
    }

    private PrinterDefaults() throws MSLAException {
        var resource = getClass().getClassLoader().getResource("printer_defaults.xml");
        if (resource == null) throw new MSLAException("Printer defaults not found");
        var xmlReader = new SAXReader();
        try {
            var input = xmlReader.read(resource.getFile());
            var root = input.getRootElement();
            var it = root.elementIterator("printer");
            while (it.hasNext()) {
                var def = parsePrinter(it.next());
                printers.put(def.getMachineFullName(), def);
            }
        } catch (DocumentException e) {
            throw new MSLAException("Error reading printer description XML file", e);
        }
    }

    public final Defaults getPrinter(String name) { return printers.get(name); }
    public final Set<String> getSupportedPrinters(Class<? extends MSLAFile<?>> cls) {
        return printers.keySet().stream().filter(printerName ->
                cls.getSimpleName().equals(printers.get(printerName).fileHandler)
        ).collect(Collectors.toSet());
    }

    private void parseOptionsBlockElement(Defaults defaults, String optionsBlockName, Element optionsBlockElement) {
        logger.fine("Getting options into '" + optionsBlockName + "'");
        var opts = new DefaultOptions();
        defaults.options.put(optionsBlockName, opts);
        for (var it = optionsBlockElement.elementIterator("option"); it.hasNext();) {
            var option = it.next();
            var name = option.attributeValue("name");
            var value = option.attributeValue("value");
            logger.fine("Got option '" + name + "' = '" + value + "'");
            opts.getOptions().put(name, value);

            // Save special options, no matter where they were located
            if (name.equals("PixelSizeUm")) defaults.pixelSizeUm = Float.parseFloat(value);
            if (name.equals("Resolution")) defaults.resolution = Size.parseSize(value);
        }
    }

    private void parseFileOptions(Defaults defaults, Element fileOptionsElement) {
        for (var it = fileOptionsElement.elementIterator("option"); it.hasNext();) {
            var option = it.next();
            var name = option.attributeValue("name");
            var value = option.attributeValue("value");
            logger.fine("Got file option '" + name + "' = '" + value + "'");
            defaults.fileOptions.put(name, value);
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

        var def = new Defaults(manufacturer, name, extension, file);
        var elem = printerElement.elements("file_options");
        if (elem.size() > 1) throw new MSLAException("<file_options> tag is incorrect for printer '" + name + "'");
        else if (elem.size() == 1) parseFileOptions(def, elem.get(0));

        for (var it = printerElement.elementIterator("options"); it.hasNext();) {
            var optionsBlockElement = it.next();
            var optionsBlockName = optionsBlockElement.attributeValue("name");
            if (optionsBlockName == null) throw new MSLAException("<options> must have name attribute");
            parseOptionsBlockElement(def, optionsBlockName, optionsBlockElement);
        }

        return def;
    }
}
