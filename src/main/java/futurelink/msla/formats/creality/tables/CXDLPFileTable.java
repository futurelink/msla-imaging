package futurelink.msla.formats.creality.tables;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.iface.MSLAFileBlock;
import futurelink.msla.formats.iface.MSLAFileBlockFields;
import futurelink.msla.formats.iface.MSLAOption;
import futurelink.msla.formats.iface.MSLAOptionContainer;
import futurelink.msla.formats.utils.FileFieldsReader;
import futurelink.msla.formats.utils.FileFieldsWriter;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;

public abstract class CXDLPFileTable implements MSLAFileBlock {
    protected MSLAFileBlockFields fields;

    public HashMap<String, Class<?>> getOptions() {
        var a = getClass().getAnnotation(MSLAOptionContainer.class);
        if (a != null) {
            var optionsMap = new HashMap<String, Class<?>>();
            Arrays.stream(a.className().getDeclaredFields())
                    .filter((f) -> f.getAnnotation(MSLAOption.class) != null)
                    .forEach((f) -> optionsMap.put(f.getName(), f.getAnnotation(MSLAOption.class).type()));
            return optionsMap;
        }
        return null;
    }

    public abstract MSLAFileBlockFields getFields();

    @Override
    public void read(FileInputStream stream, int position) throws MSLAException {
        try {
            var reader = new FileFieldsReader(stream, FileFieldsReader.Endianness.BigEndian);
            var dataRead = reader.read(getFields());
        } catch (IOException e) { throw new MSLAException("Error reading " + this.getClass().getName() + " table", e); }
    }

    @Override
    public void write(OutputStream stream) throws MSLAException {
        try {
            var writer = new FileFieldsWriter(stream, FileFieldsWriter.Endianness.BigEndian);
            writer.write(getFields());
        } catch (IOException e) {
            throw new MSLAException("Error writing " + this.getClass().getName() + " table", e);
        }
    }

}
