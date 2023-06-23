package futurelink.msla.formats.anycubic;

import com.google.common.io.LittleEndianDataInputStream;
import com.google.common.io.LittleEndianDataOutputStream;
import futurelink.msla.formats.anycubic.tables.PhotonWorkshopFileTable;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

/**
 * File descriptor section representation.
 * This descriptor is located at the beginning of a file and starts with 'ANYCUBIC' padded up to 12 bytes.
 */
public class PhotonWorkshopFileDescriptor {
    public static class PhotonWorkshopFileDescriptorFields {
        /**
         * 4 for v1, 5 for v2.3, 8 for v2.4 etc.
         */
        public int NumberOfTables;

        /**
         * Gets the header start address
         */
        @Getter @Setter private int HeaderAddress;
        @Getter @Setter private int SoftwareAddress;

        /**
         * Gets the preview start offset
         */
        @Getter @Setter private int PreviewAddress;
        @Getter @Setter private int LayerImageColorTableAddress; // v2.3 and greater
        @Getter @Setter private int LayerDefinitionAddress;
        @Getter @Setter private int ExtraAddress;               // v2.4 and greater
        @Getter @Setter private int MachineAddress;             // v2.4 and greater
        @Getter @Setter private int LayerImageAddress;
        @Getter @Setter private int ModelAddress;               // v2.6 and greater
        @Getter @Setter private int SubLayerDefinitionAddress;  // v2.6 and greater
        @Getter @Setter private int Preview2Address;            // v2.6 and greater
    }

    static final HashMap<Byte, HashMap<Byte, String>> versions = new HashMap<>();

    static {
        versions.put((byte) 1, new HashMap<>());
        versions.get((byte) 1).put((byte) 0, "1.0");
        versions.put((byte) 2, new HashMap<>());
        versions.get((byte) 2).put((byte) 3, "2.3");
        versions.get((byte) 2).put((byte) 4, "2.4");
        versions.get((byte) 2).put((byte) 5, "2.5");
        versions.get((byte) 2).put((byte) 6, "2.6");
    }

    /**
     * Gets the file mark placeholder. Fixed to "ANYCUBIC".
     */
    static public final String Mark = "ANYCUBIC";

    @Getter
    private final byte[] version;
    @Getter private final PhotonWorkshopFileDescriptorFields fields;

    public byte getVersionMajor() {
        return version[0];
    }

    public byte getVersionMinor() {
        return version[1];
    }

    public PhotonWorkshopFileDescriptor(byte versionMajor, byte versionMinor) {
        this.version = new byte[2];
        this.version[0] = versionMajor;
        this.version[1] = versionMinor;
        this.fields = new PhotonWorkshopFileDescriptorFields();
        if (versionMajor >= 2) {
            fields.NumberOfTables = switch (versionMinor) {
                case 3 -> 5;
                case 4 -> 8;
                case 5 -> 9;
                case 6 -> 11;
                default -> 4;
            };
        } else fields.NumberOfTables = 4;
    }

    public int calculateDataLength() {
        // 12 bytes mark length + 4 x NumberOfTables + 4 bytes version + 4 bytes table count
        return PhotonWorkshopFileTable.MarkLength + fields.NumberOfTables * 4 + 8;
    }

    public void write(LittleEndianDataOutputStream stream, byte versionMajor, byte versionMinor) throws IOException {
        stream.write(PhotonWorkshopFileDescriptor.Mark.getBytes());
        stream.write(new byte[PhotonWorkshopFileTable.MarkLength - PhotonWorkshopFileDescriptor.Mark.length()]);
        stream.write(versionMinor);
        stream.write(versionMajor);
        stream.write(0); // Skip a byte
        stream.write(0); // Skip a byte
        stream.writeInt(fields.NumberOfTables);

        stream.writeInt(fields.HeaderAddress);
        stream.writeInt(fields.SoftwareAddress);
        stream.writeInt(fields.PreviewAddress);
        stream.writeInt(fields.LayerImageColorTableAddress);
        stream.writeInt(fields.LayerDefinitionAddress);

        // Version 2.3 and greater
        if ((versionMajor >= 2) && (versionMinor >= 4)) {
            stream.writeInt(fields.ExtraAddress);
        }

        stream.writeInt(fields.MachineAddress);
        stream.writeInt(fields.LayerImageAddress);

        // Version 2.5 and greater
        if ((versionMajor >= 2) && (versionMinor >= 5)) {
            stream.writeInt(fields.ModelAddress);
        }

        // Version 2.6 and greater
        if ((versionMajor >= 2) && (versionMinor >= 6)) {
            stream.writeInt(fields.SubLayerDefinitionAddress);
            stream.writeInt(fields.Preview2Address);
        }
        stream.flush();
    }

    public static PhotonWorkshopFileDescriptor read(LittleEndianDataInputStream stream) throws IOException {
        byte[] markPattern = Arrays.copyOf(PhotonWorkshopFileDescriptor.Mark.getBytes(), PhotonWorkshopFileTable.MarkLength);
        byte[] mark = stream.readNBytes(PhotonWorkshopFileTable.MarkLength);
        if (!Arrays.equals(mark, markPattern))
            throw new IOException("Not a Photon Workshop file : " + Arrays.toString(mark) + " not equal to " +
                    Arrays.toString(markPattern));

        // Read version
        var versionMinor = stream.readByte();
        var versionMajor = stream.readByte();
        stream.readByte(); // Skip a byte
        stream.readByte(); // Skip a byte
        if ((PhotonWorkshopFileDescriptor.versions.get(versionMajor) == null) ||
                (PhotonWorkshopFileDescriptor.versions.get(versionMajor).get(versionMinor) == null)) {
            throw new IOException("Unknown file version: " + versionMajor + "." + versionMinor);
        }

        var descriptor = new PhotonWorkshopFileDescriptor(versionMajor, versionMinor);
        var f = descriptor.getFields();

        // Read fields
        f.NumberOfTables = stream.readInt();
        f.HeaderAddress = stream.readInt();
        f.SoftwareAddress = stream.readInt();
        f.PreviewAddress = stream.readInt();
        f.LayerImageColorTableAddress = stream.readInt();
        f.LayerDefinitionAddress = stream.readInt();

        // Version 2.4 and greater
        if ((versionMajor == 2) && (versionMinor >= 4)) f.ExtraAddress = stream.readInt();

        f.MachineAddress = stream.readInt();
        f.LayerImageAddress = stream.readInt();

        // Version 2.5 and greater
        if ((versionMajor == 2) && (versionMinor >= 5)) f.ModelAddress = stream.readInt();

        // Version 2.6 and greater
        if ((versionMajor == 2) && (versionMinor >= 6)) {
            f.SubLayerDefinitionAddress = stream.readInt();
            f.Preview2Address = stream.readInt();
        }

        return descriptor;
    }

    @Override
    public String toString() {
        var b = new StringBuilder();
        b.append("-- Descriptor data --\n");
        b.append("Version: ").append(PhotonWorkshopFileDescriptor.versions.get(version[0]).get(version[1])).append("\n");
        b.append("Tables count: ").append(fields.NumberOfTables).append("\n");
        b.append("Header address: ").append(fields.HeaderAddress).append("\n");
        b.append("Software address: ").append(fields.SoftwareAddress).append("\n");
        b.append("Preview address: ").append(fields.PreviewAddress).append("\n");
        b.append("LayerImageColorTable address: ").append(fields.LayerImageColorTableAddress).append("\n");
        b.append("LayerDefinition address: ").append(fields.LayerDefinitionAddress).append("\n");

        // Version 2.4 and greater
        if ((version[0] >= 2) && (version[1] >= 4)) b.append("Extra address: ").append(fields.ExtraAddress).append("\n");

        b.append("Machine address: ").append(fields.MachineAddress).append("\n");
        b.append("LayerImage address: ").append(fields.LayerImageAddress).append("\n");

        // Version 2.5 and greater
        if ((version[0] >= 2) && (version[1] >= 5)) {
            b.append("Model address: ").append(fields.ModelAddress).append("\n");
        }

        // Version 2.6 and greater
        if ((version[0] >= 2) && (version[1] >= 6)) {
            b.append("SubLayerDefinition address: ").append(fields.SubLayerDefinitionAddress).append("\n");
            b.append("Preview2 address: ").append(fields.Preview2Address).append("\n");
        }
        return b.toString();
    }
}