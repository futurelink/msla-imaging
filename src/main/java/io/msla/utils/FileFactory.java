package io.msla.utils;

import io.msla.formats.*;
import io.msla.formats.anycubic.PhotonWorkshopFileFactory;
import io.msla.formats.chitubox.CTBFileFactory;
import io.msla.formats.creality.CXDLPFileFactory;
import io.msla.formats.elegoo.GOOFileFactory;
import io.msla.formats.iface.MSLAFile;
import io.msla.formats.iface.MSLAFileFactory;
import io.msla.utils.defaults.MachineDefaults;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Common entry point for mSLA file manipulation.
 */
public final class FileFactory {
    private static final Logger logger = Logger.getLogger(FileFactory.class.getName());
    private static final ArrayList<MSLAFileFactory> supportedFiles = new ArrayList<>();
    public static FileFactory instance;

    static { if (instance == null) instance = new FileFactory(); }

    private FileFactory() {
        addFileTypeFactory(new PhotonWorkshopFileFactory());
        addFileTypeFactory(new CXDLPFileFactory());
        addFileTypeFactory(new GOOFileFactory());
        addFileTypeFactory(new CTBFileFactory());
    }

    public void addFileTypeFactory(MSLAFileFactory factory) {
        supportedFiles.add(factory);
    }

    private Optional<MSLAFileFactory> getMachineFactory(String machineName) {
        return supportedFiles.stream().filter((f) -> {
            try {
                return f.checkDefaults(machineName);
            } catch (MSLAException e) {
                throw new RuntimeException(e);
            }
        }).findFirst();
    }

    /**
     * Creates a file object that can be processed by a specified machine.
     *
     * @param machineName machine name (can be obtained with getSupportedMachines)
     * @return MSLAFile
     */
    public MSLAFile<?> create(String machineName) throws MSLAException {
        var defaults = MachineDefaults.getInstance().getMachineDefaults(machineName)
                .orElseThrow(() -> new MSLAException("Printer has no defaults: " + machineName));
        var machineFactory = getMachineFactory(machineName).orElseThrow(
                () -> new MSLAException("Machine '" + machineName + "' is not supported"));
        logger.info("Creating file for " + machineName + " using " + machineFactory.getClass().getName());
        var file = machineFactory.create(defaults.getFileProps());
        file.reset(defaults);
        return file;
    }

    /**
     * Loads a mSLA data.
     *
     * @param stream data to load
     * @return MSLAFile
     * @throws MSLAException on mSLA format related errors
     */
    public MSLAFile<?> load(DataInputStream stream) throws MSLAException {
        if (!stream.markSupported()) throw new MSLAException("Can't use " + stream.getClass() + ". Mark/reset is not supported");
        var factory = supportedFiles.stream().filter((t) -> {
            try {
                if (t.checkType(stream)) {
                    logger.info("Found file of type " + t.getClass());
                    return true;
                }
                return false;
            } catch (MSLAException e) { return false; }
        }).findFirst().orElse(null);
        if (factory != null) return factory.load(stream);
        else throw new MSLAException("File is not supported");
    }

    /**
     * Loads a mSLA data file.
     *
     * @param fileName file name to load
     * @return MSLAFile
     * @throws MSLAException on mSLA format related errors
     */
    public MSLAFile<?> load(String fileName) throws MSLAException {
        try {
            return load(new DataInputStream(new MarkedFileInputStream(fileName)));
        } catch (IOException e) {
            throw new MSLAException("Can't load file " + fileName, e);
        }
    }

    /**
     * Returns a list of supported machines (producer/model)
     *
     * @return list of string values
     */
    public ArrayList<String> getSupportedMachines() {
        var a = new ArrayList<String>();
        supportedFiles.forEach((f) -> {
            try {
                if (f.getSupportedMachines() != null) a.addAll(f.getSupportedMachines());
            } catch (MSLAException e) {
                throw new RuntimeException(e);
            }
        });
        return a;
    }
}
