package futurelink.msla.formats.iface;

import futurelink.msla.formats.MSLAException;

import java.io.DataInputStream;
import java.util.Set;

public interface MSLAFileFactory {
    String getName();
    MSLAFile<?> create(MSLAFileProps initialProps) throws MSLAException;
    boolean checkType(DataInputStream stream) throws MSLAException;
    boolean checkDefaults(String machineName) throws MSLAException;
    Set<String> getSupportedMachines() throws MSLAException;

    /**
     * Implement this to load mSLA file data from stream.
     * @param stream input data stream
     */
    MSLAFile<?> load(DataInputStream stream) throws MSLAException;
}
