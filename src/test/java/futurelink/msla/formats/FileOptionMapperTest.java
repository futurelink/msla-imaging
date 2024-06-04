package futurelink.msla.formats;

import futurelink.msla.formats.iface.MSLAFileLayer;
import futurelink.msla.utils.FileFactory;
import futurelink.msla.tools.ImageReader;
import futurelink.msla.utils.FileOptionMapper;
import futurelink.msla.utils.LayerOptionMapper;
import futurelink.msla.utils.defaults.PrinterDefaults;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FileOptionMapperTest extends CommonTestRoutines {
    @Test
    @SuppressWarnings("unchecked")
    void LayerOptionsTest() throws MSLAException {
        var machine = "ELEGOO SATURN";
        var defaults = PrinterDefaults.instance.getPrinter(machine)
                .orElseThrow(() -> new MSLAException("Printer has no defaults: " + machine));
        var file = FileFactory.instance.create(machine);
        var options = new FileOptionMapper(file, defaults);

        // Assert Chitubox format has 24 file options
        assertEquals(24, options.available().size());

        options.set("Bottom layers exposure time", "12");
        assertEquals(12f, Float.parseFloat(options.get("Bottom layers exposure time")));

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
        var layerOptions = new LayerOptionMapper(file, defaults.getLayerDefaults());
        assertEquals(13, layerOptions.available().size());

        // Try to set layer option and retrieve its value
        layerOptions.setLayerNumber(0);
        layerOptions.set("Light PWM", "128");
        assertEquals(128f, Float.parseFloat(layerOptions.get("Light PWM")));
    }

    @Test
    void LayerOptionParamsTest() throws MSLAException {
        var machine = "Anycubic Photon M3 Max";
        var defaults = PrinterDefaults.instance.getPrinter(machine)
                .orElseThrow(() -> new MSLAException("Printer has no defaults: " + machine));
        var file = FileFactory.instance.create(machine);
        var options = new FileOptionMapper(file, defaults);
        assertEquals(30.0f, options.getParameters("Bottom layers exposure time").getFloat());
        assertEquals(Float.class, options.getType("Bottom layers exposure time"));

        // Check valid value is processed properly
        options.set("Bottom layers exposure time", "50");
        assertEquals(50.0f, Float.parseFloat(options.get("Bottom layers exposure time")));

        // Check min and max values are processed properly
        assertThrows(MSLAException.class, () -> options.set("Bottom layers exposure time", "121"));
        assertThrows(MSLAException.class, () -> options.set("Bottom layers exposure time", "-1"));
    }
}
