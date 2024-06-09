package futurelink.msla.formats.chitubox;

import futurelink.msla.formats.CommonTestRoutines;
import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.chitubox.common.CTBCommonFile;
import futurelink.msla.utils.FileFactory;
import futurelink.msla.tools.ImageReader;
import futurelink.msla.tools.ImageWriter;
import futurelink.msla.utils.FileOptionMapper;
import futurelink.msla.utils.defaults.MachineDefaults;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CTBFileTest extends CommonTestRoutines {

    @Test
    void ReadChituboxFileTest() throws MSLAException, IOException, InterruptedException {
        logger.info("Temporary dir: " + temp_dir);
        var file = FileFactory.instance.load(
                resourceFile("test_data/ChituboxFileTest/Example_Chitubox_Slices.ctb")
        );
        assertTrue(file.isValid());
        assertEquals("ELEGOO JUPITER", file.getMachineName());

        ImageIO.write(file.getPreview(0).getImage(), "png", new File(temp_dir + "encrypted_file_small_preview.png"));
        ImageIO.write(file.getPreview(1).getImage(), "png", new File(temp_dir + "encrypted_file_large_preview.png"));

        assertFileExactSize(temp_dir + "encrypted_file_small_preview.png", 4612);
        assertFileExactSize(temp_dir + "encrypted_file_large_preview.png", 14994);

        // Asynchronously extract image files
        var layerPixels = new int[3];
        var layerFiles = new String[3];
        var writer = new ImageWriter(file, temp_dir, "png", (layerNumber, fileName, pixels) -> {
            layerPixels[layerNumber] = pixels;
            layerFiles[layerNumber] = fileName;
        });
        file.readLayer(writer, 0);
        file.readLayer(writer, 1);
        file.readLayer(writer, 2);
        while (file.getDecodersPool().isDecoding()) Thread.sleep(10); // Wait while decoding-writing is done

        // Assert all pixels were decoded properly
        assertEquals(72849, layerPixels[0]);
        assertEquals(74830, layerPixels[1]);
        assertEquals(76499, layerPixels[2]);

        // Check files exist
        assertFileExactSize(layerFiles[0], 17987);
        assertFileExactSize(layerFiles[1], 18021);
        assertFileExactSize(layerFiles[2], 18044);
    }

    @Test
    void ReadELEGOOFileTest() throws IOException, InterruptedException, MSLAException {
        logger.info("Temporary dir: " + temp_dir);
        var file = (CTBCommonFile) FileFactory.instance.load(
                resourceFile("test_data/ChituboxFileTest/Example_ELEGOO_SATURN.ctb")
        );
        assertTrue(file.isValid());
        assertEquals("3840 x 2400", file.getResolution().toString());

        ImageIO.write(file.getPreview((short) 0).getImage(), "png", new File(temp_dir + "ctb_preview.png"));
        assertFileExactSize(temp_dir + "ctb_preview.png", 6924);

        // Asynchronously extract image files
        var layerPixels = new int[3];
        var layerFiles = new String[3];
        var writer = new ImageWriter(file, temp_dir, "png", (layerNumber, fileName, pixels) -> {
            layerPixels[layerNumber] = pixels;
            layerFiles[layerNumber] = fileName;
        });
        file.readLayer(writer, 0);
        file.readLayer(writer, 1);
        file.readLayer(writer, 2);
        while (file.getDecodersPool().isDecoding()) Thread.sleep(10); // Wait while decoding-writing is done

        // Check pixels were written
        assertEquals(82031, layerPixels[0]);
        assertEquals(83634, layerPixels[1]);
        assertEquals(85202, layerPixels[2]);

        // Check files exist
        assertFileExactSize(layerFiles[0], 10841);
        assertFileExactSize(layerFiles[1], 10852);
        assertFileExactSize(layerFiles[2], 10869);

        ImageIO.write(file.getPreview(0).getImage(), "png", new File(temp_dir + "elegoo_large_preview.png"));
        ImageIO.write(file.getPreview(1).getImage(), "png", new File(temp_dir + "elegoo_small_preview.png"));

        assertFileExactSize(temp_dir + "elegoo_large_preview.png", 6924);
        assertFileExactSize(temp_dir + "elegoo_small_preview.png", 3789);
    }

    @Test
    void CreateTestFile() throws MSLAException, InterruptedException, IOException {
        var outFile = temp_dir + "chitubox_file_test.ctb";
        var file = FileFactory.instance.create("ELEGOO SATURN");
        assertTrue(file.isValid());

        var files = new String[]{
                "test_data/ChituboxFileTest/ELEGOO_Saturn_Layer_0.png",
                "test_data/ChituboxFileTest/ELEGOO_Saturn_Layer_1.png",
                "test_data/ChituboxFileTest/ELEGOO_Saturn_Layer_2.png"
        };
        var layerBytes = new int[3];
        var encoders = file.getEncodersPool();
        try {
            for (var f : files) {
                file.addLayer(new ImageReader(file, resourceFile(f)), (layer, data) -> layerBytes[layer] = data.size());
            }
            while (encoders.isEncoding()) Thread.sleep(10); // Wait while reading-encoding is done
        } catch (IOException e) {
            throw new MSLAException("Error adding layers", e);
        }

        // Assert encoded layers' data sizes
        assertEquals(1814, layerBytes[0]);
        assertEquals(1831, layerBytes[1]);
        assertEquals(1923, layerBytes[2]);

        // Set large preview
        file.setPreview(0, ImageIO.read(new File(resourceFile("test_data/ChituboxFileTest/ctb_preview_large_example.png"))));
        assertEquals("400 x 300", file.getPreview(0).getResolution().toString());

        file.setPreview(1, ImageIO.read(new File(resourceFile("test_data/ChituboxFileTest/ctb_preview_small_example.png"))));
        assertEquals("200 x 125", file.getPreview(1).getResolution().toString());

        writeMSLAFile(outFile, file);

        // Read exported file and do checks
        file = FileFactory.instance.load(outFile);
        assertTrue(file.isValid());

        // Asynchronously extract image files
        var layerPixels = new int[3];
        var layerFiles = new String[3];
        var writer = new ImageWriter(file, temp_dir, "png", (layerNumber, fileName, pixels) -> {
            layerPixels[layerNumber] = pixels;
            layerFiles[layerNumber] = fileName;
        });
        file.readLayer(writer, 0);
        file.readLayer(writer, 1);
        file.readLayer(writer, 2);
        while (file.getDecodersPool().isDecoding()) Thread.sleep(10); // Wait while decoding-writing is done

        // Check pixels were written
        assertEquals(82031, layerPixels[0]);
        assertEquals(83634, layerPixels[1]);
        assertEquals(85202, layerPixels[2]);

        // Check files exist
        assertFileExactSize(layerFiles[0], 10841);
        assertFileExactSize(layerFiles[1], 10852);
        assertFileExactSize(layerFiles[2], 10869);

        ImageIO.write(file.getPreview(0).getImage(), "png", new File(temp_dir + "large_preview.png"));
        ImageIO.write(file.getPreview(1).getImage(), "png", new File(temp_dir + "small_preview.png"));
    }

    @Test
    void OptionsTest() throws MSLAException {
        var machine = "ELEGOO SATURN";
        var defaults = MachineDefaults.instance.getMachineDefaults(machine)
                .orElseThrow(() -> new MSLAException("Machine has not defaults: " + machine));
        var file = (CTBCommonFile) FileFactory.instance.create(machine);
        var options = new FileOptionMapper(file, defaults);
        assertTrue(file.isValid());

        assertEquals(70.0, Float.parseFloat(options.get("Normal layers lift speed")));
        assertEquals(0.0, Float.parseFloat(options.get("Bottom layers light off delay")));

        options.set("Bottom layers light off delay", "1.0");
        assertEquals(1.0, Float.parseFloat(options.get("Bottom layers light off delay")));
    }
}
