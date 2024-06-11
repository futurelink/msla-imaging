package futurelink.msla.formats.creality.tables;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.iface.MSLAFileBlockFields;
import futurelink.msla.formats.iface.MSLAFileField;
import futurelink.msla.formats.iface.MSLAPreview;
import futurelink.msla.utils.Size;
import lombok.Getter;

@Getter
public class CXDLPFilePreviews extends CXDLPFileTable {
    private final Fields blockFields = new Fields();

    @SuppressWarnings("unused")
    static class Fields implements MSLAFileBlockFields {
        @MSLAFileField() CXDLPFilePreview Preview1 = new CXDLPFilePreview(new Size(116, 116));
        @MSLAFileField(order = 1) CXDLPFilePreview Preview2 = new CXDLPFilePreview(new Size(290, 290));
        @MSLAFileField(order = 2) CXDLPFilePreview Preview3 = new CXDLPFilePreview(new Size(290, 290));
    }

    public final MSLAPreview getPreview(int index) throws MSLAException {
        return switch (index) {
            case 0 -> blockFields.Preview1;
            case 1 -> blockFields.Preview2;
            case 2 -> blockFields.Preview3;
            default -> throw new MSLAException("Preview is not available");
        };
    }

    @Override public String getName() { return null; }
    @Override public int getDataLength() {
        return (blockFields.Preview1.getResolution().length() +
                blockFields.Preview2.getResolution().length() +
                blockFields.Preview3.getResolution().length()) * 2 + 6;
    }
    @Override public String toString() { return blockFields.fieldsAsString(" = ", "\n"); }
}
