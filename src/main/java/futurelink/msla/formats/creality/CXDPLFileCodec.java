package futurelink.msla.formats.creality;

import futurelink.msla.formats.iface.MSLAFileCodec;
import futurelink.msla.formats.iface.MSLALayerDecodeWriter;
import futurelink.msla.formats.creality.tables.CXDLPFileLayer;

import java.io.*;

public class CXDPLFileCodec implements MSLAFileCodec {

    @Override
    public byte[] Encode(byte[] pixels) {
        throw new RuntimeException("CXDLP encoding is not supported yet");
    }

    @Override
    public int Encode(InputStream iStream, OutputStream oStream) throws IOException {
        throw new RuntimeException("CXDLP encoding is not supported yet");
    }

    @Override
    public byte[] Encode(InputStream iStream) throws IOException {
        throw new RuntimeException("CXDLP encoding is not supported yet");
    }

    @Override
    public int Decode(byte[] data, int layerNumber, int decodedDataLength, MSLALayerDecodeWriter writer) throws IOException {
        var stream = new DataInputStream(new ByteArrayInputStream(data));
        stream.readInt();
        var lineCount = stream.readInt();
        var pos = 0;
        var count = 0;
        var pixelsCount = 0;
        for (int i = 0; i < lineCount; i++) {
            var line = CXDLPFileLayer.LayerLine.fromByteArray(stream.readNBytes(6));
            count = line.getLength();
            pos = line.getStartX() + line.getStartY() * writer.getLayerResolution().getWidth();
            writer.stripe(layerNumber, line.getGray(), pos, count, MSLALayerDecodeWriter.WriteDirection.WRITE_COLUMN);
            pixelsCount += count;
        }
        stream.readShort(); // 0x0d 0x0a - page break
        return pixelsCount;
    }
}
