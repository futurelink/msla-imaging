package futurelink.msla.formats.creality;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.iface.MSLAFile;
import futurelink.msla.formats.iface.MSLAFileDefaults;
import futurelink.msla.formats.iface.MSLAFileFactory;
import futurelink.msla.formats.utils.defaults.PrinterDefaults;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Set;

public class CXDLPFileFactory implements MSLAFileFactory {
    @Override public String getName() { return "ChituBox"; }

    @Override public MSLAFile<?> create(String machineName) throws MSLAException {
        var def = defaults(machineName);
        if (def == null) throw new MSLAException("No defaults found for " + machineName);
        return new CXDLPFile(def);
    }

    @Override public MSLAFile<?> load(String machineName, String fileName) throws MSLAException {
        try {
            return new CXDLPFile(defaults(machineName), new DataInputStream(new FileInputStream(fileName)));
        } catch (IOException e) {
            throw new MSLAException("Could not open file " + fileName, e);
        }
    }

    @Override public MSLAFile<?> load(String machineName, DataInputStream stream) throws MSLAException {
        return new CXDLPFile(defaults(machineName), stream);
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

    @Override public MSLAFileDefaults defaults(String machineName) {
        if (machineName == null) return null;
        return PrinterDefaults.instance.getPrinter(machineName);
    }

    @Override public boolean checkDefaults(String machineName) {
        return getSupportedMachines().contains(machineName);
    }

    @Override public Set<String> getSupportedMachines() {
        return PrinterDefaults.instance.getSupportedPrinters(CXDLPFile.class);
    }
}
