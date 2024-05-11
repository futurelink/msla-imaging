package futurelink.msla.formats.iface;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.iface.annotations.MSLAOption;
import futurelink.msla.formats.utils.FileFieldsException;
import futurelink.msla.formats.utils.FileFieldsIO;
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
    int getDataLength() throws FileFieldsException;
    int getDataFieldOffset(String fieldName) throws FileFieldsException;

    MSLAFileBlockFields getFileFields();

    default FileFieldsIO.Endianness getEndianness() { return FileFieldsIO.Endianness.BigEndian; }

    default void beforeRead() {}
    default void afterRead() {}
    default void beforeWrite() throws MSLAException {}

    default long read(FileInputStream stream, long position) throws MSLAException {
        try {
            var fc = stream.getChannel();
            fc.position(position);
            var reader = new FileFieldsReader(stream, getEndianness());
            return reader.read(this);
        } catch (IOException | FileFieldsException e) { throw new MSLAException("Error reading " + this.getClass().getName() + " table", e); }
    }

    default void write(OutputStream stream) throws MSLAException {
        try {
            var writer = new FileFieldsWriter(stream, getEndianness());
            writer.write(this);
        } catch (IOException | FileFieldsException e) {
            throw new MSLAException("Error writing " + this.getClass().getName() + " table", e);
        }
    }

    default HashMap<String, String> getOptionNames() {
        var optionsMap = new HashMap<String, String>();
        Arrays.stream(getFileFields().getClass().getDeclaredFields())
                .filter((f) -> f.getAnnotation(MSLAOption.class) != null)
                .forEach((f) -> optionsMap.put(
                        !f.getAnnotation(MSLAOption.class).value().isEmpty() ? f.getAnnotation(MSLAOption.class).value() : f.getName(),
                        f.getName()));
        return optionsMap;
    }

    default HashMap<String, Class<?>> getOptions() {
        var optionsMap = new HashMap<String, Class<?>>();
        Arrays.stream(getFileFields().getClass().getDeclaredFields())
                .filter((f) -> f.getAnnotation(MSLAOption.class) != null)
                .forEach((f) -> optionsMap.put(f.getName(), f.getType()));
            return optionsMap;
    }
}
