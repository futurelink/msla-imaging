package futurelink.msla.tools;

import futurelink.Main;
import futurelink.msla.formats.MSLAEncodeReader;
import futurelink.msla.formats.MSLAFileCodec;
import futurelink.msla.formats.anycubic.PhotonWorkshopFile;
import futurelink.msla.formats.anycubic.PhotonWorkshopFileDefaults;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

public class ImageTools {
    public static void exportLayers(String fileName, String destinationDir, String format) throws IOException {
        try (var fis = new FileInputStream(fileName)) {
            var wsFile = new PhotonWorkshopFile(fis);

            System.out.println(wsFile);

            if (wsFile.isValid()) {
                var decodeWriter = new ImageWriter(wsFile, destinationDir, "png");

                // Start decoding layers
                for (int i = 0; i < wsFile.getLayerCount(); i++)
                    wsFile.readLayer(fis, i, decodeWriter);
            }
        }
    }

    public static void createFromSVG(String machineName, String svgFileName, String outputFileName) throws IOException {
        var defaults = PhotonWorkshopFileDefaults.get(machineName);
        if (defaults == null) throw new IOException("Machine name '" + machineName + "' is incorrect");
        try (var stream = new FileInputStream(svgFileName)) {
            var reader = new BufferedReader(new InputStreamReader(stream));
            var svgImage = new TranscoderInput(reader);

            var resultByteStream = new ByteArrayOutputStream();
            var transcoderOutput = new TranscoderOutput(resultByteStream);

            var pngTranscoder = new PNGTranscoder();
            pngTranscoder.addTranscodingHint(PNGTranscoder.KEY_PIXEL_UNIT_TO_MILLIMETER, defaults.getHeader().getPixelSizeUm() / 1000);
            pngTranscoder.transcode(svgImage, transcoderOutput);

            var image = ImageIO.read(new ByteArrayInputStream(resultByteStream.toByteArray()));
            createFromBufferedImage(machineName, image, outputFileName);
        } catch (TranscoderException e) {
            throw new RuntimeException(e);
        }
    }

    public static void createFromPNG(String machineName, String pngFileName, String outputFileName) throws IOException  {
        try (var pngImage = new FileInputStream(pngFileName)) {
            var image = ImageIO.read(pngImage);
            createFromBufferedImage(machineName, image, outputFileName);
        }
    }

    public static void createFromBufferedImage(String machineName, BufferedImage image, String outputFileName) throws IOException  {
        var defaults = PhotonWorkshopFileDefaults.get(machineName);
        var wsFile = new PhotonWorkshopFile(defaults);
        if (!wsFile.isValid()) throw new IOException("File header has no resolution info");
        wsFile.setOption("BottomExposureTime", 12);

        // Create preview image
        wsFile.addLayer(new MSLAEncodeReader() {
            @Override public MSLAFileCodec getCodec() {
                return wsFile.getCodec();
            }
            @Override public InputStream read(int layerNumber) throws IOException {
                var w = wsFile.getResolution().getWidth();
                var h = wsFile.getResolution().getHeight();
                var outImage = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
                outImage.getGraphics().drawImage(image,
                        (w - image.getWidth()) / 2,
                        (h - image.getHeight()) / 2,
                        null);
                return new Main.RasterBytesInputStream(outImage.getRaster());
            }
            @Override public void onStart(int layerNumber) {
                System.out.print("Encoding layer " + layerNumber + "... ");
            }
            @Override public void onFinish(int layerNumber, int pixels, int length) {
                System.out.println("done pixels: " + pixels + ", " + "bytes: " + length);

                // Encoding only one layer, so we write a file here.
                try (var fos = new FileOutputStream(outputFileName + "." + defaults.getFileExtension())) {
                    wsFile.write(fos);
                } catch (IOException e) {
                    System.out.println("Error writing data: " + e.getMessage());
                }
            }
            @Override public void onError(int layerNumber, String error) {
                System.out.println("error: " + error);
            }
        });
    }
}
