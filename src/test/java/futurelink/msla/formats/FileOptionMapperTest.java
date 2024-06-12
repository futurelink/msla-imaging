package futurelink.msla.formats;

import futurelink.msla.formats.iface.options.MSLAOptionName;
import futurelink.msla.utils.FileFactory;
import futurelink.msla.tools.ImageReader;
import futurelink.msla.utils.options.FileOptionMapper;
import futurelink.msla.utils.options.LayerOptionMapper;
import futurelink.msla.utils.defaults.MachineDefaults;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FileOptionMapperTest extends CommonTestRoutines {
    @Test
    void LayerOptionsTest() throws MSLAException {
        var machine = "ELEGOO SATURN";
        var defaults = MachineDefaults.getInstance().getMachineDefaults(machine)
                .orElseThrow(() -> new MSLAException("Printer has no defaults: " + machine));
        var file = FileFactory.instance.create(machine);
        var options = new FileOptionMapper(file, defaults);

        // Assert Chitubox format has 29 file options
        assertEquals(29, options.available().size());

        options.set(MSLAOptionName.BottomLayersExposureTime, "12");
        assertEquals(12f, Float.parseFloat(options.get(MSLAOptionName.BottomLayersExposureTime)));

        assertEquals("0.05", options.get(MSLAOptionName.LayerHeight));

        // It's necessary to add a layer to access layer options
        var resource = resourceFile("test_data/ChituboxFileTest/ELEGOO_Saturn_Layer_0.png");
        var encoders = file.getEncodersPool();
        try {
            file.addLayer(new ImageReader(file, resource), null);
            while (encoders.isEncoding()) Thread.sleep(10); // Wait while reading-encoding is done
        } catch (IOException | InterruptedException e) {
            throw new MSLAException("Error adding layers", e);
        }

        // Assert Chitubox format has 13 layer options
        var layerOptions = new LayerOptionMapper(file, defaults.getLayerDefaults());
        assertEquals(13, layerOptions.available().size());

        // Try to set layer option and retrieve its value
        layerOptions.setLayerNumber(0);
        layerOptions.set(MSLAOptionName.LayerLightPWM, "128");
        assertEquals(128f, Float.parseFloat(layerOptions.get(MSLAOptionName.LayerLightPWM)));

        System.out.println(layerOptions.available());
        for (var option : layerOptions.available()) {
            System.out.print(option);
            System.out.println(" = " + layerOptions.get(option));
        }

    }

    @Test
    void LayerOptionParamsTest() throws MSLAException {
        var machine = "Anycubic Photon M3 Max";
        var defaults = MachineDefaults.getInstance().getMachineDefaults(machine)
                .orElseThrow(() -> new MSLAException("Printer has no defaults: " + machine));
        var file = FileFactory.instance.create(machine);
        var options = new FileOptionMapper(file, defaults);
        assertEquals(30.0f, options.getParameters(MSLAOptionName.BottomLayersExposureTime).getFloat());
        assertEquals(Float.class, options.getType(MSLAOptionName.BottomLayersExposureTime));

        // Check option group mappings
        assertEquals("Print settings", options.getGroup(MSLAOptionName.LayerHeight).name());
        assertEquals("All layers", options.getGroup(MSLAOptionName.LightPWM).name());

        // Check valid value is processed properly
        options.set(MSLAOptionName.BottomLayersExposureTime, "50");
        assertEquals(50.0f, Float.parseFloat(options.get(MSLAOptionName.BottomLayersExposureTime)));

        // Check min and max values are processed properly
        assertThrows(MSLAException.class, () -> options.set(MSLAOptionName.BottomLayersExposureTime, "121"));
        assertThrows(MSLAException.class, () -> options.set(MSLAOptionName.BottomLayersExposureTime, "-1"));
    }

    @Test
    void GetOptionGroupsTest() throws MSLAException {
        var machine = "Anycubic Photon M3 Max";
        var defaults = MachineDefaults.getInstance().getMachineDefaults(machine)
                .orElseThrow(() -> new MSLAException("Printer has no defaults: " + machine));
        var file = FileFactory.instance.create(machine);
        var options = new FileOptionMapper(file, defaults);
        assertEquals(5, options.getGroups().size()); // We expect 5 option groups for this machine
    }
}
