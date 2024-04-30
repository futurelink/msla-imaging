package futurelink.msla.tools;

import futurelink.msla.formats.iface.MSLAFile;
import futurelink.msla.formats.iface.MSLALayerDecodeWriter;
import futurelink.msla.formats.iface.MSLAFileCodec;
import futurelink.msla.formats.utils.Size;
import lombok.Setter;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Generic image writer implementation.
 * MSLADecodeWriter is necessary to get the data from mSLA data file layer and put
 * it into an image file.
 */
public class ImageWriter implements MSLALayerDecodeWriter {
    @Setter private MSLAFile file;
    @Setter private String destinationDir;
    @Setter private String format;
    @Setter private OutputStream messageStream;
    private final ConcurrentHashMap<Integer, BufferedImage> img = new ConcurrentHashMap<>();
    public ImageWriter(MSLAFile file, String destinationDir, String format) {
        this.file = file;
        this.destinationDir = destinationDir;
        this.format = format;
        this.messageStream = System.out;
    }
    @Override public Class<? extends MSLAFileCodec> getCodec() {
        return file.getCodec();
    }
    @Override public Size getLayerResolution() { return file.getResolution(); }
    @Override public void stripe(int layerNumber, int color, int position, int length, WriteDirection direction) {
        img.get(layerNumber).getGraphics().setColor(new Color(color));
        if (direction == WriteDirection.WRITE_COLUMN) {
            var y = position / file.getResolution().getWidth();
            var x = position % file.getResolution().getWidth();
            img.get(layerNumber).getGraphics().drawLine(
                    x, file.getResolution().getHeight() - y, x,
                    file.getResolution().getHeight() - y - length);
        } else {
            var y = position / file.getResolution().getWidth();
            var x = position % file.getResolution().getWidth();
            img.get(layerNumber).getGraphics().drawLine(x, y, x + length - 1, y);
        }
    }

    @Override public void onStart(int layerNumber) {
        try { messageStream.write(("Layer " + layerNumber + " started\n").getBytes()); } catch (IOException ignored) {}
        img.put(layerNumber,new BufferedImage(
                file.getResolution().getWidth(),
                file.getResolution().getHeight(),
                BufferedImage.TYPE_BYTE_GRAY
        ));
    }

    @Override public void onFinish(int layerNumber, int pixels) throws IOException {
        messageStream.write(("Layer " + layerNumber + " done pixels: " + pixels + "\n").getBytes());
        var d = new File(destinationDir);
        if (!d.exists() && !d.mkdirs()) throw new IOException("Unable to create destination directory!");
        try (var f = new FileOutputStream(destinationDir + "/" + layerNumber + "." + format)) {
            ImageIO.write(img.get(layerNumber), format, f);
            f.flush();
        }
        img.remove(layerNumber);
    }

    @Override
    public void onError(int layerNumber, String error) throws IOException  {
        messageStream.write(("Layer " + layerNumber + " error: " + error + "\n").getBytes());
        img.remove(layerNumber);
    }
}
