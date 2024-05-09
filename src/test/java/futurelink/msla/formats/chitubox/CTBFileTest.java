package futurelink.msla.formats.chitubox;

import futurelink.msla.formats.CommonTestRoutines;
import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.utils.FileFactory;
import futurelink.msla.tools.ImageWriter;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CTBFileTest extends CommonTestRoutines {
    @Test
    void ReadTestFile() throws IOException, InterruptedException {
        logger.info("Temporary dir: " + temp_dir);
        try {
            var file = (CTBFile) FileFactory.instance.load(
                    resourceFile("test_data/ChituboxFileTest/Example_ELEGOO_SATURN.ctb")
            );
            assertTrue(file.isValid());
            assertEquals("3840 x 2400", file.getResolution().toString());
            System.out.println(file);

            ImageIO.write(file.getPreview().getImage(), "png", new File(temp_dir + "ctb_preview.png"));
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
            while (file.getDecodersPool().isDecoding()) Thread.sleep(100); // Wait while decoding-writing is done

            // Check pixels were written
            assertEquals(82031, layerPixels[0]);
            assertEquals(83634, layerPixels[1]);
            assertEquals(85202, layerPixels[2]);
        } catch (MSLAException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void CreateTestFile() throws MSLAException {
        var file = (CTBFile) FileFactory.instance.create("ELEGOO Saturn");
        assertTrue(file.isValid());
        System.out.println(file);
    }
}
