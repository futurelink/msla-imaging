package futurelink.msla.formats.anycubic;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.iface.MSLAFile;
import futurelink.msla.formats.iface.MSLAFileDefaults;
import futurelink.msla.formats.iface.MSLAFileFactory;
import futurelink.msla.formats.utils.defaults.PrinterDefaults;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Set;

public class PhotonWorkshopFileFactory implements MSLAFileFactory {
    @Override public String getName() { return "Anycubic"; }

    @Override public MSLAFile<?> create(String machineName) throws MSLAException {
        var def = defaults(machineName);
        if (def == null) throw new MSLAException("No defaults found for " + machineName);
        return new PhotonWorkshopFile(def);
    }

    @Override public MSLAFile<?> load(String machineName, String fileName) throws MSLAException {
        try {
            return new PhotonWorkshopFile(defaults(machineName), new DataInputStream(new FileInputStream(fileName)));
        } catch (IOException e) {
            throw new MSLAException("Can't load a file " + fileName, e);
        }
    }

    @Override public MSLAFile<?> load(String machineName, DataInputStream stream) throws MSLAException {
        try {
            return new PhotonWorkshopFile(defaults(machineName), stream);
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

    @Override public MSLAFileDefaults defaults(String machineName) {
        if (machineName == null) return null;
        return PrinterDefaults.instance.getPrinter(machineName);
    }

    @Override public boolean checkDefaults(String machineName) {
        return getSupportedMachines().contains(machineName);
    }

    @Override
    public Set<String> getSupportedMachines() {
        return PrinterDefaults.instance.getSupportedPrinters(PhotonWorkshopFile.class);
    }
}
