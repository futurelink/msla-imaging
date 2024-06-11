package futurelink.msla.formats.anycubic;

import futurelink.msla.formats.CommonTestRoutines;
import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.iface.options.MSLAOptionName;
import futurelink.msla.utils.FileFactory;
import futurelink.msla.tools.ImageReader;
import futurelink.msla.tools.ImageWriter;
import futurelink.msla.utils.options.FileOptionMapper;
import futurelink.msla.utils.defaults.MachineDefaults;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PhotonWorkshopFileTest extends CommonTestRoutines {

    @Test
    void TestFileRead() {
        try {
            var file = FileFactory.instance.load(
                    resourceFile("test_data/PhotonFileTest/Example_Photon_Mono_4K.pwma")
            );
            assertTrue(file.isValid());
        } catch (MSLAException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void TestFileExtract() throws InterruptedException, MSLAException, IOException {
        delete_file(temp_dir + "1.png");  // Clean up files just in case
        delete_file(temp_dir + "10.png");

        var file = (PhotonWorkshopFile) FileFactory.instance.load(
                resourceFile("test_data/PhotonFileTest/Example_Photon_Mono_4K.pwma")
        );

        // Extract preview
        ImageIO.write(file.getPreview(0).getImage(), "png", new File(temp_dir + "photon_preview.png"));
        assertFileExactSize(temp_dir + "photon_preview.png", 3535);

        // Asynchronously extract image files
        var layerPixels = new int[2];
        var layerFiles = new String[2];
        var writer = new ImageWriter(file, temp_dir, "png", (layerNumber, fileName, pixels) -> {
            layerPixels[layerNumber] = pixels;
            layerFiles[layerNumber] = fileName;
        });
        file.readLayer(writer, 0);
        file.readLayer(writer, 1);
        while (file.getDecodersPool().isDecoding()) { Thread.sleep(10); } // Wait while decoding-writing is done
        logger.info("Done");

        assertEquals(166587, layerPixels[0]);
        assertEquals(169644, layerPixels[1]);

        assertFileMinSize(layerFiles[0], 11000);
        assertFileMinSize(layerFiles[1], 11000);

        // Save previews
        file.getPreview((short) 0).getImage();
    }

    @Test
    void TestFileCreate() throws MSLAException, InterruptedException {
        var outFile = temp_dir + "test_one_layer_file.pwma";
        logger.info("Temporary file: " + outFile);
        delete_file(outFile); // Clean up files just in case

        var machine = "Anycubic Photon Mono X 6K";
        var defaults = MachineDefaults.getInstance().getMachineDefaults(machine)
                .orElseThrow(() -> new MSLAException("Machine has not defaults: " + machine));
        var file = (PhotonWorkshopFile) FileFactory.instance.create(machine);
        var options = new FileOptionMapper(file, defaults);
        options.set(MSLAOptionName.BottomLayersExposureTime, "12");

        var pngFileLayers = new String[]{
                resourceFile("test_data/PhotonFileTest/Layer_1.png"),
                resourceFile("test_data/PhotonFileTest/Layer_2.png")
        };
        for (var pngFile : pngFileLayers) {
            try { file.addLayer(new ImageReader(file, pngFile),  null); }
            catch (IOException e) { throw new MSLAException("Can't read layer image", e); }
        }
        while (file.getEncodersPool().isEncoding()) { Thread.sleep(10); } // Wait while encoding
        logger.info("Done");

        // Write output file
        writeMSLAFile(outFile, file);

        // Check if file exists
        assertFileMinSize(outFile, 1000000);
    }
}
