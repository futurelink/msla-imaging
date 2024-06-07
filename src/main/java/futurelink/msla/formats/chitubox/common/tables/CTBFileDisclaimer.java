package futurelink.msla.formats.chitubox.common.tables;

import futurelink.msla.formats.iface.MSLAFileBlock;
import futurelink.msla.formats.iface.MSLAFileBlockFields;
import futurelink.msla.formats.iface.annotations.MSLAFileField;
import futurelink.msla.formats.io.FileFieldsException;
import futurelink.msla.formats.io.FileFieldsIO;
import lombok.Getter;

@Getter
public class CTBFileDisclaimer implements MSLAFileBlock {
    public static final String CTBv4_DISCLAIMER = "Layout and record format for the ctb and cbddlp file types are the " +
            "copyrighted programs or codes of CBD Technology (China) Inc..The Customer or User shall not in any manner " +
            "reproduce, distribute, modify, decompile, disassemble, decrypt, extract, reverse engineer, lease, assign, " +
            "or sublicense the said programs or codes.";

    private final Fields fileFields = new Fields();

    @Getter
    @SuppressWarnings("unused")
    public static class Fields implements MSLAFileBlockFields {
        @MSLAFileField(lengthAt = "DisclaimerSize") public String Disclaimer = CTBv4_DISCLAIMER;
        private int DisclaimerSize() { return CTBv4_DISCLAIMER.length(); }
    }

    @Override public String getName() { return null; }
    @Override public int getDataLength() throws FileFieldsException { return FileFieldsIO.getBlockLength(this); }
    @Override public int getDataFieldOffset(String fieldName) throws FileFieldsException {
        return FileFieldsIO.getBlockLength(getFileFields(), fieldName);
    }
    @Override public String toString() {
        return fileFields.fieldsAsString(" = ", "\n");
    }
}
