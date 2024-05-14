package futurelink.msla.formats.anycubic;

import futurelink.msla.formats.CommonTestRoutines;
import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.anycubic.tables.*;
import futurelink.msla.formats.utils.FileFieldsException;
import futurelink.msla.formats.utils.FileFieldsIO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PhotonWorkshopTableLengthTest extends CommonTestRoutines {
    @Test
    void HeaderTableTest() throws FileFieldsException {
        var block = new PhotonWorkshopFileHeaderTable((byte) 2, (byte) 0);
        var length = FileFieldsIO.getBlockLength(block);
        assertEquals(96, length);
        System.out.println(length);
    }

    @Test
    void ExtraTableTest() throws FileFieldsException {
        var block = new PhotonWorkshopFileExtraTable((byte) 2, (byte) 0);
        var length = FileFieldsIO.getBlockLength(block);
        assertEquals(72, length);
        System.out.println(length);
    }

    @Test
    void MachineTableTest() throws FileFieldsException {
        var block = new PhotonWorkshopFileMachineTable((byte) 2, (byte) 0);
        var length = FileFieldsIO.getBlockLength(block);
        assertEquals(156, length);
        System.out.println(length);
    }

    @Test
    void PreviewTableTest() throws FileFieldsException {
        var block = new PhotonWorkshopFilePreviewTable((byte) 2, (byte) 0);
        var length = FileFieldsIO.getBlockLength(block);

        // Add 16 bytes of shades of gray table that should not be counted
        assertEquals(75304 + 16, length);
        System.out.println(length);
    }

    @Test
    void SoftwareTableTest() throws FileFieldsException {
        var block = new PhotonWorkshopFileSoftwareTable((byte) 2, (byte) 0);
        var length = FileFieldsIO.getBlockLength(block);
        assertEquals(164, length);
        System.out.println(length);
    }

    @Test
    void LayerDefTableTest() throws FileFieldsException {
        // Test empty layer definition table
        var block = new PhotonWorkshopFileLayerDefTable((byte) 2, (byte) 0);
        block.allocate();
        var length = FileFieldsIO.getBlockLength(block);
        assertEquals(52, length);
        System.out.println(block.calculateTableLength());
        System.out.println(length);
    }
}
