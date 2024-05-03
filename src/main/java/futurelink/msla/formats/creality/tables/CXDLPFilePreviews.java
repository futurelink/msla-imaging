package futurelink.msla.formats.creality.tables;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.iface.MSLAFileBlock;
import futurelink.msla.formats.iface.MSLAPreview;
import futurelink.msla.formats.utils.Size;
import lombok.Getter;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CXDLPFilePreviews implements MSLAFileBlock {
    @Getter List<CXDLPFilePreview> previews = new ArrayList<>();
    public Size[] PreviewOriginalSizes = {
            new Size(116, 116),
            new Size(290, 290),
            new Size(290, 290)
    };
    public MSLAPreview getPreview(int index) {
        return previews.size() > index ? previews.get(index) : null;
    }
    @Override
    public int getDataLength() {
        return (PreviewOriginalSizes[0].length() + PreviewOriginalSizes[1].length() + PreviewOriginalSizes[2].length()) * 2 + 6;
    }
    @Override
    public void read(FileInputStream stream, int position) throws MSLAException {
        var fc = stream.getChannel();
        try {
            fc.position(position);

            var dis = new DataInputStream(stream);
            for (var size : PreviewOriginalSizes) {
                var data = new int[size.length()];
                for (int i = 0; i < size.length(); i++) data[i] = dis.readShort();
                previews.add(CXDLPFilePreview.fromArray(size.getWidth(), size.getHeight(), data));
                dis.readNBytes(2); // PageBreak 0x0d 0x0a
            }
        } catch (IOException e) {
            throw new MSLAException("Can't read preview", e);
        }
    }
    @Override
    public void write(OutputStream stream) throws MSLAException {
        // Write dummy null previews
        var dos = new DataOutputStream(stream);
        try {
            for (Size previewOriginalSize : PreviewOriginalSizes) {
                var length = previewOriginalSize.length();
                for (int l = 0; l < length; l++) dos.writeShort(0);
                dos.write(0x0d);
                dos.write(0x0a); // Page break
            }
        } catch (IOException e) {
            throw new MSLAException("Can't write preview", e);
        }
    }

    @Override
    public String toString() {
        return "CXDLPFilePreviews {" + previews + '}';
    }
}
