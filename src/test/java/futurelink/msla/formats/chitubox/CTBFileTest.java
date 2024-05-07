package futurelink.msla.formats.chitubox;

import futurelink.msla.formats.CommonTestRoutines;
import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.utils.FileFactory;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CTBFileTest extends CommonTestRoutines {
    @Test
    void ReadTestFile() throws IOException {
        logger.info("Temporary dir: " + temp_dir);
        try {
            var file = (CTBFile) FileFactory.instance.load(
                    resourceFile("test_data/ChituboxFileTest/Example_ELEGOO_SATURN.ctb")
            );
            assertTrue(file.isValid());
            System.out.println(file);

            ImageIO.write(file.getPreview().getImage(), "png", new File(temp_dir + "ctb_preview.png"));
            assertFileExactSize(temp_dir + "ctb_preview.png", 6801);
        } catch (MSLAException e) {
            throw new RuntimeException(e);
        }
    }
}
