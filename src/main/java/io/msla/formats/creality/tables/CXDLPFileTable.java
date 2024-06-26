package io.msla.formats.creality.tables;

import io.msla.formats.iface.MSLAFileBlock;
import io.msla.formats.io.FileFieldsException;
import io.msla.formats.io.FileFieldsIO;

public abstract class CXDLPFileTable implements MSLAFileBlock {
    @Override
    public int getDataFieldOffset(String fieldName) throws FileFieldsException {
        return FileFieldsIO.getBlockLength(this.getBlockFields(), fieldName);
    }
}
