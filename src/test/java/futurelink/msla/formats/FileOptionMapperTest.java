package futurelink.msla.formats;

import futurelink.msla.formats.utils.FileFactory;
import futurelink.msla.tools.ImageReader;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FileOptionMapperTest extends CommonTestRoutines {
    @Test
    @SuppressWarnings("unchecked")
    void LayerOptionsTest() throws MSLAException {
        var file = FileFactory.instance.create("ELEGOO Saturn");

        // Assert Chitubox format has 24 file options
        assertEquals(24, file.getOptions().available().size());

        file.getOptions().set("Bottom layers exposure time", 12.0F);
        assertEquals(12f, file.getOptions().get("Bottom layers exposure time"));

        // It's necessary to add a layer to access layer options
        var resource = resourceFile("test_data/ChituboxFileTest/ELEGOO_Saturn_Layer_0.png");
        var encoders = file.getEncodersPool();
        try {
            file.addLayer(new ImageReader(file, resource), null);
            while (encoders.isEncoding()) Thread.sleep(100); // Wait while reading-encoding is done
        } catch (IOException | InterruptedException e) {
            throw new MSLAException("Error adding layers", e);
        }

        // Assert Chitubox format has 13 layer options
        assertEquals(13, file.getLayers().options(0).available().size());

        // Try to set layer option and retrieve its value
        file.getLayers().options(0).set("Light PWM", 128f);
        assertEquals(128f, file.getLayers().options(0).get("Light PWM"));
    }
}
