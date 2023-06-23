package futurelink.msla.formats;

import futurelink.msla.formats.MSLAFileCodec;

import java.io.IOException;

public interface MSLADecodeWriter {

    /**
     * Should be implemented to return MSLAFileCodec object.
     * @return codec to be used
     */
    MSLAFileCodec getCodec();

    /**
     * Must be implemented to write sequence of pixels of the same color into some sort of output image.
     * @param color pixel color
     * @param linearPos position in a sequence of pixels
     * @param count number of pixels of the same color
     */
    void pixels(int color, int linearPos, int count);

    /**
     *
     */
    void onStart();

    /**
     *
     * @param nonZeroPixels
     */
    void onFinish(int nonZeroPixels);
}
