package futurelink.msla.formats.iface;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.utils.fields.FileFieldsException;
import futurelink.msla.formats.utils.fields.FileFieldsIO;
import futurelink.msla.formats.utils.fields.FileFieldsReader;
import futurelink.msla.formats.utils.fields.FileFieldsWriter;

import java.io.*;

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

    default long read(DataInputStream input, long position) throws MSLAException {
        try {
            input.reset();
            input.skipBytes((int) position);
            var reader = new FileFieldsReader(input, getEndianness());
            return reader.read(this);
        } catch (IOException | FileFieldsException e) {
            throw new MSLAException("Error reading " + this.getClass().getName() + " table", e);
        }
    }

    default void write(OutputStream stream) throws MSLAException {
        try {
            var writer = new FileFieldsWriter(stream, getEndianness());
            writer.write(this);
        } catch (IOException | FileFieldsException e) {
            throw new MSLAException("Error writing " + this.getClass().getName() + " table", e);
        }
    }
}
