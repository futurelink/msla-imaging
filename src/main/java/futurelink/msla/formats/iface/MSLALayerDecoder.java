package futurelink.msla.formats.iface;

import futurelink.msla.formats.MSLAException;

public interface MSLALayerDecoder<D> {
    /**
     * Returns true if there's no any free decoder thread.
     */
    boolean isBusy();

    /**
     * Returns true if there's any decode job that's not done yet.
     */
    boolean isDecoding();

    /**
     * Puts a job into decoding queue.
     * @param layer layer number
     * @param data {@link MSLALayerDecodeInput} to be used as data input channel
     * @param decodedDataLength expected decoded data length
     * @return true if job was added successfully, otherwise - false.
     */
    boolean decode(int layer, MSLALayerDecodeInput<D> data, int decodedDataLength) throws MSLAException;
}
