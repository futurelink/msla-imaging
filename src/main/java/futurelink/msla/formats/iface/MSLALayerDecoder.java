package futurelink.msla.formats.iface;

import futurelink.msla.formats.MSLAException;

import java.util.Map;

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
     * @param params is a {@link Map} of codec parameters
     * @return true if job was added successfully, otherwise - false.
     */
    boolean decode(int layer,
                   MSLALayerDecodeWriter writer,
                   MSLALayerDecodeInput<D> data,
                   Map<String, Object> params) throws MSLAException;
}
