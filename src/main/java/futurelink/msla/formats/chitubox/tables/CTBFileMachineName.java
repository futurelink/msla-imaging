package futurelink.msla.formats.chitubox.tables;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.iface.MSLAFileBlock;
import futurelink.msla.formats.iface.MSLAFileBlockFields;
import futurelink.msla.formats.iface.MSLAFileDefaults;
import futurelink.msla.formats.iface.annotations.MSLAFileField;
import futurelink.msla.formats.utils.FileFieldsException;
import futurelink.msla.formats.utils.FileFieldsIO;
import lombok.Getter;
import lombok.Setter;

@Getter
public class CTBFileMachineName implements MSLAFileBlock {
    private final String OPTIONS_SECTION_NAME = "MachineName";
    private final Fields fileFields = new Fields();

    @Override
    public int getDataLength() throws FileFieldsException {
        return FileFieldsIO.getBlockLength(this);
    }

    @Override
    public int getDataFieldOffset(String fieldName) throws FileFieldsException {
        return FileFieldsIO.getBlockLength(getFileFields(), fieldName);
    }

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

    public CTBFileMachineName(MSLAFileDefaults defaults) throws MSLAException {
        this();
        defaults.setFields(OPTIONS_SECTION_NAME, fileFields);
    }

    @Override
    public String toString() {
        return fileFields.fieldsAsString(" = ", "\n");
    }
}
