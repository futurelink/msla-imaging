package futurelink.msla.formats.anycubic;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.iface.MSLAFile;
import futurelink.msla.formats.iface.MSLAFileFactory;
import futurelink.msla.formats.iface.MSLAFileProps;
import futurelink.msla.utils.defaults.MachineDefaults;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Set;

public class PhotonWorkshopFileFactory implements MSLAFileFactory {
    @Override public String getName() { return "Anycubic"; }

    @Override public MSLAFile<?> create(MSLAFileProps initialProps) throws MSLAException {
        return new PhotonWorkshopFile(initialProps);
    }

    @Override public MSLAFile<?> load(DataInputStream stream) throws MSLAException {
        try {
            return new PhotonWorkshopFile(stream);
        } catch (IOException e) {
            throw new MSLAException("Can't load data ", e);
        }
    }

    @Override public boolean checkType(DataInputStream stream) throws MSLAException {
        try {
            stream.reset();
            var bytes = stream.readNBytes(12);
            return new String(bytes).trim().equals("ANYCUBIC");
        } catch (IOException e) {
            throw new MSLAException("Can't read stream", e);
        }
    }

    @Override public boolean checkDefaults(String machineName) throws MSLAException {
        return getSupportedMachines().contains(machineName);
    }

    @Override
    public Set<String> getSupportedMachines() throws MSLAException {
        return MachineDefaults.getInstance().getMachines(PhotonWorkshopFile.class);
    }
}
