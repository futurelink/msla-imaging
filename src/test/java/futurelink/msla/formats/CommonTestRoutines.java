package futurelink.msla.formats;

import org.junit.jupiter.api.BeforeAll;

import java.io.File;
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
