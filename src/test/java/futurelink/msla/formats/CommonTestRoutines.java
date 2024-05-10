package futurelink.msla.formats;

import futurelink.msla.formats.anycubic.tables.PhotonWorkshopFileHeaderTable;
import futurelink.msla.formats.iface.MSLAFile;
import futurelink.msla.formats.utils.FileFieldsException;
import futurelink.msla.formats.utils.FileFieldsIO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CommonTestRoutines {
    protected static Logger logger = Logger.getLogger(CommonTestRoutines.class.getName());
    protected final String temp_dir = System.getProperty("java.io.tmpdir");

    @BeforeAll
    static void setUpBeforeClass() {
        System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tF %1$tT | %4$s | %2$s -> %5$s%6$s%n");
    }

    protected void writeMSLAFile(String fileName, MSLAFile<?> file) throws MSLAException {
        try(var fos = new FileOutputStream(fileName)) {
            file.write(fos);
            fos.flush();
        } catch (IOException e) {
            throw new MSLAException("Can't write test file", e);
        }
    }

    protected void delete_file(String file_path) {
        var temp_file = new File(file_path);
        var deleted = temp_file.delete();
        if (deleted) logger.info("Temp file " + file_path + " deleted");
    }

    protected String resourceFile(String resourceName) {
        var classLoader = getClass().getClassLoader();
        var resource = classLoader.getResource(resourceName);
        if (resource == null) throw new RuntimeException("Resource data file not found");
        return resource.getFile();
    }

    protected void assertFileMinSize(String fileName, int minSize) {
        var outFile2 = new File(fileName);
        outFile2.deleteOnExit();
        assertTrue(outFile2.exists());
        assertTrue(outFile2.length() > minSize);
    }

    protected void assertFileExactSize(String fileName, int size) {
        var outFile2 = new File(fileName);
        outFile2.deleteOnExit();
        assertTrue(outFile2.exists());
        assertEquals(size, outFile2.length());
    }
}
