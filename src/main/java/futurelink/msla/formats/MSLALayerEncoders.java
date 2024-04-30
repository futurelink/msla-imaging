package futurelink.msla.formats;

import futurelink.msla.formats.iface.MSLALayerEncodeReader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public final class MSLALayerEncoders {
    private final Integer maxEncoders;
    private volatile Integer encoders = 0;

    public interface Callback {
        void onFinish(int size, byte[] data);
        default void onError(String error) {
            throw new RuntimeException(error);
        }
    }

    public MSLALayerEncoders() {
        this.maxEncoders = 5;
    }

    public synchronized boolean isBusy() {
        synchronized (this) {
            return (encoders >= maxEncoders);
        }
    }

    public synchronized boolean isEncoding() {
        synchronized (this) {
            return encoders > 0;
        }
    }

    public boolean encode(int layer, MSLALayerEncodeReader reader, Callback callback) throws MSLAException {
        if (reader.getCodec() == null) throw new MSLAException("No codec defined for layer data");
        if (isBusy()) return false; // No encoders available
        try {
            // Create codec object
            synchronized(this) { encoders++; }
            var codec = reader.getCodec().getDeclaredConstructor().newInstance();

            // Start encode thread
            new Thread(() -> {
                try {
                    var input = reader.read(layer, MSLALayerEncodeReader.ReadDirection.READ_ROW);
                    var iSize = input.available();
                    var output = new ByteArrayOutputStream();
                    var oSize = codec.Encode(input, output);
                    reader.onStart(layer);
                    if (output.size() > 0) {
                        reader.onFinish(layer, iSize, oSize);
                        if (callback != null) callback.onFinish(oSize, output.toByteArray());
                    } else {
                        reader.onError(layer, "Empty image");
                        if (callback != null) callback.onError("Empty image");
                    }
                } catch (IOException e) {
                    try {
                        reader.onError(layer, "Encoder error " + e.getMessage());
                        if (callback != null) callback.onError("Encoder error " + e.getMessage());
                    } catch (IOException e2) {
                        throw new RuntimeException(e2);
                    }
                }
                synchronized(this) { encoders--; }
            }).start();
        } catch (InstantiationException | IllegalAccessException |
                 NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }

        return true;
    }

}
