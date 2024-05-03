package futurelink.msla.formats.elegoo;

import futurelink.msla.formats.CommonTestRoutines;
import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.utils.FileFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GOOFileTest extends CommonTestRoutines {
    @Test
    void CreateFileTest() throws MSLAException {
        var file = FileFactory.instance.create("ELEGOO ELEGOO");
        assertEquals(GOOFile.class, file.getClass());
        assertEquals("3840 x 2400", file.getResolution().toString());
        assertEquals(35.0F, file.getPixelSizeUm());

        file.write(System.out);
    }
}
