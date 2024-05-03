package futurelink.msla.formats.elegoo.tables;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.iface.MSLAFileBlock;
import futurelink.msla.formats.iface.MSLAFileBlockFields;
import futurelink.msla.formats.iface.MSLAFileField;
import futurelink.msla.formats.utils.FileFieldsReader;
import futurelink.msla.formats.utils.FileFieldsWriter;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

public class GOOFileFooter implements MSLAFileBlock {
    private final Fields fields = new Fields();
    static class Fields implements MSLAFileBlockFields {
        @MSLAFileField(order = 0) byte Padding1;
        @MSLAFileField(order = 1) byte Padding2;
        @MSLAFileField(order = 2) byte Padding3;
        @MSLAFileField(order = 3, length = 8) byte[] Magic = {0x07, 0x00, 0x00, 0x00, 0x44, 0x4C, 0x50, 0x00}; // 8 bytes;
    }

    @Override
    public int getDataLength() {
        return 0;
    }

    @Override
    public void read(FileInputStream stream, int position) throws MSLAException {
        try {
            var reader = new FileFieldsReader(stream, FileFieldsReader.Endianness.BigEndian);
            var dataRead = reader.read(fields);
        } catch (IOException e) { throw new MSLAException("Error reading " + this.getClass().getName() + " table", e); }
    }

    @Override
    public void write(OutputStream stream) throws MSLAException {
        try {
            var writer = new FileFieldsWriter(stream, FileFieldsWriter.Endianness.BigEndian);
            writer.write(fields);
        } catch (IOException e) {
            throw new MSLAException("Error writing " + this.getClass().getName() + " table", e);
        }
    }
}
