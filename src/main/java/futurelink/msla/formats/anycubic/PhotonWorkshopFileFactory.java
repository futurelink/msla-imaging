package futurelink.msla.formats.anycubic;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.iface.MSLAFile;
import futurelink.msla.formats.iface.MSLAFileFactory;
import futurelink.msla.formats.iface.MSLAFileProps;
import futurelink.msla.utils.defaults.MachineDefaults;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Set;

public class PhotonWorkshopFileFactory implements MSLAFileFactory {
    @Override public String getName() { return "Anycubic"; }

    @Override public MSLAFile<?> create(MSLAFileProps initialProps) throws MSLAException {
        var VersionMajor = initialProps.getByte("VersionMajor");
        var VersionMinor = initialProps.getByte("VersionMinor");
        return new PhotonWorkshopFile(VersionMajor, VersionMinor);
    }

    @Override public MSLAFile<?> load(String fileName) throws MSLAException {
        try {
            return new PhotonWorkshopFile(new DataInputStream(new FileInputStream(fileName)));
        } catch (IOException e) {
            throw new MSLAException("Can't load a file " + fileName, e);
        }
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

    @Override public boolean checkDefaults(String machineName) {
        return getSupportedMachines().contains(machineName);
    }

    @Override
    public Set<String> getSupportedMachines() {
        return MachineDefaults.instance.getMachines(PhotonWorkshopFile.class);
    }
}
