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
import futurelink.msla.formats.io.FileFieldsException;
import futurelink.msla.utils.Size;
import lombok.Getter;

/**
 * Main class that allows to work with Anycubic mSLA file formats.
 * Currently only PW0 encoding is supported, so ancient printers are out of the list.
 */
public class PhotonWorkshopFile extends MSLAFileGeneric<byte[]> {
    @Getter private Class<? extends PhotonWorkshopCodec> codec;
    private DataInputStream iStream;

    private PhotonWorkshopFileDescriptor Descriptor;
    @Getter @MSLAOptionContainer private PhotonWorkshopFileHeaderTable Header;
    private PhotonWorkshopFilePreview1Table Preview;
    @Getter private PhotonWorkshopFileLayerDefTable Layers;
    private PhotonWorkshopFileSoftwareTable Software;
    @Getter @MSLAOptionContainer private PhotonWorkshopFileExtraTable Extra;
    @Getter @MSLAOptionContainer private PhotonWorkshopFileMachineTable Machine;

    public PhotonWorkshopFile(byte versionMajor, byte versionMinor) {
        super();
        Descriptor = new PhotonWorkshopFileDescriptor(versionMajor, versionMinor);
        Header = new PhotonWorkshopFileHeaderTable(versionMajor, versionMinor);
        Machine = new PhotonWorkshopFileMachineTable(versionMajor, versionMinor);
        Preview = new PhotonWorkshopFilePreview1Table(versionMajor, versionMinor);
        Software = new PhotonWorkshopFileSoftwareTable(versionMajor, versionMinor);
        Layers = new PhotonWorkshopFileLayerDefTable(versionMajor, versionMinor);
        if ((versionMajor >= 2) && (versionMinor >= 4)) {
            Extra = new PhotonWorkshopFileExtraTable(versionMajor, versionMinor);
        }
    }

    public PhotonWorkshopFile(DataInputStream stream) throws IOException, MSLAException {
        super();
        iStream = stream;
        readTables(iStream);

        // Get layers definition table. This requires defaults to be read out if available.
        if (Descriptor.getFields().getLayerDefinitionAddress() > 0) {
            Layers = new PhotonWorkshopFileLayerDefTable(
                    Descriptor.getVersionMajor(),
                    Descriptor.getVersionMinor());
            Layers.read(stream, Descriptor.getFields().getLayerDefinitionAddress());
        } else throw new MSLAException("No layer definition section found!");
    }

    @Override public String getMachineName() { return Header.getName(); }
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
        var imageFormat = Machine.getLayerImageFormat();
        if ("pw0Img".equals(imageFormat)) {
            codec = PhotonWorkshopCodecPW0.class;
        } else if ("pwsImg".equals(imageFormat)) {
            codec = PhotonWorkshopCodecPWS.class;
        } else {
            throw new MSLAException("Codec not implemented for image format '" + imageFormat + "'");
        }
    }

    private void readTables(DataInputStream stream) throws IOException, MSLAException {
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
            Preview = new PhotonWorkshopFilePreview1Table(Descriptor.getVersionMajor(), Descriptor.getVersionMinor());
            Preview.read(stream, Descriptor.getFields().getPreviewAddress());
        }

        if (Descriptor.getFields().getExtraAddress() > 0) {
            Extra = new PhotonWorkshopFileExtraTable(Descriptor.getVersionMajor(), Descriptor.getVersionMinor());
            Extra.read(stream, Descriptor.getFields().getExtraAddress());
        }

        if (Descriptor.getFields().getMachineAddress() > 0) {
            Machine = new PhotonWorkshopFileMachineTable(Descriptor.getVersionMajor(), Descriptor.getVersionMinor());
            Machine.read(stream, Descriptor.getFields().getMachineAddress());
        }

        initCodec(); // Codec must be configured after tables were read out
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
            iStream.reset();
            iStream.skipBytes(address);
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
    public void reset(MSLAFileDefaults defaults) throws MSLAException {
        super.reset(defaults);
        if (isMachineValid(defaults)) {
            defaults.setFields(Header.getName(), Header.getFileFields());
            defaults.setFields(Machine.getName(), Machine.getFileFields());
            getLayers().setDefaults(defaults.getLayerDefaults());
            initCodec(); // Codec must be configured after setting defaults
        } else throw new MSLAException("Defaults of '" + defaults.getMachineFullName() + "' not applicable to this file");
    }

    @Override
    public boolean isMachineValid(MSLAFileDefaults defaults) {
        return defaults.getFileClass().equals(this.getClass()) &&
                ((getResolution() == null) || defaults.getResolution().equals(getResolution()));
    }

    @Override public MSLAPreview getLargePreview() { return Preview; }

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
