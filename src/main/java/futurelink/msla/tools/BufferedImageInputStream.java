package futurelink.msla.tools;

import futurelink.msla.formats.iface.MSLALayerEncodeReader;

import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.IOException;
import java.io.InputStream;

public class BufferedImageInputStream extends InputStream {
    private final BufferedImage image;
    private DataBuffer rotatedBuffer;
    private int position;
    private MSLALayerEncodeReader.ReadDirection readDirection;

    public final void setReadDirection(MSLALayerEncodeReader.ReadDirection direction) {
        readDirection = direction;
        BufferedImage rotatedImage;
        if (readDirection == MSLALayerEncodeReader.ReadDirection.READ_ROW) {
            rotatedImage = image;
        } else {
            rotatedImage = new BufferedImage(image.getHeight(), image.getWidth(), BufferedImage.TYPE_USHORT_GRAY);
            var g = rotatedImage.createGraphics();
            var at = new AffineTransform();
            at.translate(
                    (rotatedImage.getWidth() - image.getWidth()) / 2.0f,
                    (rotatedImage.getHeight() - image.getHeight()) / 2.0f);

            at.rotate(Math.toRadians(90), image.getWidth() / 2.0f, image.getHeight() / 2.0f);
            g.setTransform(at);
            g.drawImage(image, 0, 0, null);
            g.dispose();
        }
        rotatedBuffer = rotatedImage.getRaster().getDataBuffer();
    }

    public BufferedImageInputStream(BufferedImage img, MSLALayerEncodeReader.ReadDirection mode) {
        readDirection = mode;
        image = img;
        if (image.getRaster().getNumDataElements() > 1)
            throw new RuntimeException("BufferedImageInputStream can't be used with images " +
                    "that require more than one byte per pixel");
        setReadDirection(mode);
    }

    @Override
    public int read() throws IOException {
        return rotatedBuffer.getElem(position++);
    }

    @Override
    public int available() {
        return rotatedBuffer.getSize() - position;
    }
}
