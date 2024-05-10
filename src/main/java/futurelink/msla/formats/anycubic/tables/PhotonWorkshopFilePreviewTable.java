package futurelink.msla.formats.anycubic.tables;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.iface.MSLAFileBlockFields;
import futurelink.msla.formats.iface.annotations.MSLAFileField;
import futurelink.msla.formats.iface.MSLAPreview;
import futurelink.msla.formats.utils.Size;
import lombok.Getter;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.OutputStream;

/**
 * "PREVIEW" section representation.
 */
@Getter
public class PhotonWorkshopFilePreviewTable extends PhotonWorkshopFileTable implements MSLAPreview  {
    private final Fields fileFields;
    private BufferedImage Image;

    @Getter
    @SuppressWarnings("unused")
    static class Fields implements MSLAFileBlockFields {
        private final PhotonWorkshopFileTable parent;
        private Size Resolution = new Size(224, 168);

        @MSLAFileField(length = MarkLength, dontCount = true) private String Name() { return "PREVIEW"; }
        // Validation setter checks for what's been read from file
        // and throws an exception when that is something unexpected.
        private void setName(String name) throws MSLAException {
            if (!"PREVIEW".equals(name)) throw new MSLAException("Table name '" + name + "' is invalid");
        }
        @MSLAFileField(order = 1) private Integer TableLength() { return parent.calculateTableLength() + 4; }
        private void setTableLength(Integer length) { parent.TableLength = length; }
        @MSLAFileField(order = 2) private Integer ResolutionX() { return Resolution.getWidth(); }
        private void setResolutionX(Integer width) { Resolution = new Size(width, Resolution.getHeight()); }
        @MSLAFileField(order = 3) private final int Mark = 'x'; /// Gets the operation mark 'x'
        @MSLAFileField(order = 4) private Integer ResolutionY() { return Resolution.getHeight(); }
        private void setResolutionY(Integer height) { Resolution = new Size(Resolution.getWidth(), height); }
        @Getter private int ImageDataSize = Resolution.length() * 2;
        @MSLAFileField(order = 5, lengthAt="ImageDataSize") public byte[] ImageData = null;

        /* Color table fields are part of preview section */
        @MSLAFileField(order = 6) private int UseFullGreyscale;
        @MSLAFileField(order = 7) private final int GreyMaxCount = 16;
        @MSLAFileField(order = 8, length = 16, dontCount = true) public byte[] ShadesOfGrey = {
                15, 31, 47, 63, 79, 95, 111, 127, (byte) 143, (byte) 159, (byte) 175,
                (byte) 191, (byte) 207, (byte) 223, (byte) 239, (byte) 255
        };
        @MSLAFileField(order = 9) private int Unknown;

        public Fields(PhotonWorkshopFileTable parent) {
            this.parent = parent;
        }
    }

    public PhotonWorkshopFilePreviewTable(byte versionMajor, byte versionMinor) {
        super(versionMajor, versionMinor);
        fileFields = new Fields(this);
        setImage(null);
    }

    @Override
    public void setImage(BufferedImage image) {
        Image = new BufferedImage(224, 168, BufferedImage.TYPE_USHORT_GRAY);
        if (image != null) Image.getGraphics().drawImage(image, 0, 0, null);
    }

    @Override public Size getResolution() { return fileFields.Resolution; }
    @Override int calculateTableLength() { return 12 + 12 + fileFields.ImageDataSize; }

    @Override
    public void afterRead() {
        this.Image = new BufferedImage(getResolution().getWidth(), getResolution().getHeight(), BufferedImage.TYPE_USHORT_GRAY);
    }

    @Override public void beforeWrite() throws MSLAException {
        var buffer = getImage().getData().getDataBuffer();
        var size = buffer.getSize() * 2;
        if (fileFields.getImageDataSize() != size)
            throw new MSLAException("Preview size " + size + " does not match resolution size " + fileFields.getImageDataSize());

        fileFields.ImageData = new byte[size];
        for (int i = 0; i < size; i+=2) {
            int elem = buffer.getElem(i / 2);
            fileFields.ImageData[i] = (byte) ((elem >> 8) & 0xff);
            fileFields.ImageData[i+1] = (byte) (elem & 0xff);
        }
    }

    @Override
    public long read(FileInputStream stream, long position) throws MSLAException {
        var dataRead = super.read(stream, position);
        if (dataRead != TableLength) throw new MSLAException(
                "Preview table was not completely read out (" + dataRead + " of " + TableLength +
                        "), some extra data left unread"
        );
        return dataRead;
    }

    @Override
    public void write(OutputStream stream) throws MSLAException {
        TableLength = calculateTableLength();
        super.write(stream);
    }

    public String toString() { return "-- Preview --\n" + fileFields.fieldsAsString(" = ", "\n"); }
}
