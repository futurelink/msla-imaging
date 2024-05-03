package futurelink.msla.formats.anycubic.tables;

import futurelink.msla.formats.MSLAException;

import java.io.FileInputStream;
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
    int calculateTableLength() {
        return 0;
    }

    @Override
    public int getDataLength() { return 0; }

    @Override
    public void read(FileInputStream stream, int position) throws MSLAException {}

    @Override
    public void write(OutputStream stream) throws MSLAException {}
}
