package futurelink.msla.formats.chitubox.common;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.chitubox.CTBCrypto;
import futurelink.msla.formats.iface.*;
import futurelink.msla.tools.BufferedImageInputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

public class CTBCommonFileCodec implements MSLALayerCodec<byte[]> {
    private static final Logger logger = Logger.getLogger(CTBCommonFileCodec.class.getName());
    private Integer EncryptionKey = 0;

    public static class Input implements MSLALayerDecodeInput<byte[]> {
        private final byte[] data;
        @Override public int size() { return data.length; }
        @Override public byte[] data() { return data; }
        public Input(byte[] data) { this.data = data; }
    }

    public static class Output implements MSLALayerEncodeOutput<byte[]> {
        private final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        private int pixelsCount = 0;
        private int size = 0;

        public Output(List<Byte> data) throws IOException {
            var d = new byte[data.size()];
            for (int i = 0; i < data.size(); ++i) { d[i] = data.get(i); }
            write(d);
        }
        @Override public int pixels() { return pixelsCount; }
        @Override public int size() { return size; }
        @Override public int sizeInBytes() { return size(); }
        @Override public void write(byte[] data) throws IOException {
            for (byte pixel : data) if (pixel != 0) pixelsCount++;
            this.size += data.length;
            this.stream.write(data);
        }
        @Override public byte[] data() { return stream.toByteArray(); }
    }

    @Override public void setParam(String paramName, Object paramValue) {
        if ("EncryptionKey".equals(paramName)) EncryptionKey = (Integer) paramValue;
    }

    @Override public Object getParam(String paramName) {
        if ("EncryptionKey".equals(paramName)) return EncryptionKey;
        return null;
    }

    void AddRep(List<Byte> data, int stride, int color) {
        if (stride == 0) return;
        if (stride > 1) color |= 0x80;
        data.add((byte) color);

        if (stride <= 1) return; // no run needed
        if (stride <= 0x7f) { data.add((byte) stride); return; }
        if (stride <= 0x3fff) {
            data.add((byte) ((stride >> 8) | 0x80));
            data.add((byte) stride);
            return;
        }

        if (stride <= 0x1fffff) {
            data.add((byte) ((stride >> 16) | 0xc0));
            data.add((byte) (stride >> 8));
            data.add((byte) stride);
            return;
        }

        if (stride <= 0xfffffff) {
            data.add((byte) ((stride >> 24) | 0xe0));
            data.add((byte) (stride >> 16));
            data.add((byte) (stride >> 8));
            data.add((byte) stride);
        }
    }


    @Override
    public MSLALayerEncodeOutput<byte[]> Encode(int layerNumber, MSLALayerEncodeReader reader)
            throws MSLAException
    {
        try (var input = new BufferedImageInputStream(
                reader.read(layerNumber),
                MSLALayerEncodeReader.ReadDirection.READ_ROW)
        ) {
            List<Byte> data = new LinkedList<>();
            byte color = 0xff >> 1;
            int stride = 0;
            while (input.available() > 0) {
                var grey7 = (byte) (input.read() >> 1);
                if (grey7 == color) stride++;
                else {
                    AddRep(data, stride, color); // Add stride
                    color = grey7;
                    stride = 1;
                }
            }

            AddRep(data, stride, color); // Final stride

            var key = (Integer) getParam("EncryptionKey");
            if (key == null) logger.warning("No EncryptionKey parameter set to codec, make sure if that is correct");
            else CTBCrypto.LayerRLECrypt(key, layerNumber, new CTBCrypto.LayerRLECryptCallback() {
                @Override public byte getByte(int index) { return data.get(index); }
                @Override public void setByte(int index, byte b) { data.set(index, b); }
                @Override public int getSize() { return data.size(); }
            });

            return new Output(data);
        } catch (IOException e) {
            throw new MSLAException("Error encoding layer " + layerNumber, e);
        }
    }

    @Override
    public int Decode(
            int layerNumber,
            MSLALayerDecodeInput<byte[]> data,
            MSLALayerDecodeWriter writer) throws MSLAException
    {
        logger.info("Starting decoding layer " + layerNumber + ", " + data.size() + " bytes");
        var encodedRLE = data.data();

        var key = (Integer) getParam("EncryptionKey");
        if (key == null) logger.warning("No EncryptionKey parameter set to codec, make sure if that is correct");
        else CTBCrypto.LayerRLECrypt(key, layerNumber, new CTBCrypto.LayerRLECryptCallback() {
            @Override public byte getByte(int index) { return encodedRLE[index]; }
            @Override public void setByte(int index, byte b) { encodedRLE[index] = b; }
            @Override public int getSize() { return encodedRLE.length; }
        });

        int pixels = 0;
        int position = 0;
        for (var n = 0; n < data.size(); n++) {
            byte code = encodedRLE[n];
            int stride = 1;
            if ((code & 0x80) == 0x80) { // It's a run
                code &= 0x7f; // Get the run length
                n++;

                int len = encodedRLE[n] & 0xff;
                if ((len & 0x80) == 0) stride = len;
                else if ((len & 0xc0) == 0x80) { // One byte stride
                    stride = ((len & 0x3f) << 8) + (encodedRLE[n + 1] & 0xff);
                    n++;
                } else if ((len & 0xe0) == 0xc0) { // Two bytes stride
                    stride = ((len & 0x1f) << 16) +
                            ((encodedRLE[n + 1] & 0xff) << 8) +
                            (encodedRLE[n + 2] & 0xff);
                    n += 2;
                } else if ((len & 0xf0) == 0xe0) { // Three bytes stride
                    stride = ((len & 0xf) << 24) +
                            ((encodedRLE[n + 1] & 0xff) << 16) +
                            ((encodedRLE[n + 2] & 0xff) << 8) +
                            (encodedRLE[n + 3] & 0xff);
                    n += 3;
                } else {
                    throw new MSLAException("Corrupted RLE data");
                }
            }

            if (code != 0) code = (byte)((code << 1) | 1); // Bit extend from 7-bit to 8-bit grey map
            if (code != 0) { // Write stride if pixel color is not black
                writer.stripe(layerNumber, (code & 0xff), position, stride, MSLALayerDecodeWriter.WriteDirection.WRITE_ROW);
                pixels += stride;
            }
            position += stride;
        }
        return pixels;
    }
}
