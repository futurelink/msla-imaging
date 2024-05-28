package futurelink.msla.tools;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.iface.MSLAFile;
import futurelink.msla.formats.iface.MSLALayerDecodeWriter;
import futurelink.msla.formats.utils.Size;
import lombok.Setter;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Generic image writer implementation.
 * MSLADecodeWriter is necessary to get the data from mSLA data file layer and put
 * it into an image file.
 * This writer needs an {@link MSLAFile} file in order to get image resolution and write graphics properly.
 */
public class ImageWriter implements MSLALayerDecodeWriter {
    @Setter private MSLAFile<?> file;
    @Setter private String destinationDir;
    @Setter private String format;
    @Setter private Callback callback;

    private final ConcurrentHashMap<Integer, BufferedImage> img = new ConcurrentHashMap<>();
    private final String prefix;
    private static final Logger logger = Logger.getLogger(ImageWriter.class.getName());

    public interface Callback {
        @SuppressWarnings("unused")
        default void onStart(int layerNumber) {}
        void onFinish(int layerNumber, String fileName, int pixels);
        default void onError(int layerNumber, String error) throws MSLAException {
            throw new MSLAException("Error writing layer " + layerNumber + " : " + error);
        }
    }

    public ImageWriter(MSLAFile<?> file, String destinationDir, String prefix, String format) {
        this.file = file;
        this.destinationDir = destinationDir.endsWith(File.separator) ?
                destinationDir :
                destinationDir + File.separator;
        this.format = format;
        this.prefix = prefix;
    }

    public ImageWriter(MSLAFile<?> file, String destinationDir, String format) {
        this(file, destinationDir, "", format);
    }

    public ImageWriter(MSLAFile<?> file, String destinationDir, String format, Callback callback) {
        this(file, destinationDir, "", format);
        this.callback = callback;
    }

    @Override public Size getLayerResolution() { return file.getResolution(); }
    @Override public void stripe(int layerNumber, int color, int position, int length, WriteDirection direction)
            throws MSLAException
    {
        if (color < 0) throw new MSLAException("Color can't be equal to " + color);
        img.get(layerNumber).getGraphics().setColor(new Color(color, color, color));
        if (direction == WriteDirection.WRITE_COLUMN) {
            var y = position / file.getResolution().getWidth();
            var x = position % file.getResolution().getWidth();
            img.get(layerNumber).getGraphics().drawLine(x, file.getResolution().getHeight() - y, x,
                    file.getResolution().getHeight() - y - length);
        } else {
            var y = position / file.getResolution().getWidth();
            var x = position % file.getResolution().getWidth();
            img.get(layerNumber).getGraphics().drawLine(x, y, x + length - 1, y);
        }
    }

    @Override public void onStart(int layerNumber) throws MSLAException {
        if (file.getResolution() == null) throw new MSLAException("File resolution is undefined, can't write");
        logger.info("Layer " + layerNumber + " write started, size is " + file.getResolution());
        img.put(layerNumber, new BufferedImage(
                file.getResolution().getWidth(),
                file.getResolution().getHeight(),
                BufferedImage.TYPE_BYTE_GRAY
        ));
        if (callback != null) callback.onStart(layerNumber);
    }

    @Override public void onFinish(int layerNumber, int nonZeroPixels) throws MSLAException {
        try {
            var fileName = destinationDir + prefix + layerNumber + "." + format;
            logger.info("Layer " + layerNumber + " writing pixels: " + nonZeroPixels);
            logger.info("File name is " + fileName);
            var d = new File(destinationDir);
            if (!d.exists() && !d.mkdirs()) throw new IOException("Unable to create destination directory!");
            try (var f = new FileOutputStream(fileName)) {
                ImageIO.write(img.get(layerNumber), format, f);
                f.flush();
            }
            img.remove(layerNumber);
            if (callback != null) callback.onFinish(layerNumber, fileName, nonZeroPixels);
        } catch (IOException e) {
            throw new MSLAException("Error finalizing image file!", e);
        }
    }

    @Override
    public void onError(int layerNumber, String error) throws MSLAException {
        logger.info("Layer " + layerNumber + " write error: " + error);
        img.remove(layerNumber);
        if (callback != null) callback.onError(layerNumber, error);
    }
}
