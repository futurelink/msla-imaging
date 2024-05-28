package futurelink.msla.formats.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class MarkedFileInputStream extends FileInputStream {
    private int mark = -1;
    public MarkedFileInputStream(String name) throws FileNotFoundException {
        super(name);
    }

    public MarkedFileInputStream(File file) throws FileNotFoundException {
        super(file);
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public synchronized void mark(int position) {
        mark = position;
    }

    @Override
    public synchronized void reset() throws IOException {
        var fc = getChannel();
        if (mark != -1) fc.position(mark); else fc.position(0);
    }
}
