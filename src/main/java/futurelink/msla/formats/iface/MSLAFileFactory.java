package futurelink.msla.formats.iface;

import futurelink.msla.formats.MSLAException;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Set;

public interface MSLAFileFactory {
    String getName();
    MSLAFile create(String machineName) throws MSLAException, IOException;
    MSLAFile load(String fileName) throws MSLAException, IOException;
    boolean checkType(FileInputStream stream) throws MSLAException, IOException;
    MSLAFileDefaults defaults(String machineName);
    boolean checkDefaults(String machineName);
    Set<String> getSupportedMachines();
}
