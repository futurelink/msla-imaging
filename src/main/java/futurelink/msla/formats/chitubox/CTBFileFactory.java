package futurelink.msla.formats.chitubox;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.chitubox.tables.CTBFileHeader;
import futurelink.msla.formats.iface.MSLAFile;
import futurelink.msla.formats.iface.MSLAFileDefaults;
import futurelink.msla.formats.iface.MSLAFileFactory;
import futurelink.msla.formats.utils.PrinterDefaults;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Set;

public class CTBFileFactory implements MSLAFileFactory {
    @Override
    public String getName() { return "Chitubox"; }

    @Override
    public MSLAFile<?> create(String machineName) throws MSLAException {
        return new CTBFile(defaults(machineName));
    }

    @Override public MSLAFile<?> load(String fileName) throws MSLAException {
        try {
            return new CTBFile(new FileInputStream(fileName));
        } catch (IOException e) {
            throw new MSLAException("Can't load a file " + fileName, e);
        }
    }

    @Override public boolean checkType(FileInputStream stream) throws MSLAException {
        try {
            var fc = stream.getChannel();
            fc.position(0);
            return checkMagic(new DataInputStream(stream).readNBytes(4));
        } catch (IOException e) {
            throw new MSLAException("Can't read stream", e);
        }
    }

    @Override public MSLAFileDefaults defaults(String machineName) {
        return PrinterDefaults.instance.getPrinter(machineName);
    }

    @Override public boolean checkDefaults(String machineName) {
        return getSupportedMachines().contains(machineName);
    }

    @Override public Set<String> getSupportedMachines() {
        return PrinterDefaults.instance.getSupportedPrinters(CTBFile.class);
    }

    public final String getVersionByMagic(byte[] magic) {
        long magicNumber = magic[0] | ((magic[1] & 0xff) << 8) | ((magic[2] & 0xff) << 16) | ((long) (magic[3] & 0xff) << 24);
        return switch ((int) magicNumber) {
            case CTBFileHeader.MAGIC_CBD_DLP -> "CBD_DLP";
            case CTBFileHeader.MAGIC_CTB -> "CTB";
            case CTBFileHeader.MAGIC_CTBv4 -> "CTBv4";
            case CTBFileHeader.MAGIC_CTBv4_GK_two -> "CTBv4_GK_two";
            default -> null;
        };
    }

    public final boolean checkMagic(byte[] magic) { return getVersionByMagic(magic) != null; }
}
