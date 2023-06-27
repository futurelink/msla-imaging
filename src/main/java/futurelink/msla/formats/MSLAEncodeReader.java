package futurelink.msla.formats;

import java.io.IOException;
import java.io.InputStream;

public interface MSLAEncodeReader {

    /**
     * Should be implemented to return MSLAFileCodec object.
     * @return codec to be used
     */
    MSLAFileCodec getCodec();

    /**
     * Read layer data.
     * @param layerNumber layer number being read
     * @return InputStream of encoded data ready to be placed into an output file.
     */
    InputStream read(int layerNumber) throws IOException;

    /**
     * Method is being called when encoding process starts.
     * @param layerNumber layer number to be encoded
     */
    void onStart(int layerNumber);

    /**
     * Method is being called when encoding process ends.
     * @param layerNumber layer number being that had been being encoded
     */
    void onFinish(int layerNumber, int pixels, int length);

    /**
     * Method is being called when encoding process encounters an error.
     * @param layerNumber layer number that had been being encoded
     */
    void onError(int layerNumber, String error);
}
