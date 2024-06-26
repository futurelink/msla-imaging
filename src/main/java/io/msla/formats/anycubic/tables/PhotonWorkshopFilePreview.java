package io.msla.formats.anycubic.tables;

import io.msla.formats.MSLAException;
import io.msla.formats.iface.MSLAFileBlockFields;
import io.msla.formats.iface.MSLAPreview;
import io.msla.utils.Size;
import lombok.Getter;

import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.OutputStream;

/**
 * Abstract class for preview sections.
 */
@Getter
abstract class PhotonWorkshopFilePreview extends PhotonWorkshopFileTable implements MSLAPreview {
    protected BufferedImage Image;

    interface Fields extends MSLAFileBlockFields {
        Size getResolution();
        int getImageDataSize();
        byte[] getImageData();
        void setImageData(byte[] imageData);
    }

    public PhotonWorkshopFilePreview(byte versionMajor, byte versionMinor) {
        super(versionMajor, versionMinor);
        setImage(null);
    }

    @Override public Size getResolution() { return ((Fields) getBlockFields()).getResolution(); }

    /**
     * Converts RGB to BGR
     */
    private int reverseRGB(int val) {
        var b = (val >> 11) & 0x1f;
        var g = (val >> 5) & 0x3f;
        var r = val & 0x1f;
        return (r << 11) | (g << 5) | b;
    }

    @Override
    public void afterRead() {
        var fields = (Fields) getBlockFields();
        this.Image = new BufferedImage(getResolution().getWidth(), getResolution().getHeight(), BufferedImage.TYPE_USHORT_565_RGB);
        var buffer = this.Image.getRaster().getDataBuffer();
        for (int i = 0; i < fields.getImageDataSize(); i+=2) {
            int val = ((fields.getImageData()[i+1] & 0xff) << 8) | (fields.getImageData()[i] & 0xff);
            // Convert BGR to RGB
            buffer.setElem(i / 2, reverseRGB(val));
        }
    }

    @Override public void beforeWrite() throws MSLAException {
        var fields = (Fields) getBlockFields();
        var buffer = getImage().getData().getDataBuffer();
        var size = buffer.getSize() * 2;
        if (fields.getImageDataSize() != size)
            throw new MSLAException("Preview size " + size + " does not match resolution size " +
                    ((Fields) getBlockFields()).getImageDataSize());

        fields.setImageData(new byte[size]);
        for (int i = 0; i < size; i+=2) {
            var elem = reverseRGB(buffer.getElem(i / 2));
            fields.getImageData()[i] = (byte) ((elem >> 8) & 0xff);
            fields.getImageData()[i+1] = (byte) (elem & 0xff);
        }
    }

    @Override
    public long read(DataInputStream stream, long position) throws MSLAException {
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
}
