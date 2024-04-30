package futurelink.msla.formats.anycubic;

import futurelink.msla.formats.iface.MSLALayerDecodeWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class PhotonWorkshopCodecPWS implements PhotonWorkshopCodec {
    @Override
    public byte[] Encode(byte[] pixels) {
        return new byte[0];
    }

    @Override
    public int Encode(InputStream iStream, OutputStream oStream) throws IOException {
        return 0;
    }

    @Override
    public byte[] Encode(InputStream iStream) throws IOException {
        return new byte[0];
    }

    @Override
    public int Decode(byte[] data, int layerNumber, int decodedDataLength, MSLALayerDecodeWriter writer) throws IOException {
        return 0;
    }
}
