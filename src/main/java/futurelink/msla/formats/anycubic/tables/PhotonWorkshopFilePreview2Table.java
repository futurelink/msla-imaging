package futurelink.msla.formats.anycubic.tables;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.iface.annotations.MSLAFileField;
import futurelink.msla.utils.Size;
import lombok.Getter;
import lombok.Setter;

import java.awt.image.BufferedImage;

@Getter
public class PhotonWorkshopFilePreview2Table extends PhotonWorkshopFilePreview {
    private final Fields blockFields;

    @Getter
    @SuppressWarnings("unused")
    static class Fields implements PhotonWorkshopFilePreview.Fields {
        private final PhotonWorkshopFileTable parent;
        private Size Resolution = new Size(320, 190);

        @MSLAFileField(length = MarkLength, dontCount = true) private String Name() { return "PREVIEW2"; }
        // Validation setter checks for what's been read from file
        // and throws an exception when that is something unexpected.
        private void setName(String name) throws MSLAException {
            if (!"PREVIEW2".equals(name)) throw new MSLAException("Table name '" + name + "' is invalid");
        }
        @MSLAFileField(order = 1) private Integer TableLength() { return parent.calculateTableLength() + 4; }
        private void setTableLength(Integer length) { parent.TableLength = length; }
        @MSLAFileField(order = 2) private Integer ResolutionX() { return Resolution.getWidth(); }
        private void setResolutionX(Integer width) { Resolution = new Size(width, Resolution.getHeight()); }
        @MSLAFileField(order = 3) private short BackgroundColor1;
        @MSLAFileField(order = 4) private short BackgroundColor2;
        @MSLAFileField(order = 5) private Integer ResolutionY() { return Resolution.getHeight(); }
        private void setResolutionY(Integer height) { Resolution = new Size(Resolution.getWidth(), height); }
        @Getter private int ImageDataSize = Resolution.length() * 2;
        @MSLAFileField(order = 6, lengthAt="ImageDataSize") @Setter private byte[] ImageData = null;

        public Fields(PhotonWorkshopFileTable parent) { this.parent = parent; }
    }

    public PhotonWorkshopFilePreview2Table(byte versionMajor, byte versionMinor) {
        super(versionMajor, versionMinor);
        blockFields = new Fields(this);
        setImage(null);
    }

    @Override
    public void setImage(BufferedImage image) {
        Image = new BufferedImage(320, 190, BufferedImage.TYPE_USHORT_565_RGB);
        if (image != null) Image.getGraphics().drawImage(image, 0, 0, null);
    }

    @Override int calculateTableLength() { return 20 + getBlockFields().getImageDataSize(); }

    public String toString() { return "-- Preview 2 --\n" + blockFields.fieldsAsString(" = ", "\n"); }
}
