package futurelink.msla.formats.iface;

import futurelink.msla.formats.MSLAException;

import java.io.FileInputStream;
import java.util.Set;

public interface MSLAFileFactory {
    String getName();
    MSLAFile<?> create(String machineName) throws MSLAException;
    MSLAFile<?> load(String fileName) throws MSLAException;
    boolean checkType(FileInputStream stream) throws MSLAException;
    MSLAFileDefaults defaults(String machineName);
    boolean checkDefaults(String machineName);
    Set<String> getSupportedMachines();
}
