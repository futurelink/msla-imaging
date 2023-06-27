package futurelink.msla.formats.anycubic.tables;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * "SOFTWARE" section representation.
 * TODO Not implemented yet.
 */
public class PhotonWorkshopFileSoftwareTable extends PhotonWorkshopFileTable {
    public static final String Name = "SOFTWARE";

    public PhotonWorkshopFileSoftwareTable(byte versionMajor, byte versionMinor) {
        super(versionMajor, versionMinor);
    }

    @Override
    int calculateTableLength(byte versionMajor, byte versionMinor) {
        return 0;
    }

    @Override
    public int getDataLength() { return 0; }

    @Override
    public void read(FileInputStream stream, int position) throws IOException {}

    @Override
    public void write(OutputStream stream) throws IOException {}
}
