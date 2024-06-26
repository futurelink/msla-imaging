package io.msla.formats;

import io.msla.formats.iface.MSLALayerEncodeReader;
import lombok.Getter;

import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.IOException;
import java.io.InputStream;

public class BufferedImageInputStream extends InputStream {
    @Getter private BufferedImage image;
    private DataBuffer rotatedBuffer;
    private int position;
    private MSLALayerEncodeReader.ReadDirection readDirection;

    public final void setReadDirection(MSLALayerEncodeReader.ReadDirection direction) {
        readDirection = direction;
        BufferedImage rotatedImage;
        if (readDirection == MSLALayerEncodeReader.ReadDirection.READ_COLUMN) {
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
            image = rotatedImage;
        }
        rotatedBuffer = image.getRaster().getDataBuffer();
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
    public synchronized void reset() throws IOException {
        position = 0;
    }

    @Override
    public int read() throws IOException {
        if (rotatedBuffer.getSize() > position) return rotatedBuffer.getElem(position++);
        return -1;
    }

    @Override
    public int available() {
        return rotatedBuffer.getSize() - position;
    }
}
