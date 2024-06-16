package futurelink.msla.formats.elegoo.tables;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.iface.MSLAFileBlock;
import futurelink.msla.formats.iface.MSLAFileBlockFields;
import futurelink.msla.formats.iface.MSLAFileField;
import futurelink.msla.formats.iface.MSLAPreview;
import futurelink.msla.formats.io.FileFieldsException;
import futurelink.msla.utils.Size;
import lombok.Getter;
import lombok.Setter;

import java.awt.image.BufferedImage;

@SuppressWarnings("unused")
@Getter
public class GOOFilePreview implements MSLAFileBlock, MSLAPreview {
    private BufferedImage Image;
    private final Fields blockFields = new Fields();

    @Getter
    public static class Fields implements MSLAFileBlockFields {
        private Size Resolution;
        int getImageLength() { return Resolution.length() * 2; }
        @Setter @MSLAFileField(lengthAt = "getImageLength") byte[] Data;
        @MSLAFileField(order = 1, length = 2) byte[] Delimiter = new byte[]{ 0x0d, 0x0a };
    }

    public GOOFilePreview(Size resolution) {
        this.blockFields.Resolution = new Size(resolution);
        this.blockFields.Data = new byte[this.getBlockFields().getImageLength()];
        this.Image = new BufferedImage(resolution.getWidth(), resolution.getHeight(), BufferedImage.TYPE_USHORT_565_RGB);
    }

    @Override public BufferedImage getImage() { return Image; }
    @Override public void setImage(BufferedImage image) {
        this.Image = new BufferedImage(
                blockFields.Resolution.getWidth(),
                blockFields.Resolution.getHeight(),
                BufferedImage.TYPE_USHORT_565_RGB);
        if (image != null) { Image.getGraphics().drawImage(image,0, 0,  null); }
    }
    @Override public Size getResolution() { return this.getBlockFields().Resolution; }

    @Override public String getName() { return null; }
    @Override public int getDataLength() throws FileFieldsException { return 0; }
    @Override public int getDataFieldOffset(String fieldName) throws FileFieldsException { return 0; }

    @Override
    public void afterRead() {
        var fields = getBlockFields();
        this.Image = new BufferedImage(getResolution().getWidth(), getResolution().getHeight(), BufferedImage.TYPE_USHORT_565_RGB);
        var buffer = this.Image.getRaster().getDataBuffer();
        for (int i = 0; i < fields.getImageLength(); i+=2) {
            int val = ((fields.getData()[i+1] & 0xff) << 8) | (fields.getData()[i] & 0xff);
            // Convert BGR to RGB
            var b = (val >> 11) & 0x1f;
            var g = (val >> 5) & 0x3f;
            var r = val & 0x1f;
            buffer.setElem(i / 2, (r << 11) | (g << 5) | b);
        }
    }

    @Override public void beforeWrite() throws MSLAException {
        var fields = getBlockFields();
        var buffer = getImage().getData().getDataBuffer();
        var size = buffer.getSize() * 2;
        if (fields.getImageLength() != size)
            throw new MSLAException("Preview size " + size + " does not match resolution size " +
                    getBlockFields().getImageLength());

        fields.setData(new byte[size]);
        for (int i = 0; i < size; i+=2) {
            // Convert RGB to BGR
            int elem = buffer.getElem(i / 2);
            var r = (elem >> 11) & 0x1f;
            var g = (elem >> 5) & 0x3f;
            var b = elem & 0x1f;
            elem = (b << 11) | (g << 5) | r;
            fields.getData()[i] = (byte) ((elem >> 8) & 0xff);
            fields.getData()[i+1] = (byte) (elem & 0xff);
        }
    }

    @Override
    public String toString() { return "GOOFilePreview { " + getResolution() + " }"; }
}
