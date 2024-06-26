package io.msla.formats.chitubox.common.tables;

import io.msla.formats.iface.MSLAFileBlock;
import io.msla.formats.io.FileFieldsIO;
import lombok.Getter;

@Getter
abstract public class CTBFileBlock implements MSLAFileBlock {
    private final int Version;
    public CTBFileBlock(int version) {
        Version = version;
    }
    @Override public FileFieldsIO.Endianness getEndianness() { return FileFieldsIO.Endianness.LittleEndian; }
}
