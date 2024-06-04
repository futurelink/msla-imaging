package futurelink.msla.formats;

import futurelink.msla.utils.FileFactory;
import futurelink.msla.tools.PCBCalibration;
import futurelink.msla.utils.FileOptionMapper;
import futurelink.msla.utils.defaults.PrinterDefaults;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PCBCalibrationTest extends CommonTestRoutines {
    @Test
    void TestPCBCalibrationOnPhoton() throws MSLAException {
        var machineName = "Anycubic Photon Mono X 6K";
        var filePath = PCBCalibration.generateTestPattern(
                machineName,
                temp_dir + "test_pcb_calibration",
                10, 1, 10);

        assertFileExactSize(filePath, 3048460);

        var defaults = PrinterDefaults.instance.getPrinter(machineName)
                .orElseThrow(() -> new MSLAException("Machine has not defaults: " + machineName));
        var file = FileFactory.instance.load(filePath);
        var options = new FileOptionMapper(file, defaults);
        logger.info(file.toString());

        assertEquals("224 x 168", file.getPreview((short) 0).getResolution().toString());
        assertEquals("5760 x 3600", file.getResolution().toString());
        assertEquals(10, file.getLayers().count());
        assertEquals(2.0F, Float.parseFloat(options.get("Normal layers lift speed")));
        assertEquals(10, Integer.parseInt(options.get("Transition layers count")));
        assertEquals(1, Integer.parseInt(options.get("Advanced mode")));
    }

    @Test
    void TestPCBCalibrationOnCreality() throws MSLAException {
        var machineName = "CREALITY HALOT-ONE PLUS";
        var filePath = PCBCalibration.generateTestPattern(
                machineName,
                temp_dir + "test_pcb_calibration",
                10, 1, 10);

        assertFileExactSize(filePath,472231);

        var defaults = PrinterDefaults.instance.getPrinter(machineName)
                .orElseThrow(() -> new MSLAException("Machine has not defaults: " + machineName));
        var file = FileFactory.instance.load(filePath);
        var options = new FileOptionMapper(file, defaults);
        logger.info(file.toString());

        assertEquals("116 x 116", file.getPreview((short) 0).getResolution().toString());
        assertEquals("4320 x 2560", file.getResolution().toString());
        assertEquals(10, file.getLayers().count());
        assertEquals("1", options.get("Normal layers lift speed"));
        assertEquals("0", options.get("Antialias"));
    }
}
