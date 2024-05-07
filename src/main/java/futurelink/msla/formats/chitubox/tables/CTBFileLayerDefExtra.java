package futurelink.msla.formats.chitubox.tables;

import futurelink.msla.formats.iface.MSLAFileBlock;
import futurelink.msla.formats.iface.MSLAFileBlockFields;
import futurelink.msla.formats.iface.annotations.MSLAFileField;
import futurelink.msla.formats.utils.FileFieldsIO;
import lombok.Getter;

@Getter
public class CTBFileLayerDefExtra implements MSLAFileBlock {
    private final Fields fields = new Fields();
    public static final int TABLE_SIZE = 48;

    @Getter
    @SuppressWarnings("unused")
    public static class Fields implements MSLAFileBlockFields {
        @MSLAFileField private Integer TotalSize;
        @MSLAFileField(order = 1) private Float LiftHeight;
        @MSLAFileField(order = 2) private Float LiftSpeed;
        @MSLAFileField(order = 3) private Float LiftHeight2;
        @MSLAFileField(order = 4) private Float LiftSpeed2;
        @MSLAFileField(order = 5) private Float RetractSpeed;
        @MSLAFileField(order = 6) private Float RetractHeight2;
        @MSLAFileField(order = 7) private Float RetractSpeed2;
        @MSLAFileField(order = 8) private Float RestTimeBeforeLift;
        @MSLAFileField(order = 9) private Float RestTimeAfterLift;
        @MSLAFileField(order = 10) private Float RestTimeAfterRetract;
        @MSLAFileField(order = 11) private Float LightPWM;
    }

    @Override public FileFieldsIO.Endianness getEndianness() { return FileFieldsIO.Endianness.LittleEndian; }
    @Override public int getDataLength() { return TABLE_SIZE; }
    @Override public String toString() { return "{ " + fields.fieldsAsString(":", ", ") +" }"; }
}
