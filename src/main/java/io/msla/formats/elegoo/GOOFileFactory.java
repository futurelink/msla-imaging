package io.msla.formats.elegoo;

import io.msla.formats.MSLAException;
import io.msla.formats.iface.MSLAFile;
import io.msla.formats.iface.MSLAFileFactory;
import io.msla.formats.iface.MSLAFileProps;
import io.msla.utils.defaults.MachineDefaults;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Set;

/**
 * ELEGOO GOO file factory.
 */
public class GOOFileFactory implements MSLAFileFactory {
    @Override
    public String getName() { return "ELEGOO"; }

    @Override
    public MSLAFile<?> create(MSLAFileProps initialProps) throws MSLAException {
        return new GOOFile(initialProps);
    }

    @Override public MSLAFile<?> load(DataInputStream stream) throws MSLAException {
        try {
            return new GOOFile(stream);
        } catch (IOException e) {
            throw new MSLAException("Can't load data", e);
        }
    }

    @Override public boolean checkType(DataInputStream stream) throws MSLAException {
        try {
            stream.reset();
            return new String(stream.readNBytes(4)).trim().equals("V3.0");
        } catch (IOException e) {
            throw new MSLAException("Can't read stream", e);
        }
    }

    @Override public boolean checkDefaults(String machineName) throws MSLAException {
        return getSupportedMachines().contains(machineName);
    }

    @Override public Set<String> getSupportedMachines() throws MSLAException {
        return MachineDefaults.getInstance().getMachines(GOOFile.class);
    }
}
