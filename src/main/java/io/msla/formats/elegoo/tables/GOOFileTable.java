package io.msla.formats.elegoo.tables;

import io.msla.formats.iface.MSLAFileBlock;
import io.msla.formats.io.FileFieldsException;
import io.msla.formats.io.FileFieldsIO;

/**
 * ELEGOO GOO file format block generic for all its derivatives.
 */
public abstract class GOOFileTable implements MSLAFileBlock {
    @Override
    public int getDataFieldOffset(String fieldName) throws FileFieldsException {
        return FileFieldsIO.getBlockLength(this, fieldName);
    }

    @Override public int getDataLength() throws FileFieldsException {
        return FileFieldsIO.getBlockLength(this);
    }
}
