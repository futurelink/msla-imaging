package io.msla.formats.anycubic;

import io.msla.formats.iface.MSLALayerCodec;
import io.msla.formats.iface.MSLALayerDecodeInput;
import io.msla.formats.iface.MSLALayerEncodeOutput;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Abstract Anycubic Photon codec class.
 */
abstract public class PhotonWorkshopCodec implements MSLALayerCodec<byte[]> {
    protected Integer decodedDataLength = 0;

    public static class Input implements MSLALayerDecodeInput<byte[]> {
        private final byte[] data;
        public Input(byte[] data) { this.data = data; }
        @Override public int size() { return data.length; }
        @Override public byte[] data() { return data; }
    }

    public static class Output implements MSLALayerEncodeOutput<byte[]> {
        private final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        private int pixelsCount = 0;
        private int size = 0;

        public Output() {}
        public Output(byte[] data) throws IOException { write(data); }
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
        if ("DecodedDataLength".equals(paramName)) decodedDataLength = (Integer) paramValue;
    }

    @Override public Object getParam(String paramName) {
        if ("DecodedDataLength".equals(paramName)) return decodedDataLength;
        return null;
    }
}
