package futurelink.msla.formats.iface;

import futurelink.msla.formats.MSLAException;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

    /**
     * Default implementation loads a file.
     * @param fileName file name
     */
    default MSLAFile<?> load(String fileName) throws MSLAException {
        try {
            return load(new DataInputStream(new FileInputStream(fileName)));
        } catch (FileNotFoundException e) {
            throw new MSLAException("Can't load a file " + fileName, e);
        }
    }
}
