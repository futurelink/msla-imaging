package futurelink.msla.formats;

import futurelink.msla.formats.iface.*;
import lombok.Getter;

import java.util.Map;

@Getter
public abstract class MSLAFileGeneric<T> implements MSLAFile<T> {
    private final java.util.UUID UUID;

    protected MSLAFileGeneric() {
        UUID = java.util.UUID.randomUUID();
    }

    @Override
    @SuppressWarnings("unchecked")
    public MSLALayerEncoder<T> getEncodersPool() throws MSLAException {
        return (MSLALayerEncoder<T>) MSLALayerEncoders.getInstance(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public MSLALayerDecoder<T> getDecodersPool() throws MSLAException {
        return (MSLALayerDecoder<T>) MSLALayerDecoders.getInstance(this);
    }
}
