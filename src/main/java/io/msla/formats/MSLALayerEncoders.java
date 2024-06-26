package io.msla.formats;

import io.msla.formats.iface.*;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class MSLALayerEncoders<D> extends ThreadPoolExecutor implements MSLALayerEncoder<D> {
    private final Class<? extends MSLALayerCodec<D>> codec;
    private static final Map<UUID, MSLALayerEncoders<?>> instances = new ConcurrentHashMap<>();
    private final AtomicInteger counter = new AtomicInteger();
    private final Map<String, Object> params = new HashMap<>();

    public static synchronized <T> MSLALayerEncoders<?> getInstance(MSLAFile<T> file) throws MSLAException {
        var uuid = file.getUUID();
        if (uuid == null) throw new MSLAException("No UUID was found");
        if (instances.containsKey(uuid)) {
            return instances.get(uuid);
        } else {
            var pool = new MSLALayerEncoders<>(file.getCodec());
            instances.put(uuid, pool);
            return pool;
        }
    }

    private MSLALayerEncoders(Class<? extends MSLALayerCodec<D>> codec) throws MSLAException {
        this(5, codec);
    }

    private MSLALayerEncoders(int maxEncoders, Class<? extends MSLALayerCodec<D>> codec) throws MSLAException {
        super(maxEncoders, maxEncoders, 1000000L, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        if (codec == null) throw new MSLAException("No codec defined for layer data");
        this.codec = codec;
    }

    @Override
    public boolean isEncoding() {
        return counter.get() > 0;
    }

    @Override
    public void encode(int layer, MSLALayerEncodeReader reader, Map<String, Object> params, Callback<D> callback)
            throws MSLAException
    {
        if (reader == null) throw new MSLAException("Reader can't be null");
        try {
            counter.getAndIncrement();

            // Create codec object, one per encoding process
            var codecObj = codec.getDeclaredConstructor().newInstance();
            if (params != null) {
                for (var param : params.keySet()) codecObj.setParam(param, params.get(param));
            }

            // Start encode thread
            submit(() -> {
                try {
                    reader.onStart(layer);
                    var output = codecObj.Encode(layer, reader);
                    if (output.size() > 0) {
                        reader.onFinish(layer, reader.getSize(), output);
                        if (callback != null) callback.onFinish(layer, output);
                        counter.decrementAndGet();
                    } else {
                        reader.onError(layer, "Encoder output is empty");
                        if (callback != null) callback.onError("Empty image");
                        counter.decrementAndGet();
                    }
                } catch (MSLAException e) {
                    try {
                        reader.onError(layer, "Encoder error " + e.getMessage());
                        if (callback != null) callback.onError("Encoder error " + e.getMessage());
                        counter.decrementAndGet();
                    } catch (MSLAException e2) {
                        throw new RuntimeException(e2);
                    }
                }
            });
        } catch (InstantiationException | IllegalAccessException |
                 NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        if (t != null) throw new RuntimeException(t);
    }
}
