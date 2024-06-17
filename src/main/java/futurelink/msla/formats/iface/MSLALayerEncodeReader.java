package futurelink.msla.formats.iface;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.utils.Size;

import java.awt.image.BufferedImage;

/**
 * Generic interface for mSLA layer reader.
 * Method read() returns raw bytes that should be fed to encoder in order to get
 * proper {@link  MSLALayerEncodeOutput} that can be added as layer data that depends on file format.
 */
public interface MSLALayerEncodeReader {

    enum ReadDirection { READ_ROW, READ_COLUMN }

    /**
     * @return image resolution object
     */
    Size getResolution();

    /**
     * Gets input data size after read.
     */
    int getSize();

    /**
     * Sets read direction row-by-row or column-by-column.
     * @param readDirection defines read order row-by-row or column-by-column
     */
    void setReadDirection(ReadDirection readDirection);

    /**
     * Read RAW layer data.
     * @param layerNumber layer number being read
     * @return InputStream of encoded data ready to be placed into an output file.
     */
    BufferedImage read(int layerNumber) throws MSLAException;

    /**
     * Method is being called when encoding process starts.
     * @param layerNumber layer number to be encoded
     */
    void onStart(int layerNumber) throws MSLAException;

    /**
     * Method is being called when encoding process ends.
     * @param layerNumber layer number being that had been being encoded
     */
    void onFinish(int layerNumber, int pixels, MSLALayerEncodeOutput<?> output) throws MSLAException;

    /**
     * Method is being called when encoding process encounters an error.
     * @param layerNumber layer number that had been being encoded
     */
    void onError(int layerNumber, String error) throws MSLAException;
}
