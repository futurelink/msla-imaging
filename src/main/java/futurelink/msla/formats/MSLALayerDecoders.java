package futurelink.msla.formats;

import futurelink.msla.formats.iface.MSLALayerDecodeWriter;

import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public final class MSLALayerDecoders {
    private final Integer maxDecoders;
    private volatile Integer decoders = 0;
    private final MSLALayerDecodeWriter writer;

    public MSLALayerDecoders(MSLALayerDecodeWriter writer) {
        this.writer = writer;
        this.maxDecoders = 5;
    }

    private synchronized boolean isBusy() {
        synchronized (this) {
            return (decoders >= maxDecoders);
        }
    }

    public synchronized boolean isDecoding() {
        synchronized (this) {
            return decoders > 0;
        }
    }

    public boolean decode(int layer, byte[] data, int decodedDataLength) throws MSLAException {
        if (writer.getCodec() == null) throw new MSLAException("No codec defined for layer data");

        if (isBusy()) return false; // No decoder slots available

        try {
            synchronized (this) { decoders++; }
            var codecObj = writer.getCodec().getDeclaredConstructor().newInstance();

            // Start decode thread
            new Thread(() -> {
                try {
                    writer.onStart(layer);
                    var pixels = codecObj.Decode(data, layer, decodedDataLength, writer);
                    writer.onFinish(layer, pixels);
                } catch (IOException e) {
                    try {
                        writer.onError(layer, e.getMessage());
                    } catch (IOException e2) {
                        throw new RuntimeException(e2);
                    }
                }
                synchronized (this) { decoders--; }
            }).start();
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException |
                 NoSuchMethodException e) {
            throw new MSLAException("Can't proceed with decoding", e);
        }

        return true;
    }

    public boolean decode(int layer, DataInputStream stream, int encodedDataLength, int decodedDataLength)
            throws MSLAException {
        try {
            return decode(layer, stream.readNBytes(encodedDataLength), decodedDataLength);
        } catch (IOException e) {
            throw new MSLAException("Can't proceed with decoding as couldn't read data from stream", e);
        }
    }
}
