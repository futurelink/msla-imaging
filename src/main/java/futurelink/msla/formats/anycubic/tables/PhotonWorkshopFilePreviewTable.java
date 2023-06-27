package futurelink.msla.formats.anycubic.tables;

import com.google.common.io.LittleEndianDataInputStream;
import com.google.common.io.LittleEndianDataOutputStream;
import futurelink.msla.formats.MSLAPreview;
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
public class PhotonWorkshopFilePreviewTable extends PhotonWorkshopFileTable implements MSLAPreview {
    public static final String Name = "PREVIEW";
    @Getter private Size Resolution = new Size(224, 168);
    @Getter private int Mark = 'x'; /// Gets the operation mark 'x'
    @Getter byte[] Data = null;
    @Getter private int DataSize = Resolution.length() * 2;

    /* Color table fields are part of preview section */
    @Getter public int UseFullGreyscale;
    @Getter public int GreyMaxCount = 16;
    @Getter public byte[] ShadesOfGrey = { /* AA16: 255, 239, 223, 207, 191, 175, 159, 143, 127, 111, 95, 79, 63, 47, 31, 15 */
            15, 31, 47, 63,
            79, 95, 111, 127,
            (byte) 143, (byte) 159, (byte) 175, (byte) 191,
            (byte) 207, (byte) 223, (byte) 239, (byte) 255
    };
    public int Unknown;

    @Getter BufferedImage Image;
    public byte[] ImageData = null;

    public PhotonWorkshopFilePreviewTable(byte versionMajor, byte versionMinor) {
        super(versionMajor, versionMinor);
        Image = new BufferedImage(getResolution().getWidth(), getResolution().getHeight(), BufferedImage.TYPE_USHORT_GRAY);
    }

    public void updateImageData() throws IOException{
        var buffer = getImage().getData().getDataBuffer();
        var size = buffer.getSize() * 2;
        if (getDataSize() != size)
            throw new IOException("Preview size " + size + " does not match resolution size " + getDataSize());

        ImageData = new byte[size];
        for (int i = 0; i < size; i+=2) {
            int elem = buffer.getElem(i / 2);
            ImageData[i] = (byte) ((elem >> 8) & 0xff);
            ImageData[i+1] = (byte) (elem & 0xff);
        }
    }

    @Override
    int calculateTableLength(byte versionMajor, byte versionMinor) {
        return 12 + 12 + DataSize;
    }

    @Override
    public void read(FileInputStream stream, int position) throws IOException {
        var fc = stream.getChannel(); fc.position(position);
        var dis = new LittleEndianDataInputStream(stream);
        var dataRead = 0;
        var mark = stream.readNBytes(Name.length());
        if (!Arrays.equals(mark, Name.getBytes())) {
            throw new IOException("Preview mark not found! Found: '" + new String(mark) + "' at " + position + ". Corrupted data.");
        }
        stream.readNBytes(MarkLength - Name.length()); // Skip section name zeroes
        TableLength = dis.readInt();
        if (TableLength > 0) {
            var ResolutionX = dis.readInt();
            Mark = dis.readInt(); // 'x' character and 3 zeroes
            var ResolutionY = dis.readInt();
            Resolution = new Size(ResolutionX, ResolutionY);
            dataRead = 12;

            dis.skipBytes(DataSize); // We won't read data here
            dataRead += DataSize;

            // Read color table
            UseFullGreyscale = dis.readInt();
            GreyMaxCount = dis.readInt();
            ShadesOfGrey = dis.readNBytes(GreyMaxCount);
            Unknown = dis.readInt();
            dataRead += 16; // Grey table is not counted, don't know why

            if (dataRead != TableLength) {
                throw new IOException("Preview was not completely read out (" + dataRead + " of " + TableLength +
                        "), some extra data left unread.");
            }
        }
    }

    @Override
    public void write(OutputStream stream) throws IOException {
        var fos = new LittleEndianDataOutputStream(stream);
        TableLength = calculateTableLength(versionMajor, versionMinor) + 4; // TODO sort out where this comes from :()
        fos.write(Name.getBytes());
        fos.write(new byte[PhotonWorkshopFileTable.MarkLength - Name.length()]);
        fos.writeInt(TableLength);
        fos.writeInt(Resolution.getWidth());
        fos.writeInt(Mark);
        fos.writeInt(Resolution.getHeight());
        fos.write(ImageData);
        fos.writeInt(UseFullGreyscale);
        fos.writeInt(GreyMaxCount);
        fos.write(ShadesOfGrey);
        fos.writeInt(Unknown);
    }

    public String toString() {
        return "-- Preview data --\n" +
                "TableLength: " + TableLength + "\n" +
                "Resolution: " + Resolution + "\n" +
                "DataSize: " + DataSize + "\n" +
                "UseFullGreyscale: " + UseFullGreyscale + "\n" +
                "GreyMaxCount: " + GreyMaxCount + "\n";
    }
}
