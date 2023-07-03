package futurelink.msla.formats.anycubic;

import java.io.*;

import com.google.common.io.LittleEndianDataInputStream;
import com.google.common.io.LittleEndianDataOutputStream;
import futurelink.msla.formats.*;
import futurelink.msla.formats.anycubic.tables.*;
import futurelink.msla.formats.utils.Size;
import lombok.Getter;

/**
 * Main class that allows to work with Anycubic mSLA file formats.
 * Currently only PW0 encoding is supported, so ancient printers are out of the list.
 */
public class PhotonWorkshopFile implements MSLAFile {
    private FileInputStream iStream;
    @Getter private MSLAFileCodec codec;
    private PhotonWorkshopFileDescriptor descriptor;
    PhotonWorkshopFileHeaderTable header;
    private PhotonWorkshopFilePreviewTable preview;
    private PhotonWorkshopFileLayerDefTable layerDef;
    PhotonWorkshopFileSoftwareTable software;
    PhotonWorkshopFileExtraTable extra;
    private PhotonWorkshopFileMachineTable machine;
    private final MSLAOptionMapper optionMapper;

    public PhotonWorkshopFile(MSLAFileDefaults defaults) throws IOException {
        var VersionMajor = defaults.getOptionByte("VersionMajor");
        var VersionMinor = defaults.getOptionByte("VersionMinor");
        var Header = defaults.getOptionBlock("Header");
        var Machine = defaults.getOptionBlock("Machine");
        descriptor = new PhotonWorkshopFileDescriptor(VersionMajor, VersionMinor);
        header = new PhotonWorkshopFileHeaderTable(Header, VersionMajor, VersionMinor);
        machine = new PhotonWorkshopFileMachineTable(Machine, VersionMajor, VersionMinor);
        preview = new PhotonWorkshopFilePreviewTable(VersionMajor, VersionMinor);
        software = new PhotonWorkshopFileSoftwareTable(VersionMajor, VersionMinor);
        layerDef = new PhotonWorkshopFileLayerDefTable(VersionMajor, VersionMinor);
        if ((VersionMajor >= 2) && (VersionMinor >= 4)) {
            extra = new PhotonWorkshopFileExtraTable(VersionMajor, VersionMinor);
        }
        optionMapper = new PhotonWorkshopFileOptionMapper(this);
        updatePreviewImage();
        initCodec();
    }

    public PhotonWorkshopFile(FileInputStream stream) throws IOException {
        this.iStream = stream;
        this.readTables(iStream);
        this.optionMapper = new PhotonWorkshopFileOptionMapper(this);
        initCodec();
    }

    @Override
    public Size getResolution() {
        return header.getResolution();
    }

    @Override
    public float getPixelSizeUm() {
        return header.getPixelSizeUm();
    }

    @Override
    public int getLayerCount() {
        return layerDef.getLayerCount();
    }

    @Override
    public boolean isValid() {
        return (header != null) && (layerDef != null);
    }

    @Override
    public void setOption(String option, Serializable value) throws IOException {
        optionMapper.setOption(option, value);
    }

    @Override
    public float getDPI() {
        if (header == null) return 0.0f;
        return 1 / (header.getPixelSizeUm() / 25400);
    }

    private boolean hasOption(String option, Class<?> type) {
        return false;
    }

    private void initCodec() throws IOException {
        if (machine == null) throw new IOException("Machine section was not initialized properly");
        var format = machine.getLayerImageFormat();
        if (format.equals("pw0Img")) {
            codec = new PhotonWorkshopCodecPW0();
        } else if (format.equals("pwsImg")) {
            codec = new PhotonWorkshopCodecPWS();
        } else {
            throw new IOException("Codec not implemented for '" + format + "'");
        }
    }

    private void readTables(FileInputStream stream) throws IOException {
        descriptor = PhotonWorkshopFileDescriptor.read(new LittleEndianDataInputStream(iStream));

        if (descriptor.getFields().getHeaderAddress() > 0) {
            header = new PhotonWorkshopFileHeaderTable(descriptor.getVersionMajor(), descriptor.getVersionMinor());
            header.read(stream, descriptor.getFields().getHeaderAddress());
        } else throw new IOException("No HEADER section found!");

        if (descriptor.getFields().getSoftwareAddress() > 0) {
            software = new PhotonWorkshopFileSoftwareTable(descriptor.getVersionMajor(), descriptor.getVersionMinor());
            software.read(stream, descriptor.getFields().getSoftwareAddress());
        }

        if (descriptor.getFields().getPreviewAddress() > 0) {
            preview = new PhotonWorkshopFilePreviewTable(descriptor.getVersionMajor(), descriptor.getVersionMinor());
            preview.read(stream, descriptor.getFields().getPreviewAddress());
        }

        if (descriptor.getFields().getLayerDefinitionAddress() > 0) {
            layerDef = new PhotonWorkshopFileLayerDefTable(descriptor.getVersionMajor(), descriptor.getVersionMinor());
            layerDef.read(stream, descriptor.getFields().getLayerDefinitionAddress());
        } else throw new IOException("No LAYERDEF section found!");

        if (descriptor.getFields().getExtraAddress() > 0) {
            extra = new PhotonWorkshopFileExtraTable(descriptor.getVersionMajor(), descriptor.getVersionMinor());
            extra.read(stream, descriptor.getFields().getExtraAddress());
        }

        if (descriptor.getFields().getMachineAddress() > 0) {
            machine = new PhotonWorkshopFileMachineTable(descriptor.getVersionMajor(), descriptor.getVersionMinor());
            machine.read(stream, descriptor.getFields().getMachineAddress());
        }
    }

    @Override
    public final void readLayer(int layer, MSLADecodeWriter writer) throws IOException {
        if (layerDef == null) throw new IOException("LayerDef does not exist");
        if (layer > layerDef.getLayerCount()) throw new IOException("Layer is out of range");
        var address = layerDef.getLayer(layer).DataAddress;
        var length = layerDef.getLayer(layer).DataLength;
        if ((address == 0) || (length == 0)) throw new IOException("Invalid layer data in Layer definition table");

        // Go to data position
        var fc = iStream.getChannel();
        fc.position(address);

        if (header == null) throw new IOException("Header was not defined!");

        layerDef.decodeLayer(new DataInputStream(iStream), layer, header.getResolution().length(), writer);
    }

    @Override
    public MSLAPreview getPreview() {
        return preview;
    }

    @Override
    public final void updatePreviewImage() throws IOException {
        if (getPreview() == null) return;
        preview.updateImageData();
    }

    @Override
    public final void addLayer(MSLAEncodeReader reader,
                               float layerHeight, float exposureTime,
                               float liftSpeed, float liftHeight) throws IOException  {
        if (layerDef == null) throw new IOException("LayerDef table does not exist!");

        var layer = new PhotonWorkshopFileLayerDefTable.PhotonWorkshopFileLayerDef();
        layer.LayerHeight = layerHeight;
        layer.ExposureTime = exposureTime;
        layer.LiftSpeed = liftSpeed;
        layer.LiftHeight = liftHeight;

        // Encode a layer data and calculate offsets
        layer.DataAddress = 0;
        layer.NonZeroPixelCount = 0;

        layerDef.encodeLayer(layer, reader);
    }

    @Override
    public final void addLayer(MSLAEncodeReader reader) throws IOException {
        if ((layerDef == null) || (header == null)) throw new IOException("Ho LayerDef or Header created");

        var layerNumber = layerDef.getLayerCount() + 1;
        float layerHeight, exposureTime, liftSpeed, liftHeight;
        if (layerNumber < header.getBottomLayersCount()) {
            layerHeight = header.getLayerHeight();
            exposureTime = header.getBottomExposureTime();
            liftSpeed = header.getLiftSpeed();
            liftHeight = header.getLiftHeight();
        } else {
            layerHeight = header.getLayerHeight();
            exposureTime = header.getExposureTime();
            liftSpeed = header.getLiftSpeed();
            liftHeight = header.getLiftHeight();
        }
        addLayer(reader, layerHeight, exposureTime, liftSpeed, liftHeight);
    }

    @Override
    public final void write(OutputStream stream) throws IOException {
        if (header == null) throw new IOException("Header is empty, a file cannot be written");
        if (layerDef == null) throw new IOException("LayerDef is empty, a file cannot be written");

        var oStream = new LittleEndianDataOutputStream(stream);

        /*
         * Prepare data offsets for descriptor
         * (order is important!)
         */
        var offset = descriptor.calculateDataLength();
        descriptor.getFields().setHeaderAddress(offset);
        offset += header.getDataLength();

        if (getPreview() != null) {
            descriptor.getFields().setPreviewAddress(offset);
            offset += 16; // 16 bytes of color data is not counted in preview table length
            offset += preview.getDataLength();
            // Color table is not a standalone table and has no header mark, it's a part of a preview
            descriptor.getFields().setLayerImageColorTableAddress(offset - 28);
        }

        descriptor.getFields().setLayerDefinitionAddress(offset);
        offset += layerDef.getDataLength();

        if (extra != null) {
            descriptor.getFields().setExtraAddress(offset);
            offset += extra.getDataLength();
        }

        if (machine != null) {
            descriptor.getFields().setMachineAddress(offset);
            offset += machine.getDataLength();
        }

        descriptor.getFields().setLayerImageAddress(offset);

        /*
         * Calculate layer data offsets
         */
        for (int i = 0; i < layerDef.getLayerCount(); i++) {
            layerDef.getLayer(i).DataAddress = offset;
            offset += layerDef.getLayer(i).DataLength;
        }

        // Write descriptor
        descriptor.write(oStream, descriptor.getVersionMajor(), descriptor.getVersionMinor());

        /*
         * Write data in the same order we prepared offsets
         */
        header.write(oStream);
        if (software != null) software.write(oStream);
        if (preview != null) preview.write(oStream);
        layerDef.write(oStream);
        if (extra != null) extra.write(oStream);
        if (machine != null) machine.write(oStream);

        /*
         * Write layers
         */
        for (int i = 0; i < layerDef.getLayerCount(); i++) {
            oStream.write(layerDef.getLayerData(i));
        }
    }

    @Override
    public String toString() {
        return "Codec: " + codec +
                "Descriptor: " + descriptor +
                "Header: " + header +
                "Preview: " + preview +
                "LayerDef: " + layerDef +
                "Software: " + software +
                "Extra: " + extra +
                "Machine" + machine;
    }
}
