package futurelink.msla.tools;

import futurelink.msla.formats.utils.FileFactory;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

public class ImageTools {
    public static void exportLayers(String fileName, String destinationDir, String format) throws IOException {
        var wsFile = FileFactory.load(fileName);
        if (wsFile != null) {
            System.out.println(wsFile);

            if (wsFile.isValid()) {
                var decodeWriter = new ImageWriter(wsFile, destinationDir, "png");

                // Start decoding layers
                for (int i = 0; i < wsFile.getLayerCount(); i++)
                    wsFile.readLayer(i, decodeWriter);
            }
        }
    }

    public static void createFromSVG(String machineName, String svgFileName, String outputFileName) throws IOException {
        var defaults = FileFactory.defaults(machineName);
        if (defaults == null) throw new IOException("Machine name '" + machineName + "' is incorrect");
        try (var stream = new FileInputStream(svgFileName)) {
            var reader = new BufferedReader(new InputStreamReader(stream));
            var svgImage = new TranscoderInput(reader);

            var resultByteStream = new ByteArrayOutputStream();
            var transcoderOutput = new TranscoderOutput(resultByteStream);

            var pngTranscoder = new PNGTranscoder();
            pngTranscoder.addTranscodingHint(PNGTranscoder.KEY_PIXEL_UNIT_TO_MILLIMETER, defaults.getPixelSizeUm() / 1000);
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
        var defaults = FileFactory.defaults(machineName);
        var wsFile = FileFactory.create(machineName);
        if (wsFile != null) {
            if (!wsFile.isValid()) throw new IOException("File header has no resolution info");
            wsFile.setOption("BottomExposureTime", 12);
            wsFile.addLayer(new ImageReader(wsFile, outputFileName + "." + defaults.getFileExtension(), image));
        }
    }
}
