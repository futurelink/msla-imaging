package futurelink.msla.formats.iface;

import futurelink.msla.formats.MSLAException;

import java.io.*;

public interface MSLAFileBlock {

    /**
     * Calculates and returns block length in bytes.
     * @return block length in bytes.
     */
    int getDataLength();
    void read(FileInputStream stream, int position) throws MSLAException, IOException;
    void write(OutputStream stream) throws MSLAException, IOException;
}
