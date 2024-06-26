package io.msla.formats.anycubic;

import io.msla.formats.CommonTestRoutines;
import io.msla.formats.MSLAException;
import io.msla.formats.iface.options.MSLAOptionName;
import io.msla.utils.FileFactory;
import io.msla.utils.options.FileOptionMapper;
import io.msla.utils.defaults.MachineDefaults;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PhotonWorkshopOptionsTest extends CommonTestRoutines {
    @Test
    void AvailableOptionsTest() throws MSLAException {
        var machine = "Anycubic Photon Mono X 6K";
        var defaults = MachineDefaults.getInstance().getMachineDefaults(machine)
                .orElseThrow(() -> new MSLAException("Machine has not defaults: " + machine));
        var file = FileFactory.instance.create(machine);
        var options = new FileOptionMapper(file, defaults);
        var optionsAvailable = options.available();
        assertTrue(optionsAvailable.contains(MSLAOptionName.TransitionLayersType));
        logger.info(optionsAvailable.toString());
    }

    @Test
    void SetOptionTest() throws MSLAException {
        var machine = "Anycubic Photon Mono X 6K";
        var defaults = MachineDefaults.getInstance().getMachineDefaults(machine)
                .orElseThrow(() -> new MSLAException("Machine has not defaults: " + machine));
        var file = FileFactory.instance.create(machine);
        var options = new FileOptionMapper(file, defaults);
        options.set(MSLAOptionName.AdvancedMode, "true");
        var option = options.get(MSLAOptionName.AdvancedMode);
        assertTrue(Boolean.parseBoolean(option));
        assertThrows(MSLAException.class, () -> options.set(MSLAOptionName.AdvancedMode, "string"));
        assertThrows(MSLAException.class, () -> options.set(MSLAOptionName.AdvancedMode, "cool"));
    }

    @Test
    void GetDPI() throws MSLAException {
        var file = FileFactory.instance.create("Anycubic Photon Mono X 6K");
        assertEquals(738.3722, (double) Math.round(file.getDPI() * 10000) / 10000);
    }

    @Test
    void GetResolution() throws MSLAException {
        var file = FileFactory.instance.create("Anycubic Photon Mono X 6K");
        assertEquals("5760 x 3600", file.getResolution().toString());
    }
}
