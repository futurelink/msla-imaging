package futurelink.msla.tools;

import futurelink.Main;
import futurelink.msla.formats.MSLAEncodeReader;
import futurelink.msla.formats.MSLAFile;
import futurelink.msla.formats.MSLAFileCodec;
import futurelink.msla.formats.anycubic.PhotonWorkshopFile;
import futurelink.msla.formats.anycubic.PhotonWorkshopFileDefaults;

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
        var defaults = PhotonWorkshopFileDefaults.get(machineName);
        if (defaults == null) throw new IOException("Unknown machine name: " + machineName);
        try (var fos = new FileOutputStream(fileName + "." + defaults.getFileExtension())) {
            var wsFile = new PhotonWorkshopFile(defaults);
            var wsHeader = wsFile.getHeader();
            var wsExtra =  wsFile.getExtra();
            if ((wsHeader == null) || (wsExtra == null)) throw new IOException("File was not initialized properly");

            // Set options
            wsHeader.setPerLayerOverride(1);
            wsHeader.setBottomLayersCount(1);
            wsHeader.setBottomExposureTime(startTime);
            wsHeader.setExposureTime(interval);
            wsHeader.setLiftHeight(0.5f);
            wsHeader.setTransitionLayers(0);

            wsExtra.setLiftHeight1(0.5f);
            wsExtra.setLiftHeight2(0.5f);
            wsExtra.setBottomLiftHeight1(0.5f);
            wsExtra.setBottomLiftHeight2(0.5f);

            // Create preview image
            createPreview(wsFile);

            // Generate pattern layers
            var pattern = new PCBCalibrationPattern(wsHeader.getResolutionX(), wsHeader.getResolutionY(),
                    wsHeader.getPixelSizeUm());
            pattern.setStartTime(startTime);
            var encoder = new MSLAEncodeReader() {
                volatile int layersLeft = repetitions;
                @Override public MSLAFileCodec getCodec() {
                    return wsFile.getCodec();
                }
                @Override public InputStream read(int layerNumber) throws IOException {
                    var image = pattern.generate(wsHeader.getResolutionY() / 3, layerNumber, repetitions);
                    var raster = image.getRaster();
                    return new Main.RasterBytesInputStream(raster);
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
                    preview.getResolutionX() - 20,
                    preview.getResolutionY() - 20,
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
