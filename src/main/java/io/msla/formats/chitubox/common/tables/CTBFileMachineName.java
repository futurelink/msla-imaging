package io.msla.formats.chitubox.common.tables;

import io.msla.formats.iface.MSLAFileBlock;
import io.msla.formats.iface.MSLAFileBlockFields;
import io.msla.formats.iface.MSLAFileField;
import io.msla.formats.io.FileFieldsException;
import io.msla.formats.io.FileFieldsIO;
import lombok.Getter;
import lombok.Setter;


@Getter
public class CTBFileMachineName implements MSLAFileBlock {
    private final Fields blockFields = new Fields();

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
    @Override public int getDataLength() throws FileFieldsException { return FileFieldsIO.getBlockLength(getBlockFields()); }
    @Override public int getDataFieldOffset(String fieldName) throws FileFieldsException {
        return FileFieldsIO.getBlockLength(getBlockFields(), fieldName);
    }
    @Override public String toString() {
        return blockFields.fieldsAsString(" = ", "\n");
    }
}