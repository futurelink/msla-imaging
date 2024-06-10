package futurelink.msla.formats.chitubox;

import futurelink.msla.formats.CommonTestRoutines;
import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.chitubox.common.tables.*;
import futurelink.msla.formats.io.FileFieldsException;
import futurelink.msla.formats.io.FileFieldsIO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CTBFileTableLengthTest extends CommonTestRoutines {

    @Test
    void CTBFileHeaderTest() throws FileFieldsException, MSLAException {
        var block = new CTBFileHeader(4, null);
        var length = FileFieldsIO.getBlockLength(block);
        assertEquals(112, length);
    }

    @Test
    void CTBFilePreviewTest() throws FileFieldsException {
        var block = new CTBFilePreview(4, CTBFilePreview.Type.Large);
        var length = FileFieldsIO.getBlockLength(block);
        assertEquals(152, length);
    }

    @Test
    void CTBFilePrintParamsTest() throws FileFieldsException {
        var block = new CTBFilePrintParams(4);
        var length = FileFieldsIO.getBlockLength(block);
        assertEquals(60, length);
    }

    @Test
    void CTBFilePrintParamsV4Test() throws FileFieldsException {
        var block = new CTBFilePrintParamsV4(4);
        var length = FileFieldsIO.getBlockLength(block);
        assertEquals(464, length);
    }

    @Test
    void CTBFileSliceInfoTest() throws FileFieldsException {
        var block = new CTBFileSlicerInfo(4);
        var length = FileFieldsIO.getBlockLength(block);
        assertEquals(76, length);
    }

    @Test
    void CTBFileLayerDefTest() throws FileFieldsException, MSLAException {
        var block = new CTBFileLayerDef(4);
        var length = FileFieldsIO.getBlockLength(block.getBlockFields());
        assertEquals(CTBFileLayerDef.BRIEF_TABLE_SIZE, length);
    }

    @Test
    void CTBFileLayerDefExtra() throws FileFieldsException {
        var block = new CTBFileLayerDefExtra();
        var length = FileFieldsIO.getBlockLength(block);
        assertEquals(CTBFileLayerDefExtra.TABLE_SIZE, length);
    }
}
