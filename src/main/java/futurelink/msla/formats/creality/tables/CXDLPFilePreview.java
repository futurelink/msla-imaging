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
    private final BufferedImage image;
    private final Size resolution;

    @MSLAFileField(lengthAt = "getImageDataLength") Short[] ImageData() {
        var data =  new Short[getResolution().length()];
        if (image == null) return data;
        var length = getImageDataLength();
        for (int i = 0; i < length; i++)
            data[i] = (short) image.getRaster().getDataBuffer().getElem(i);
        return data;
    }
    @MSLAFileField(order = 1, length = 2) byte[] PageBreak = new byte[]{ 0x0d, 0x0a };

    void setImageData(Short[] imageData) {
        var length = getImageDataLength();
        for (int i = 0; i < length; i++) image.getRaster().getDataBuffer().setElem(i, imageData[i]);
    }

    public int getImageDataLength() { return this.resolution.getWidth() * this.resolution.getHeight(); }

    public CXDLPFilePreview(Size resolution) {
        this.resolution = new Size(resolution);
        this.image = new BufferedImage(
                resolution.getWidth(),
                resolution.getHeight(),
                BufferedImage.TYPE_USHORT_565_RGB);
    }

    @Override
    public String toString() {
        return "CXDLPFilePreview { " + getResolution() + " }";
    }
}
