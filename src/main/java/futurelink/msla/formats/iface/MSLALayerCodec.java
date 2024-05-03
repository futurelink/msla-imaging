package futurelink.msla.formats.iface;

import futurelink.msla.formats.MSLAException;

/**
 *
 * @param <D> codec destination data structure
 */
public interface MSLALayerCodec<D> {
    MSLALayerEncodeOutput<D> Encode(int layerNumber, MSLALayerEncodeReader input)
            throws MSLAException;

    /**
     * Decodees layer data from byte array.
     * @param data {@link MSLALayerDecodeInput} object
     * @param layerNumber layer number
     * @param decodedDataLength expected decoded data length
     * @param writer writer object to be used as output channel
     * @return number of decoded pixels
     */
    int Decode(int layerNumber, MSLALayerDecodeInput<D> data, int decodedDataLength, MSLALayerDecodeWriter writer)
            throws MSLAException;
}
