package futurelink.msla.formats;

import futurelink.msla.formats.anycubic.PhotonWorkshopFile;
import futurelink.msla.formats.anycubic.tables.PhotonWorkshopFileHeaderTable;
import futurelink.msla.formats.anycubic.tables.PhotonWorkshopFileMachineTable;
import futurelink.msla.formats.creality.CXDLPFile;
import futurelink.msla.utils.FileFactory;
import futurelink.msla.utils.defaults.PrinterDefaults;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DefaultsLoaderTest extends CommonTestRoutines {
    @Test
    void testDefaultsLoader() throws MSLAException {
        var machine = "Anycubic Photon M3 Max";
        var defaults = PrinterDefaults.instance.getPrinter(machine)
                .orElseThrow(() -> new MSLAException("Machine has not defaults: " + machine));
        assertEquals("Anycubic Photon M3 Max", defaults.getMachineFullName());
        assertEquals("6480x3600", defaults.getOptionsBlock("Header").getOption("Resolution").getDefaultValue());
        assertEquals("34.399998", defaults.getOptionsBlock("Header").getOption("PixelSizeUm").getDefaultValue());
        assertEquals(2, defaults.getFileProps().size());
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
        var machineOptions = new PhotonWorkshopFileMachineTable.Fields(null);
        defaults.setFields("Machine", machineOptions);
        assertEquals("pw0Img", machineOptions.getLayerImageFormat());

        System.out.println(PrinterDefaults.instance.getSupportedPrinters(CXDLPFile.class));
        System.out.println(PrinterDefaults.instance.getSupportedPrinters(PhotonWorkshopFile.class));
    }

    @Test
    void testSuitableDefaults() throws MSLAException {
        var machine = "Anycubic Photon Mono X 6K";
        var wsFile = FileFactory.instance.create(machine);
        var suitableDefaults = PrinterDefaults.instance.getSuitableDefaults(wsFile);
        assertEquals(1, suitableDefaults.size());
        assertEquals(machine, suitableDefaults.get(0).getMachineFullName());
    }
}
