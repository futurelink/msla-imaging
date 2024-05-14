package futurelink.msla.formats.creality.tables;

import lombok.Getter;

import java.io.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@Getter
public class CXDLPFileLayer {

    // Used to position in a file when read
    Long DataOffset = 0L;
    Integer DataLength = 0;

    // Layer data
    Integer LayerArea = 0;
    private final List<CXDLPFileLayerLine> Lines = new LinkedList<>();

    protected CXDLPFileLayer() {}

    @SuppressWarnings("unused")
    protected CXDLPFileLayer(List<CXDLPFileLayerLine> data) {
        for (var line : data) addLine(line);
    }

    public final void addLine(CXDLPFileLayerLine line) {
        Lines.add(line);
        DataLength = Lines.size() * 6 + 2;
    }

    public void writeData(OutputStream stream) throws IOException {
        var dos = new DataOutputStream(stream); // Do not need to close stream after write is done!
        dos.writeInt(LayerArea);
        dos.writeInt(Lines.size());
        for (var line : Lines) dos.write(line.bytes());
        dos.writeShort(0x0d0a);
    }

    @Override
    public String toString() {
        return "CXDLPFileLayer { " +
                "DataOffset=" + DataOffset +
                ", DataLength=" + DataLength +
                ", LayerArea=" + LayerArea +
                ", LineCount=" + Lines.size() +
                ", Lines=" + Arrays.toString(Lines.toArray()) +
                '}';
    }
}
