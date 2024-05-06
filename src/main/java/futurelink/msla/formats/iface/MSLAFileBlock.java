package futurelink.msla.formats.iface;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.utils.FileFieldsReader;
import futurelink.msla.formats.utils.FileFieldsWriter;

import java.io.*;

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
}
