package futurelink.msla.formats;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface MSLAFileCodec {
    byte[] Encode(byte[] pixels);
    int Encode(InputStream iStream, OutputStream oStream) throws IOException;
    byte[] Encode(InputStream iStream) throws IOException;
    void Decode(byte[] data, int decodedDataLength, MSLADecodeWriter writer) throws IOException;
    void Decode(DataInputStream stream, int decodedDataLength, MSLADecodeWriter writer) throws IOException;
}
