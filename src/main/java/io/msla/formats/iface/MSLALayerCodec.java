package io.msla.formats.iface;

import io.msla.formats.MSLAException;

/**
 *
 * @param <D> codec destination data structure
 */
public interface MSLALayerCodec<D> {
    /**
     * Encodes layer data from byte array.
     * @param layerNumber layer number
     * @param reader an agent that reads data to encode
     * @return number of decoded pixels
     */
    MSLALayerEncodeOutput<D> Encode(int layerNumber, MSLALayerEncodeReader reader) throws MSLAException;

    /**
     * Decodes layer data from byte array.
     * @param data {@link MSLALayerDecodeInput} object
     * @param layerNumber layer number
     * @param writer writer object to be used as output channel
     * @return number of decoded pixels
     */
    int Decode(int layerNumber, MSLALayerDecodeInput<D> data, MSLALayerDecodeWriter writer) throws MSLAException;

    void setParam(String paramName, Object paramValue);
    Object getParam(String paramName);
}
