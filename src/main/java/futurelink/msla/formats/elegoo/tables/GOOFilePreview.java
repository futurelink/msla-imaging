package futurelink.msla.formats.elegoo.tables;

import futurelink.msla.formats.iface.MSLAFileBlockFields;
import futurelink.msla.formats.iface.annotations.MSLAFileField;
import futurelink.msla.formats.iface.MSLAPreview;
import futurelink.msla.formats.utils.Size;

import java.awt.image.BufferedImage;

@SuppressWarnings("unused")
public class GOOFilePreview implements MSLAFileBlockFields, MSLAPreview {
    private final Size Resolution;
    private BufferedImage image;

    int getImageLength() { return Resolution.length() * 2; }
    @MSLAFileField(lengthAt = "getImageLength") byte[] Data;
    @MSLAFileField(order = 1, length = 2) byte[] Delimiter = new byte[]{ 0x0d, 0x0a };

    GOOFilePreview(Size resolution) {
        this.Resolution = new Size(resolution);
        this.Data = new byte[getImageLength()];
    }

    @Override public BufferedImage getImage() { return image; }
    @Override public Size getResolution() { return Resolution; }

    @Override
    public String toString() { return "GOOFilePreview { " + Resolution + " }"; }
}
