package futurelink.msla.tools;

import futurelink.msla.formats.*;
import futurelink.msla.formats.iface.MSLAFile;
import futurelink.msla.formats.iface.MSLALayerEncodeOutput;
import futurelink.msla.formats.iface.MSLALayerEncodeReader;
import futurelink.msla.formats.iface.options.MSLAOptionName;
import futurelink.msla.utils.FileFactory;
import futurelink.msla.utils.options.FileOptionMapper;
import futurelink.msla.utils.Size;
import futurelink.msla.utils.defaults.MachineDefaults;
import lombok.Setter;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

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
    public static String generateTestPattern(
            String machineName,
            String filePath,
            int startTime,
            int interval,
            int repetitions) throws MSLAException
    {
        var defaults = MachineDefaults.getInstance().getMachineDefaults(machineName)
                .orElseThrow(() -> new MSLAException("Printer has no defaults: " + machineName));
        var wsFile = FileFactory.instance.create(machineName);
        var options = new FileOptionMapper(wsFile, defaults);

        filePath = filePath.endsWith(defaults.getFileExtension()) ?
                filePath :
                filePath + "." + defaults.getFileExtension();
        try (var fos = new FileOutputStream(filePath)) {
            // Set options
            options.set(MSLAOptionName.BottomLayersCount, 1);
            options.set(MSLAOptionName.BottomLayersExposureTime, startTime);
            options.set(MSLAOptionName.NormalLayersExposureTime, interval);
            if (options.hasOption(MSLAOptionName.LiftHeight)) options.set(MSLAOptionName.LiftHeight, 1);
            if (options.hasOption(MSLAOptionName.NormalLayersLiftHeight)) options.set(MSLAOptionName.NormalLayersLiftHeight, 1);

            //wsFile.setOption("PerLayerOverride", 0);
            //wsFile.setOption("TransitionLayerCount", 0);

            //wsFile.setOption("LiftHeight", 0.5f);
            //wsFile.setOption("BottomLiftHeight", 0.5f);
            //wsFile.setOption("LiftHeight1", 0.5f);
            //wsFile.setOption("LiftHeight2", 0.5f);
            //wsFile.setOption("BottomLiftHeight1", 0.5f);
            //wsFile.setOption("BottomLiftHeight2", 0.5f);

            // Create preview image
            createPreview(wsFile);

            // Generate pattern layers
            var pattern = new PCBCalibrationPattern(wsFile.getResolution(), wsFile.getPixelSize());
            pattern.setStartTime(startTime);
            var reader = new EncodeReader(wsFile, pattern, repetitions);
            for (int i = 0; i < repetitions; i++)
                wsFile.addLayer(reader, null);
            try {
                // Wait until all layers are encoded
                while (wsFile.getEncodersPool().isEncoding()) Thread.sleep(10);
            } catch (InterruptedException ignored) {}
            logger.info("Encoding done, writing a file");

            System.out.println(wsFile);

            wsFile.write(fos);
            fos.flush();

            return filePath;
        } catch (IOException e) {
            throw new MSLAException("Error writing PCBCalibration image", e);
        }
    }

    public static void createPreview(MSLAFile<?> file) throws MSLAException {
        var preview = file.getPreview((short) 0);
        if (preview != null) {
            var graphics = preview.getImage().getGraphics();
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
        }
    }
}
