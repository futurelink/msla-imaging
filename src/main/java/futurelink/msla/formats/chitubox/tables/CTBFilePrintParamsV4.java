package futurelink.msla.formats.chitubox.tables;

import futurelink.msla.formats.iface.MSLAFileBlockFields;
import futurelink.msla.formats.iface.annotations.MSLAFileField;
import futurelink.msla.formats.io.FileFieldsException;
import futurelink.msla.formats.io.FileFieldsIO;
import lombok.Getter;
import lombok.Setter;

@Getter
public class CTBFilePrintParamsV4 extends CTBFileBlock {
    private final String OPTIONS_SECTION_NAME = "PrintParamsV4";
    private static final int CTBv4_RESERVED_SIZE = 380;

    private final Fields fileFields;

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
        @MSLAFileField(order = 10) private final Float Unknown1 = 2955.996f; // 2955.996 or uint:1161347054 but changes
        @MSLAFileField(order = 11) private final Integer Unknown2 = 73470; // 73470 but changes
        @MSLAFileField(order = 12) private final Integer Unknown3 = 5; // 5?
        @MSLAFileField(order = 13) @Setter private Integer LastLayerIndex = -1;
        @MSLAFileField(order = 14) private final Integer Padding3 = 0;
        @MSLAFileField(order = 15) private final Integer Padding4 = 0;
        @MSLAFileField(order = 16) private final Integer Padding5 = 0;
        @MSLAFileField(order = 17) private final Integer Padding6 = 0;
        @MSLAFileField(order = 18) @Setter private Integer DisclaimerOffset;
        @MSLAFileField(order = 18) @Setter private Integer DisclaimerLength;
        @MSLAFileField(order = 19) @Setter private Integer ResinParametersOffset; // CTBv5
        @MSLAFileField(order = 22, length = CTBv4_RESERVED_SIZE) private final byte[] Reserved = new byte[CTBv4_RESERVED_SIZE]; // 384 bytes
    }

    public CTBFilePrintParamsV4(int version) {
        super(version);
        fileFields = new Fields();
    }

    @Override public String getName() { return OPTIONS_SECTION_NAME; }
    @Override public int getDataLength() throws FileFieldsException { return FileFieldsIO.getBlockLength(this); }
    @Override
    public int getDataFieldOffset(String fieldName) throws FileFieldsException {
        return FileFieldsIO.getBlockLength(this.getFileFields(), fieldName);
    }
    @Override public String toString() { return fileFields.fieldsAsString(" = ", "\n"); }
}
