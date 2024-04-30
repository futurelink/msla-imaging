package futurelink.msla.formats.creality.tables;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.MSLALayerDecoders;
import futurelink.msla.formats.MSLALayerEncoders;
import futurelink.msla.formats.anycubic.PhotonWorkshopCodec;
import futurelink.msla.formats.anycubic.PhotonWorkshopCodecPW0;
import futurelink.msla.formats.utils.FileFactory;
import futurelink.msla.tools.ImageReader;
import futurelink.msla.tools.ImageWriter;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class PhotonWorkshopFileTest {

    private void delete_file(String file_path) {
        var temp_file = new File(file_path);
        var deleted = temp_file.delete();
        if (deleted) System.out.println("Temp file deleted");
    }

    private String resourceFile(String resourceName) {
        var classLoader = getClass().getClassLoader();
        var resource = classLoader.getResource(resourceName);
        if (resource == null) throw new RuntimeException("Resource data file not found");
        return resource.getFile();
    }

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
            var temp_dir = System.getProperty("java.io.tmpdir");
            System.out.println("Temporary path: " + temp_dir);
            delete_file(temp_dir + "/1.png");  // Clean up files just in case
            delete_file(temp_dir + "/10.png");

            var file = FileFactory.instance.load(resourceFile("test_data/PhotonFileTest/Example_Photon_Mono_4K.pwma"));

            // Asynchronously extract image files
            var decoders = new MSLALayerDecoders(new ImageWriter(file, temp_dir, "png"));
            file.readLayer(decoders, 1);
            file.readLayer(decoders, 10);
            while (decoders.isDecoding()) {} // Wait while decoding-writing is done
            System.out.println("Done");

            var outFile = new File(temp_dir + "/1.png");
            outFile.deleteOnExit();
            assertTrue(outFile.exists());
            assertTrue(outFile.length() > 11000);

            var outFile2 = new File(temp_dir + "/10.png");
            outFile2.deleteOnExit();
            assertTrue(outFile2.exists());
            assertTrue(outFile2.length() > 11000);
        } catch (MSLAException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void TestFileCreate() throws MSLAException {
        var temp_dir = System.getProperty("java.io.tmpdir");
        System.out.println("Temporary path: " + temp_dir);

        var outFile = temp_dir + "/test_one_layer_file.pwma";
        delete_file(outFile); // Clean up files just in case

        var file = FileFactory.instance.create("Anycubic Photon Mono X 6K");
        file.options().set("BottomExposureTime", 12.0F);

        var pngFileLayers = new String[]{
                resourceFile("test_data/PhotonFileTest/Layer_1.png"),
                resourceFile("test_data/PhotonFileTest/Layer_2.png")
        };
        var encoders = new MSLALayerEncoders(); // Create encoders pool
        for (var pngFile : pngFileLayers) {
            try { file.addLayer(new ImageReader(file, pngFile), encoders); }
            catch (IOException e) {
                throw new MSLAException("Can't read layer image", e);
            }
        }
        while (encoders.isEncoding()) {} // Wait while encoding

        // Write output file
        try(var fos = new FileOutputStream(outFile)) {
            file.write(fos);
            fos.flush();
        } catch (IOException e) {
            throw new MSLAException("Can't write test file", e);
        }

        // Check if file exists
        var outFile2 = new File(outFile);
        outFile2.deleteOnExit();
        assertTrue(outFile2.exists());
        assertTrue(outFile2.length() > 1000000);
    }
}
