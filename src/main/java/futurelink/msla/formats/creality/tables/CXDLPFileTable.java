package futurelink.msla.formats.creality.tables;

import futurelink.msla.formats.iface.MSLAFileBlock;
import futurelink.msla.formats.io.FileFieldsException;
import futurelink.msla.formats.io.FileFieldsIO;

public abstract class CXDLPFileTable implements MSLAFileBlock {
    @Override
    public int getDataFieldOffset(String fieldName) throws FileFieldsException {
        return FileFieldsIO.getBlockLength(this.getBlockFields(), fieldName);
    }
}
