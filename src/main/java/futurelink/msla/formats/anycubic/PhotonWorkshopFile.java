package futurelink.msla.formats.anycubic;

import java.awt.image.BufferedImage;
import java.io.*;

import com.google.common.io.LittleEndianDataInputStream;
import com.google.common.io.LittleEndianDataOutputStream;
import futurelink.msla.formats.*;
import futurelink.msla.formats.anycubic.tables.*;
import futurelink.msla.formats.iface.*;
import futurelink.msla.formats.iface.annotations.MSLAOptionContainer;
import futurelink.msla.formats.utils.FileFieldsException;
import futurelink.msla.formats.utils.OptionMapper;
import futurelink.msla.formats.utils.Size;
import lombok.Getter;

/**
 * Main class that allows to work with Anycubic mSLA file formats.
 * Currently only PW0 encoding is supported, so ancient printers are out of the list.
 */
public class PhotonWorkshopFile extends MSLAFileGeneric<byte[]> {
    @Getter private Class<? extends PhotonWorkshopCodec> codec;
    private FileInputStream iStream;
    @Getter private final MSLAOptionMapper options;

    private PhotonWorkshopFileDescriptor Descriptor;
    @Getter @MSLAOptionContainer private PhotonWorkshopFileHeaderTable Header;
    private PhotonWorkshopFilePreviewTable Preview;
    private PhotonWorkshopFileLayerDefTable LayerDef;
    private PhotonWorkshopFileSoftwareTable Software;
    @Getter @MSLAOptionContainer private PhotonWorkshopFileExtraTable Extra;
    @Getter @MSLAOptionContainer private PhotonWorkshopFileMachineTable Machine;

    public PhotonWorkshopFile(MSLAFileDefaults defaults) throws MSLAException {
        super();
        var VersionMajor = defaults.getOptionByte(null, "VersionMajor");
        var VersionMinor = defaults.getOptionByte(null, "VersionMinor");
        Descriptor = new PhotonWorkshopFileDescriptor(VersionMajor, VersionMinor);
        Header = new PhotonWorkshopFileHeaderTable(defaults, VersionMajor, VersionMinor);
        Machine = new PhotonWorkshopFileMachineTable(defaults, VersionMajor, VersionMinor);
        Preview = new PhotonWorkshopFilePreviewTable(VersionMajor, VersionMinor);
        Software = new PhotonWorkshopFileSoftwareTable(VersionMajor, VersionMinor);
        LayerDef = new PhotonWorkshopFileLayerDefTable(VersionMajor, VersionMinor);
        if ((VersionMajor >= 2) && (VersionMinor >= 4)) {
            Extra = new PhotonWorkshopFileExtraTable(defaults, VersionMajor, VersionMinor);
        }
        options = new OptionMapper(this);
        initCodec();
    }

    public PhotonWorkshopFile(FileInputStream stream) throws IOException, MSLAException {
        super();
        iStream = stream;
        readTables(iStream);
        options = new OptionMapper(this);
        initCodec();
    }

    @Override public Size getResolution() { return Header.getResolution(); }
    @Override public float getPixelSizeUm() {
        return Header.getPixelSizeUm();
    }
    @Override public int getLayerCount() {
        return LayerDef.getLayerCount();
    }
    @Override public boolean isValid() {
        return (Header != null) && (LayerDef != null);
    }

    @Override
    public float getDPI() {
        if (Header == null) return 0.0f;
        return 1 / (Header.getPixelSizeUm() / 25400);
    }

    private void initCodec() throws MSLAException {
        if (Machine == null) throw new MSLAException("Machine section was not initialized properly");
        var format = Machine.getLayerImageFormat();
        if (format.equals("pw0Img")) {
            codec = PhotonWorkshopCodecPW0.class;
        } else if (format.equals("pwsImg")) {
            codec = PhotonWorkshopCodecPWS.class;
        } else {
            throw new MSLAException("Codec not implemented for '" + format + "'");
        }
    }

    private void readTables(FileInputStream stream) throws IOException, MSLAException {
        Descriptor = PhotonWorkshopFileDescriptor.read(new LittleEndianDataInputStream(iStream));

        if (Descriptor.getFields().getHeaderAddress() > 0) {
            Header = new PhotonWorkshopFileHeaderTable(Descriptor.getVersionMajor(), Descriptor.getVersionMinor());
            Header.read(stream, Descriptor.getFields().getHeaderAddress());
        } else throw new MSLAException("No HEADER section found!");

        if (Descriptor.getFields().getSoftwareAddress() > 0) {
            Software = new PhotonWorkshopFileSoftwareTable(Descriptor.getVersionMajor(), Descriptor.getVersionMinor());
            Software.read(stream, Descriptor.getFields().getSoftwareAddress());
        }

        if (Descriptor.getFields().getPreviewAddress() > 0) {
            Preview = new PhotonWorkshopFilePreviewTable(Descriptor.getVersionMajor(), Descriptor.getVersionMinor());
            Preview.read(stream, Descriptor.getFields().getPreviewAddress());
        }

        if (Descriptor.getFields().getLayerDefinitionAddress() > 0) {
            LayerDef = new PhotonWorkshopFileLayerDefTable(Descriptor.getVersionMajor(), Descriptor.getVersionMinor());
            LayerDef.read(stream, Descriptor.getFields().getLayerDefinitionAddress());
        } else throw new MSLAException("No layer definition section found!");

        if (Descriptor.getFields().getExtraAddress() > 0) {
            Extra = new PhotonWorkshopFileExtraTable(Descriptor.getVersionMajor(), Descriptor.getVersionMinor());
            Extra.read(stream, Descriptor.getFields().getExtraAddress());
        }

        if (Descriptor.getFields().getMachineAddress() > 0) {
            Machine = new PhotonWorkshopFileMachineTable(Descriptor.getVersionMajor(), Descriptor.getVersionMinor());
            Machine.read(stream, Descriptor.getFields().getMachineAddress());
        }
    }

    @Override
    public final boolean readLayer(MSLALayerDecodeWriter writer, int layer) throws MSLAException {
        if (writer == null) throw new MSLAException("Writer is not specified");
        if (LayerDef == null) throw new MSLAException("LayerDef does not exist in a file");
        if (Header == null) throw new MSLAException("Header was not defined");

        if (layer > LayerDef.getLayerCount()) throw new MSLAException("Layer is out of range");
        var address = LayerDef.getLayer(layer).getDataAddress();
        var length = LayerDef.getLayer(layer).getDataLength();
        if ((address == 0) || (length == 0)) throw new MSLAException("Invalid layer data in Layer definition table");

        // Go to data position
        try {
            var fc = iStream.getChannel();
            fc.position(address);
        } catch (IOException e) {
            throw new MSLAException("Can't go to image data position", e);
        }

        // Decode layer using codec
        return LayerDef.decodeLayer(layer, new DataInputStream(iStream),
                Header.getResolution().length(), getDecodersPool(), writer);
    }

    @Override public MSLAPreview getPreview(int index) { return Preview; }
    @Override public void setPreview(int index, BufferedImage image) { Preview.setImage(image); }

    @Override
    public final void addLayer(
            MSLALayerEncodeReader reader,
            MSLALayerEncoder.Callback<byte[]> callback,
            float layerHeight, float exposureTime, float liftSpeed, float liftHeight) throws MSLAException
    {
        if (LayerDef == null) throw new MSLAException("LayerDef table does not exist!");

        var layer = new PhotonWorkshopFileLayerDefTable.PhotonWorkshopFileLayerDef();
        layer.setLayerHeight(layerHeight);
        layer.setExposureTime(exposureTime);
        layer.setLiftSpeed(liftSpeed);
        layer.setLiftHeight(liftHeight);

        LayerDef.encodeLayer(layer, reader, getEncodersPool(), callback);
    }

    @Override
    public final void addLayer(
            MSLALayerEncodeReader reader,
            MSLALayerEncoder.Callback<byte[]> callback) throws MSLAException
    {
        if ((LayerDef == null) || (Header == null)) throw new MSLAException("Ho LayerDef or Header created");

        var layerNumber = LayerDef.getLayerCount() + 1;
        float layerHeight, exposureTime, liftSpeed, liftHeight;
        if (layerNumber < Header.getBottomLayersCount()) {
            layerHeight = Header.getLayerHeight();
            exposureTime = Header.getBottomExposureTime();
            liftSpeed = Header.getLiftSpeed();
            liftHeight = Header.getLiftHeight();
        } else {
            layerHeight = Header.getLayerHeight();
            exposureTime = Header.getExposureTime();
            liftSpeed = Header.getLiftSpeed();
            liftHeight = Header.getLiftHeight();
        }

        addLayer(reader, callback, layerHeight, exposureTime, liftSpeed, liftHeight);
    }

    @Override
    public final void write(OutputStream stream) throws MSLAException {
        if (Header == null) throw new MSLAException("Header is empty, a file cannot be written");
        if (LayerDef == null) throw new MSLAException("LayerDef is empty, a file cannot be written");

        var oStream = new LittleEndianDataOutputStream(stream);

        /*
         * Prepare data offsets for descriptor
         * (order is important!)
         */
        var offset = Descriptor.calculateDataLength();
        try {
            Descriptor.getFields().setHeaderAddress(offset);
            offset += Header.getDataLength();

            if (Software != null) {
                Descriptor.getFields().setSoftwareAddress(offset);
                offset += Software.getDataLength();
            }

            if (getPreview((short) 0) != null) {
                Descriptor.getFields().setPreviewAddress(offset);
                offset += Preview.getDataLength();
                // Color table is not a standalone table and has no header mark, it's a part of a preview
                Descriptor.getFields().setLayerImageColorTableAddress(offset - 28);
            }

            Descriptor.getFields().setLayerDefinitionAddress(offset);
            offset += LayerDef.getDataLength();

            if (Extra != null) {
                Descriptor.getFields().setExtraAddress(offset);
                offset += Extra.getDataLength();
            }

            if (Machine != null) {
                Descriptor.getFields().setMachineAddress(offset);
                offset += Machine.getDataLength();
            }
        } catch (FileFieldsException ex) {
            throw new MSLAException("Error determining file section length", ex);
        }

        Descriptor.getFields().setLayerImageAddress(offset);

        /*
         * Calculate layer data offsets
         */
        for (int i = 0; i < LayerDef.getLayerCount(); i++) {
            LayerDef.getLayer(i).setDataAddress(offset);
            offset += LayerDef.getLayer(i).getDataLength();
        }

        // Write descriptor
        Descriptor.write(oStream, Descriptor.getVersionMajor(), Descriptor.getVersionMinor());

        /*
         * Write data in the same order we prepared offsets
         */
        Header.write(oStream);
        if (Software != null) Software.write(oStream);
        if (Preview != null) Preview.write(oStream);
        LayerDef.write(oStream);
        if (Extra != null) Extra.write(oStream);
        if (Machine != null) Machine.write(oStream);

        /*
         * Write layers
         */
        try {
            for (int i = 0; i < LayerDef.getLayerCount(); i++) {
                oStream.write(LayerDef.getLayerData(i));
            }
        } catch (IOException e) {
            throw new MSLAException("Can't write layer data", e);
        }
    }

    @Override
    public String toString() {
        return "Codec: " + codec + "\n" +
                "Descriptor:\n" + Descriptor +
                "Header:\n" + Header +
                "Preview:\n" + Preview +
                "LayerDef:\n" + LayerDef +
                "Software:\n" + Software +
                "Extra:\n" + Extra +
                "Machine:\n" + Machine;
    }
}
