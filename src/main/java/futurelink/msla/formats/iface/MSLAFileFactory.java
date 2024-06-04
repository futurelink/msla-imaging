package futurelink.msla.formats.iface;

import futurelink.msla.formats.MSLAException;

import java.io.DataInputStream;
import java.util.Set;

public interface MSLAFileFactory {
    String getName();
    MSLAFile<?> create(MSLAFileDefaults.FileProps initialProps) throws MSLAException;
    MSLAFile<?> load(String fileName) throws MSLAException;
    MSLAFile<?> load(DataInputStream stream) throws MSLAException;
    boolean checkType(DataInputStream stream) throws MSLAException;
    boolean checkDefaults(String machineName);
    Set<String> getSupportedMachines();
}
