package futurelink.msla.formats.creality;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.creality.tables.CXDLPFileLayerLine;
import futurelink.msla.formats.iface.*;
import futurelink.msla.tools.BufferedImageInputStream;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

public class CXDLPLayerCodec implements MSLALayerCodec<List<CXDLPFileLayerLine>> {
    private static final Logger logger = Logger.getLogger(CXDLPLayerCodec.class.getName());

    public static class Input implements MSLALayerDecodeInput<List<CXDLPFileLayerLine>> {
        private final List<CXDLPFileLayerLine> data = new LinkedList<>();
        public Input(DataInputStream stream, int encodedDataSize) throws MSLAException {
            try {
                var bytesRead = 0;
                stream.readInt(); // Skip integer
                var lineCount = stream.readInt();
                bytesRead += 8;
                while (bytesRead < encodedDataSize - 2) { // Minus 2, because there's one extra 2 bytes at the end
                    this.data.add(CXDLPFileLayerLine.fromByteArray(stream.readNBytes(6)));
                    bytesRead += 6;
                    lineCount--;
                }
                if (lineCount != 0) throw new MSLAException("Error decoding data, not all " + lineCount + " lines were read");
            } catch (IOException e) {
                throw new MSLAException("Error decoding data", e);
            }
        }

        @Override public int size() { return data.size(); }
        @Override public List<CXDLPFileLayerLine> data() { return data; }
    }

    public static class Output implements MSLALayerEncodeOutput<List<CXDLPFileLayerLine>> {
        private final List<CXDLPFileLayerLine> data = new LinkedList<>();

        public Output() {}
        @Override public int size() { return this.data.size(); }
        @Override public int sizeInBytes() { return this.data.size() * 6; }
        @Override public int pixels() { return 0; }
        @Override public void write(List<CXDLPFileLayerLine> data) throws IOException { this.data.addAll(data); }
        @Override public List<CXDLPFileLayerLine> data() { return this.data; }
        public void writeLine(CXDLPFileLayerLine line) { this.data.add(line); }
    }

    /**
     * Reads data using reader, encodes and puts it into internal layer structure.
     * @param layerNumber layer number
     * @param reader reader object to be used as data input channel
     */
    @Override
    public MSLALayerEncodeOutput<List<CXDLPFileLayerLine>> Encode(
            int layerNumber,
            MSLALayerEncodeReader reader) throws MSLAException
    {
        if (reader == null) throw new MSLAException("Reader can't be null");
        var output = new CXDLPLayerCodec.Output();
        var img = reader.read(layerNumber);
        try (var data = new BufferedImageInputStream(img, MSLALayerEncodeReader.ReadDirection.READ_COLUMN)) {
            var width = data.getImage().getWidth();
            var iSize = data.available();
            logger.finest("Data amount to encode: " + iSize);

            var color = 0;
            int startY = 0, endY = 0, startX = 0;
            int pos = 0;
            while (data.available() > 0) {
                var pixel = data.read();
                if (color != pixel) {
                    if (color != 0) {   // color change, break the line and start new
                        var line = new CXDLPFileLayerLine((short) startY, (short) endY, (short) startX, (byte) color);
                        output.writeLine(line);
                    } else {            // break the line, no start
                        startX = pos / width;
                        startY = pos % width;
                        endY = startY;
                    }
                } else endY++;          // continue the line
                color = pixel;
                pos++;
            }
            return output;
        } catch (IOException e) {
            throw new MSLAException("Error encoding data", e);
        }
    }

    @Override
    public int Decode(
            int layerNumber,
            MSLALayerDecodeInput<List<CXDLPFileLayerLine>> input,
            int decodedDataLength,
            MSLALayerDecodeWriter writer) throws MSLAException
    {
        if (input == null) throw new MSLAException("Input data can't be null");
        if (writer == null) throw new MSLAException("Writer can't be null");
        var pixelsCount = 0;
        for (var line : input.data()) {
            var count = line.getLength();
            var pos = line.getStartX() + line.getStartY() * writer.getLayerResolution().getWidth();
            writer.stripe(layerNumber, line.getGray(), pos, count, MSLALayerDecodeWriter.WriteDirection.WRITE_COLUMN);
            pixelsCount += count;
        }
        return pixelsCount;
    }
}
