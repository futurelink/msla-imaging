package futurelink.msla.formats;

import futurelink.msla.formats.iface.options.MSLAOptionName;
import futurelink.msla.utils.FileFactory;
import futurelink.msla.tools.PCBCalibration;
import futurelink.msla.utils.options.FileOptionMapper;
import futurelink.msla.utils.defaults.MachineDefaults;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PCBCalibrationTest extends CommonTestRoutines {
    @Test
    void TestPCBCalibrationOnPhoton() throws MSLAException {
        var machineName = "Anycubic Photon Mono X 6K";
        var filePath = PCBCalibration.generateTestPattern(
                machineName,
                temp_dir + "test_pcb_calibration",
                10, 1, 10);

        assertFileExactSize(filePath, 3048460);

        var defaults = MachineDefaults.getInstance().getMachineDefaults(machineName)
                .orElseThrow(() -> new MSLAException("Machine has not defaults: " + machineName));
        var file = FileFactory.instance.load(filePath);
        var options = new FileOptionMapper(file, defaults);
        logger.info(file.toString());

        assertEquals("224 x 168", file.getPreview((short) 0).getResolution().toString());
        assertEquals("5760 x 3600", file.getResolution().toString());
        assertEquals(10, file.getLayers().count());
        assertEquals(2.0F, Float.parseFloat(options.get(MSLAOptionName.LiftSpeed)));
        assertEquals(10, Integer.parseInt(options.get(MSLAOptionName.TransitionLayersCount)));
        assertTrue(Boolean.parseBoolean(options.get(MSLAOptionName.AdvancedMode)));
    }

    @Test
    void TestPCBCalibrationOnCreality() throws MSLAException {
        var machineName = "CREALITY HALOT-ONE PLUS";
        var filePath = PCBCalibration.generateTestPattern(
                machineName,
                temp_dir + "test_pcb_calibration",
                10, 1, 10);

        assertFileExactSize(filePath,472231);

        var defaults = MachineDefaults.getInstance().getMachineDefaults(machineName)
                .orElseThrow(() -> new MSLAException("Machine has not defaults: " + machineName));
        var file = FileFactory.instance.load(filePath);
        var options = new FileOptionMapper(file, defaults);
        logger.info(file.toString());

        assertEquals("116 x 116", file.getPreview((short) 0).getResolution().toString());
        assertEquals("4320 x 2560", file.getResolution().toString());
        assertEquals(10, file.getLayers().count());
        assertEquals("1", options.get(MSLAOptionName.NormalLayersLiftSpeed));
        assertFalse(Boolean.parseBoolean(options.get(MSLAOptionName.Antialias)));
    }
}
