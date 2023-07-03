package futurelink.msla.tools;

import futurelink.msla.formats.MSLAEncodeReader;
import futurelink.msla.formats.MSLAFile;
import futurelink.msla.formats.MSLAFileCodec;
import futurelink.msla.formats.utils.Size;

import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageReader implements MSLAEncodeReader {
    private final MSLAFile file;
    private final String outputFileName;
    private final BufferedImage image;
    public ImageReader(MSLAFile file, String outputFileName, BufferedImage image) {
        this.file = file;
        this.outputFileName = outputFileName;
        this.image = image;
    }
    @Override public MSLAFileCodec getCodec() {
        return file.getCodec();
    }
    @Override public Size getResolution() { return file.getResolution(); }
    @Override public InputStream read(int layerNumber, MSLAEncodeReader.ReadDirection direction) throws IOException {
        var w = file.getResolution().getWidth();
        var h = file.getResolution().getHeight();
        var outImage = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        outImage.getGraphics().drawImage(image,
                (w - image.getWidth()) / 2,
                (h - image.getHeight()) / 2,
                null);
        return new BufferedImageInputStream(outImage, direction);
    }
    @Override public void onStart(int layerNumber) {
        System.out.print("Encoding layer " + layerNumber + "... ");
    }
    @Override public void onFinish(int layerNumber, int pixels, int length) {
        System.out.println("done pixels: " + pixels + ", " + "bytes: " + length);

        // Encoding only one layer, so we write a file here.
        try (var fos = new FileOutputStream(outputFileName)) {
            file.write(fos);
        } catch (IOException e) {
            System.out.println("Error writing data: " + e.getMessage());
        }
    }
    @Override public void onError(int layerNumber, String error) {
        System.out.println("error: " + error);
    }
}
