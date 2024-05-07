package futurelink.msla.formats.anycubic;

import futurelink.msla.formats.CommonTestRoutines;
import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.utils.FileFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PhotonWorkshopOptionsTest extends CommonTestRoutines {
    @Test
    void AvailableOptionsTest() throws MSLAException {
        var file = FileFactory.instance.create("Anycubic Photon Mono X 6K");
        var options = file.options().getAvailable();
        logger.info(options.toString());
        assertEquals("BottomLiftSpeed1", options.toArray()[4]);
    }

    @Test
    void SetOptionTest() throws MSLAException {
        var file = FileFactory.instance.create("Anycubic Photon Mono X 6K");
        file.options().set("AdvancedMode", 1);
        var option = file.options().get("AdvancedMode");
        assertEquals(1, option);
        assertThrows(ClassCastException.class, () -> file.options().set("AdvancedMode", "string"));
        assertThrows(MSLAException.class, () -> file.options().set("UnavailableOption", true));
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
