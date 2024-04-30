package futurelink.msla.formats.creality.tables;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.MSLALayerDecoders;
import futurelink.msla.formats.MSLALayerEncoders;
import futurelink.msla.formats.utils.FileFactory;
import futurelink.msla.formats.creality.CXDPLFileCodec;
import futurelink.msla.tools.ImageReader;
import futurelink.msla.tools.ImageWriter;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CXDLPFileTest {

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
    void TestFileCreate() throws MSLAException {
        var temp_dir = System.getProperty("java.io.tmpdir");
        System.out.println("Temporary path: " + temp_dir);
        delete_file(temp_dir + "/test_one_layer_file.cxdlp"); // Clean up files just in case
        var file = FileFactory.instance.create("CREALITY HALOT-ONE PLUS");
        file.options().set("BottomExposureTime", 12);

        var pngFileLayers = new String[]{
                resourceFile("test_data/CXDLPFileTest/Layer_1.png"),
                resourceFile("test_data/CXDLPFileTest/Layer_2.png")
        };
        var encoders = new MSLALayerEncoders();
        for (var pngFile : pngFileLayers) {
            try {
                file.addLayer(new ImageReader(file, pngFile), encoders);
            } catch (IOException e) {
                throw new MSLAException("Can't read layer image", e);
            }
        }
    }

    @Test
    void TestFileExtract() throws MSLAException {
        var temp_dir = System.getProperty("java.io.tmpdir");
        System.out.println("Temporary path: " + temp_dir);
        delete_file(temp_dir + "/Layer_2.png"); // Clean up files just in case
        delete_file(temp_dir + "/10.png");

        var file = FileFactory.instance.load(resourceFile("test_data/CXDLPFileTest/Example_HALOT_ONE_PLUS.cxdlp"));

        // Asynchronously extract image files
        var decoders = new MSLALayerDecoders(new ImageWriter(file, temp_dir, "png"));
        file.readLayer(decoders, 1);
        file.readLayer(decoders, 10);
        while (decoders.isDecoding()) {} // Wait while decoding-writing is done
        System.out.println("Done");

        var outFile = new File(temp_dir + "/Layer_2.png");
        outFile.deleteOnExit();
        assertTrue(outFile.exists());
        assertTrue(outFile.length() > 12000);

        var outFile2 = new File(temp_dir + "/10.png");
        outFile2.deleteOnExit();
        assertTrue(outFile2.exists());
        assertTrue(outFile2.length() > 12000);
    }
}
