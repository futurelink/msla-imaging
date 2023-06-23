package futurelink.msla.formats.anycubic;

import java.io.*;
import java.util.HashMap;
import java.util.Objects;

import com.google.common.io.LittleEndianDataInputStream;
import com.google.common.io.LittleEndianDataOutputStream;
import futurelink.msla.formats.MSLADecodeWriter;
import futurelink.msla.formats.MSLAEncodeReader;
import futurelink.msla.formats.MSLAFileCodec;
import futurelink.msla.formats.MSLAFile;
import futurelink.msla.formats.anycubic.tables.*;
import lombok.Getter;

/**
 * Main class that allows to work with Anycubic mSLA file formats.
 * Currently only PW0 encoding is supported, so ancient printers are out of the list.
 */
public class PhotonWorkshopFile implements MSLAFile {

    @Getter private MSLAFileCodec codec;
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

    public PhotonWorkshopFile(PhotonWorkshopFileDefaults.Values defaults) throws IOException {
        var format = defaults.getMachine().getLayerImageFormat();
        if (format.equals("pw0Img")) {
            codec = new PhotonWorkshopCodecPW0();
        } else if (format.equals("pwsImg")) {
            codec = new PhotonWorkshopCodecPWS();
        } else {
            throw new IOException("Codec not implemented for '" + format + "'");
        }
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
    public final void readLayer(FileInputStream iStream, int layer, MSLADecodeWriter writer) throws IOException {
        if (getLayerDef() == null) throw new IOException("LayerDef does not exist");
        if (layer > getLayerDef().getLayerCount()) throw new IOException("Layer is out of range");
        var address = getLayerDef().getLayer(layer).DataAddress;
        var length = getLayerDef().getLayer(layer).DataLength;
        if ((address == 0) || (length == 0)) throw new IOException("Invalid layer data in Layer definition table");

        // Go to data position
        var fc = iStream.getChannel();
        fc.position(address);

        if (getHeader() == null) throw new IOException("Header was not defined!");

        var width = getHeader().getResolutionX();
        var height = getHeader().getResolutionY();
        var imageLength = width * height;
        codec.Decode(new DataInputStream(iStream), imageLength, writer);
    }

    @Override
    public final void updatePreviewImage() throws IOException {
        if (getPreview() == null) return;
        getPreview().updateImageData();
    }

    @Override
    public final void addLayer(MSLAEncodeReader reader,
                               float layerHeight, float exposureTime,
                               float liftSpeed, float liftHeight) throws IOException  {
        if (getLayerDef() == null) throw new IOException("LayerDef table does not exist!");

        var layerDef = new PhotonWorkshopFileLayerDefTable.PhotonWorkshopFileLayerDef();
        layerDef.LayerHeight = layerHeight;
        layerDef.ExposureTime = exposureTime;
        layerDef.LiftSpeed = liftSpeed;
        layerDef.LiftHeight = liftHeight;

        // Encode a layer data and calculate offsets
        layerDef.DataAddress = 0;
        layerDef.NonZeroPixelCount = 0;

        getLayerDef().addLayer(layerDef, reader);
    }

    @Override
    public final void addLayer(MSLAEncodeReader reader) throws IOException {
        if ((getLayerDef() == null) || (getHeader() == null)) throw new IOException("Ho LayerDef or Header created");

        var layerNumber = getLayerDef().getLayerCount() + 1;
        float layerHeight, exposureTime, liftSpeed, liftHeight;
        if (layerNumber < getHeader().getBottomLayersCount()) {
            layerHeight = getHeader().getBottomLayerHeight();
            exposureTime = getHeader().getBottomExposureTime();
            liftSpeed = getHeader().getBottomLiftSpeed();
            liftHeight = getHeader().getBottomLiftHeight();
        } else {
            layerHeight = getHeader().getLayerHeight();
            exposureTime = getHeader().getExposureTime();
            liftSpeed = getHeader().getLiftSpeed();
            liftHeight = getHeader().getLiftHeight();
        }
        addLayer(reader, layerHeight, exposureTime, liftSpeed, liftHeight);
    }

    @Override
    public final void write(OutputStream stream) throws IOException {
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
