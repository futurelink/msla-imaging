package io.msla.formats.anycubic;

import io.msla.formats.MSLAException;
import io.msla.formats.iface.MSLALayerDecodeInput;
import io.msla.formats.iface.MSLALayerDecodeWriter;
import io.msla.formats.iface.MSLALayerEncodeOutput;
import io.msla.formats.iface.MSLALayerEncodeReader;

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
