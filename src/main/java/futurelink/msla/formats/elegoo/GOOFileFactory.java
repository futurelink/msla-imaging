package futurelink.msla.formats.elegoo;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.iface.MSLAFile;
import futurelink.msla.formats.iface.MSLAFileDefaults;
import futurelink.msla.formats.iface.MSLAFileFactory;
import futurelink.msla.formats.utils.PrinterDefaults;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Set;

public class GOOFileFactory implements MSLAFileFactory {
    @Override
    public String getName() { return "ELEGOO"; }

    @Override
    public MSLAFile<?> create(String machineName) throws MSLAException {
        return new GOOFile(defaults(machineName));
    }

    @Override public MSLAFile<?> load(String fileName) throws MSLAException {
        try {
            return new GOOFile(new FileInputStream(fileName));
        } catch (IOException e) {
            throw new MSLAException("Can't load a file " + fileName, e);
        }
    }
    @Override public boolean checkType(FileInputStream stream) throws MSLAException {
        try {
            var fc = stream.getChannel();
            fc.position(0);
            return new String(new DataInputStream(stream).readNBytes(4)).trim().equals("V3.0");
        } catch (IOException e) {
            throw new MSLAException("Can't read stream", e);
        }
    }

    @Override public MSLAFileDefaults defaults(String machineName) {
        return PrinterDefaults.instance.getPrinter(machineName);
    }

    @Override public boolean checkDefaults(String machineName) {
        return (PrinterDefaults.instance.getPrinter(machineName) != null);
    }

    @Override public Set<String> getSupportedMachines() {
        return PrinterDefaults.instance.getSupportedPrinters(GOOFile.class);
    }
}
