package futurelink.msla.formats.utils;

import futurelink.msla.formats.*;
import futurelink.msla.formats.anycubic.PhotonWorkshopFileFactory;
import futurelink.msla.formats.creality.CXDLPFileFactory;
import futurelink.msla.formats.iface.MSLAFile;
import futurelink.msla.formats.iface.MSLAFileDefaults;
import futurelink.msla.formats.iface.MSLAFileFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Common entry point for mSLA file manipulation.
 */
public final class FileFactory {
    private static final ArrayList<MSLAFileFactory> supportedFiles = new ArrayList<>();
    public static FileFactory instance = new FileFactory();

    private FileFactory() {
        addFileTypeFactory(new PhotonWorkshopFileFactory());
        addFileTypeFactory(new CXDLPFileFactory());
    }

    public void addFileTypeFactory(MSLAFileFactory factory) {
        supportedFiles.add(factory);
    }

    /**
     * Returns an object containing default values suitable for a specified machine.
     *
     * @param machineName machine name (can be obtained with getSupportedMachines)
     * @return MSLAFileDefaults
     */
    public MSLAFileDefaults defaults(String machineName) {
        return supportedFiles
                .stream()
                .filter((f) -> f.checkDefaults(machineName))
                .findFirst()
                .map(f -> f.defaults(machineName))
                .orElse(null);
    }

    /**
     * Creates a file object that can be processed by a specified machine.
     *
     * @param machineName machine name (can be obtained with getSupportedMachines)
     * @return MSLAFile
     */
    public MSLAFile create(String machineName) {
        return supportedFiles
                .stream()
                .filter((f) -> f.checkDefaults(machineName))
                .findFirst()
                .map(f -> {
                    try { return f.create(machineName); } catch (Exception e) { throw new RuntimeException(e); }
                })
                .orElse(null);
    }

    /**
     * Loads a mSLA data file.
     *
     * @param fileName data file to load
     * @return MSLAFile
     * @throws IOException on IO errors
     * @throws MSLAException on mSLA format related errors
     */
    public MSLAFile load(String fileName) throws MSLAException {
        try (var stream = new FileInputStream(fileName)) {
            return supportedFiles.stream().filter((t) -> {
                try {
                    if (t.checkType(stream)) {
                        System.out.println("Found file of type " + t.getClass());
                        return true;
                    }
                    return false;
                } catch (Exception e) { throw new RuntimeException(e); }
            }).findFirst().map(f -> {
                try { return f.load(fileName); } catch (Exception e) { throw new RuntimeException(e); }
            }).orElse(null);
        } catch (IOException e) {
            throw new MSLAException("File error", e);
        }
    }

    /**
     * Returns a list of supported machines (producer/model)
     *
     * @return list of string values
     */
    public ArrayList<String> getSupportedMachines() {
        var a = new ArrayList<String>();
        supportedFiles.forEach((f) -> { if (f.getSupportedMachines() != null) a.addAll(f.getSupportedMachines()); });
        return a;
    }
}
