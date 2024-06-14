package futurelink.msla.formats;

import futurelink.msla.formats.iface.*;
import lombok.Getter;

@Getter
public abstract class MSLAFileGeneric<T> implements MSLAFile<T> {
    private final java.util.UUID UUID;
    @Getter private Float PixelSize;

    protected MSLAFileGeneric(MSLAFileProps initialProps) throws MSLAException {
        if (initialProps != null) this.PixelSize = initialProps.getFloat("PixelSize");
        this.UUID = java.util.UUID.randomUUID();
    }

    public void reset(MSLAFileDefaults defaults) throws MSLAException {
        if (defaults == null) throw new MSLAException("Defaults can't be null");
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
