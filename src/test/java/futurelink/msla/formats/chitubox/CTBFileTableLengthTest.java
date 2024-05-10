package futurelink.msla.formats.chitubox;

import futurelink.msla.formats.CommonTestRoutines;
import futurelink.msla.formats.chitubox.tables.*;
import futurelink.msla.formats.utils.FileFieldsException;
import futurelink.msla.formats.utils.FileFieldsIO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CTBFileTableLengthTest extends CommonTestRoutines {

    @Test
    void CTBFileHeaderTest() throws FileFieldsException {
        var block = new CTBFileHeader();
        var length = FileFieldsIO.getBlockLength(block.getFileFields());
        assertEquals(112, length);
    }

    @Test
    void CTBFilePreviewTest() throws FileFieldsException {
        var block = new CTBFilePreview(CTBFilePreview.Type.Large);
        var length = FileFieldsIO.getBlockLength(block.getFileFields());
        assertEquals(152, length);
    }

    @Test
    void CTBFilePrintParamsTest() throws FileFieldsException {
        var block = new CTBFilePrintParams();
        var length = FileFieldsIO.getBlockLength(block.getFileFields());
        assertEquals(60, length);
    }

    @Test
    void CTBFilePrintParamsV4Test() throws FileFieldsException {
        var block = new CTBFilePrintParamsV4();
        var length = FileFieldsIO.getBlockLength(block.getFileFields());
        assertEquals(464, length);
    }

    @Test
    void CTBFileSliceInfoTest() throws FileFieldsException {
        var block = new CTBFileSlicerInfo();
        var length = FileFieldsIO.getBlockLength(block.getFileFields());
        assertEquals(76, length);
    }

    @Test
    void CTBFileLayerDefTest() throws FileFieldsException {
        var block = new CTBFileLayerDef();
        var length = FileFieldsIO.getBlockLength(block.getFileFields());
        assertEquals(CTBFileLayerDef.TABLE_SIZE, length);
    }

    @Test
    void CTBFileLayerDefExtra() throws FileFieldsException {
        var block = new CTBFileLayerDefExtra();
        var length = FileFieldsIO.getBlockLength(block.getFileFields());
        assertEquals(CTBFileLayerDefExtra.TABLE_SIZE, length);
    }
}
