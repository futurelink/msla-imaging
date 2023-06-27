package futurelink.msla.formats.chitubox.tables;

import futurelink.msla.formats.MSLAFileBlock;
import futurelink.msla.formats.MSLAPreview;
import futurelink.msla.formats.utils.Size;
import lombok.Getter;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ChituBoxDLPFilePreviews implements MSLAFileBlock {
    @Getter List<ChituBoxDLPFilePreview> previews = new ArrayList<>();
    public Size[] PreviewOriginalSizes = {
            new Size(116, 116),
            new Size(290, 290),
            new Size(290, 290)
    };
    public MSLAPreview getPreview(int index) {
        return previews.get(index);
    }
    @Override
    public int getDataLength() {
        return (PreviewOriginalSizes[0].length() + PreviewOriginalSizes[1].length() + PreviewOriginalSizes[2].length()) * 2 + 6;
    }
    @Override
    public void read(FileInputStream stream, int position) throws IOException {
        var fc = stream.getChannel();
        fc.position(position);

        var dis = new DataInputStream(stream);
        for (var size : PreviewOriginalSizes) {
            var data = new int[size.length()];
            for (int i = 0; i < size.length(); i++) data[i] = dis.readShort();
            previews.add(ChituBoxDLPFilePreview.fromArray(size.getWidth(), size.getHeight(), data));
            dis.readNBytes(2); // PageBreak 0x0d 0x0a
        }
    }
    @Override
    public void write(OutputStream stream) throws IOException {

    }
}
