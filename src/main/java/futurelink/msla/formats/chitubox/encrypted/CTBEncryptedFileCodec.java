package futurelink.msla.formats.chitubox.encrypted;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.iface.*;

public class CTBEncryptedFileCodec  implements MSLALayerCodec<byte[]> {
    @Override
    public MSLALayerEncodeOutput<byte[]> Encode(int layerNumber, MSLALayerEncodeReader reader) throws MSLAException {
        return null;
    }

    @Override
    public int Decode(int layerNumber, MSLALayerDecodeInput<byte[]> data, MSLALayerDecodeWriter writer) throws MSLAException {
        return 0;
    }
}
