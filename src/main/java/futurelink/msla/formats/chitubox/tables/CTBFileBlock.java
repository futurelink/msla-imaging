package futurelink.msla.formats.chitubox.tables;

import futurelink.msla.formats.iface.MSLAFileBlock;
import lombok.Getter;

@Getter
abstract public class CTBFileBlock implements MSLAFileBlock {
    private final int Version;
    public CTBFileBlock(int version) {
        Version = version;
    }
}
