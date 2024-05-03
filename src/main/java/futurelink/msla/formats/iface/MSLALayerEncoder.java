package futurelink.msla.formats.iface;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.MSLALayerEncoders;

public interface MSLALayerEncoder<D> {

    /**
     * Callback interface to be used with encode(...) method.
     * @param <T> type of encoded data
     */
    interface Callback<T> {
        void onFinish(int layer, MSLALayerEncodeOutput<T> data) throws MSLAException;
        default void onError(String error) throws MSLAException {
            throw new MSLAException(error);
        }
    }

    /**
     * Returns true if encoder has some job that is not done yet.
     */
    boolean isEncoding();

    /**
     * Puts an encoding job into a queue.
     * @param layer layer number
     * @param reader {@link MSLALayerEncodeReader} object to be used as an input data channel
     * @param callback {@link MSLALayerEncoders.Callback} to be executed when encoding is done
     */
    void encode(int layer, MSLALayerEncodeReader reader, MSLALayerEncoders.Callback<D> callback) throws MSLAException;
}
