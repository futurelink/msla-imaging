package futurelink.msla.formats.iface;

import futurelink.msla.formats.MSLAException;

import java.io.DataInputStream;
import java.util.Set;

public interface MSLAFileFactory {
    String getName();
    MSLAFile<?> create(String machineName) throws MSLAException;
    MSLAFile<?> load(String machineName, String fileName) throws MSLAException;
    MSLAFile<?> load(String machineName, DataInputStream stream) throws MSLAException;
    boolean checkType(DataInputStream stream) throws MSLAException;
    MSLAFileDefaults defaults(String machineName);
    boolean checkDefaults(String machineName);
    Set<String> getSupportedMachines();
}
