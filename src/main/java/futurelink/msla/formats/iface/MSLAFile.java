package futurelink.msla.formats.iface;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.MSLAOptionMapper;
import futurelink.msla.formats.utils.Size;

import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.UUID;

/**
 * General interface for all MSLA printer files.
 * @param <T> is a layer decode output or encode input data type
 */
public interface MSLAFile<T> {
    /**
     * Returns {@code MSLAFileCodec} codec class.
     */
    Class<? extends MSLALayerCodec<T>> getCodec();

    /**
     * Get encoders pool object.
     */
    MSLALayerEncoder<T> getEncodersPool() throws MSLAException;

    /**
     * Get decoders pool object.
     */
    MSLALayerDecoder<T> getDecodersPool() throws MSLAException;

    /**
     * Returns {@code MSLAPreview} object.
     */
    MSLAPreview getPreview(int index) throws MSLAException;

    /**
     * Sets preview.
     * @param index preview number
     * @param image a {@link BufferedImage} containing preview data
     */
    void setPreview(int index, BufferedImage image) throws MSLAException;

    /**
     * Gets file UUID
     */
    UUID getUUID();

    /**
     * Returns DPI value of a file.
     */
    float getDPI();

    /**
     * Returns file layer size.
     */
    Size getResolution();

    /**
     * Returns pixel size in um.
     */
    float getPixelSizeUm();

    /**
     * Returns layers object.
     */
    MSLAFileLayers<?, ?> getLayers();

    /**
     * Adds new layer to a file.
     * @param reader is {@link MSLALayerEncodeReader} object to be used as data input channel
     * @param callback is {@link MSLALayerEncoder.Callback} to be executed when encoding is done
     */
    void addLayer(
            MSLALayerEncodeReader reader,
            MSLALayerEncoder.Callback<T> callback) throws MSLAException;

    /**
     * Reads layer from a file.
     * @param writer  {@link MSLALayerDecodeWriter} object to be used as output channel
     * @param layer layer number
     */
    boolean readLayer(MSLALayerDecodeWriter writer, int layer) throws MSLAException;

    /**
     * Writes file data to a stream.
     * @param stream output stream (usually a file)
     */
    void write(OutputStream stream) throws MSLAException;

    /**
     * Checks if a file is valid.
     */
    boolean isValid();

    /**
     * Returns file- and printer-specific option mapper.
     */
    MSLAOptionMapper getOptions();
}
