package futurelink.msla.formats.creality.tables;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.utils.FileFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PhotonWorkshopOptionsTest {
    @Test
    void AvailableOptionsTest() {
        var file = FileFactory.instance.create("Anycubic Photon Mono X 6K");
        var options = file.options().getAvailable();
        System.out.println(options);
        assertEquals("PriceCurrencySymbol", options.toArray()[4]);
    }

    @Test
    void SetOptionTest() throws MSLAException {
        var file = FileFactory.instance.create("Anycubic Photon Mono X 6K");
        file.options().set("AdvancedMode", 1);
        var option = file.options().get("AdvancedMode");
        assertEquals(1, option);
        assertThrows(MSLAException.class, () -> file.options().set("AdvancedMode", "string"));
        assertThrows(MSLAException.class, () -> file.options().set("UnavailableOption", true));
    }

    @Test
    void GetDPI() {
        var file = FileFactory.instance.create("Anycubic Photon Mono X 6K");
        assertEquals(738.3722, (double) Math.round(file.getDPI() * 10000) / 10000);
    }

    @Test
    void GetResolution() {
        var file = FileFactory.instance.create("Anycubic Photon Mono X 6K");
        assertEquals("5760 x 3600", file.getResolution().toString());
    }
}
