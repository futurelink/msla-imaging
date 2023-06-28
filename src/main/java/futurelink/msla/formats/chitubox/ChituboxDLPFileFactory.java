package futurelink.msla.formats.chitubox;

import futurelink.msla.formats.MSLAFile;
import futurelink.msla.formats.MSLAFileDefaults;
import futurelink.msla.formats.MSLAFileFactory;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Set;

public class ChituboxDLPFileFactory implements MSLAFileFactory {
    @Override public String getName() { return "ChituBox"; }

    @Override public MSLAFile create(String machineName) throws IOException { return null; }

    @Override public MSLAFile load(String fileName) throws IOException {
        return new ChituBoxDLPFile(new FileInputStream(fileName));
    }

    @Override public boolean checkType(FileInputStream stream) throws IOException {
        var fc = stream.getChannel();
        fc.position(0);
        var dis = new DataInputStream(stream);
        var markLength = dis.readInt();
        if (markLength > 9) return false;
        var mark = dis.readNBytes(markLength);
        return new String(mark).trim().startsWith("CXSW3D");
    }

    @Override public MSLAFileDefaults defaults(String machineName) {
        return null;
    }

    @Override public boolean checkDefaults(String machineName) {
        return false;
    }

    @Override
    public Set<String> getSupportedMachines() {
        return null;
    }
}
