package futurelink.msla.formats.creality;

import futurelink.msla.formats.CommonTestRoutines;
import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.utils.FileFactory;
import futurelink.msla.tools.ImageReader;
import futurelink.msla.tools.ImageWriter;
import org.junit.jupiter.api.Test;

import java.io.FileOutputStream;
import java.io.IOException;

public class CXDLPFileTest extends CommonTestRoutines {

    @Test
    void TestFileCreate() throws MSLAException {
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
        while (file.getEncodersPool().isEncoding()) {}
        logger.info("Done");

        // Write output file
        try(var fos = new FileOutputStream(outFile)) {
            file.write(fos);
            fos.flush();
        } catch (IOException e) {
            throw new MSLAException("Can't write test file", e);
        }

        assertFileExists(outFile, 400000);
    }

    @Test
    void TestFileCreateAndExtract() throws MSLAException {
        var outFile = temp_dir + "test_create_and_extract.cxdlp";
        logger.info("Temporary file: " + outFile);
        delete_file(temp_dir + "extracted_1.png"); // Clean up files just in case
        delete_file(temp_dir + "extracted_10.png");

        var file = (CXDLPFile) FileFactory.instance.load(
                resourceFile("test_data/CXDLPFileTest/Example_HALOT_ONE_PLUS.cxdlp")
        );

        // Asynchronously extract image files
        var decoders = file.getDecodersPool(new ImageWriter(file, temp_dir, "extracted_", "png"));
        file.readLayer(decoders, 1);
        file.readLayer(decoders, 10);
        while (decoders.isDecoding()) {} // Wait while decoding-writing is done
        logger.info("Done");

        assertFileExists(temp_dir + "extracted_1.png", 12000);
        assertFileExists(temp_dir + "extracted_10.png", 12000);

        // Create new file from those images
        var newFile = (CXDLPFile) FileFactory.instance.create("CREALITY HALOT-ONE PLUS");
        try {
            newFile.addLayer(new ImageReader(file, temp_dir + "extracted_1.png"), null);
            newFile.addLayer(new ImageReader(file, temp_dir + "extracted_10.png"), null);
        } catch (IOException e) {
            throw new MSLAException("Can't read test file", e);
        }
        while (newFile.getEncodersPool().isEncoding()) {}

        try(var fos = new FileOutputStream(outFile)) {
            newFile.write(fos);
            fos.flush();
        } catch (IOException e) {
            throw new MSLAException("Can't write test file", e);
        }

        delete_file(temp_dir + "final_0.png"); // Clean up files just in case
        delete_file(temp_dir + "final_1.png");

        // Extract images from newly created file
        file = (CXDLPFile) FileFactory.instance.load(outFile);
        decoders = file.getDecodersPool(new ImageWriter(file, temp_dir, "final_", "png"));
        file.readLayer(decoders, 0);
        file.readLayer(decoders, 1);
        while(decoders.isDecoding()) {}

        assertFileExists(temp_dir + "final_0.png", 12000);
        assertFileExists(temp_dir + "final_1.png", 12000);
    }

    @Test
    void TestFileExtract() throws MSLAException {
        delete_file(temp_dir + "1.png"); // Clean up files just in case
        delete_file(temp_dir + "10.png");

        var file = (CXDLPFile) FileFactory.instance.load(
                resourceFile("test_data/CXDLPFileTest/Example_HALOT_ONE_PLUS.cxdlp")
        );

        // Asynchronously extract image files
        var decoders = file.getDecodersPool(new ImageWriter(file, temp_dir, "png"));
        file.readLayer(decoders, 1);
        file.readLayer(decoders, 10);
        while (decoders.isDecoding()) {} // Wait while decoding-writing is done

        assertFileExists(temp_dir + "/1.png", 12000);
        assertFileExists(temp_dir + "/10.png", 12000);
    }
}
