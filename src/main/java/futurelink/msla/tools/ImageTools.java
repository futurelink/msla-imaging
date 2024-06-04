package futurelink.msla.tools;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.utils.FileFactory;
import futurelink.msla.utils.FileOptionMapper;
import futurelink.msla.utils.defaults.PrinterDefaults;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

@SuppressWarnings("unused")
public class ImageTools {

    public static void exportLayers(String fileName, String destinationDir, String format)
            throws MSLAException, InterruptedException
    {
        var wsFile = FileFactory.instance.load(fileName);
        if (wsFile != null) {
            if (wsFile.isValid()) {
                for (int i = 0; i < wsFile.getLayers().count(); i++)
                    // Wait while layer can be read (returns false when busy)
                    while (!wsFile.readLayer(new ImageWriter(wsFile, destinationDir, "png"), i))
                        Thread.sleep(100);
            }
        }
    }

    public static void createFromSVG(String machineName, String svgFileName, String outputFileName)
            throws IOException, MSLAException {
        var defaults = PrinterDefaults.instance.getPrinter(machineName)
                .orElseThrow(() -> new MSLAException("Printer has no defaults: " + machineName));
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

    public static void createFromPNG(String machineName, String pngFileName, String outputFileName)
            throws MSLAException, IOException  {
        try (var pngImage = new FileInputStream(pngFileName)) {
            var image = ImageIO.read(pngImage);
            createFromBufferedImage(machineName, image, outputFileName);
        }
    }

    @SuppressWarnings("unchecked")
    public static void createFromBufferedImage(String machineName, BufferedImage image, String outputFileName)
            throws MSLAException  {
        var defaults = PrinterDefaults.instance.getPrinter(machineName)
                .orElseThrow(() -> new MSLAException("Printer has no defaults: " + machineName));
        var wsFile = FileFactory.instance.create(machineName);
        if (!wsFile.isValid()) throw new MSLAException("File header has no resolution info");
        var options = new FileOptionMapper(wsFile, defaults);
        options.set("BottomExposureTime", "12");
        wsFile.addLayer(new ImageReader(wsFile, image), null);
    }
}
