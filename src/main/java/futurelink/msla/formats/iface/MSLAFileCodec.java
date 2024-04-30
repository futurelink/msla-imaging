package futurelink.msla.formats.iface;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface MSLAFileCodec {
    byte[] Encode(byte[] pixels);
    int Encode(InputStream iStream, OutputStream oStream) throws IOException;
    byte[] Encode(InputStream iStream) throws IOException;

    /**
     * Decodees layer data from byte array.
     * @param data
     * @param layerNumber
     * @param decodedDataLength
     * @param writer
     * @return number of decoded pixels
     * @throws IOException
     */
    int Decode(byte[] data, int layerNumber, int decodedDataLength, MSLALayerDecodeWriter writer) throws IOException;
}
