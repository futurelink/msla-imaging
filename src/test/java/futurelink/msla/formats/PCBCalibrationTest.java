package futurelink.msla.formats;

import futurelink.msla.formats.utils.FileFactory;
import futurelink.msla.tools.PCBCalibration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PCBCalibrationTest extends CommonTestRoutines {
    @Test
    void TestPCBCalibrationOnPhoton() throws MSLAException {

        var filePath = PCBCalibration.generateTestPattern(
                "Anycubic Photon Mono X 6K",
                temp_dir + "test_pcb_calibration",
                10, 1, 10);

        assertFileExactSize(filePath, 3048296);

        var file = FileFactory.instance.load(filePath);
        logger.info(file.toString());

        assertEquals("224 x 168", file.getPreview().getResolution().toString());
        assertEquals("5760 x 3600", file.getResolution().toString());
        assertEquals(10, file.getLayerCount());
        assertEquals(2.0F, file.options().get("LiftSpeed"));
        assertEquals(10, file.options().get("TransitionLayerCount"));
        assertEquals(1, file.options().get("AdvancedMode"));
    }

    @Test
    void TestPCBCalibrationOnCreality() throws MSLAException {
        var filePath = PCBCalibration.generateTestPattern(
                "CREALITY HALOT-ONE PLUS",
                temp_dir + "test_pcb_calibration",
                10, 1, 10);

        assertFileExactSize(filePath,472231);

        var file = FileFactory.instance.load(filePath);
        logger.info(file.toString());

        assertEquals("116 x 116", file.getPreview().getResolution().toString());
        assertEquals("4320 x 2560", file.getResolution().toString());
        assertEquals(10, file.getLayerCount());
        assertEquals(1, (short) file.options().get("LiftSpeed"));
        assertEquals(0, (byte) file.options().get("AntiAliasEnabled"));
    }
}
