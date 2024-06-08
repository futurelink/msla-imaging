package futurelink.msla.formats.chitubox.encrypted.tables;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.chitubox.common.tables.CTBFileBlock;
import futurelink.msla.formats.iface.MSLAFileBlockFields;
import futurelink.msla.formats.iface.annotations.MSLAFileField;
import futurelink.msla.formats.io.FileFieldsException;
import lombok.Getter;

import java.util.Objects;

@Getter
public class CTBEncryptedFileHeader extends CTBFileBlock {
    private final Fields blockFields;
    public static final int MAGIC_CTBv4_ENCRYPTED = 0x12FD0107; // 318570759


    @SuppressWarnings("unused")
    public static class Fields implements MSLAFileBlockFields {
        @MSLAFileField private Integer Magic;
        @MSLAFileField(order = 1) @Getter private Integer SettingsSize;
        @MSLAFileField(order = 2) @Getter private final Integer SettingsOffset = 48;
        @MSLAFileField(order = 3) private final Integer Unknown1 = 0; // set to 0
        @MSLAFileField(order = 4) @Getter private Integer Version = 5;
        public void setVersion(Integer version) throws MSLAException {
            // When file is being read - Magic is set first, so version should match,
            // oppositely, when file is created then version defines Magic.
            if ((Magic != null) && !Objects.equals(Magic, MAGIC_CTBv4_ENCRYPTED))
                throw new MSLAException("Version is not valid for magic number");
            else {
                Version = version;
                Magic = MAGIC_CTBv4_ENCRYPTED;
            }
        }
        @MSLAFileField(order = 5) @Getter private Integer SignatureSize;
        @MSLAFileField(order = 6) @Getter private Integer SignatureOffset;
        @MSLAFileField(order = 7) private final Integer Unknown = 0;  //set to 0
        @MSLAFileField(order = 8) private final Short Unknown4 = 1; // set to 1
        @MSLAFileField(order = 9) private final Short Unknown5 = 1; // set to 1
        @MSLAFileField(order = 10) private final Integer Unknown6 = 0; // set to 0
        @MSLAFileField(order = 11) private final Integer Unknown7 = 0x2A; // probably 0x2A
        @MSLAFileField(order = 12) private final Integer Unknown8 = 0; // probably 0 or 1
    }

    public CTBEncryptedFileHeader(Integer version) { super(version); blockFields = new Fields(); }

    @Override public String getName() { return "Header"; }
    @Override public int getDataLength() throws FileFieldsException { return 0; }
    @Override public int getDataFieldOffset(String fieldName) throws FileFieldsException { return 0; }
}
