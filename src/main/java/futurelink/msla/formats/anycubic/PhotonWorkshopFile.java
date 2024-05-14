package futurelink.msla.formats.anycubic;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;

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
    @Getter private PhotonWorkshopFileLayerDefTable Layers;
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
        Layers = new PhotonWorkshopFileLayerDefTable(VersionMajor, VersionMinor);
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
    @Override public boolean isValid() {
        return (Header != null) && (Layers != null);
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
            Layers = new PhotonWorkshopFileLayerDefTable(Descriptor.getVersionMajor(), Descriptor.getVersionMinor());
            Layers.read(stream, Descriptor.getFields().getLayerDefinitionAddress());
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
        if (Layers == null) throw new MSLAException("LayerDef does not exist in a file");
        if (Header == null) throw new MSLAException("Header was not defined");

        if (layer > Layers.count()) throw new MSLAException("Layer is out of range");
        var address = Layers.get(layer).getDataAddress();
        var length = Layers.get(layer).getDataLength();
        if ((address == 0) || (length == 0)) throw new MSLAException("Invalid layer data in Layer definition table");

        // Go to data position
        try {
            var fc = iStream.getChannel();
            fc.position(address);
        } catch (IOException e) {
            throw new MSLAException("Can't go to image data position", e);
        }

        // Decode layer using codec
        return Layers.decodeLayer(layer, new DataInputStream(iStream),
                Header.getResolution().length(), getDecodersPool(), writer);
    }

    @Override public MSLAPreview getPreview(int index) { return Preview; }
    @Override public void setPreview(int index, BufferedImage image) { Preview.setImage(image); }

    @Override
    public final void addLayer(
            MSLALayerEncodeReader reader,
            MSLALayerEncoder.Callback<byte[]> callback) throws MSLAException
    {
        if ((Layers == null) || (Header == null)) throw new MSLAException("Ho LayerDef or Header created");
        Layers.add(getEncodersPool(), reader, new HashMap<>(), callback);
    }

    @Override
    public final void write(OutputStream stream) throws MSLAException {
        if (Header == null) throw new MSLAException("Header is empty, a file cannot be written");
        if (Layers == null) throw new MSLAException("LayerDef is empty, a file cannot be written");

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
            offset += Layers.getDataLength();

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
        for (int i = 0; i < Layers.count(); i++) {
            Layers.get(i).setDataAddress(offset);
            offset += Layers.get(i).getDataLength();
        }

        // Write descriptor
        Descriptor.write(oStream, Descriptor.getVersionMajor(), Descriptor.getVersionMinor());

        /*
         * Write data in the same order we prepared offsets
         */
        Header.write(oStream);
        if (Software != null) Software.write(oStream);
        if (Preview != null) Preview.write(oStream);
        Layers.write(oStream);
        if (Extra != null) Extra.write(oStream);
        if (Machine != null) Machine.write(oStream);

        /*
         * Write layers
         */
        try {
            for (int i = 0; i < Layers.count(); i++) {
                oStream.write(Layers.getLayerData(i));
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
                "LayerDef:\n" + Layers +
                "Software:\n" + Software +
                "Extra:\n" + Extra +
                "Machine:\n" + Machine;
    }
}
