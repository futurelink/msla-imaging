package io.msla.formats.anycubic.tables;

import io.msla.formats.iface.MSLAFileBlock;
import io.msla.formats.io.FileFieldsException;
import io.msla.formats.io.FileFieldsIO;
import lombok.Getter;

/**
 * Abstract class for section representation.
 */
@Getter
abstract public class PhotonWorkshopFileTable implements MSLAFileBlock {
    public static final int MarkLength = 12;

    protected String Name;
    protected int TableLength;
    protected byte VersionMajor;
    protected byte VersionMinor;

    public PhotonWorkshopFileTable(byte versionMajor, byte versionMinor) {
        setVersion(versionMajor, versionMinor);
    }

    public void setVersion(byte versionMajor, byte versionMinor) {
        this.VersionMajor = versionMajor;
        this.VersionMinor = versionMinor;
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
