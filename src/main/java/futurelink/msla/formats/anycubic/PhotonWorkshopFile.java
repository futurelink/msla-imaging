package futurelink.msla.formats.anycubic;

import java.io.*;
import java.util.HashMap;
import java.util.Objects;

import com.google.common.io.LittleEndianDataInputStream;
import com.google.common.io.LittleEndianDataOutputStream;
import futurelink.msla.formats.MSLAFile;
import futurelink.msla.formats.anycubic.tables.*;
import lombok.Getter;

public class PhotonWorkshopFile implements MSLAFile {
    public final byte RLE1EncodingLimit = 0x7d;
    public final short RLE4EncodingLimit = 0xfff;
    public static int[] CRC16Table = {
            0x0000, 0xc0c1, 0xc181, 0x0140, 0xc301, 0x03c0, 0x0280, 0xc241, 0xc601, 0x06c0, 0x0780, 0xc741, 0x0500,
            0xc5c1, 0xc481, 0x0440, 0xcc01, 0x0cc0, 0x0d80, 0xcd41, 0x0f00, 0xcfc1, 0xce81, 0x0e40, 0x0a00, 0xcac1,
            0xcb81, 0x0b40, 0xc901, 0x09c0, 0x0880, 0xc841, 0xd801, 0x18c0, 0x1980, 0xd941, 0x1b00, 0xdbc1, 0xda81,
            0x1a40, 0x1e00, 0xdec1, 0xdf81, 0x1f40, 0xdd01, 0x1dc0, 0x1c80, 0xdc41, 0x1400, 0xd4c1, 0xd581, 0x1540,
            0xd701, 0x17c0, 0x1680, 0xd641, 0xd201, 0x12c0, 0x1380, 0xd341, 0x1100, 0xd1c1, 0xd081, 0x1040, 0xf001,
            0x30c0, 0x3180, 0xf141, 0x3300, 0xf3c1, 0xf281, 0x3240, 0x3600, 0xf6c1, 0xf781, 0x3740, 0xf501, 0x35c0,
            0x3480, 0xf441, 0x3c00, 0xfcc1, 0xfd81, 0x3d40, 0xff01, 0x3fc0, 0x3e80, 0xfe41, 0xfa01, 0x3ac0, 0x3b80,
            0xfb41, 0x3900, 0xf9c1, 0xf881, 0x3840, 0x2800, 0xe8c1, 0xe981, 0x2940, 0xeb01, 0x2bc0, 0x2a80, 0xea41,
            0xee01, 0x2ec0, 0x2f80, 0xef41, 0x2d00, 0xedc1, 0xec81, 0x2c40, 0xe401, 0x24c0, 0x2580, 0xe541, 0x2700,
            0xe7c1, 0xe681, 0x2640, 0x2200, 0xe2c1, 0xe381, 0x2340, 0xe101, 0x21c0, 0x2080, 0xe041, 0xa001, 0x60c0,
            0x6180, 0xa141, 0x6300, 0xa3c1, 0xa281, 0x6240, 0x6600, 0xa6c1, 0xa781, 0x6740, 0xa501, 0x65c0, 0x6480,
            0xa441, 0x6c00, 0xacc1, 0xad81, 0x6d40, 0xaf01, 0x6fc0, 0x6e80, 0xae41, 0xaa01, 0x6ac0, 0x6b80, 0xab41,
            0x6900, 0xa9c1, 0xa881, 0x6840, 0x7800, 0xb8c1, 0xb981, 0x7940, 0xbb01, 0x7bc0, 0x7a80, 0xba41, 0xbe01,
            0x7ec0, 0x7f80, 0xbf41, 0x7d00, 0xbdc1, 0xbc81, 0x7c40, 0xb401, 0x74c0, 0x7580, 0xb541, 0x7700, 0xb7c1,
            0xb681, 0x7640, 0x7200, 0xb2c1, 0xb381, 0x7340, 0xb101, 0x71c0, 0x7080, 0xb041, 0x5000, 0x90c1, 0x9181,
            0x5140, 0x9301, 0x53c0, 0x5280, 0x9241, 0x9601, 0x56c0, 0x5780, 0x9741, 0x5500, 0x95c1, 0x9481, 0x5440,
            0x9c01, 0x5cc0, 0x5d80, 0x9d41, 0x5f00, 0x9fc1, 0x9e81, 0x5e40, 0x5a00, 0x9ac1, 0x9b81, 0x5b40, 0x9901,
            0x59c0, 0x5880, 0x9841, 0x8801, 0x48c0, 0x4980, 0x8941, 0x4b00, 0x8bc1, 0x8a81, 0x4a40, 0x4e00, 0x8ec1,
            0x8f81, 0x4f40, 0x8d01, 0x4dc0, 0x4c80, 0x8c41, 0x4400, 0x84c1, 0x8581, 0x4540, 0x8701, 0x47c0, 0x4680,
            0x8641, 0x8201, 0x42c0, 0x4380, 0x8341, 0x4100, 0x81c1, 0x8081, 0x4040
    };

    @Getter private PhotonWorkshopFileDescriptor descriptor;
    @Getter private final HashMap<String, PhotonWorkshopFileTable> tables = new HashMap<>();

    public final PhotonWorkshopFileHeaderTable getHeader() {
        return tables.containsKey("HEADER") ? (PhotonWorkshopFileHeaderTable) tables.get("HEADER") : null;
    }

    public final PhotonWorkshopFilePreviewTable getPreview() {
        return tables.containsKey("PREVIEW") ? (PhotonWorkshopFilePreviewTable) tables.get("PREVIEW") : null;
    }

    public final PhotonWorkshopFileLayerDefTable getLayerDef() {
        return tables.containsKey("LAYERDEF") ? (PhotonWorkshopFileLayerDefTable) tables.get("LAYERDEF") : null;
    }

    public final PhotonWorkshopFileSoftwareTable getSoftware() {
        return tables.containsKey("SOFTWARE") ? (PhotonWorkshopFileSoftwareTable) tables.get("SOFTWARE") : null;
    }

    public final PhotonWorkshopFileExtraTable getExtra() {
        return tables.containsKey("EXTRA") ? (PhotonWorkshopFileExtraTable) tables.get("EXTRA") : null;
    }

    public final PhotonWorkshopFileMachineTable getMachine() {
        return tables.containsKey("MACHINE") ? (PhotonWorkshopFileMachineTable) tables.get("MACHINE") : null;
    }

    public PhotonWorkshopFile(PhotonWorkshopFileDefaults.Values defaults) {
        descriptor = new PhotonWorkshopFileDescriptor(defaults.VersionMajor, defaults.VersionMinor);
        tables.put("HEADER", new PhotonWorkshopFileHeaderTable(defaults.Header));
        tables.put("MACHINE", new PhotonWorkshopFileMachineTable(defaults.Machine));
        tables.put("PREVIEW", new PhotonWorkshopFilePreviewTable());
        tables.put("SOFTWARE", new PhotonWorkshopFileSoftwareTable());
        tables.put("LAYERDEF", new PhotonWorkshopFileLayerDefTable());
        tables.put("EXTRA", new PhotonWorkshopFileExtraTable());
    }

    public PhotonWorkshopFile(FileInputStream stream) throws IOException {
        read(stream);
    }

    @Override
    public float getDPI() {
        if (getHeader() == null) return 0.0f;
        return 1 / (getHeader().getPixelSizeUm() / 25400);
    }

    @Override
    public final void read(FileInputStream iStream) throws IOException {
        descriptor = PhotonWorkshopFileDescriptor.read(new LittleEndianDataInputStream(iStream));
        readTables(iStream);
    }

    private void readTables(FileInputStream iStream) throws IOException {
        var stream = new LittleEndianDataInputStream(iStream);
        var fc = iStream.getChannel();

        System.out.println(descriptor);

        if (descriptor.getFields().getHeaderAddress() > 0) {
            fc.position(descriptor.getFields().getHeaderAddress());
            tables.put("HEADER", new PhotonWorkshopFileHeaderTable());
            Objects.requireNonNull(getHeader()).read(stream);
        } else throw new IOException("No HEADER section found!");

        if (descriptor.getFields().getSoftwareAddress() > 0) {
            fc.position(descriptor.getFields().getSoftwareAddress());
            tables.put("SOFTWARE", new PhotonWorkshopFileSoftwareTable());
            Objects.requireNonNull(getSoftware()).read(stream);
        }

        if (descriptor.getFields().getPreviewAddress() > 0) {
            fc.position(descriptor.getFields().getPreviewAddress());
            tables.put("PREVIEW", new PhotonWorkshopFilePreviewTable());
            Objects.requireNonNull(getPreview()).read(stream);
        }

        if (descriptor.getFields().getLayerDefinitionAddress() > 0) {
            fc.position(descriptor.getFields().getLayerDefinitionAddress());
            tables.put("LAYERDEF", new PhotonWorkshopFileLayerDefTable());
            Objects.requireNonNull(getLayerDef()).read(stream);
        } else throw new IOException("No LAYERDEF section found!");

        if (descriptor.getFields().getExtraAddress() > 0) {
            fc.position(descriptor.getFields().getExtraAddress());
            tables.put("EXTRA", new PhotonWorkshopFileExtraTable());
            Objects.requireNonNull(getExtra()).read(stream);
        }

        if (descriptor.getFields().getMachineAddress() > 0) {
            fc.position(descriptor.getFields().getMachineAddress());
            tables.put("MACHINE", new PhotonWorkshopFileMachineTable());
            Objects.requireNonNull(getMachine()).read(stream);
        }
    }

    @Override
    public final void readLayer(FileInputStream iStream, int layer, PhotonWorkshopDecodeWriter writer) throws IOException {
        if (getLayerDef() == null) throw new IOException("LayerDef does not exist");
        if (layer > getLayerDef().getLayerCount()) throw new IOException("Layer is out of range");
        var address = getLayerDef().getLayer(layer).DataAddress;
        var length = getLayerDef().getLayer(layer).DataLength;
        if ((address == 0) || (length == 0)) throw new IOException("Invalid layer data in Layer definition table");

        // Go to data position
        var fc = iStream.getChannel();
        fc.position(address);

        DecodePW0(new DataInputStream(iStream), writer);
    }

    private void EncodePW0PutReps(ByteArrayOutputStream stream, int reps, byte lastColor) {
        while (reps > 0) {
            int done = reps;
            if ((lastColor == 0x00) || (lastColor == 0x0f)) {
                if (done > RLE4EncodingLimit) done = RLE4EncodingLimit;
                short more = (short) (done | (lastColor << 12));
                stream.write((byte) (more >> 8));
                stream.write((byte) more);
            } else {
                if (done > 0xf) done = 0xf;
                stream.write((byte) (done | (lastColor << 4)));
            }
            reps -= done;
        }
    }

    private byte[] EncodePW0(InputStream iStream) throws IOException {
        var stream = new ByteArrayOutputStream();
        var lastColor = (byte) 0xff;
        int reps = 0;

        System.out.println("Encoding pixels: " + iStream.available());

        while (iStream.available() > 0) {
            var b = iStream.readNBytes(1);
            var color = (byte) (((b[0] & 0xf0) >> 4));
            if (color == lastColor) {
                reps++;
            } else {
                EncodePW0PutReps(stream, reps, lastColor);
                lastColor = color;
                reps = 1;
            }
        }

        // Put the remainder
        EncodePW0PutReps(stream, reps, lastColor);

        //short crc = CRCRle4(stream.toByteArray());
        //stream.write((byte)(crc >> 8));
        //stream.write((byte)crc);

        System.out.println("Encoded bytes: " + stream.size());

        return stream.toByteArray();
    }

    private byte[] EncodePW0(byte[] pixels) {
        var iStream = new ByteArrayInputStream(pixels);
        try {
            return EncodePW0(iStream);
        } catch (IOException e) {
            return null;
        }
    }

    private void DecodePW0(DataInputStream stream, PhotonWorkshopDecodeWriter writer) throws IOException {
        if (getHeader() == null) throw new IOException("Header was not defined!");

        var width = getHeader().getResolutionX();
        var height = getHeader().getResolutionY();
        var imageLength = width * height;
        int pixelPos = 0;
        int pixels = 0;

        while (stream.available() > 0) {
            byte b = stream.readByte();
            byte code = (byte) ((b & 0xf0) >> 4);         // 1st 4 bits is a code
            int repeat = Byte.toUnsignedInt((byte) (b & 0x0f)); // 2nd 4 bits is repetitions
            byte color;
            if (code == 0) {                                    // Black sequences
                color = 0;
                if (stream.available() > 0) repeat = (repeat << 8) + Byte.toUnsignedInt(stream.readByte());
                else repeat = imageLength - pixelPos;
            } else if (code == 0x0f) {                          // White sequences
                color = (byte) 0xff;
                if (stream.available() > 0) repeat = (repeat << 8) + Byte.toUnsignedInt(stream.readByte());
                else repeat = imageLength - pixelPos;
            } else {                                            // Other colors
                color = (byte) ((code << 4) | code);
            }

            if (pixelPos + repeat > imageLength)
                throw new IOException("Image ran off the end: " + pixelPos + " + " + repeat +
                        " = " + (pixelPos + repeat) + ", expecting: " + imageLength);

            /*
             * Set pixels or something like this...
             */
            if (color != 0) {
                writer.writeSpan(color, pixelPos, repeat);
                pixels += repeat;
            }
            pixelPos += repeat;

            if (pixelPos >= imageLength) break;
        }

        if ((pixelPos > 0) && (pixelPos != imageLength))
            throw new IOException("Image ended short: " + pixelPos + ", expecting: " + imageLength);

        System.out.println("Decoded successfully pixels: " + pixels);
    }

    private void DecodePW0(byte[] data, PhotonWorkshopDecodeWriter writer) throws IOException {
        DecodePW0(new DataInputStream(new ByteArrayInputStream(data)), writer);
    }

    public static short CRCRle4(byte[] data) {
        short crc16 = 0;
        for (byte datum : data) {
            crc16 = (short) ((crc16 << 8) ^ CRC16Table[((crc16 >> 8) ^ CRC16Table[datum]) & 0xff]);
        }
        crc16 = (short) ((CRC16Table[crc16 & 0xff] * 0x100) + CRC16Table[(crc16 >> 8) & 0xff]);
        return crc16;
    }

    @Override
    public final void addLayer(PhotonWorkshopEncodeReader reader) throws IOException {
        if (getLayerDef() == null) throw new IOException("LayerDef table does not exist!");

        var layerDef = new PhotonWorkshopFileLayerDefTable.PhotonWorkshopFileLayerDef();
        layerDef.LayerHeight = 0.05f;
        layerDef.ExposureTime = 20.0f;
        layerDef.LiftSpeed = 10.0f;
        layerDef.LiftHeight = 50.0f;

        // Encode a layer data and calculate offsets
        layerDef.DataAddress = 0;
        layerDef.NonZeroPixelCount = 0;

        var encoded = EncodePW0(reader.read());
        if (encoded.length > 0) {
            layerDef.DataLength = encoded.length;
            getLayerDef().addLayer(layerDef, encoded);
        }
    }

    @Override
    public final void write(FileOutputStream stream) throws IOException {
        var oStream = new LittleEndianDataOutputStream(stream);

        /*
         * Prepare data offsets for descriptor
         * (order is important!)
         */
        var offset = descriptor.calculateDataLength();
        if (getHeader() != null) {
            descriptor.getFields().setHeaderAddress(offset);
            offset += getHeader().calculateDataLength(descriptor.getVersionMajor(), descriptor.getVersionMinor());
        }

        if (getPreview() != null) {
            descriptor.getFields().setPreviewAddress(offset);
            offset += 16; // 16 bytes of color data is not counted in preview table length
            offset += getPreview().calculateDataLength(descriptor.getVersionMajor(), descriptor.getVersionMinor());
            // Color table is not a standalone table and has no header mark, it's a part of a preview
            descriptor.getFields().setLayerImageColorTableAddress(offset - 28);
        }

        if (getLayerDef() != null) {
            descriptor.getFields().setLayerDefinitionAddress(offset);
            offset += getLayerDef().calculateDataLength(descriptor.getVersionMajor(), descriptor.getVersionMinor());
        }

        if (getExtra() != null) {
            descriptor.getFields().setExtraAddress(offset);
            offset += getExtra().calculateDataLength(descriptor.getVersionMajor(), descriptor.getVersionMinor());
        }

        if (getMachine() != null) {
            descriptor.getFields().setMachineAddress(offset);
            offset += getMachine().calculateDataLength(descriptor.getVersionMajor(), descriptor.getVersionMinor());
        }

        descriptor.getFields().setLayerImageAddress(offset);

        /*
         * Calculate layer data offsets
         */
        for (int i = 0; i < getLayerDef().getLayerCount(); i++) {
            getLayerDef().getLayer(i).DataAddress = offset;
            offset += getLayerDef().getLayer(i).DataLength;
        }

        // Write descriptor
        descriptor.write(oStream, descriptor.getVersionMajor(), descriptor.getVersionMinor());

        /*
         * Write data in the same order we prepared offsets
         */
        if (getHeader() != null) getHeader().write(oStream, descriptor.getVersionMajor(), descriptor.getVersionMinor());
        if (getSoftware() != null) getSoftware().write(oStream, descriptor.getVersionMajor(), descriptor.getVersionMinor());
        if (getPreview() != null) getPreview().write(oStream, descriptor.getVersionMajor(), descriptor.getVersionMinor());
        if (getLayerDef() != null) getLayerDef().write(oStream, descriptor.getVersionMajor(), descriptor.getVersionMinor());
        if (getExtra() != null) getExtra().write(oStream, descriptor.getVersionMajor(), descriptor.getVersionMinor());
        if (getMachine() != null) getMachine().write(oStream, descriptor.getVersionMajor(), descriptor.getVersionMinor());

        /*
         * Write layers
         */
        for (int i = 0; i < getLayerDef().getLayerCount(); i++) {
            oStream.write(getLayerDef().getLayerData(i));
        }
    }

}
