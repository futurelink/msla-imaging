package futurelink.msla.formats.creality.tables;

import futurelink.msla.formats.iface.MSLAFileBlockFields;
import futurelink.msla.formats.iface.MSLAPreview;
import futurelink.msla.formats.utils.Size;

import java.awt.image.BufferedImage;

public class CXDLPFilePreview implements MSLAPreview {
    private BufferedImage image;
    public static CXDLPFilePreview fromArray(int width, int height, int[] data) {
        var length = width * height;
        var p = new CXDLPFilePreview();
        p.image = new BufferedImage(width, height, BufferedImage.TYPE_USHORT_565_RGB);
        for (int i = 0; i < length; i++) {
            p.image.getRaster().getDataBuffer().setElem(i, data[i]);
        }
        return p;
    }
    @Override public BufferedImage getImage() {
        return image;
    }
    @Override public Size getResolution() {
        return new Size(image.getWidth(), image.getHeight());
    }


    @Override
    public String toString() {
        return "CXDLPFilePreview { " + getResolution() + " }";
    }
}
