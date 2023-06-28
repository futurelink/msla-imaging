package futurelink.msla.formats;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Set;

public interface MSLAFileFactory {
    String getName();
    MSLAFile create(String machineName) throws IOException;
    MSLAFile load(String fileName) throws IOException;
    boolean checkType(FileInputStream stream) throws IOException;
    MSLAFileDefaults defaults(String machineName);
    boolean checkDefaults(String machineName);
    Set<String> getSupportedMachines();
}
