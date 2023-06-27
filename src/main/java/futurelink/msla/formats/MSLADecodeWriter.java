package futurelink.msla.formats;

import futurelink.msla.formats.utils.Size;

import java.io.IOException;

public interface MSLADecodeWriter {

    /**
     * Should be implemented to return MSLAFileCodec object.
     * @return codec to be used
     */
    MSLAFileCodec getCodec();

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
     * @param vertical specifies if stripes should be aligned vertically
     */
    void stripe(int layerNumber, int color, int position, int length, boolean vertical);

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
    void onFinish(int layerNumber, int nonZeroPixels) throws IOException;

    /**
     * Method is being called when decoding process encounters an error.
     * @param layerNumber layer number that had been being decoded
     */
    void onError(int layerNumber, String error);
}
