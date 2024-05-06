package futurelink.msla.formats.iface;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.utils.Size;

/**
 * Generic interface for mSLA layer writer.
 */
public interface MSLALayerDecodeWriter {

    enum WriteDirection { WRITE_ROW, WRITE_COLUMN }

    /**
     * Must be implemented to get printer specific image size.
     * @return machine display resolution
     */
    Size getLayerResolution();

    /**
     * Must be implemented to write sequence of pixels of the same color into some sort of output image.
     * @param color pixel color
     * @param position position in a sequence of pixels
     * @param length number of pixels of the same color
     * @param direction specifies if stripes should be aligned vertically
     */
    void stripe(int layerNumber, int color, int position, int length, WriteDirection direction) throws MSLAException;

    /**
     * Method is being called when decoding process starts.
     * @param layerNumber layer number that is to be decoded
     */
    void onStart(int layerNumber);

    /**
     * Method is being called when decoding process ends.
     * @param layerNumber layer number that had been being decoded
     * @param nonZeroPixels a number of exposed pixels in decoded data
     */
    void onFinish(int layerNumber, int nonZeroPixels) throws MSLAException;

    /**
     * Method is being called when decoding process encounters an error.
     * @param layerNumber layer number that had been being decoded
     */
    void onError(int layerNumber, String error) throws MSLAException;
}
