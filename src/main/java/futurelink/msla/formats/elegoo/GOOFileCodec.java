package futurelink.msla.formats.elegoo;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.iface.*;
import futurelink.msla.tools.BufferedImageInputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.logging.Logger;

public class GOOFileCodec implements MSLALayerCodec<byte[]> {
    private final Logger logger = Logger.getLogger(GOOFileCodec.class.getName());
    private final boolean useColorDifferenceCompression = false;
    private final byte LAYER_MAGIC = 0x55;

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

        public Output(LinkedList<Byte> data) throws IOException {
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

    private void GOOFileCodecAddRep(LinkedList<Byte> rle, int stride, int currentColor, int previousColor) {
        if (stride == 0) return;
        int lastByteIndex = rle.size();
        var lastValue = rle.getLast();
        rle.add((byte) 0);

        // Difference mode
        var colorDifference = (byte) Math.abs(currentColor - previousColor);
        if (useColorDifferenceCompression && colorDifference <= 0xF && stride <= 255 && (currentColor > 0 && currentColor < 255)) {
            rle.set(lastByteIndex, (byte) (0b10 << 6 | (colorDifference & 0x0F)));
            if (stride > 1) {
                rle.set(lastByteIndex, (byte) (0x1 << 4));
                rle.add((byte) stride);
            }

            if (currentColor < previousColor) rle.set(lastByteIndex, (byte) (lastValue | 0x01 << 5));
        } else {
            // 1 1 This chunk contain all 0xff pixels
            if (currentColor == 255) rle.set(lastByteIndex, (byte) ((lastValue | 0b11 << 6) & 0xff));

            // 0 1 This chunk contain the value of gray between 0x1 to 0xfe. The gray value is after byte0.
            else if (currentColor > 0) {
                rle.set(lastByteIndex, (byte) (lastValue | 0b01 << 6));
                rle.add((byte) currentColor);
            }

            rle.set(lastByteIndex, (byte) (lastValue | stride & 0x0F));
            if (stride > 0x0F) {
                if (stride <= 0xFFF) {
                    rle.set(lastByteIndex, (byte) ((lastValue | 0b01 << 4) & 0xff));
                    rle.add((byte) (stride >> 4));
                } else if (stride <= 0xFFFFF) {
                    rle.set(lastByteIndex, (byte) ((lastValue | 0b10 << 4) & 0xff));
                    rle.add((byte) (stride >> 12));
                    rle.add((byte) (stride >> 4));
                } else if (stride <= 0xFFFFFFF) {
                    rle.set(lastByteIndex, (byte) ((lastValue | 0b11 << 4) & 0xff));
                    rle.add((byte) (stride >> 20));
                    rle.add((byte) (stride >> 12));
                    rle.add((byte) (stride >> 4));
                }
            }
        }
    }

    @Override
    public MSLALayerEncodeOutput<byte[]> Encode(int layerNumber, MSLALayerEncodeReader reader) throws MSLAException {
        var rle = new LinkedList<Byte>();
        int previousColor = 0;
        int currentColor = 0;
        int stride = 0;
        byte checkSum = 0;

        rle.add(LAYER_MAGIC);
        try (var input = new BufferedImageInputStream(
                reader.read(layerNumber),
                MSLALayerEncodeReader.ReadDirection.READ_ROW)
        ) {
            while (input.available() > 0) {
                var color = input.read();
                if (currentColor == color) stride++;
                else {
                    GOOFileCodecAddRep(rle, stride, currentColor, previousColor);
                    stride = 1;
                    previousColor = currentColor;
                    currentColor = color;
                }
            }

            GOOFileCodecAddRep(rle, stride, currentColor, previousColor);

            // Calculate checksum
            for (int i = 1; i < rle.size(); i++) checkSum += rle.get(i);
            rle.add((byte) (~checkSum & 0xff));

            return new Output(rle);
        } catch (IOException e) {
            throw new MSLAException("Can't encode layer data", e);
        }
    }

    @Override
    public final int Decode(
            int layerNumber,
            MSLALayerDecodeInput<byte[]> data,
            MSLALayerDecodeWriter writer) throws MSLAException
    {
        logger.info("Starting decoding " + data.size() + " bytes");
        var RLEData = data.data();

        if (data.size() <= 3) return 0;
        if (RLEData[0] != LAYER_MAGIC)
            throw new MSLAException("RLE for layer " + layerNumber +
                    " is corrupted, should start with " + LAYER_MAGIC + " but got " + RLEData[0]);

        var lastByteIndex = RLEData.length - 1;
        int pixels = 0;
        int color = 0;
        int checkSum = 0;
        int fileCheckSum = ((int)RLEData[lastByteIndex] & 0xff);

        for (var i = 1; i < lastByteIndex; i++) checkSum += RLEData[i];
        checkSum = ~checkSum & 0xff;
        if (fileCheckSum != checkSum)
            throw new MSLAException("Decoded RLE for layer " + layerNumber +
                    " is corrupted, expected checksum " + fileCheckSum + " , got <" + checkSum + ">");

        int position = 0;
        for (var i = 1; i < lastByteIndex; i++) {
            /* Byte0[7:6]: The type of chunk
             * (0x0) 0 0 This chunk contain all 0x0 pixels
             * (0x1) 0 1 This chunk contain the value of gray between 0x1 to 0xfe. The gray value is after byte0.
             * (0x2) 1 0 This chunk contain the diff value from the previous pixel
             * (0x3) 1 1 This chunk contain all 0xff pixels
             */
            int chunkType = (RLEData[i] & 0b11000000) >> 6;
            int stride = 0;
            var strideIdx0 = i;
            var strideIdx1 = i + 1;
            var strideIdx2 = i + 2;
            var strideIdx3 = i + 3;

            switch (chunkType) {
                case 0x00: color = 0; break;
                case 0x01: color = RLEData[++i] & 0xff; strideIdx1++; strideIdx2++; strideIdx3++; break;
                case 0x02:
                    /* When byte0[7:6] is [1:0], the meaning of byte0[5:4] follow below definition:
                     * 0 0 byte0[3:0] is the positive diff value. that's mean
                           current value subtract previous value is bigger
                           than 0. The range is from 0 to 15. 0x0 map to 0.
                           0xf map to 15.
                     * 0 1 byte0[3:0] is the positive diff value. And this
                           value's run-length represent by byte1[7:0]
                     * 1 0 byte0[3:0] is the negative diff value.that's mean
                           current value subtract previous value is smaller
                           than 0. The range is from 0 to 15. 0x0 map to 0.
                           0xf map to 15.
                     * 1 1 byte0[3:0] is the negative diff value. And this
                           value's run-length represent by byte1[7:0]
                    */
                    var diffType = (RLEData[i] >> 4) & 0x03;
                    var diffValue = RLEData[i] & 0x0f;
                    switch (diffType) {
                        case 0x00: color += diffValue; stride = 1; break;
                        case 0x01: color += diffValue; stride = RLEData[++i] & 0xff; break;
                        case 0x02: color -= diffValue; stride = 1; break;
                        case 0x03: color -= diffValue; stride = RLEData[++i] & 0xff; break;
                        default: throw new MSLAException("Diff type " + diffType + " is out of range, must be <= 0x03.");
                    }
                    break;
                case 0x03: color = 255; break;
                default: throw new MSLAException("Chunk type " + chunkType + " is out of range, must be <= 0x03.");
            }

            if (chunkType != 0x02) {
                /* Byte0[5:4]: The length of chunk except when byte0[7:6] is [1 0]
                 * (0x0) 0 0 4-bit run-length use byte0[3:0]
                 * (0x1) 0 1 The run-length consist by byte1[7:0] and byte0[3:0]
                 * (0x2) 1 0 The run-length consist by byte1[7:0], byte2[7:0] and byte0[3:0]
                 * (0x3) 1 1 The run-length consist by byte1[7:0], byte2[7:0], byte3[7:0] and byte0[3:0]
                 */
                byte chunkLength = (byte) (RLEData[strideIdx0] >> 4 & 0x03);
                switch (chunkLength) {
                    case 0x0: stride = RLEData[strideIdx0] & 0x0f; break;
                    case 0x1: stride = ((RLEData[strideIdx1] & 0xff) << 4) + (RLEData[strideIdx0] & 0x0f); i += 1; break;
                    case 0x2: stride = ((RLEData[strideIdx1] & 0xff) << 12) + ((RLEData[strideIdx2] & 0xff) << 4) +
                                (RLEData[strideIdx0] & 0xF); i += 2; break;
                    case 0x3: stride = ((RLEData[strideIdx1] & 0xff) << 20) + ((RLEData[strideIdx2] & 0xff) << 12) +
                                ((RLEData[strideIdx3] & 0xff) << 4) + (RLEData[strideIdx0] & 0x0f); i += 3; break;
                    default: throw new MSLAException("Chunk length " + chunkLength + " is out of range, must be <= 0x03.");
                }
            }

            // Draw only non-black stripes
            if ((color & 0xff) > 0) {
                writer.stripe(layerNumber, (color & 0xff), position, stride, MSLALayerDecodeWriter.WriteDirection.WRITE_ROW);
                pixels += stride;
            }

            // Increment position
            position += stride;
        }

        return pixels;
    }

    @Override public void setParam(String paramName, Object paramValue) {}
    @Override public Object getParam(String paramName) { return null; }
}
