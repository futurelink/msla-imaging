package futurelink.msla.formats.elegoo;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.iface.MSLAFile;
import futurelink.msla.formats.iface.MSLAFileDefaults;
import futurelink.msla.formats.iface.MSLAFileFactory;
import futurelink.msla.formats.utils.PrinterDefaults;

import java.io.FileInputStream;
import java.util.Set;

public class GOOFileFactory implements MSLAFileFactory {
    @Override
    public String getName() { return "ELEGOO"; }

    @Override
    public MSLAFile<?> create(String machineName) throws MSLAException {
        return new GOOFile(defaults(machineName));
    }

    @Override public MSLAFile<?> load(String fileName) throws MSLAException { return null; }
    @Override public boolean checkType(FileInputStream stream) throws MSLAException { return false; }

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
