package futurelink.msla.formats;

import futurelink.msla.formats.iface.MSLALayerEncodeReader;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BufferedImageTest extends CommonTestRoutines {
    @Test
    void TestBufferedImageStream() {
        var img = new BufferedImage(200, 100, BufferedImage.TYPE_BYTE_GRAY);
        img.getGraphics().drawLine(0, 10, 0, 50);
        img.getGraphics().drawLine( 0,55, 0, 75);
        img.getGraphics().drawLine( 20, 55, 20, 75);

        try (var bis = new BufferedImageInputStream(img, MSLALayerEncodeReader.ReadDirection.READ_ROW)) {
            assertEquals(20000, bis.available()); // 200 * 100 * 1 byte
            assertEquals(20000, bis.readAllBytes().length);

            bis.reset(); // Test reset, should go to position 0
            assertEquals(20000, bis.available()); // 200 * 100 * 1 byte
            assertEquals(20000, bis.readAllBytes().length);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
