package futurelink.msla.formats.creality.tables;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.iface.MSLAFileBlockFields;
import futurelink.msla.formats.iface.MSLAFileDefaults;
import futurelink.msla.formats.iface.annotations.MSLAFileField;
import futurelink.msla.utils.Size;
import lombok.Getter;
import lombok.experimental.Delegate;

@Getter
public class CXDLPFileHeader extends CXDLPFileTable {
    @Delegate private Fields fileFields;

    @SuppressWarnings("unused")
    public static class Fields implements MSLAFileBlockFields {
        public static final String HEADER_VALUE = "CXSW3DV2";
        public static final String HEADER_VALUE_GENERIC = "CXSW3D";
        public static final Short DEFAULT_VERSION = 3;

        @Getter private Size Resolution = new Size(0, 0);
        @Getter private Float PixelSizeUm;

        @MSLAFileField @Getter private Integer HeaderSize = 9;
        @MSLAFileField(order = 1, lengthAt = "HeaderSize") @Getter private String HeaderValue = HEADER_VALUE;
        @MSLAFileField(order = 2) @Getter private Short Version = DEFAULT_VERSION;
        @MSLAFileField(order = 3) @Getter private Integer PrinterModelSize = 6;
        @MSLAFileField(order = 4, lengthAt = "PrinterModelSize") @Getter private String PrinterModel = "";
        @MSLAFileField(order = 5) @Getter private Short LayerCount = 0;
        @MSLAFileField(order = 6) private Short ResolutionX() { return (short) Resolution.getWidth(); }
        private void setResolutionX(Short width) { Resolution = new Size(width, Resolution.getHeight()); }
        @MSLAFileField(order = 7) private Short ResolutionY() { return (short) Resolution.getHeight(); }
        private void setResolutionY(Short height) { Resolution = new Size(Resolution.getWidth(), height); }
        @MSLAFileField(order = 8, length = 64) byte[] Padding = new byte[64];

        public int getDataLength() { return HeaderSize + PrinterModelSize + 16 + 64; }
    }

    public CXDLPFileHeader() { fileFields = new Fields(); }

    @Override public String getName() { return "Header"; }
    public void setLayerCount(short count) {
        fileFields.LayerCount = count;
    }

    @Override
    public String toString() { return fieldsAsString(" = ", "\n"); }
}
