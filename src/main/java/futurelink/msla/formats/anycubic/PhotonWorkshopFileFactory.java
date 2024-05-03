package futurelink.msla.formats.anycubic;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.iface.MSLAFile;
import futurelink.msla.formats.iface.MSLAFileDefaults;
import futurelink.msla.formats.iface.MSLAFileFactory;
import futurelink.msla.formats.utils.PrinterDefaults;

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

    @Override public MSLAFile<?> load(String fileName) throws MSLAException {
        try {
            return new PhotonWorkshopFile(new FileInputStream(fileName));
        } catch (IOException e) {
            throw new MSLAException("Can't load a file " + fileName, e);
        }
    }

    @Override public boolean checkType(FileInputStream stream) throws MSLAException {
        try {
            var fc = stream.getChannel();
            fc.position(0);
            return new String(new DataInputStream(stream).readNBytes(12)).trim().equals("ANYCUBIC");
        } catch (IOException e) {
            throw new MSLAException("Can't read stream", e);
        }
    }

    @Override public MSLAFileDefaults defaults(String machineName) {
        return PrinterDefaults.instance.getPrinter(machineName);
    }

    @Override public boolean checkDefaults(String machineName) {
        return PrinterDefaults.instance.getSupportedPrinters(PhotonWorkshopFile.class).contains(machineName);
    }

    @Override
    public Set<String> getSupportedMachines() {
        return PrinterDefaults.instance.getSupportedPrinters(PhotonWorkshopFile.class);
    }
}
