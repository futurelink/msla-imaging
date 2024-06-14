package futurelink.msla.formats.creality;

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

public class CXDLPFileTest extends CommonTestRoutines {

    @Test
    void TestFileCreate() throws MSLAException, InterruptedException {
        var outFile = temp_dir + "test_one_layer_file.cxdlp";
        logger.info("Temporary file: " + outFile);
        delete_file(outFile); // Clean up files just in case

        var machine = "CREALITY HALOT-ONE PLUS";
        var defaults = MachineDefaults.getInstance().getMachineDefaults(machine)
                .orElseThrow(() -> new MSLAException("Machine has not defaults: " + machine));
        var file = (CXDLPFile) FileFactory.instance.create(machine);
        var options = new FileOptionMapper(file, defaults);
        options.set(MSLAOptionName.BottomLayersExposureTime, 12);
        options.set(MSLAOptionName.LayerHeight, 0.1);

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
        while (file.getEncodersPool().isEncoding()) Thread.sleep(10);
        logger.info("Done");

        System.out.println(file);

        // Write output file
        writeMSLAFile(outFile, file);

        assertFileExactSize(outFile, 441595);
    }

    @Test
    void TestFileWithDefaultParams() throws MSLAException {
        var machine = "CREALITY HALOT-RAY";
        var defaults = MachineDefaults.getInstance().getMachineDefaults(machine)
                .orElseThrow(() -> new MSLAException("Machine has not defaults: " + machine));
        var file = (CXDLPFile) FileFactory.instance.create(machine);
        var options = new FileOptionMapper(file, defaults);
        options.set(MSLAOptionName.BottomLayersExposureTime, "12");
        options.set(MSLAOptionName.LayerHeight, "0.1");
        System.out.println(options.getParameters(MSLAOptionName.LayerHeight));
    }

    @Test
    void TestFileCreateAndExtract() throws MSLAException, InterruptedException, IOException {
        var outFile = temp_dir + "test_create_and_extract.cxdlp";
        logger.info("Temporary file: " + outFile);
        delete_file(temp_dir + "extracted_1.png"); // Clean up files just in case
        delete_file(temp_dir + "extracted_10.png");

        var file = (CXDLPFile) FileFactory.instance.load(
                resourceFile("test_data/CXDLPFileTest/Example_HALOT_ONE_PLUS.cxdlp")
        );

        assertEquals("CL-79", file.getHeader().getPrinterModel());
        assertEquals(3, (int) file.getHeader().getVersion());

        ImageIO.write(file.getLargePreview().getImage(), "png", new File(temp_dir + "cxdlp_preview.png"));
        assertFileExactSize(temp_dir + "cxdlp_preview.png", 5468);

        // Asynchronously extract image files
        var writer = new ImageWriter(file, temp_dir, "extracted_", "png");
        file.readLayer(writer, 1);
        file.readLayer(writer, 10);
        while (file.getDecodersPool().isDecoding()) Thread.sleep(10); // Wait while decoding-writing is done
        logger.info("Done");

        assertFileMinSize(temp_dir + "extracted_1.png", 12000);
        assertFileMinSize(temp_dir + "extracted_10.png", 12000);

        /* ********************************* *
         * Create new file from those images *
         * ********************************* */
        var newFile = (CXDLPFile) FileFactory.instance.create("CREALITY HALOT-ONE PLUS");
        try {
            newFile.addLayer(new ImageReader(file, temp_dir + "extracted_1.png"), null);
            newFile.addLayer(new ImageReader(file, temp_dir + "extracted_10.png"), null);
        } catch (IOException e) {
            throw new MSLAException("Can't read test file", e);
        }
        while (newFile.getEncodersPool().isEncoding()) { Thread.sleep(10);  }

        assertEquals("CL-79", newFile.getHeader().getPrinterModel());
        assertEquals(3, (int) newFile.getHeader().getVersion());

        // Write new file
        writeMSLAFile(outFile, newFile);

        delete_file(temp_dir + "final_0.png"); // Clean up files just in case
        delete_file(temp_dir + "final_1.png");

        // Extract images from newly created file
        file = (CXDLPFile) FileFactory.instance.load(outFile);
        writer = new ImageWriter(file, temp_dir, "final_", "png");
        file.readLayer(writer, 0);
        file.readLayer(writer, 1);
        while (file.getDecodersPool().isDecoding()) { Thread.sleep(10); } // Wait while decoding-writing is done

        assertFileMinSize(temp_dir + "final_0.png", 12000);
        assertFileMinSize(temp_dir + "final_1.png", 12000);
    }

    @Test
    void TestFileExtract() throws MSLAException, InterruptedException, IOException {
        delete_file(temp_dir + "1.png"); // Clean up files just in case
        delete_file(temp_dir + "10.png");

        var file = (CXDLPFile) FileFactory.instance.load(
                resourceFile("test_data/CXDLPFileTest/HALOT_RAY_Original_Slicer.cxdlp")
        );

        ImageIO.write(file.getPreview(1).getImage(), "png", new File(temp_dir + "cxdlp_preview.png"));
        assertFileExactSize(temp_dir + "cxdlp_preview.png", 9021);

        // Asynchronously extract image files
        var writer = new ImageWriter(file, temp_dir, "png");
        file.readLayer(writer, 1);
        file.readLayer(writer, 10);
        while (file.getDecodersPool().isDecoding()) { Thread.sleep(10); } // Wait while decoding-writing is done

        System.out.println(file);

        assertFileMinSize(temp_dir + "1.png", 12000);
        assertFileMinSize(temp_dir + "10.png", 12000);
    }
}
