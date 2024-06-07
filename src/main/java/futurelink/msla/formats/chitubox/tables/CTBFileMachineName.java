package futurelink.msla.formats.chitubox.tables;

import futurelink.msla.formats.iface.MSLAFileBlock;
import futurelink.msla.formats.iface.MSLAFileBlockFields;
import futurelink.msla.formats.iface.annotations.MSLAFileField;
import futurelink.msla.formats.io.FileFieldsException;
import futurelink.msla.formats.io.FileFieldsIO;
import lombok.Getter;
import lombok.Setter;


@Getter
public class CTBFileMachineName implements MSLAFileBlock {
    private final Fields fileFields = new Fields();

    @Getter
    @SuppressWarnings("unused")
    public static class Fields implements MSLAFileBlockFields {
        @Setter private Integer MachineNameSize = 0;
        @MSLAFileField(lengthAt = "MachineNameSize") public String MachineName = "";

        public void setMachineName(String machineName) {
            MachineName = machineName;
            MachineNameSize = machineName.length();
        }
    }

    public CTBFileMachineName() {}

    @Override public String getName() { return "MachineName"; }
    @Override public int getDataLength() throws FileFieldsException { return FileFieldsIO.getBlockLength(this); }
    @Override public int getDataFieldOffset(String fieldName) throws FileFieldsException {
        return FileFieldsIO.getBlockLength(getFileFields(), fieldName);
    }
    @Override public String toString() {
        return fileFields.fieldsAsString(" = ", "\n");
    }
}
