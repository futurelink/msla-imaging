package futurelink.msla.formats.creality;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.creality.tables.CXDLPFileLayer;
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
        public Input(CXDLPFileLayer layer) {
            data.addAll(layer.getLines());
        }

        @Override public int size() { return data.size(); }
        @Override public List<CXDLPFileLayerLine> data() { return data; }
    }

    public static class Output implements MSLALayerEncodeOutput<List<CXDLPFileLayerLine>> {
        private final List<CXDLPFileLayerLine> data = new LinkedList<>();
        private Integer totalLinesLength = 0;

        public Output() {}
        @Override public int size() { return this.data.size(); }
        @Override public int sizeInBytes() { return this.data.size() * 6; }
        @Override public int pixels() { return totalLinesLength; }
        @Override public void write(List<CXDLPFileLayerLine> data) throws IOException { this.data.addAll(data); }
        @Override public List<CXDLPFileLayerLine> data() { return this.data; }
        public void writeLine(CXDLPFileLayerLine line) {
            this.totalLinesLength += line.getLength();
            this.data.add(line);
        }
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
                        var line = new CXDLPFileLayerLine((short) startY, (short) endY, (short) startX, color & 0xff);
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
