package futurelink.msla.formats.anycubic.tables;

import futurelink.msla.formats.iface.MSLAFileBlock;
import futurelink.msla.formats.utils.fields.FileFieldsException;
import futurelink.msla.formats.utils.fields.FileFieldsIO;
import lombok.Getter;

/**
 * Common class for section representation.
 */
abstract public class PhotonWorkshopFileTable implements MSLAFileBlock {
    public static final int MarkLength = 12;
    public static final float DefaultLiftHeight = 100;

    @Getter protected String Name;
    @Getter protected int TableLength;
    protected byte versionMajor;
    protected byte versionMinor;

    public PhotonWorkshopFileTable(byte versionMajor, byte versionMinor) {
        setVersion(versionMajor, versionMinor);
    }

    public void setVersion(byte versionMajor, byte versionMinor) {
        this.versionMajor = versionMajor;
        this.versionMinor = versionMinor;
    }

    abstract int calculateTableLength();

    @Override public FileFieldsIO.Endianness getEndianness() { return FileFieldsIO.Endianness.LittleEndian; }
    @Override public int getDataLength() throws FileFieldsException {
        return FileFieldsIO.getBlockLength(this);
    }

    @Override
    public int getDataFieldOffset(String fieldName) throws FileFieldsException {
        return FileFieldsIO.getBlockLength(this, fieldName);
    }
}
