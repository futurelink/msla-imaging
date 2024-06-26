package io.msla.formats;

import io.msla.formats.iface.*;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class MSLALayerDecoders<D> extends ThreadPoolExecutor implements MSLALayerDecoder<D> {
    private final Integer maxDecoders;
    private final Class<? extends MSLALayerCodec<D>> codec;
    private static final HashMap<UUID, MSLALayerDecoders<?>> instances = new HashMap<>();
    private final AtomicInteger counter = new AtomicInteger(0);

    public static <T> MSLALayerDecoders<?> getInstance(

            MSLAFile<T> file) throws MSLAException
    {
        var uuid = file.getUUID();
        if (uuid == null) throw new MSLAException("No UUID was found");
        if (instances.containsKey(uuid)) {
            return instances.get(uuid);
        } else {
            var pool = new MSLALayerDecoders<>(file.getCodec());
            instances.put(uuid, pool);
            return pool;
        }
    }

    private MSLALayerDecoders(Class<? extends MSLALayerCodec<D>> codec) throws MSLAException {
        this(codec, 5);
    }

    private MSLALayerDecoders(Class<? extends MSLALayerCodec<D>> codec, int maxDecoders) throws MSLAException {
        super(maxDecoders, maxDecoders, 10L, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        if (codec == null) throw new MSLAException("No codec defined for layer data");
        this.maxDecoders = maxDecoders;
        this.codec = codec;
    }

    @Override
    public boolean isBusy() {
        return counter.get() >= maxDecoders;
    }

    @Override
    public boolean isDecoding() {
        return counter.get() > 0;
    }

    @Override
    public boolean decode(int layer, MSLALayerDecodeWriter writer, MSLALayerDecodeInput<D> data, Map<String, Object> params)
            throws MSLAException
    {
        if (data == null) throw new MSLAException("No data to decode");
        if (writer == null) throw new MSLAException("Writer is not defined");
        if (isBusy()) return false; // No decoder slots available
        counter.getAndIncrement();
        try {
            // Create codec object, one per encoding process
            var codecObj = codec.getDeclaredConstructor().newInstance();
            if (params != null) {
                for (var param : params.keySet()) codecObj.setParam(param, params.get(param));
            }

            // Start decode thread
            submit(() -> {
                try {
                    writer.onStart(layer);
                    var pixels = codecObj.Decode(layer, data, writer);
                    writer.onFinish(layer, pixels);
                    counter.decrementAndGet();
                } catch (Exception e) {
                    try {
                        writer.onError(layer, e.getMessage());
                        counter.decrementAndGet();
                    } catch (Exception e2) {
                        counter.decrementAndGet();
                        throw new RuntimeException(e2);
                    }
                }
            });
        } catch (InvocationTargetException | InstantiationException |
                 IllegalAccessException | NoSuchMethodException e) {
            throw new MSLAException("Can't proceed with decoding", e);
        }
        return true;
    }
}
