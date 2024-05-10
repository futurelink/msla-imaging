package futurelink.msla.formats.chitubox.tables;

import futurelink.msla.formats.iface.MSLAFileBlockFields;
import futurelink.msla.formats.iface.annotations.MSLAFileField;
import futurelink.msla.formats.utils.FileFieldsException;
import futurelink.msla.formats.utils.FileFieldsIO;
import lombok.Getter;

@Getter
public class CTBFileResinParams extends CTBFileBlock {
    private final Fields fileFields = new Fields();

    public CTBFileResinParams(int version) {
        super(version);
    }

    @Getter
    @SuppressWarnings("unused")
    public static class Fields implements MSLAFileBlockFields {
        @MSLAFileField private Integer Padding1;
        @MSLAFileField(order = 1) private Byte ResinColorB;
        @MSLAFileField(order = 2) private Byte ResinColorG;
        @MSLAFileField(order = 3) private Byte ResinColorR;
        @MSLAFileField(order = 4) private Byte ResinColorA;
        @MSLAFileField(order = 5) private Integer MachineNameOffset;
        @MSLAFileField(order = 6) private Integer ResinTypeLength;
        @MSLAFileField(order = 7) private Integer ResinTypeAddress;
        @MSLAFileField(order = 8) private Integer ResinNameLength;
        @MSLAFileField(order = 9) private Integer ResinNameAddress;
        @MSLAFileField(order = 10) private Integer MachineNameLength;
        @MSLAFileField(order = 11) private final Float ResinDensity = 1.1f;
        @MSLAFileField(order = 12) private Integer Padding2;
        @MSLAFileField(order = 13, lengthAt = "ResinTypeLength") private final String ResinType = "";
        @MSLAFileField(order = 14, lengthAt = "ResinNameLength") private final String ResinName = "";
        @MSLAFileField(order = 15, lengthAt = "MachineNameLength") private final String MachineName = "";
    }

    @Override public FileFieldsIO.Endianness getEndianness() { return FileFieldsIO.Endianness.LittleEndian; }
    @Override public int getDataLength() throws FileFieldsException { return FileFieldsIO.getBlockLength(this); }
    @Override
    public int getDataFieldOffset(String fieldName) throws FileFieldsException {
        return FileFieldsIO.getBlockLength(this.getFileFields(), fieldName);
    }
}
