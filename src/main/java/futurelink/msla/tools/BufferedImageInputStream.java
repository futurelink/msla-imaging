package futurelink.msla.tools;

import futurelink.msla.formats.MSLAEncodeReader;

import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class BufferedImageInputStream extends InputStream {
    private final BufferedImage image;
    private DataBuffer rotatedBuffer;
    private int position;
    private MSLAEncodeReader.ReadDirection readDirection;

    public final void setReadDirection(MSLAEncodeReader.ReadDirection direction) {
        readDirection = direction;
        BufferedImage rotatedImage;
        if (readDirection == MSLAEncodeReader.ReadDirection.READ_ROW) {
            rotatedImage = image;
        } else {
            //System.out.print("Rotating image...");
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
            //System.out.println("done");

            /*try { ImageIO.write(rotatedImage, "png", new FileOutputStream("sample_data/ttt2.png"));
            } catch (IOException e) { throw new RuntimeException(e); }*/
        }
        rotatedBuffer = rotatedImage.getRaster().getDataBuffer();
    }

    public BufferedImageInputStream(BufferedImage img, MSLAEncodeReader.ReadDirection mode) {
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
