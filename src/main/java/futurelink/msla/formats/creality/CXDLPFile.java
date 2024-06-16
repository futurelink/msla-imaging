package futurelink.msla.formats.creality;

import futurelink.msla.formats.*;
import futurelink.msla.formats.creality.tables.*;
import futurelink.msla.formats.iface.*;
import futurelink.msla.formats.iface.options.MSLAOptionContainer;
import futurelink.msla.utils.Size;
import lombok.Getter;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;
import java.util.logging.Logger;

public class CXDLPFile extends MSLAFileGeneric<List<CXDLPFileLayerLine>> {
    private static final Logger logger = Logger.getLogger(CXDLPFile.class.getName());
    private final DataInputStream iStream;

    @Getter @MSLAOptionContainer private final CXDLPFileHeader Header;
    @Getter @MSLAOptionContainer private final CXDLPFileSliceInfo SliceInfo;
    @Getter @MSLAOptionContainer private final CXDLPFileSliceInfoV3 SliceInfoV3;
    private final CXDLPFilePreviews Previews = new CXDLPFilePreviews();
    @Getter private final CXDLPFileLayerDef Layers = new CXDLPFileLayerDef();

    public CXDLPFile(MSLAFileProps initialProps) throws MSLAException {
        super(initialProps);
        iStream = null;
        Header = new CXDLPFileHeader(initialProps);
        SliceInfo = new CXDLPFileSliceInfo(initialProps);
        SliceInfoV3 = new CXDLPFileSliceInfoV3();
    }

    public CXDLPFile(DataInputStream stream) throws MSLAException {
        super(null);
        var position = 0;
        try {
            stream.reset();
            iStream = stream;
            Header = new CXDLPFileHeader(null);
            SliceInfo = new CXDLPFileSliceInfo(null);
            SliceInfoV3 = new CXDLPFileSliceInfoV3();
            Header.read(iStream, position); position += Header.getDataLength();
            Previews.read(iStream, position); position += Previews.getDataLength();
            SliceInfo.read(iStream, position); position += SliceInfo.getDataLength();

            // Skip layer areas (don't know what's their purpose)
            try { iStream.skipNBytes(Header.getLayerCount() * 4 + 2); }
            catch (IOException e) { throw new MSLAException("Can't read file", e); }
            position += Header.getLayerCount() * 4 + 2;
            if (Header.getVersion() >= 3) SliceInfoV3.read(iStream, position); position += SliceInfoV3.getDataLength();

            // Scan layer data and get layer data lengths and offsets
            // ------------------------------------------------------
            for (int i = 0; i < Header.getLayerCount(); i++) Layers.allocate();
            Layers.read(iStream, position);
        } catch (IOException e) {
            throw new MSLAException("Can't read CXDLP data", e);
        }
    }

    @Override
    public Class<? extends MSLALayerCodec<List<CXDLPFileLayerLine>>> getCodec() {
        return CXDLPLayerCodec.class;
    }

    @Override public String getMachineName() {
        return switch (getHeader().getPrinterModel()) {
            case "CL-60" -> "CREALITY HALOT-ONE";
            case "CL-70" -> "CREALITY HALOT-ONE PRO";
            case "CL-79" -> "CREALITY HALOT-ONE PLUS";
            case "CL925" -> "CREALITY HALOT-RAY";
            default -> null;
        };
    }
    @Override public Short getPreviewsNumber() { return Previews.getPreviewsNumber(); }
    @Override public MSLAPreview getPreview(int index) throws MSLAException { return Previews.getPreview(index); }
    @Override public MSLAPreview getLargePreview() throws MSLAException { return getPreview(1); }

    @Override public void setPreview(int index, BufferedImage image) throws MSLAException {
        Previews.getPreview(index).setImage(image);
    }

    @Override
    public void reset(MSLAFileDefaults defaults) throws MSLAException {
        super.reset(defaults);
        defaults.setFields(Header.getBlockFields());
        defaults.setFields(SliceInfo.getBlockFields());
        defaults.setFields(SliceInfoV3.getBlockFields());
        getLayers().setDefaults(defaults.getLayerDefaults());
    }

    @Override public float getDPI() { return 0; }

    @Override
    public boolean isMachineValid(MSLAFileDefaults defaults) {
        try {
            return defaults.getFileClass().equals(this.getClass()) &&
                    ((getResolution() == null) || defaults.getResolution().equals(getResolution()));
        } catch (MSLAException e) {
            return false;
        }
    }

    @Override
    public void addLayer(MSLALayerEncodeReader reader,
                         MSLALayerEncoder.Callback<List<CXDLPFileLayerLine>> callback)
            throws MSLAException
    {
        Layers.add(getEncodersPool(), reader, null, callback);
    }

    public final CXDLPFileLayer getLayer(int index) { return Layers.get(index); }

    @Override
    public boolean readLayer(MSLALayerDecodeWriter writer, int layer) throws MSLAException {
        logger.finest("Reading layer " + layer + "...");
        return getDecodersPool().decode(layer, writer, new CXDLPLayerCodec.Input(Layers.get(layer)), null);
    }

    @Override
    public void write(OutputStream stream) throws MSLAException {
        if (Header == null) throw new MSLAException("Header is empty, a file cannot be written");
        if (SliceInfo == null) throw new MSLAException("SliceInfo is empty, a file cannot be written");
        Header.setLayerCount((short) Layers.count());
        Header.write(stream);
        Previews.write(stream);
        SliceInfo.write(stream);
        Layers.writeLayerAreas(stream);
        if (Header.getVersion() >= 3) SliceInfoV3.write(stream);
        Layers.write(stream);
    }

    @Override public Size getResolution() { return Header.getResolution(); }
    @Override public boolean isValid() { return (Header != null) && (SliceInfo != null); }

    @Override public String toString() {
        return "---- Header ----\n" + Header.toString() + "\n" +
                "---- SliceInfo ----\n" + SliceInfo + "\n" +
                "---- SliceInfoV3 ----\n" + SliceInfoV3 + "\n" +
                "---- Previews ----\n" + Previews;
    }
}
