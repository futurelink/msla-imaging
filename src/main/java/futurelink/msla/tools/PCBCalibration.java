package futurelink.msla.tools;

import futurelink.msla.formats.MSLAEncodeReader;
import futurelink.msla.formats.MSLAFile;
import futurelink.msla.formats.MSLAFileCodec;
import futurelink.msla.formats.utils.FileFactory;
import futurelink.msla.formats.utils.Size;

import java.awt.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class PCBCalibration {
    /**
     * Creates a PCB photo resistive film calibration pattern.
     *
     * @param machineName model of a mSLA 3D printing machine
     * @param startTime initial curing time
     * @param interval curing time interval
     * @param repetitions number of samples or intervals
     */
    public static void generateTestPattern(String machineName, String fileName, float startTime, float interval, int repetitions) throws IOException {
        var defaults = FileFactory.defaults(machineName);
        if (defaults == null) throw new IOException("Unknown machine name: '" + machineName + "'");

        var wsFile = FileFactory.create(machineName);
        if (wsFile == null) throw new IOException("File was not initialized properly!");

        try (var fos = new FileOutputStream(fileName + "." + defaults.getFileExtension())) {
            // Set options
            wsFile.setOption("BottomLayersCount", 1);
            wsFile.setOption("BottomExposureTime", startTime);
            wsFile.setOption("ExposureTime", interval);
            wsFile.setOption("LiftHeight", 0.5f);

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
            var encoder = new MSLAEncodeReader() {
                volatile int layersLeft = repetitions;
                @Override public MSLAFileCodec getCodec() {
                    return wsFile.getCodec();
                }
                @Override public Size getResolution() { return wsFile.getResolution(); }
                @Override public InputStream read(int layerNumber, MSLAEncodeReader.ReadDirection direction) {
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
            };
            for (int i = 0; i < repetitions; i++) wsFile.addLayer(
                    encoder, 0.05f, interval, 10.0f, 0.5f
            );
            while (encoder.layersLeft > 0); // Wait until all layers are encoded
            wsFile.write(fos);
        }
    }

    public static void createPreview(MSLAFile file) throws IOException {
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
