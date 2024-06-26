package io.msla.tools;

import io.msla.formats.BufferedImageInputStream;
import io.msla.formats.MSLAException;
import io.msla.formats.iface.*;
import io.msla.utils.Size;
import lombok.Setter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.logging.Logger;

/**
 * Generic image reader and encoder.
 * MSLAEncodeReader instance is necessary to read the graphics data and encode it to a
 * mSLA data file layer. That is exactly what this class does.
 */
public class ImageReader implements MSLALayerEncodeReader {
    private final MSLAFile<?> file;
    private final BufferedImage image;
    private int size;
    private static final Logger logger = Logger.getLogger(ImageReader.class.getName());
    @Setter private ReadDirection readDirection = MSLALayerEncodeReader.ReadDirection.READ_ROW;
    @Setter Callback callback;

    public interface Callback {
        void onStart(int layerNumber);
        void onFinish(int layerNumber, int pixels);
        void onError(int layerNumber, String error);
    }

    public ImageReader(MSLAFile<?> file, String fileName) throws IOException {
        try (var pngImage = new FileInputStream(fileName)) {
            this.file = file;
            this.image = ImageIO.read(pngImage);
        }
    }

    public ImageReader(MSLAFile<?> file, BufferedImage image) {
        this.file = file;
        this.image = image;
    }

    @Override public Size getResolution() { return file.getResolution(); }

    @Override public int getSize() { return size; }

    @Override public BufferedImage read(int layerNumber) throws MSLAException {
        var w = file.getResolution().getWidth();
        var h = file.getResolution().getHeight();
        var outImage = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        outImage.getGraphics().drawImage(image,
                (w - image.getWidth()) / 2,
                (h - image.getHeight()) / 2,
                null);
        try (var out = new BufferedImageInputStream(outImage, readDirection)) {
            this.size = out.available();
            return outImage;
        } catch (IOException e) {
            throw new MSLAException("Error reading image", e);
        }
    }

    @Override public void onStart(int layerNumber) {
        logger.info("Reading layer " + layerNumber + "...");
        if (callback != null) callback.onStart(layerNumber);
    }

    @Override public void onFinish(int layerNumber, int pixels, MSLALayerEncodeOutput<?> output) {
        logger.info("Done read layer " + layerNumber + " pixels: " + pixels + ", " + "bytes: " + output.sizeInBytes());
        if (callback != null) callback.onFinish(layerNumber, pixels);
    }

    @Override public void onError(int layerNumber, String error) {
        logger.info("Error reading layer " + layerNumber + ": " + error);
        if (callback != null) callback.onError(layerNumber, error);
    }
}
