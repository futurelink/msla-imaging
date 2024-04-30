package futurelink.msla.formats.creality;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.iface.MSLAFile;
import futurelink.msla.formats.iface.MSLAFileDefaults;
import futurelink.msla.formats.iface.MSLAFileFactory;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Set;

public class CXDLPFileFactory implements MSLAFileFactory {
    @Override public String getName() { return "ChituBox"; }

    @Override public MSLAFile create(String machineName) throws IOException {
        return new CXDLPFile(CXDLPFileDefaults.get(machineName));
    }

    @Override public MSLAFile load(String fileName) throws IOException {
        return new CXDLPFile(new FileInputStream(fileName));
    }

    @Override public boolean checkType(FileInputStream stream) throws MSLAException, IOException {
        var fc = stream.getChannel();
        fc.position(0);
        var dis = new DataInputStream(stream);
        var markLength = dis.readInt();
        if (markLength > 9) return false;
        var mark = dis.readNBytes(markLength);
        return new String(mark).trim().startsWith("CXSW3D");
    }

    @Override public MSLAFileDefaults defaults(String machineName) { return CXDLPFileDefaults.get(machineName); }

    @Override public boolean checkDefaults(String machineName) {
        return (CXDLPFileDefaults.get(machineName) != null);
    }
    @Override public Set<String> getSupportedMachines() { return CXDLPFileDefaults.getSupported(); }
}
