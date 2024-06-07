package futurelink.msla.formats.chitubox.encrypted;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.chitubox.CTBFile;
import futurelink.msla.formats.chitubox.encrypted.tables.CTBEncryptedFileHeader;
import futurelink.msla.formats.chitubox.tables.CTBFileHeader;
import futurelink.msla.formats.iface.MSLAFile;
import futurelink.msla.formats.iface.MSLAFileFactory;
import futurelink.msla.formats.iface.MSLAFileProps;
import futurelink.msla.utils.defaults.MachineDefaults;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Set;

public class CTBEncryptedFileFactory implements MSLAFileFactory {
    @Override
    public String getName() { return "Chitubox Encrypted"; }

    @Override
    public MSLAFile<?> create(MSLAFileProps initialProps) throws MSLAException {
        return new CTBEncryptedFile(initialProps.getInt("Version"));
    }

    @Override public MSLAFile<?> load(String fileName) throws MSLAException {
        try {
            return new CTBEncryptedFile(new DataInputStream(new FileInputStream(fileName)));
        } catch (IOException e) {
            throw new MSLAException("Can't load a file " + fileName, e);
        }
    }

    @Override public MSLAFile<?> load(DataInputStream stream) throws MSLAException {
        try {
            return new CTBEncryptedFile(stream);
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

    @Override public Set<String> getSupportedMachines() { return MachineDefaults.instance.getMachines(CTBEncryptedFile.class); }

    public final String getVersionByMagic(byte[] magic) {
        long magicNumber = magic[0] | ((magic[1] & 0xff) << 8) | ((magic[2] & 0xff) << 16) | ((long) (magic[3] & 0xff) << 24);
        if (magicNumber == CTBEncryptedFileHeader.MAGIC_CTB_ENCRYPTED) return "CTB_ENCRYPTED";
        return null;
    }

    public final boolean checkMagic(byte[] magic) { return getVersionByMagic(magic) != null; }
}