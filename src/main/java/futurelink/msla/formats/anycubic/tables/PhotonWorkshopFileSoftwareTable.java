package futurelink.msla.formats.anycubic.tables;

import com.google.common.io.LittleEndianDataInputStream;
import com.google.common.io.LittleEndianDataOutputStream;

import java.io.IOException;

/**
 * "SOFTWARE" section representation.
 * TODO Not implemented yet.
 */
public class PhotonWorkshopFileSoftwareTable extends PhotonWorkshopFileTable {
    public static final String Name = "SOFTWARE";

    @Override
    int calculateTableLength(byte versionMajor, byte versionMinor) {
        return 0;
    }

    @Override
    public void read(LittleEndianDataInputStream stream) throws IOException {}

    @Override
    public void write(LittleEndianDataOutputStream stream, byte versionMajor, byte versionMinor) throws IOException {}
}
