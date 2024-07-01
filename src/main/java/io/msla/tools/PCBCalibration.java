package io.msla.tools;

import io.msla.formats.*;
import io.msla.formats.iface.MSLAFile;
import io.msla.formats.iface.MSLALayerEncodeOutput;
import io.msla.formats.iface.MSLALayerEncodeReader;
import io.msla.formats.iface.options.MSLAOptionName;
import io.msla.utils.FileFactory;
import io.msla.utils.options.FileOptionMapper;
import io.msla.utils.Size;
import io.msla.utils.defaults.MachineDefaults;
import lombok.Setter;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * PCB calibration creator class.
 */
public class PCBCalibration {
    private static final Logger logger = Logger.getLogger(PCBCalibration.class.getName());
    private static class EncodeReader implements MSLALayerEncodeReader {
        private final MSLAFile<?> wsFile;
        private final PCBCalibrationPattern pattern;
        private int size;
        @Setter private ReadDirection readDirection = ReadDirection.READ_ROW;
        private int repetitions;
        public EncodeReader(MSLAFile<?> file, PCBCalibrationPattern pattern, int repetitions) {
            this.wsFile = file;
            this.pattern = pattern;
            this.repetitions = repetitions;
        }
        volatile int layersLeft = repetitions;
        @Override public Size getResolution() { return wsFile.getResolution(); }
        @Override public int getSize() { return size; }
        @Override public BufferedImage read(int layerNumber) throws MSLAException {
            var image = pattern.generate(wsFile.getResolution().getWidth() / 3, layerNumber, repetitions);
            try (var out = new BufferedImageInputStream(image, readDirection)) {
                this.size = out.available();
                return image;
            } catch (IOException e) {
                throw new MSLAException("Error reading PCBCalibration image", e);
            }
        }
        @Override public void onStart(int layerNumber) {}
        @Override public void onFinish(int layerNumber, int pixels, MSLALayerEncodeOutput<?> output) {
            logger.info("Layer " + layerNumber + " done: " + pixels + " pixels, " + output.sizeInBytes() + " bytes");
            synchronized (wsFile) { layersLeft--; }
        }
        @Override public void onError(int layerNumber, String error) {
            logger.info("Layer " + layerNumber + " error: " + error);
            synchronized (wsFile) { layersLeft--; }
        }
    }

    /**
     * Creates a PCB photo resistive film calibration pattern.
     *
     * @param machineName model of a mSLA 3D printing machine
     * @param filePath file path and name to store calibration pattern
     * @param startTime initial curing time
     * @param interval curing time interval
     * @param repetitions number of samples or intervals
     */
    public static String generateTestFile(
            String machineName,
            String filePath,
            int startTime,
            int interval,
            int repetitions) throws MSLAException {

        var defaults = MachineDefaults.getInstance().getMachineDefaults(machineName)
                .orElseThrow(() -> new MSLAException("Printer has no defaults: " + machineName));


        filePath = filePath.endsWith(defaults.getFileExtension()) ?
                filePath :
                filePath + "." + defaults.getFileExtension();
        try (var fos = new FileOutputStream(filePath)) {
            var wsFile = generateTestPattern(machineName, startTime, interval, repetitions);
            wsFile.write(fos);
            fos.flush();
            return filePath;
        } catch (IOException e) {
            throw new MSLAException("Error writing PCBCalibration image", e);
        }
    }
    /**
     * Creates a PCB photo resistive film calibration pattern.
     *
     * @param machineName model of a mSLA 3D printing machine
     * @param startTime initial curing time
     * @param interval curing time interval
     * @param repetitions number of samples or intervals
     */
    public static MSLAFile<?> generateTestPattern(
            String machineName,
            int startTime,
            int interval,
            int repetitions) throws MSLAException
    {
        var defaults = MachineDefaults.getInstance().getMachineDefaults(machineName)
                .orElseThrow(() -> new MSLAException("Printer has no defaults: " + machineName));
        var wsFile = FileFactory.instance.create(machineName);
        if (wsFile.getResolution() == null) throw new MSLAException("Machine has no resolution set");
        if (wsFile.getPixelSize() == 0) throw new MSLAException("Machine has no pixel size");

        var options = new FileOptionMapper(wsFile, defaults);

        // Set options
        options.set(MSLAOptionName.BottomLayersCount, 1);
        options.set(MSLAOptionName.BottomLayersExposureTime, startTime);
        options.set(MSLAOptionName.NormalLayersExposureTime, interval);
        if (options.hasOption(MSLAOptionName.LiftHeight)) options.set(MSLAOptionName.LiftHeight, 1);
        if (options.hasOption(MSLAOptionName.NormalLayersLiftHeight)) options.set(MSLAOptionName.NormalLayersLiftHeight, 1);
        if (options.hasOption(MSLAOptionName.LayerSettings)) options.set(MSLAOptionName.LayerSettings, false);
        if (options.hasOption(MSLAOptionName.TransitionLayersCount)) options.set(MSLAOptionName.TransitionLayersCount, 0);
        if (options.hasOption(MSLAOptionName.Antialias)) options.set(MSLAOptionName.Antialias, false);

        // Create preview image
        createPreview(wsFile);

        // Generate pattern layers
        var pattern = new PCBCalibrationPattern(wsFile.getResolution(), wsFile.getPixelSize());
        pattern.setStartTime(startTime);
        var reader = new EncodeReader(wsFile, pattern, repetitions);
        for (int i = 0; i < repetitions; i++) wsFile.addLayer(reader, null);
        try {
            // Wait until all layers are encoded
            while (wsFile.getEncodersPool().isEncoding()) Thread.sleep(10);
        } catch (InterruptedException ignored) {}

        logger.info("Encoding done");

        return wsFile;
    }

    public static void createPreview(MSLAFile<?> file) throws MSLAException {
        for (var i = 0; i < file.getPreviewsNumber(); i++) {
            var preview = file.getPreview(i);
            if (preview != null) {
                var image = new BufferedImage(
                        preview.getResolution().getWidth(),
                        preview.getResolution().getHeight(),
                        BufferedImage.TYPE_INT_RGB);
                var graphics = image.getGraphics();
                graphics.setFont(graphics.getFont().deriveFont(20.0f));
                graphics.setColor(Color.WHITE);
                graphics.drawRoundRect(10, 10,
                        preview.getResolution().getWidth() - 20,
                        preview.getResolution().getHeight() - 20,
                        10, 10);
                graphics.setColor(Color.WHITE);
                graphics.drawString("PCB Test pattern", 16, 16 + 20);
                graphics.setFont(graphics.getFont().deriveFont(16.0f));
                graphics.setColor(Color.RED);
                graphics.drawString("DO NOT PRINT!", 16, 16 + 20 + 30);
                preview.setImage(image);
            }
        }
    }
}