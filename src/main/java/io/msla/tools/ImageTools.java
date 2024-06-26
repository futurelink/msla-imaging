package io.msla.tools;

import io.msla.formats.MSLAException;
import io.msla.formats.iface.options.MSLAOptionName;
import io.msla.utils.FileFactory;
import io.msla.utils.options.FileOptionMapper;
import io.msla.utils.defaults.MachineDefaults;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * Simple image-to-mSLA manipulation tools.
 */
@SuppressWarnings("unused")
public class ImageTools {

    /**
     * Exports all layers from specified mSLA file and saves them into a folder. Target files are named
     * according to layer numbers.
     * @param fileName source file name
     * @param destinationDir a directory to save layer images
     * @param format format of output layer images
     */
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

    /**
     * Creates a single-layer mSLA file using SVG image as source.
     * @param machineName machine name
     * @param svgFileName source SVG file name
     * @param outputFileName output target file name
     */
    public static void createFromSVG(String machineName, String svgFileName, String outputFileName)
            throws IOException, MSLAException {
        var defaults = MachineDefaults.getInstance().getMachineDefaults(machineName)
                .orElseThrow(() -> new MSLAException("Printer has no defaults: " + machineName));
        try (var stream = new FileInputStream(svgFileName)) {
            var reader = new BufferedReader(new InputStreamReader(stream));
            var svgImage = new TranscoderInput(reader);

            var resultByteStream = new ByteArrayOutputStream();
            var transcoderOutput = new TranscoderOutput(resultByteStream);

            var pngTranscoder = new PNGTranscoder();
            pngTranscoder.addTranscodingHint(PNGTranscoder.KEY_PIXEL_UNIT_TO_MILLIMETER, defaults.getPixelSize() / 1000);
            pngTranscoder.transcode(svgImage, transcoderOutput);

            var image = ImageIO.read(new ByteArrayInputStream(resultByteStream.toByteArray()));
            createFromBufferedImage(machineName, image, outputFileName);
        } catch (TranscoderException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a single-layer mSLA file using PNG image as source.
     * @param machineName machine name
     * @param pngFileName source PNG file name
     * @param outputFileName output target file name
     */
    public static void createFromPNG(String machineName, String pngFileName, String outputFileName)
            throws MSLAException, IOException  {
        try (var pngImage = new FileInputStream(pngFileName)) {
            var image = ImageIO.read(pngImage);
            createFromBufferedImage(machineName, image, outputFileName);
        }
    }

    /**
     * Creates a single-layer mSLA file using {@code BufferedImage} as source.
     * @param machineName machine name
     * @param image source image
     * @param outputFileName output target file name
     */
    public static void createFromBufferedImage(String machineName, BufferedImage image, String outputFileName)
            throws MSLAException  {
        var defaults = MachineDefaults.getInstance().getMachineDefaults(machineName)
                .orElseThrow(() -> new MSLAException("Printer has no defaults: " + machineName));
        var wsFile = FileFactory.instance.create(machineName);
        if (!wsFile.isValid()) throw new MSLAException("File header has no resolution info");
        var options = new FileOptionMapper(wsFile, defaults);
        options.set(MSLAOptionName.BottomLayersExposureTime, "12");
        wsFile.addLayer(new ImageReader(wsFile, image), null);
    }
}
