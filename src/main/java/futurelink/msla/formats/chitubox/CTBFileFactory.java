package futurelink.msla.formats.chitubox;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.chitubox.common.CTBCommonFile;
import futurelink.msla.formats.chitubox.common.tables.CTBFileHeader;
import futurelink.msla.formats.chitubox.encrypted.CTBEncryptedFile;
import futurelink.msla.formats.chitubox.encrypted.tables.CTBEncryptedFileHeader;
import futurelink.msla.formats.iface.MSLAFile;
import futurelink.msla.formats.iface.MSLAFileFactory;
import futurelink.msla.formats.iface.MSLAFileProps;
import futurelink.msla.utils.defaults.MachineDefaults;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CTBFileFactory implements MSLAFileFactory {
    @Override
    public String getName() { return "Chitubox"; }

    @Override
    public MSLAFile<?> create(MSLAFileProps initialProps) throws MSLAException {
        var Version = initialProps.getByte("Version");
        if (initialProps.getBoolean("Encrypted")) return new CTBEncryptedFile((int) Version);
        return new CTBCommonFile(Version);
    }

    @Override public MSLAFile<?> load(DataInputStream stream) throws MSLAException {
        try {
            // Determine if a file is encrypted by magic 4 bytes
            stream.reset();
            var magic = getVersionByMagic(stream.readNBytes(4));
            if (magic == null) throw new MSLAException("Data is not in Chitubox format or malformed");
            else if ("CTBv4_ENCRYPTED".equals(magic)) return new CTBEncryptedFile(stream);
            else return new CTBCommonFile(stream);
        } catch (IOException e) {
            throw new MSLAException("Can't load data", e);
        }
    }

    @Override public boolean checkType(DataInputStream stream) throws MSLAException {
        try {
            stream.reset();
            return checkMagic(stream.readNBytes(4));
        } catch (IOException e) {
            throw new MSLAException("Can't read stream", e);
        }
    }

    @Override public boolean checkDefaults(String machineName) {
        return getSupportedMachines().contains(machineName);
    }

    @Override public Set<String> getSupportedMachines() {
        return Stream.of(
                MachineDefaults.instance.getMachines(CTBCommonFile.class),
                MachineDefaults.instance.getMachines(CTBEncryptedFile.class)
                ).flatMap(Set::stream).collect(Collectors.toSet());
    }

    public final String getVersionByMagic(byte[] magic) {
        long magicNumber = magic[0] | ((magic[1] & 0xff) << 8) | ((magic[2] & 0xff) << 16) | ((long) (magic[3] & 0xff) << 24);
        return switch ((int) magicNumber) {
            case CTBFileHeader.MAGIC_CBD_DLP -> "CBD_DLP";
            case CTBFileHeader.MAGIC_CTB -> "CTB";
            case CTBFileHeader.MAGIC_CTBv4 -> "CTBv4";
            case CTBFileHeader.MAGIC_CTBv4_GK_two -> "CTBv4_GK_two";
            case CTBEncryptedFileHeader.MAGIC_CTBv4_ENCRYPTED -> "CTBv4_ENCRYPTED";
            default -> null;
        };
    }

    public final boolean checkMagic(byte[] magic) { return getVersionByMagic(magic) != null; }
}
