package futurelink.msla.formats;

import java.io.*;

public interface MSLAFileBlock {

    /**
     * Calculates and returns block length in bytes.
     * @return block length in bytes.
     */
    int getDataLength();
    void read(FileInputStream stream, int position) throws IOException;
    void write(OutputStream stream) throws IOException;
}
