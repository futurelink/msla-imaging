package futurelink.msla.formats.anycubic.tables;

import com.google.common.io.LittleEndianDataInputStream;
import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.iface.MSLAFileBlockFields;
import futurelink.msla.formats.iface.MSLAFileField;
import futurelink.msla.formats.iface.MSLAPreview;
import futurelink.msla.formats.utils.FileFieldsReader;
import futurelink.msla.formats.utils.FileFieldsWriter;
import futurelink.msla.formats.utils.Size;
import lombok.Getter;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * "PREVIEW" section representation.
 */
public class PhotonWorkshopFilePreviewTable extends PhotonWorkshopFileTable  {
    public static final String Name = "PREVIEW";
    private final Fields fields;

    @Getter
    @SuppressWarnings("unused")
    static class Fields implements MSLAFileBlockFields, MSLAPreview {
        private final PhotonWorkshopFileTable parent;
        private Size Resolution = new Size(224, 168);

        @MSLAFileField(length = MarkLength, dontCount = true) private String Name() { return PhotonWorkshopFilePreviewTable.Name; }
        // Validation setter checks for what's been read from file
        // and throws an exception when that is something unexpected.
        private void setName(String name) throws MSLAException {
            if (!PhotonWorkshopFilePreviewTable.Name.equals(name))
                throw new MSLAException("Table name '" + name + "' is invalid");
        }
        @MSLAFileField(order = 1) private Integer TableLength() { return parent.calculateTableLength() + 4; }
        private void setTableLength(Integer length) { parent.TableLength = length; }
        @MSLAFileField(order = 2) private Integer ResolutionX() { return Resolution.getWidth(); }
        private void setResolutionX(Integer width) { Resolution = new Size(width, Resolution.getHeight()); }
        @MSLAFileField(order = 3) private int Mark = 'x'; /// Gets the operation mark 'x'
        @MSLAFileField(order = 4) private Integer ResolutionY() { return Resolution.getHeight(); }
        private void setResolutionY(Integer height) { Resolution = new Size(Resolution.getWidth(), height); }
        @Getter private int ImageDataSize = Resolution.length() * 2;
        @MSLAFileField(order = 5, lengthAt="ImageDataSize") public byte[] ImageData = null;

        /* Color table fields are part of preview section */
        @MSLAFileField(order = 6) public int UseFullGreyscale;
        @MSLAFileField(order = 7) public int GreyMaxCount = 16;
        @MSLAFileField(order = 8, length = 16, dontCount = true) public byte[] ShadesOfGrey = {
                15, 31, 47, 63, 79, 95, 111, 127, (byte) 143, (byte) 159, (byte) 175,
                (byte) 191, (byte) 207, (byte) 223, (byte) 239, (byte) 255
        };
        @MSLAFileField(order = 9) public int Unknown;

        BufferedImage image;

        public Fields(PhotonWorkshopFileTable parent) {
            this.parent = parent;
            this.image = new BufferedImage(
                    getResolution().getWidth(),
                    getResolution().getHeight(),
                    BufferedImage.TYPE_USHORT_GRAY
            );
        }

        public void updateImageData() throws MSLAException {
            var buffer = getImage().getData().getDataBuffer();
            var size = buffer.getSize() * 2;
            if (getImageDataSize() != size)
                throw new MSLAException("Preview size " + size + " does not match resolution size " + getImageDataSize());

            ImageData = new byte[size];
            for (int i = 0; i < size; i+=2) {
                int elem = buffer.getElem(i / 2);
                ImageData[i] = (byte) ((elem >> 8) & 0xff);
                ImageData[i+1] = (byte) (elem & 0xff);
            }
        }
    }

    public PhotonWorkshopFilePreviewTable(byte versionMajor, byte versionMinor) {
        super(versionMajor, versionMinor);
        fields = new Fields(this);
    }

    public void updateImageData() throws MSLAException { fields.updateImageData(); }
    public MSLAPreview getPreview() { return fields; }
    @Override int calculateTableLength() { return 12 + 12 + fields.ImageDataSize; }

    @Override
    public void read(FileInputStream stream, int position) throws MSLAException {
        try {
            var reader = new FileFieldsReader(stream, FileFieldsReader.Endianness.LittleEndian);
            var dataRead = reader.read(fields);
            if (dataRead != TableLength) throw new MSLAException(
                    "Preview table was not completely read out (" + dataRead + " of " + TableLength +
                            "), some extra data left unread"
            );
        } catch (IOException e) {
            throw new MSLAException("Error reading Preview table", e);
        }
    }

    @Override
    public void write(OutputStream stream) throws MSLAException {
        TableLength = calculateTableLength();
        try {
            var writer = new FileFieldsWriter(stream, FileFieldsWriter.Endianness.LittleEndian);
            writer.write(fields);
            stream.flush();
        } catch (IOException e) {
            throw new MSLAException("Error writing Preview table", e);
        }
    }

    public String toString() {
        return "-- Preview data --\n" +
                "TableLength: " + TableLength + "\n" +
                "Resolution: " + fields.Resolution + "\n" +
                "DataSize: " + fields.ImageDataSize + "\n" +
                "UseFullGreyscale: " + fields.UseFullGreyscale + "\n" +
                "GreyMaxCount: " + fields.GreyMaxCount + "\n";
    }
}
