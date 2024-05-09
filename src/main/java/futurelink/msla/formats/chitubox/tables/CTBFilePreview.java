package futurelink.msla.formats.chitubox.tables;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.iface.MSLAFileBlock;
import futurelink.msla.formats.iface.MSLAFileBlockFields;
import futurelink.msla.formats.iface.MSLAPreview;
import futurelink.msla.formats.iface.annotations.MSLAFileField;
import futurelink.msla.formats.utils.FileFieldsIO;
import futurelink.msla.formats.utils.Size;
import lombok.Getter;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

@Getter
public class CTBFilePreview implements MSLAFileBlock, MSLAPreview {
    private BufferedImage image;
    private final Fields fields = new Fields();
    private final short REPEAT_MASK_RGB565 = 0x20;

    @Getter
    @SuppressWarnings("unused")
    static class Fields implements MSLAFileBlockFields {
        Size Resolution = new Size(0,0);
        @MSLAFileField private Integer ResolutionX() { return Resolution.getWidth(); }
        void setResolutionX(Integer value) { Resolution = new Size(value, Resolution.getHeight()); }
        @MSLAFileField(order = 1) private Integer ResolutionY() { return Resolution.getHeight(); }
        void setResolutionY(Integer value) { Resolution = new Size(Resolution.getWidth(), value); }
        @MSLAFileField(order = 2) private Integer ImageOffset;
        @MSLAFileField(order = 3) private Integer ImageLength;
        @MSLAFileField(order = 4) private final Integer Unknown1 = 0;
        @MSLAFileField(order = 5) private final Integer Unknown2 = 0;
        @MSLAFileField(order = 6) private final Integer Unknown3 = 0;
        @MSLAFileField(order = 7) private final Integer Unknown4 = 0;
    }

    public CTBFilePreview() {
        fields.Resolution = new Size(0,0);
        fields.ImageLength = 0;
        fields.ImageOffset = 0;
    }

    @Override
    public void afterRead() {
        image = new BufferedImage(fields.ResolutionX(), fields.ResolutionY(), BufferedImage.TYPE_INT_RGB);
    }

    @Override
    public void beforeWrite() {
        fields.ImageLength = fields.Resolution.length(); // Set image length in bytes
    }

    public final long readImage(FileInputStream stream) throws MSLAException {
        try {
            var fc = stream.getChannel(); fc.position(fields.ImageOffset);
            byte[] imageData = stream.readNBytes(fields.getImageLength());
            if (imageData.length != fields.ImageLength)
                throw new MSLAException("Error reading preview image data, it was not fully read");
            return Decode(imageData);
        } catch (IOException e) { throw new MSLAException("Error reading preview image data", e); }
    }

    private int Decode(byte[] rawImageData) {
        int pixel = 0;
        var buffer = image.getRaster().getDataBuffer();
        for (int n = 0; n < rawImageData.length; n++) {
            int dot = (rawImageData[n] & 0xFF | ((rawImageData[++n] & 0xFF) << 8));
            int red = (((dot >> 11) & 0x1F) << 3) & 0xFF;
            int green = (((dot >> 6) & 0x1F) << 3) & 0xFF;
            int blue = ((dot & 0x1F) << 3) & 0xFF;
            int repeat = 1;
            if ((dot & 0x0020) == 0x0020) repeat += rawImageData[++n] & 0xFF | ((rawImageData[++n] & 0x0F) << 8);
            for (int j = 0; j < repeat; j++) buffer.setElem(0, pixel++, (red << 16) | (green << 8) | blue);
        }
        return pixel;
    }

    private void RleRGB565(List<Byte> data, int rep,  int color15) {
        switch (rep) {
            case 0: return;
            case 1:
                data.add((byte)(color15 & ~REPEAT_MASK_RGB565));
                data.add((byte)((color15 & ~REPEAT_MASK_RGB565) >> 8));
                break;
            case 2:
                for (int i = 0; i < 2; i++) {
                    data.add((byte)(color15 & ~REPEAT_MASK_RGB565));
                    data.add((byte)((color15 & ~REPEAT_MASK_RGB565) >> 8));
                }
                break;
            default:
                data.add((byte)(color15 | REPEAT_MASK_RGB565));
                data.add((byte)((color15 | REPEAT_MASK_RGB565) >> 8));
                data.add((byte)((rep - 1) | 0x3000));
                data.add((byte)(((rep - 1) | 0x3000) >> 8));
                break;
        }
    }

    public Byte[] Encode() {
        List<Byte> rawData = new LinkedList<>();
        int rep = 0;
        int pixel = 0;
        int color_565 = 0;
        var buffer = image.getRaster().getDataBuffer();
        while (pixel < buffer.getSize()) {
            int pixelColor = buffer.getElem(pixel++);
            int n_color_565 = (pixelColor >> 3) | ((pixelColor >> 2) << 5) | ((pixelColor >> 3) << 11); // BGR
            if (n_color_565 == color_565) {
                if (++rep == 0xFFF) { RleRGB565(rawData, rep, color_565); rep = 0; }
            } else {
                RleRGB565(rawData, rep, color_565);
                color_565 = n_color_565;
                rep = 1;
            }
        }

        RleRGB565(rawData, rep, color_565);
        fields.ImageLength = rawData.size();

        return rawData.toArray(Byte[]::new);
    }

    @Override public Size getResolution() { return fields.Resolution; }
    @Override public FileFieldsIO.Endianness getEndianness() { return FileFieldsIO.Endianness.LittleEndian; }
    @Override public int getDataLength() { return 0; }
    @Override public String toString() { return fields.fieldsAsString(" = ", "\n"); }
}
