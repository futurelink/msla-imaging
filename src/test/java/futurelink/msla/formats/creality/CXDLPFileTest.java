package futurelink.msla.formats.creality;

import futurelink.msla.formats.CommonTestRoutines;
import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.utils.FileFactory;
import futurelink.msla.tools.ImageReader;
import futurelink.msla.tools.ImageWriter;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class CXDLPFileTest extends CommonTestRoutines {

    @Test
    void TestFileCreate() throws MSLAException, InterruptedException {
        var outFile = temp_dir + "test_one_layer_file.cxdlp";
        logger.info("Temporary file: " + outFile);
        delete_file(outFile); // Clean up files just in case

        var file = (CXDLPFile) FileFactory.instance.create("CREALITY HALOT-ONE PLUS");
        file.options().set("BottomExposureTime", 12);

        var pngFileLayers = new String[]{
                resourceFile("test_data/CXDLPFileTest/Layer_1.png"),
                resourceFile("test_data/CXDLPFileTest/Layer_2.png")
        };
        for (var pngFile : pngFileLayers) {
            try {
                file.addLayer(new ImageReader(file, pngFile),  null);
                logger.info("Added layer from " + pngFile);
            } catch (IOException e) {
                throw new MSLAException("Can't read layer image", e);
            }
        }
        while (file.getEncodersPool().isEncoding()) Thread.sleep(100);
        logger.info("Done");

        System.out.println(file);

        // Write output file
        writeMSLAFile(outFile, file);

        assertFileExactSize(outFile, 441597);
    }

    @Test
    void TestFileCreateAndExtract() throws MSLAException, InterruptedException {
        var outFile = temp_dir + "test_create_and_extract.cxdlp";
        logger.info("Temporary file: " + outFile);
        delete_file(temp_dir + "extracted_1.png"); // Clean up files just in case
        delete_file(temp_dir + "extracted_10.png");

        var file = (CXDLPFile) FileFactory.instance.load(
                resourceFile("test_data/CXDLPFileTest/Example_HALOT_ONE_PLUS.cxdlp")
        );

        // Asynchronously extract image files
        var writer = new ImageWriter(file, temp_dir, "extracted_", "png");
        file.readLayer(writer, 1);
        file.readLayer(writer, 10);
        while (file.getDecodersPool().isDecoding()) Thread.sleep(100); // Wait while decoding-writing is done
        logger.info("Done");

        assertFileMinSize(temp_dir + "extracted_1.png", 12000);
        assertFileMinSize(temp_dir + "extracted_10.png", 12000);

        // Create new file from those images
        var newFile = (CXDLPFile) FileFactory.instance.create("CREALITY HALOT-ONE PLUS");
        try {
            newFile.addLayer(new ImageReader(file, temp_dir + "extracted_1.png"), null);
            newFile.addLayer(new ImageReader(file, temp_dir + "extracted_10.png"), null);
        } catch (IOException e) {
            throw new MSLAException("Can't read test file", e);
        }
        while (newFile.getEncodersPool().isEncoding()) { Thread.sleep(100);  }

        // Write new file
        writeMSLAFile(outFile, newFile);

        delete_file(temp_dir + "final_0.png"); // Clean up files just in case
        delete_file(temp_dir + "final_1.png");

        // Extract images from newly created file
        file = (CXDLPFile) FileFactory.instance.load(outFile);
        writer = new ImageWriter(file, temp_dir, "final_", "png");
        file.readLayer(writer, 0);
        file.readLayer(writer, 1);
        while (file.getDecodersPool().isDecoding()) { Thread.sleep(100); } // Wait while decoding-writing is done

        assertFileMinSize(temp_dir + "final_0.png", 12000);
        assertFileMinSize(temp_dir + "final_1.png", 12000);
    }

    @Test
    void TestFileExtract() throws MSLAException, InterruptedException {
        delete_file(temp_dir + "1.png"); // Clean up files just in case
        delete_file(temp_dir + "10.png");

        var file = (CXDLPFile) FileFactory.instance.load(
                resourceFile("test_data/CXDLPFileTest/Example_HALOT_ONE_PLUS.cxdlp")
        );

        // Asynchronously extract image files
        var writer = new ImageWriter(file, temp_dir, "png");
        file.readLayer(writer, 1);
        file.readLayer(writer, 10);
        while (file.getDecodersPool().isDecoding()) { Thread.sleep(100); } // Wait while decoding-writing is done

        System.out.println(file);

        assertFileMinSize(temp_dir + "1.png", 12000);
        assertFileMinSize(temp_dir + "10.png", 12000);
    }
}
