package futurelink.msla.formats.iface;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.MSLAOptionMapper;
import futurelink.msla.formats.utils.Size;

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
    MSLAPreview getPreview();

    /**
     * Updates preview image.
     */
    void updatePreviewImage() throws MSLAException;

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
     * Returns file layers count.
     */
    int getLayerCount();

    /**
     * Adds new layer to a file.
     * @param reader is {@link MSLALayerEncodeReader} object to be used as data input channel
     * @param callback is {@link MSLALayerEncoder.Callback} to be executed when encoding is done
     */
    void addLayer(
            MSLALayerEncodeReader reader,
            MSLALayerEncoder.Callback<T> callback) throws MSLAException;

    /**
     * Adds new layer to a file using {@code MSLAEncodeReader} reader and parameters.
     * @param reader is {@link MSLALayerEncodeReader} object to be used as data input channel
     * @param callback is {@link MSLALayerEncoder.Callback} to be executed when encoding is done
     * @param layerHeight is layer height
     * @param exposureTime is a time to expose a layer
     * @param liftSpeed is a lift speed in mm/min after layer was exposed
     * @param liftHeight is a lift height mm after layer was exposed
     */
    void addLayer(
            MSLALayerEncodeReader reader,
            MSLALayerEncoder.Callback<T> callback,
            float layerHeight, float exposureTime, float liftSpeed, float liftHeight) throws MSLAException;

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
    MSLAOptionMapper options();
}
