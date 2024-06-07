package futurelink.msla.formats.creality;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.iface.MSLAFile;
import futurelink.msla.formats.iface.MSLAFileFactory;
import futurelink.msla.formats.iface.MSLAFileProps;
import futurelink.msla.utils.defaults.MachineDefaults;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Set;

public class CXDLPFileFactory implements MSLAFileFactory {
    @Override public String getName() { return "ChituBox"; }

    @Override public MSLAFile<?> create(MSLAFileProps initialProps) throws MSLAException {
        return new CXDLPFile();
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

    @Override public boolean checkDefaults(String machineName) {
        return getSupportedMachines().contains(machineName);
    }

    @Override public Set<String> getSupportedMachines() {
        return MachineDefaults.instance.getMachines(CXDLPFile.class);
    }
}
