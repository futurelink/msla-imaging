package futurelink.msla.formats.iface;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.MSLALayerDecoders;
import futurelink.msla.formats.MSLALayerEncoders;
import futurelink.msla.formats.MSLAOptionMapper;
import futurelink.msla.formats.utils.Size;

import java.io.IOException;
import java.io.OutputStream;

/**
 * General interface for all MSLA printer files.
 */
public interface MSLAFile {
    /**
     * Returns {@code MSLAFileCodec} codec class.
     * @return
     */
    Class<? extends MSLAFileCodec> getCodec();

    /**
     * Returns {@code MSLAPreview} object.
     * @return
     */
    MSLAPreview getPreview();

    /**
     * Updates preview image.
     * @throws MSLAException
     */
    void updatePreviewImage() throws MSLAException;

    /**
     * Returns DPI value of a file.
     * @return
     */
    float getDPI();

    /**
     * Returns file layer size.
     * @return
     */
    Size getResolution();

    /**
     * Returns pixel size in um.
     * @return
     */
    float getPixelSizeUm();

    /**
     * Returns file layers count.
     * @return
     */
    int getLayerCount();

    /**
     * Adds new empty layer to a file.
     * @param encoders
     * @return
     * @throws MSLAException
     */
    boolean addLayer(MSLALayerEncodeReader reader, MSLALayerEncoders encoders) throws MSLAException;

    /**
     * Adds new layer to a file using {@code MSLAEncodeReader} reader and parameters.
     * @param encoders
     * @param layerHeight
     * @param exposureTime
     * @param liftSpeed
     * @param liftHeight
     * @return
     * @throws MSLAException
     * @throws IOException
     */
    boolean addLayer(MSLALayerEncodeReader reader, MSLALayerEncoders encoders, float layerHeight,
                     float exposureTime, float liftSpeed, float liftHeight) throws MSLAException, IOException;

    /**
     * Reads layer from a file.
     * @param layer
     * @param decoders
     * @return
     * @throws MSLAException
     * @throws IOException
     */
    boolean readLayer(MSLALayerDecoders decoders, int layer) throws MSLAException;

    /**
     * Writes file data to a stream.
     * @param stream
     * @throws IOException
     */
    void write(OutputStream stream) throws IOException;

    /**
     * Checks if a file is valid.
     * @return
     */
    boolean isValid();

    /**
     * Returns file- and printer-specific option mapper.
     * @return
     */
    MSLAOptionMapper options();
}
