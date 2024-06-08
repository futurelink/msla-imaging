package futurelink.msla.formats.chitubox.encrypted.tables;

import futurelink.msla.formats.iface.MSLAFileBlock;
import futurelink.msla.formats.iface.MSLAFileBlockFields;
import futurelink.msla.formats.iface.annotations.MSLAFileField;
import futurelink.msla.formats.io.FileFieldsException;
import lombok.Getter;
import lombok.Setter;

@Getter
public class CTBEncryptedFileResin implements MSLAFileBlock {
    private final Fields blockFields = new Fields();

    @SuppressWarnings("unused")
    public static class Fields implements MSLAFileBlockFields {
        @MSLAFileField private final Integer Padding1 = 0;
        @MSLAFileField(order = 1) @Getter private byte ResinColorB;
        @MSLAFileField(order = 2) @Getter private byte ResinColorG;
        @MSLAFileField(order = 3) @Getter private byte ResinColorR;
        @MSLAFileField(order = 4) @Getter private byte ResinColorA;
        @MSLAFileField(order = 5) @Getter private Integer MachineNameAddress = 0;
        @MSLAFileField(order = 6) @Setter private Integer ResinTypeLength = 0;
        @MSLAFileField(order = 7) @Setter private Integer ResinTypeAddress = 0;
        @MSLAFileField(order = 8) @Setter private Integer ResinNameLength = 0;
        @MSLAFileField(order = 9) @Setter private Integer ResinNameAddress = 0;
        @MSLAFileField(order = 10) @Getter private Integer MachineNameLength = "DefaultMachineName".length();
        @MSLAFileField(order = 11) @Getter private float ResinDensity = 1.1f;
        @MSLAFileField(order = 12) private final Integer Padding2 = 0;
        @MSLAFileField(order = 13, lengthAt="ResinTypeLength") private String ResinType = "";
        @MSLAFileField(order = 14, lengthAt="ResinNameLength") private String ResinName = "";
        @MSLAFileField(order = 15, lengthAt="MachineNameLength") private String MachineName = "DefaultMachineName";
    }

    @Override public String getName() { return "Resin"; }
    @Override public int getDataLength() throws FileFieldsException { return 0; }
    @Override public int getDataFieldOffset(String fieldName) throws FileFieldsException { return 0; }
}
