package futurelink.msla.formats.chitubox.tables;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.iface.MSLAFileBlock;
import futurelink.msla.formats.iface.MSLAFileBlockFields;
import futurelink.msla.formats.iface.MSLAFileDefaults;
import futurelink.msla.formats.iface.annotations.MSLAFileField;
import futurelink.msla.formats.utils.FileFieldsIO;
import lombok.Getter;

@Getter
public class CTBFilePrintParamsV4 implements MSLAFileBlock {
    private final String OPTIONS_SECTION_NAME = "PrintParamsV4";
    private static final int CTBv4_RESERVED_SIZE = 380;
    private static final Integer CTBv4_DISCLAIMER_SIZE = 320;
    private static final String CTBv4_DISCLAIMER = "Layout and record format for the ctb and cbddlp file types are the " +
            "copyrighted programs or codes of CBD Technology (China) Inc..The Customer or User shall not in any manner " +
            "reproduce, distribute, modify, decompile, disassemble, decrypt, extract, reverse engineer, lease, assign, " +
            "or sublicense the said programs or codes.";

    private final Fields fields;

    @Getter
    @SuppressWarnings("unused")
    public static class Fields implements MSLAFileBlockFields {
        @MSLAFileField private Float BottomRetractSpeed;
        @MSLAFileField(order = 1) private Float BottomRetractSpeed2;
        @MSLAFileField(order = 2) private final Integer Padding1 = 0;
        @MSLAFileField(order = 3) private final Float Four1 = 4.0F; // 4?
        @MSLAFileField(order = 4) private final Integer Padding2 = 0;
        @MSLAFileField(order = 5) private final Float Four2 = 4.0f; // ?
        @MSLAFileField(order = 6) private Float RestTimeAfterRetract;
        @MSLAFileField(order = 7) private Float RestTimeAfterLift;
        @MSLAFileField(order = 8) private Float RestTimeBeforeLift;
        @MSLAFileField(order = 9) private Float BottomRetractHeight2;
        @MSLAFileField(order = 10) private Float Unknown1; // 2955.996 or uint:1161347054 but changes
        @MSLAFileField(order = 11) private Integer Unknown2; // 73470 but changes
        @MSLAFileField(order = 12) private final Integer Unknown3 = 5; // 5?
        @MSLAFileField(order = 13) private Integer LastLayerIndex;
        @MSLAFileField(order = 14) private final Integer Padding3 = 0;
        @MSLAFileField(order = 15) private final Integer Padding4 = 0;
        @MSLAFileField(order = 16) private final Integer Padding5 = 0;
        @MSLAFileField(order = 17) private final Integer Padding6 = 0;
        @MSLAFileField(order = 18) private Integer DisclaimerOffset;
        @MSLAFileField(order = 18) private final Integer DisclaimerLength = CTBv4_DISCLAIMER_SIZE;
        @MSLAFileField(order = 19) private Integer ResinParametersOffset; // CTBv5
        @MSLAFileField(order = 22, length = CTBv4_RESERVED_SIZE) private final byte[] Reserved = new byte[CTBv4_RESERVED_SIZE]; // 384 bytes
    }

    public CTBFilePrintParamsV4() {
        fields = new Fields();
    }

    public CTBFilePrintParamsV4(MSLAFileDefaults defaults) throws MSLAException {
        this();
        defaults.setFields(OPTIONS_SECTION_NAME, fields);
    }

    @Override public FileFieldsIO.Endianness getEndianness() { return FileFieldsIO.Endianness.LittleEndian; }
    @Override public int getDataLength() { return 0; }
    @Override public String toString() { return fields.fieldsAsString(" = ", "\n"); }
}
