package futurelink.msla.formats.creality.tables;

import futurelink.msla.formats.iface.MSLAFileBlockFields;
import futurelink.msla.formats.iface.annotations.MSLAFileField;
import futurelink.msla.formats.iface.MSLAPreview;
import futurelink.msla.formats.utils.Size;
import lombok.Getter;

import java.awt.image.BufferedImage;

@Getter
@SuppressWarnings("unused")
public class CXDLPFilePreview implements MSLAFileBlockFields, MSLAPreview {
    private BufferedImage Image;
    private final Size resolution;

    @MSLAFileField(lengthAt = "getImageDataLength") Short[] ImageData() {
        var data =  new Short[getResolution().length()];
        if (Image == null) return data;
        var length = getImageDataLength();
        for (int i = 0; i < length; i++)
            data[i] = (short) Image.getRaster().getDataBuffer().getElem(i);
        return data;
    }
    @MSLAFileField(order = 1, length = 2) byte[] PageBreak = new byte[]{ 0x0d, 0x0a };

    void setImageData(Short[] imageData) {
        var length = getImageDataLength();
        for (int i = 0; i < length; i++) {
            // Assume we've got RBG555 format
            var r = (imageData[i] >> 10) & 0x1f;
            var b = (imageData[i] >> 5) & 0x1f;
            var g = imageData[i] & 0x1f;
            Image.getRaster().getDataBuffer().setElem(i, (r << 10) | (g << 5) | b);
        }
    }

    public int getImageDataLength() { return this.resolution.getWidth() * this.resolution.getHeight(); }

    public CXDLPFilePreview(Size resolution) {
        this.resolution = new Size(resolution);
        setImage(null);
    }

    @Override public void setImage(BufferedImage image) {
        Image = new BufferedImage(
                resolution.getWidth(),
                resolution.getHeight(),
                BufferedImage.TYPE_USHORT_555_RGB);
        if (image != null) Image.getGraphics().drawImage(image, 0, 0, null);
    }

    @Override public String toString() { return "CXDLPFilePreview { " + getResolution() + " }"; }
}
