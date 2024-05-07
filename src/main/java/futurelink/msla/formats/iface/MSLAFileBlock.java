package futurelink.msla.formats.iface;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.iface.annotations.MSLAOption;
import futurelink.msla.formats.iface.annotations.MSLAOptionContainer;
import futurelink.msla.formats.utils.FileFieldsReader;
import futurelink.msla.formats.utils.FileFieldsWriter;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;

public interface MSLAFileBlock {

    /**
     * Calculates and returns block length in bytes.
     * @return block length in bytes.
     */
    int getDataLength();

    MSLAFileBlockFields getFields();

    default long read(FileInputStream stream, long position) throws MSLAException {
        try {
            var fc = stream.getChannel();
            fc.position(position);
            var reader = new FileFieldsReader(stream, FileFieldsReader.Endianness.BigEndian);
            return reader.read(getFields());
        } catch (IOException e) { throw new MSLAException("Error reading " + this.getClass().getName() + " table", e); }
    }

    default void write(OutputStream stream) throws MSLAException {
        try {
            var writer = new FileFieldsWriter(stream, FileFieldsWriter.Endianness.BigEndian);
            writer.write(getFields());
        } catch (IOException e) {
            throw new MSLAException("Error writing " + this.getClass().getName() + " table", e);
        }
    }

    default HashMap<String, Class<?>> getOptions() {
        var a = getClass().getAnnotation(MSLAOptionContainer.class);
        if (a != null) {
            var optionContainerClass = a.value() != null ? a.value() : getClass();
            var optionsMap = new HashMap<String, Class<?>>();
            Arrays.stream(optionContainerClass.getDeclaredFields())
                    .filter((f) -> f.getAnnotation(MSLAOption.class) != null)
                    .forEach((f) -> optionsMap.put(f.getName(), f.getType()));
            return optionsMap;
        }
        return null;
    }
}
