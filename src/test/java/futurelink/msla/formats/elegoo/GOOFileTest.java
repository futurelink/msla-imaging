package futurelink.msla.formats.elegoo;

import futurelink.msla.formats.CommonTestRoutines;
import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.utils.FileFactory;
import futurelink.msla.tools.ImageReader;
import futurelink.msla.tools.ImageWriter;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GOOFileTest extends CommonTestRoutines {
    @Test
    void CreateFileTest() throws MSLAException, InterruptedException {
        var outFile = temp_dir + "elegoo_test.goo";
        var file = (GOOFile) FileFactory.instance.create("ELEGOO Mars 4 Max");
        assertEquals(GOOFile.class, file.getClass());
        assertEquals("5760 x 3600", file.getResolution().toString());
        assertEquals(35.0F, file.getPixelSizeUm());

        // Write output file
        writeMSLAFile(outFile, file);

        var files = new String[]{
                "test_data/ElegooFileTest/Example_Layer_0.png",
                "test_data/ElegooFileTest/Example_Layer_1.png",
                "test_data/ElegooFileTest/Example_Layer_2.png"
        };
        var layerBytes = new int[3];
        var encoders = file.getEncodersPool();
        try {
            for (var f : files) {
                file.addLayer(new ImageReader(file, resourceFile(f)), (layer, data) -> layerBytes[layer] = data.size());
            }
            while (encoders.isEncoding()) Thread.sleep(100); // Wait while reading-encoding is done
        } catch (IOException e) {
            throw new MSLAException("Error adding layers", e);
        }

        // Check if layers were generated properly
        assertEquals(6173, layerBytes[0]);
        assertEquals(6235, layerBytes[1]);
        assertEquals(6266, layerBytes[2]);

        // Check if file is created
        assertFileMinSize(outFile, 1000);
    }

    @Test
    void ReadTestFile() throws InterruptedException {
        try {
            var file = (GOOFile) FileFactory.instance.load(
                    resourceFile("test_data/ElegooFileTest/Example_GOO.goo")
            );
            System.out.println(file);
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
            while (file.getDecodersPool().isDecoding()) Thread.sleep(100); // Wait while decoding-writing is done

            // Check pixels were written
            assertEquals(179726, layerPixels[0]);
            assertEquals(183091, layerPixels[1]);
            assertEquals(186446, layerPixels[2]);

            // Check files exist
            assertFileMinSize(layerFiles[0], 10000);
            assertFileMinSize(layerFiles[1], 10000);
            assertFileMinSize(layerFiles[2], 10000);
        } catch (MSLAException e) {
            throw new RuntimeException(e);
        }
    }
}