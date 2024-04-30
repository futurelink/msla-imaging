package futurelink.msla.tools;

import futurelink.msla.formats.iface.MSLALayerEncodeReader;
import futurelink.msla.formats.iface.MSLAFile;
import futurelink.msla.formats.iface.MSLAFileCodec;
import futurelink.msla.formats.utils.Size;
import lombok.Setter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * Generic image reader and encoder.
 * MSLAEncodeReader instance is necessary to read the graphics data and encode it to a
 * mSLA data file layer. That is exactly what this class does.
 */
public class ImageReader implements MSLALayerEncodeReader {
    private final MSLAFile file;
    private final BufferedImage image;
    @Setter private OutputStream messageStream;

    public ImageReader(MSLAFile file, String fileName) throws IOException {
        try (var pngImage = new FileInputStream(fileName)) {
            this.file = file;
            this.image = ImageIO.read(pngImage);
            this.messageStream = System.out;
        }
    }

    public ImageReader(MSLAFile file, BufferedImage image) {
        this.file = file;
        this.image = image;
        this.messageStream = System.out;
    }

    @Override public Class<? extends MSLAFileCodec> getCodec() {
        return file.getCodec();
    }
    @Override public Size getResolution() { return file.getResolution(); }
    @Override public InputStream read(int layerNumber, MSLALayerEncodeReader.ReadDirection direction) throws IOException {
        var w = file.getResolution().getWidth();
        var h = file.getResolution().getHeight();
        var outImage = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        outImage.getGraphics().drawImage(image,
                (w - image.getWidth()) / 2,
                (h - image.getHeight()) / 2,
                null);
        return new BufferedImageInputStream(outImage, direction);
    }

    @Override public void onStart(int layerNumber) throws IOException {
        messageStream.write(("Encoding layer " + layerNumber + "... ").getBytes());
    }

    @Override public void onFinish(int layerNumber, int pixels, int length) throws IOException {
        messageStream.write(("Done pixels: " + pixels + ", " + "bytes: " + length).getBytes());
    }

    @Override public void onError(int layerNumber, String error) throws IOException {
        messageStream.write(("Error: " + error).getBytes());
    }
}
