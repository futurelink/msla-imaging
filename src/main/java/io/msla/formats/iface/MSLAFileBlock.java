package io.msla.formats.iface;

import io.msla.formats.MSLAException;
import io.msla.formats.io.FileFieldsException;
import io.msla.formats.io.FileFieldsIO;
import io.msla.formats.io.FileFieldsReader;
import io.msla.formats.io.FileFieldsWriter;

import java.io.*;

public interface MSLAFileBlock {

    /**
     * Returns block name.
     */
    String getName();

    /**
     * Calculates and returns block length in bytes.
     * @return block length in bytes.
     */
    int getDataLength() throws FileFieldsException;

    /**
     * Gets field offset based on field name and block length.
     * @param fieldName a field that offset is required
     */
    int getDataFieldOffset(String fieldName) throws FileFieldsException;

    /**
     * Gets block fields object.
     */
    MSLAFileBlockFields getBlockFields();

    /**
     * Should return endianness of the data either LittleEndian or BigEndian.
     * It's a BigEndian by default.
     */
    default FileFieldsIO.Endianness getEndianness() { return FileFieldsIO.Endianness.BigEndian; }

    default void beforeRead() {}
    default void afterRead() throws MSLAException {}
    default void beforeWrite() throws MSLAException {}

    default long read(DataInputStream input, long position) throws MSLAException {
        try {
            var reader = new FileFieldsReader(input, getEndianness());
            return reader.read(this, position);
        } catch (FileFieldsException e) {
            throw new MSLAException("Error reading '" + this.getClass().getSimpleName() + "' table", e);
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
