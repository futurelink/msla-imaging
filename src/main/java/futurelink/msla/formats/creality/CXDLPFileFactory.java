package futurelink.msla.formats.creality;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.iface.MSLAFile;
import futurelink.msla.formats.iface.MSLAFileDefaults;
import futurelink.msla.formats.iface.MSLAFileFactory;
import futurelink.msla.formats.utils.PrinterDefaults;

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

    @Override public MSLAFile<?> load(String fileName) throws MSLAException {
        try {
            return new CXDLPFile(new FileInputStream(fileName));
        } catch (IOException e) {
            throw new MSLAException("Could not open file " + fileName, e);
        }
    }

    @Override public boolean checkType(FileInputStream stream) throws MSLAException {
        try {
            var fc = stream.getChannel();
            fc.position(0);
            var dis = new DataInputStream(stream);
            var markLength = dis.readInt();
            if (markLength > 9) return false;
            return new String(dis.readNBytes(markLength)).trim().startsWith("CXSW3D");
        } catch (IOException e) {
            throw new MSLAException("Could not read stream", e);
        }
    }

    @Override public MSLAFileDefaults defaults(String machineName) {
        return PrinterDefaults.instance.getPrinter(machineName);
    }

    @Override public boolean checkDefaults(String machineName) {
        return PrinterDefaults.instance.getSupportedPrinters(CXDLPFile.class).contains(machineName);
    }

    @Override public Set<String> getSupportedMachines() {
        return PrinterDefaults.instance.getSupportedPrinters(CXDLPFile.class);
    }
}
