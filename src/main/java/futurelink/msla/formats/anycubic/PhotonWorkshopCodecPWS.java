package futurelink.msla.formats.anycubic;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.iface.MSLALayerDecodeInput;
import futurelink.msla.formats.iface.MSLALayerDecodeWriter;
import futurelink.msla.formats.iface.MSLALayerEncodeOutput;
import futurelink.msla.formats.iface.MSLALayerEncodeReader;

/**
 * An INCOMPLETE draft for Photon Workshop file PWS codec.
 */
public class PhotonWorkshopCodecPWS extends PhotonWorkshopCodec {

    @Override
    public final MSLALayerEncodeOutput<byte[]> Encode(int layerNumber,
                                                      MSLALayerEncodeReader input) throws MSLAException
    {
        throw new MSLAException("PWS is not implemented yet");
    }

    @Override
    public final int Decode(int layerNumber,
                            MSLALayerDecodeInput<byte[]> data,
                            MSLALayerDecodeWriter writer) throws MSLAException
    {
        throw new MSLAException("PWS is not implemented yet");
    }
}
