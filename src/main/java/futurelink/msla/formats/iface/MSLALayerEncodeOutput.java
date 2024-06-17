package futurelink.msla.formats.iface;

import java.io.IOException;

/**
 * Interface for mSLA layer encoder output.
 * @param <T> type of incoming data produced by codec
 */
public interface MSLALayerEncodeOutput<T> {
    int size();
    int sizeInBytes();
    int pixels();
    void write(T data) throws IOException;
    T data();
    default void setParam(String paramName, Object paramValue) {}
    default Object getParam(String paramName) { return null; }
}
