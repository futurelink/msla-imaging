package futurelink.msla.formats.iface;

import java.io.IOException;

public interface MSLALayerEncodeOutput<T> {
    int size();
    int sizeInBytes();
    int pixels();
    void write(T data) throws IOException;
    T data();
    default void setParam(String paramName, Object paramValue) {}
    default Object getParam(String paramName) { return null; }
}
