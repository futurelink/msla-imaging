package futurelink.msla.formats.anycubic.tables;

import com.google.common.io.LittleEndianDataInputStream;
import com.google.common.io.LittleEndianDataOutputStream;
import lombok.Getter;

import java.io.IOException;

/**
 * Common class for section representation.
 */
abstract public class PhotonWorkshopFileTable {
    public static final int MarkLength = 12;
    public static final float DefaultLiftHeight = 100;
    @Getter protected String Name;
    @Getter protected int TableLength;

    abstract int calculateTableLength(byte versionMajor, byte versionMinor);
    public int calculateDataLength(byte versionMajor, byte versionMinor) {
        return calculateTableLength(versionMajor, versionMinor) + MarkLength + 4;
    }
    abstract public void read(LittleEndianDataInputStream stream) throws IOException;
    abstract public void write(LittleEndianDataOutputStream stream, byte versionMajor, byte versionMinor) throws IOException;
}
