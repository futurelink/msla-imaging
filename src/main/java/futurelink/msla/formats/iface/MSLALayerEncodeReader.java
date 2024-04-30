package futurelink.msla.formats.iface;

import futurelink.msla.formats.utils.Size;

import java.io.IOException;
import java.io.InputStream;

/**
 * Generic interface for mSLA layer encoder.
 * Common way of using it is:
 *  - create a MSLAFile object
 *  - set necessary MSLAFile options
 *  - implement encoder process in-line
 *  - use MSLAFile's addLayer method using encoder object
 */
public interface MSLALayerEncodeReader {

    enum ReadDirection { READ_ROW, READ_COLUMN };

    /**
     * Should be implemented to return MSLAFileCodec object.
     * @return codec to be used
     */
    Class<? extends MSLAFileCodec> getCodec();

    /**
     * @return image resolution object
     */
    Size getResolution();

    /**
     * Read layer data.
     * @param layerNumber layer number being read
     * @return InputStream of encoded data ready to be placed into an output file.
     */
    InputStream read(int layerNumber, ReadDirection direction) throws IOException;

    /**
     * Method is being called when encoding process starts.
     * @param layerNumber layer number to be encoded
     */
    void onStart(int layerNumber) throws IOException;

    /**
     * Method is being called when encoding process ends.
     * @param layerNumber layer number being that had been being encoded
     */
    void onFinish(int layerNumber, int pixels, int length) throws IOException;

    /**
     * Method is being called when encoding process encounters an error.
     * @param layerNumber layer number that had been being encoded
     */
    void onError(int layerNumber, String error) throws IOException;
}
