package futurelink.msla.formats.elegoo;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.iface.*;

public class GOOFileCodec implements MSLALayerCodec<byte[]> {
    @Override
    public MSLALayerEncodeOutput<byte[]> Encode(
            int layerNumber,
            MSLALayerEncodeReader input) throws MSLAException
    {
        return null;
    }

    @Override
    public int Decode(
            int layerNumber,
            MSLALayerDecodeInput<byte[]> data,
            int decodedDataLength,
            MSLALayerDecodeWriter writer) throws MSLAException
    {
        return 0;
    }
}
