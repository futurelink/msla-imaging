package futurelink.msla.formats.anycubic;

import futurelink.msla.formats.CommonTestRoutines;
import futurelink.msla.formats.MSLAException;
import futurelink.msla.utils.FileFactory;
import futurelink.msla.utils.FileOptionMapper;
import futurelink.msla.utils.defaults.PrinterDefaults;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PhotonWorkshopOptionsTest extends CommonTestRoutines {
    @Test
    void AvailableOptionsTest() throws MSLAException {
        var machine = "Anycubic Photon Mono X 6K";
        var defaults = PrinterDefaults.instance.getPrinter(machine)
                .orElseThrow(() -> new MSLAException("Machine has not defaults: " + machine));
        var file = FileFactory.instance.create(machine);
        var options = new FileOptionMapper(file, defaults);
        var optionsAvailable = options.available();
        logger.info(optionsAvailable.toString());
        assertEquals("Transition layer type", optionsAvailable.toArray()[4]);
    }

    @Test
    void SetOptionTest() throws MSLAException {
        var machine = "Anycubic Photon Mono X 6K";
        var defaults = PrinterDefaults.instance.getPrinter(machine)
                .orElseThrow(() -> new MSLAException("Machine has not defaults: " + machine));
        var file = FileFactory.instance.create(machine);
        var options = new FileOptionMapper(file, defaults);
        options.set("Advanced mode", "1");
        var option = options.get("Advanced mode");
        assertEquals(1, Integer.parseInt(option));
        assertThrows(NumberFormatException.class, () -> options.set("Advanced mode", "string"));
        assertThrows(MSLAException.class, () -> options.set("UnavailableOption", "true"));
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
