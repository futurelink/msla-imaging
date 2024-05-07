package futurelink.msla.formats.chitubox.tables;

import futurelink.msla.formats.iface.MSLAFileBlock;
import futurelink.msla.formats.iface.MSLAFileBlockFields;
import futurelink.msla.formats.iface.annotations.MSLAFileField;
import futurelink.msla.formats.utils.FileFieldsIO;
import lombok.Getter;

@Getter
public class CTBFileLayerDef implements MSLAFileBlock {
    public static final int TABLE_SIZE = 36;
    private final Fields fields = new Fields();

    @Getter
    @SuppressWarnings("unused")
    public static class Fields implements MSLAFileBlockFields {
        @MSLAFileField private Float PositionZ;
        @MSLAFileField(order = 1) private Float ExposureTime;
        @MSLAFileField(order = 2) private Float LightOffSeconds;
        @MSLAFileField(order = 3) private Integer DataAddress;
        @MSLAFileField(order = 4) private Integer DataSize;
        @MSLAFileField(order = 5) private Integer PageNumber;
        @MSLAFileField(order = 6) private final Integer TableSize = TABLE_SIZE;
        @MSLAFileField(order = 7) private Integer Unknown3;
        @MSLAFileField(order = 8) private Integer Unknown4;
    }

    @Override public FileFieldsIO.Endianness getEndianness() { return FileFieldsIO.Endianness.LittleEndian; }
    @Override public int getDataLength() { return TABLE_SIZE; }
    @Override public String toString() { return "{ " + fields.fieldsAsString(":", ", ") + " }"; }
}
