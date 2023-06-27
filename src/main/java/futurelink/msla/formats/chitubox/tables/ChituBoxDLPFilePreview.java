package futurelink.msla.formats.chitubox.tables;

import futurelink.msla.formats.MSLAPreview;
import futurelink.msla.formats.utils.Size;

import java.awt.image.BufferedImage;

public class ChituBoxDLPFilePreview implements MSLAPreview {
    private BufferedImage image;
    public static ChituBoxDLPFilePreview fromArray(int width, int height, int[] data) {
        var length = width * height;
        var p = new ChituBoxDLPFilePreview();
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
}
