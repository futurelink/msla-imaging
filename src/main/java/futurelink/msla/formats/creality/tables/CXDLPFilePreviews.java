package futurelink.msla.formats.creality.tables;

import futurelink.msla.formats.iface.MSLAFileBlockFields;
import futurelink.msla.formats.iface.MSLAFileField;
import futurelink.msla.formats.iface.MSLAPreview;
import futurelink.msla.formats.utils.Size;
import lombok.Getter;

@Getter
public class CXDLPFilePreviews extends CXDLPFileTable {
    private final Fields fields = new Fields();

    @SuppressWarnings("unused")
    static class Fields implements MSLAFileBlockFields {
        @MSLAFileField() CXDLPFilePreview Preview1 = new CXDLPFilePreview(new Size(116, 116));
        @MSLAFileField(order = 1) CXDLPFilePreview Preview2 = new CXDLPFilePreview(new Size(290, 290));
        @MSLAFileField(order = 2) CXDLPFilePreview Preview3 = new CXDLPFilePreview(new Size(290, 290));
    }

    public final MSLAPreview getPreview(int index) {
        return switch (index) {
            case 0 -> fields.Preview1;
            case 1 -> fields.Preview2;
            case 2 -> fields.Preview3;
            default -> null;
        };
    }

    @Override
    public int getDataLength() {
        return (fields.Preview1.getResolution().length() +
                fields.Preview2.getResolution().length() +
                fields.Preview3.getResolution().length()) * 2 + 6;
    }

    @Override
    public String toString() {
        return "CXDLPFilePreviews { " + fields.Preview1 + ", " + fields.Preview2 + ", " + fields.Preview3 + " }";
    }
}
