package io.msla.formats.creality;

import io.msla.formats.MSLAException;
import io.msla.formats.iface.MSLAFile;
import io.msla.formats.iface.MSLAFileFactory;
import io.msla.formats.iface.MSLAFileProps;
import io.msla.utils.defaults.MachineDefaults;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Set;

public class CXDLPFileFactory implements MSLAFileFactory {
    @Override public String getName() { return "ChituBox"; }

    @Override public MSLAFile<?> create(MSLAFileProps initialProps) throws MSLAException {
        return new CXDLPFile(initialProps);
    }

    @Override public MSLAFile<?> load(DataInputStream stream) throws MSLAException {
        return new CXDLPFile(stream);
    }

    @Override public boolean checkType(DataInputStream stream) throws MSLAException {
        try {
            stream.reset();
            var markLength = stream.readInt();
            if (markLength > 9) return false;
            return new String(stream.readNBytes(markLength)).trim().startsWith("CXSW3D");
        } catch (IOException e) {
            throw new MSLAException("Could not read stream", e);
        }
    }

    @Override public boolean checkDefaults(String machineName) throws MSLAException {
        return getSupportedMachines().contains(machineName);
    }

    @Override public Set<String> getSupportedMachines() throws MSLAException {
        return MachineDefaults.getInstance().getMachines(CXDLPFile.class);
    }
}