package futurelink.msla.formats.anycubic;

import futurelink.msla.formats.CommonTestRoutines;
import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.utils.FileFactory;
import futurelink.msla.tools.ImageReader;
import futurelink.msla.tools.ImageWriter;
import org.junit.jupiter.api.Test;

import java.io.FileOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PhotonWorkshopFileTest extends CommonTestRoutines {

    @Test
    void TestFileRead() {
        try {
            var classLoader = getClass().getClassLoader();
            var resource = classLoader.getResource("test_data/PhotonFileTest/Example_Photon_Mono_4K.pwma");
            if (resource == null) throw new RuntimeException("Resource data file not found");
            var file = FileFactory.instance.load(resource.getFile());
            assertTrue(file.isValid());
        } catch (MSLAException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void TestFileExtract() {
        try {
            delete_file(temp_dir + "1.png");  // Clean up files just in case
            delete_file(temp_dir + "10.png");

            var file = (PhotonWorkshopFile) FileFactory.instance.load(
                    resourceFile("test_data/PhotonFileTest/Example_Photon_Mono_4K.pwma")
            );

            // Asynchronously extract image files
            var decoders = file.getDecodersPool(new ImageWriter(file, temp_dir, "png"));
            file.readLayer(decoders, 1);
            file.readLayer(decoders, 10);
            while (decoders.isDecoding()) {} // Wait while decoding-writing is done
            logger.info("Done");

            assertFileExists(temp_dir + "/1.png", 11000);
            assertFileExists(temp_dir + "/10.png", 11000);
        } catch (MSLAException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void TestFileCreate() throws MSLAException {
        var outFile = temp_dir + "test_one_layer_file.pwma";
        logger.info("Temporary file: " + outFile);
        delete_file(outFile); // Clean up files just in case

        var file = (PhotonWorkshopFile) FileFactory.instance.create("Anycubic Photon Mono X 6K");
        file.options().set("BottomExposureTime", 12.0F);

        var pngFileLayers = new String[]{
                resourceFile("test_data/PhotonFileTest/Layer_1.png"),
                resourceFile("test_data/PhotonFileTest/Layer_2.png")
        };
        for (var pngFile : pngFileLayers) {
            try { file.addLayer(new ImageReader(file, pngFile),  null); }
            catch (IOException e) { throw new MSLAException("Can't read layer image", e); }
        }
        while (file.getEncodersPool().isEncoding()) {} // Wait while encoding
        logger.info("Done");

        // Write output file
        try(var fos = new FileOutputStream(outFile)) {
            file.write(fos);
            fos.flush();
        } catch (IOException e) {
            throw new MSLAException("Can't write test file", e);
        }

        // Check if file exists
        assertFileExists(outFile, 1000000);
    }
}
