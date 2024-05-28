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
            // We've got RGB565 format. If one can see a file created by Chitubox has
            // wrong colors that's because it creates it incorrectly.
            Image.getRaster().getDataBuffer().setElem(i, (imageData[i] & 0xff00) >> 8 | (imageData[i] & 0xff) << 8);
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
                BufferedImage.TYPE_USHORT_565_RGB);
        if (image != null) Image.getGraphics().drawImage(image, 0, 0, null);
    }

    @Override public String toString() { return "CXDLPFilePreview { " + getResolution() + " }"; }
}
