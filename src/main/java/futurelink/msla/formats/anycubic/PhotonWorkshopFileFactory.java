package futurelink.msla.formats.anycubic;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.iface.MSLAFile;
import futurelink.msla.formats.iface.MSLAFileDefaults;
import futurelink.msla.formats.iface.MSLAFileFactory;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Set;

public class PhotonWorkshopFileFactory implements MSLAFileFactory {
    @Override public String getName() { return "Anycubic"; }

    @Override public MSLAFile create(String machineName) throws IOException, MSLAException {
        return new PhotonWorkshopFile(defaults(machineName));
    }

    @Override public MSLAFile load(String fileName) throws IOException, MSLAException {
        return new PhotonWorkshopFile(new FileInputStream(fileName));
    }

    @Override public boolean checkType(FileInputStream stream) throws IOException, MSLAException {
        var fc = stream.getChannel();
        fc.position(0);
        var dis = new DataInputStream(stream);
        var mark = dis.readNBytes(12);
        return new String(mark).trim().equals("ANYCUBIC");
    }

    @Override public MSLAFileDefaults defaults(String machineName) {
        return PhotonWorkshopFileDefaults.get(machineName);
    }

    @Override public boolean checkDefaults(String machineName) {
        return (PhotonWorkshopFileDefaults.get(machineName) != null);
    }

    @Override
    public Set<String> getSupportedMachines() {
        return PhotonWorkshopFileDefaults.getSupported();
    }
}
