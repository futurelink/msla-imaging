package futurelink.msla.tools;

import futurelink.msla.formats.*;
import futurelink.msla.formats.iface.MSLAFile;
import futurelink.msla.formats.iface.MSLAFileCodec;
import futurelink.msla.formats.iface.MSLALayerEncodeReader;
import futurelink.msla.formats.utils.FileFactory;
import futurelink.msla.formats.utils.Size;

import java.awt.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class PCBCalibration {

    private static class EncodeReader implements MSLALayerEncodeReader {
        private final MSLAFile wsFile;
        private final PCBCalibrationPattern pattern;
        private int repetitions = 0;
        public EncodeReader(MSLAFile file, PCBCalibrationPattern pattern, int repetitions) {
            this.wsFile = file;
            this.pattern = pattern;
            this.repetitions = repetitions;
        }
        volatile int layersLeft = repetitions;
        @Override public Class<? extends MSLAFileCodec> getCodec() {
            return wsFile.getCodec();
        }
        @Override public Size getResolution() { return wsFile.getResolution(); }
        @Override public InputStream read(int layerNumber, MSLALayerEncodeReader.ReadDirection direction) {
            return new BufferedImageInputStream(
                    pattern.generate(wsFile.getResolution().getWidth() / 3, layerNumber, repetitions),
                    direction
            );
        }
        @Override public void onStart(int layerNumber) {}
        @Override public void onFinish(int layerNumber, int pixels, int length) {
            System.out.println("Layer " + layerNumber + " done: " + pixels + " pixels, " + length + " bytes");
            synchronized (wsFile) { layersLeft--; }
        }
        @Override public void onError(int layerNumber, String error) {
            System.out.println("Layer " + layerNumber + " error: " + error);
            synchronized (wsFile) { layersLeft--; }
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
    public static void generateTestPattern(String machineName, String fileName, float startTime, float interval, int repetitions)
            throws IOException, MSLAException {
        var defaults = FileFactory.instance.defaults(machineName);
        if (defaults == null) throw new IOException("Unknown machine name: '" + machineName + "'");

        var wsFile = FileFactory.instance.create(machineName);
        if (wsFile == null) throw new IOException("File was not initialized properly!");

        try (var fos = new FileOutputStream(fileName + "." + defaults.getFileExtension())) {
            // Set options
            wsFile.options().set("BottomLayersCount", 1);
            wsFile.options().set("BottomExposureTime", startTime);
            wsFile.options().set("ExposureTime", interval);
            wsFile.options().set("LiftHeight", 0.5f);

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
            var pattern = new PCBCalibrationPattern(wsFile.getResolution(), wsFile.getPixelSizeUm());
            pattern.setStartTime(startTime);
            var reader = new EncodeReader(wsFile, pattern, repetitions);
            var encoders = new MSLALayerEncoders();
            for (int i = 0; i < repetitions; i++) {
                // Wait while layer can be added
                while (!wsFile.addLayer(reader, encoders, 0.05f, interval, 10.0f, 0.5f)) {}
            }
            while (encoders.isEncoding()); // Wait until all layers are encoded
            wsFile.write(fos);
        }
    }

    public static void createPreview(MSLAFile file) throws MSLAException {
        var preview = file.getPreview();
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

            file.updatePreviewImage();
        }
    }
}
