package futurelink.msla.formats;

import futurelink.msla.formats.anycubic.tables.PhotonWorkshopFileHeaderTable;
import futurelink.msla.formats.anycubic.tables.PhotonWorkshopFileMachineTable;
import futurelink.msla.formats.iface.options.MSLAOptionName;
import futurelink.msla.utils.FileFactory;
import futurelink.msla.utils.defaults.MachineDefaults;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DefaultsLoaderTest extends CommonTestRoutines {
    @Test
    void testDefaults() throws MSLAException {
        var machine = "Anycubic Photon M3 Max";
        var defaults = MachineDefaults.getInstance().getMachineDefaults(machine)
                .orElseThrow(() -> new MSLAException("Machine has not defaults: " + machine));
        assertEquals(2.0f, defaults.getFileOption(MSLAOptionName.NormalLayersLiftSpeed1).getFloat());
        assertEquals(4.0f, defaults.getFileOption(MSLAOptionName.NormalLayersLiftSpeed2).getFloat());
    }

    @Test
    void testDefaultsLoader() throws MSLAException {
        var machine = "Anycubic Photon M3 Max";
        var defaults = MachineDefaults.getInstance().getMachineDefaults(machine)
                .orElseThrow(() -> new MSLAException("Machine has not defaults: " + machine));
        assertEquals("Anycubic Photon M3 Max", defaults.getMachineFullName());
        assertEquals("6480x3600", defaults.getFileProps().get("Resolution").getString());
        assertEquals("34.399998", defaults.getFileProps().get("PixelSize").getString());
        assertEquals(11, defaults.getFileProps().size());

        // Check for header options
        var header = new PhotonWorkshopFileHeaderTable((byte) 2, (byte) 4);
        defaults.setFields(header.getBlockFields());
        assertEquals(10.0f, header.getLiftHeight());
        assertEquals(4.0f, header.getLiftSpeed());

        // Check for special options
        assertEquals(34.399998f, defaults.getPixelSize());
        assertEquals("6480 x 3600", defaults.getResolution().toString());

        // Check for machine options
        var machineOptions = new PhotonWorkshopFileMachineTable((byte) 2, (byte) 4);
        defaults.setFields(machineOptions.getBlockFields());
        assertEquals("pw0Img", machineOptions.getLayerImageFormat());
    }

    @Test
    void testSuitableDefaults() throws MSLAException {
        var machine = "Anycubic Photon Mono X 6K";
        var wsFile = FileFactory.instance.create(machine);
        var suitableDefaults = MachineDefaults.getInstance().getMachineDefaults(wsFile);
        assertEquals(1, suitableDefaults.size());
        assertEquals(machine, suitableDefaults.get(0).getMachineFullName());
    }
}
