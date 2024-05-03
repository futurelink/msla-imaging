package futurelink.msla.formats;

import futurelink.msla.formats.anycubic.PhotonWorkshopFile;
import futurelink.msla.formats.anycubic.tables.PhotonWorkshopFileHeaderTable;
import futurelink.msla.formats.anycubic.tables.PhotonWorkshopFileMachineTable;
import futurelink.msla.formats.creality.CXDLPFile;
import futurelink.msla.formats.utils.PrinterDefaults;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DefaultsLoaderTest extends CommonTestRoutines {
    @Test
    void testDefaultsLoader() throws MSLAException {
        var defaults = PrinterDefaults.instance.getPrinter("Anycubic Photon M3 Max");
        assertEquals("Anycubic Photon M3 Max", defaults.getMachineFullName());
        assertEquals("6480x3600", defaults.getOptionsBlock("Header").get("Resolution"));
        assertEquals("34.399998", defaults.getOptionsBlock("Header").get("PixelSizeUm"));
        assertEquals(2, defaults.getFileOptions().size());
        assertEquals(8, defaults.getOptionsBlock("Machine").size());

        // Check for header options
        var header = new PhotonWorkshopFileHeaderTable.Fields(null);
        defaults.setFields("Header", header);
        assertEquals(34.399998f, header.getPixelSizeUm());
        assertEquals("6480 x 3600", header.getResolution().toString());

        // Check for special options
        assertEquals(34.399998f, defaults.getPixelSizeUm());
        assertEquals("6480 x 3600", defaults.getResolution().toString());

        // Check for machine options
        var machine = new PhotonWorkshopFileMachineTable.Fields(null);
        defaults.setFields("Machine", machine);
        assertEquals("pw0Img", machine.getLayerImageFormat());

        System.out.println(PrinterDefaults.instance.getSupportedPrinters(CXDLPFile.class));
        System.out.println(PrinterDefaults.instance.getSupportedPrinters(PhotonWorkshopFile.class));
    }
}
