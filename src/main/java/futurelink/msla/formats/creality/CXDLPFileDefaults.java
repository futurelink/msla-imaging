package futurelink.msla.formats.creality;

import futurelink.msla.formats.iface.MSLAFileBlockFields;
import futurelink.msla.formats.iface.MSLAFileDefaults;
import futurelink.msla.formats.creality.tables.CXDLPFileHeader;
import futurelink.msla.formats.creality.tables.CXDLPFileSliceInfo;
import futurelink.msla.formats.creality.tables.CXDLPFileSliceInfoV3;
import futurelink.msla.formats.utils.Size;
import lombok.Getter;

import java.util.HashMap;
import java.util.Set;

public class CXDLPFileDefaults {
    private static class Values implements MSLAFileDefaults {
        @Getter private String Name;
        @Getter private String FileExtension;
        @Getter private String PrinterModel;
        @Getter CXDLPFileSliceInfo.Fields SliceInfo;
        @Getter CXDLPFileSliceInfoV3.Fields SliceInfoV3;
        @Getter Size Resolution;
        @Getter float PixelSizeUm;

        Values(String name, String fileExtension, float pixelSizeUm, Size resolution, String printerModel) {
            Name = name;
            PrinterModel = printerModel;
            FileExtension = fileExtension;
            SliceInfo = new CXDLPFileSliceInfo.Fields();
            SliceInfoV3 = new CXDLPFileSliceInfoV3.Fields();
            Resolution = new Size(resolution);
            PixelSizeUm = pixelSizeUm;
        }
        @Override public Integer getOptionInt(String name) { return null; }
        @Override public Byte getOptionByte(String name) { return null; }
        @Override public Short getOptionShort(String name) { return null; }
        @Override public String getOptionString(String name) { return null; }
        @Override public MSLAFileBlockFields getOptionBlock(String name) {
            if ("SliceInfo".equals(name)) return SliceInfo;
            if ("SliceInfoV3".equals(name)) return SliceInfoV3;
            if ("Header".equals(name)) return new CXDLPFileHeader.Fields(Resolution, PrinterModel);
            return null;
        }
    }

    public static Set<String> getSupported() { return Settings.keySet(); }
    public static Values get(String machineName) {
        return Settings.get(machineName);
    }
    private static final HashMap<String, Values> Settings = new HashMap<>();

    static {
        var values = new Values("CREALITY HALOT-ONE PLUS", "cxdlp",
                40.0f, new Size(4320,2560), "CL-79");
        values.SliceInfo.setDisplayWidth("172.8");
        values.SliceInfo.setDisplayHeight("102.4");
        values.SliceInfo.setLayerHeight("0.05");
        values.SliceInfo.setExposureTime((short) 30);
        values.SliceInfo.setWaitTimeBeforeCure((short) 4);
        values.SliceInfo.setBottomExposureTime((short) 40);
        values.SliceInfo.setBottomLayersCount((short) 2);
        values.SliceInfo.setBottomLiftHeight((short) 6);
        values.SliceInfo.setBottomLiftSpeed((short) 1);
        values.SliceInfo.setLiftHeight((short) 6);
        values.SliceInfo.setLiftSpeed((short) 1);
        values.SliceInfo.setRetractSpeed((short) 1);
        values.SliceInfoV3.setDistortionCompensationEnabled((byte) 0);
        values.SliceInfoV3.setDistortionCompensationThickness(0);
        values.SliceInfoV3.setDistortionCompensationFocalLength(0);
        values.SliceInfoV3.setXYAxisProfileCompensationEnabled((byte) 0x0);
        values.SliceInfoV3.setXYAxisProfileCompensationValue((short) 0);
        values.SliceInfoV3.setZPenetrationCompensationEnabled((byte) 0);
        values.SliceInfoV3.setZPenetrationCompensationLevel((short) 0);
        values.SliceInfoV3.setAntiAliasEnabled((byte) 0);
        Settings.put(values.getName(), values);

        values = new Values("CREALITY HALOT-ONE PRO", "cxdlp",
                51.0f, new Size(2560,2400), "CL-70");
        values.SliceInfo.setDisplayWidth("130.56");
        values.SliceInfo.setDisplayHeight("122.4");
        values.SliceInfo.setLayerHeight("0.05");
        values.SliceInfo.setExposureTime((short) 30);
        values.SliceInfo.setWaitTimeBeforeCure((short) 4);
        values.SliceInfo.setBottomExposureTime((short) 40);
        values.SliceInfo.setBottomLayersCount((short) 2);
        values.SliceInfo.setBottomLiftHeight((short) 6);
        values.SliceInfo.setBottomLiftSpeed((short) 1);
        values.SliceInfo.setLiftHeight((short) 6);
        values.SliceInfo.setLiftSpeed((short) 1);
        values.SliceInfo.setRetractSpeed((short) 1);
        values.SliceInfoV3.setDistortionCompensationEnabled((byte) 0);
        values.SliceInfoV3.setDistortionCompensationThickness(0);
        values.SliceInfoV3.setDistortionCompensationFocalLength(0);
        values.SliceInfoV3.setXYAxisProfileCompensationEnabled((byte) 0x0);
        values.SliceInfoV3.setXYAxisProfileCompensationValue((short) 0);
        values.SliceInfoV3.setZPenetrationCompensationEnabled((byte) 0);
        values.SliceInfoV3.setZPenetrationCompensationLevel((short) 0);
        values.SliceInfoV3.setAntiAliasEnabled((byte) 0);
        Settings.put(values.getName(), values);

        values = new Values("CREALITY HALOT-ONE", "cxdlp",
                50.0f, new Size(1620,2560), "CL-60");
        values.SliceInfo.setDisplayWidth("81.0");
        values.SliceInfo.setDisplayHeight("128.0");
        values.SliceInfo.setLayerHeight("0.05");
        values.SliceInfo.setExposureTime((short) 30);
        values.SliceInfo.setWaitTimeBeforeCure((short) 4);
        values.SliceInfo.setBottomExposureTime((short) 40);
        values.SliceInfo.setBottomLayersCount((short) 2);
        values.SliceInfo.setBottomLiftHeight((short) 6);
        values.SliceInfo.setBottomLiftSpeed((short) 1);
        values.SliceInfo.setLiftHeight((short) 6);
        values.SliceInfo.setLiftSpeed((short) 1);
        values.SliceInfo.setRetractSpeed((short) 1);
        values.SliceInfoV3.setDistortionCompensationEnabled((byte) 0);
        values.SliceInfoV3.setDistortionCompensationThickness(0);
        values.SliceInfoV3.setDistortionCompensationFocalLength(0);
        values.SliceInfoV3.setXYAxisProfileCompensationEnabled((byte) 0x0);
        values.SliceInfoV3.setXYAxisProfileCompensationValue((short) 0);
        values.SliceInfoV3.setZPenetrationCompensationEnabled((byte) 0);
        values.SliceInfoV3.setZPenetrationCompensationLevel((short) 0);
        values.SliceInfoV3.setAntiAliasEnabled((byte) 0);
        Settings.put(values.getName(), values);

        values = new Values("CREALITY HALOT-RAY", "cxdlp",
                34.39f, new Size(5760,3600), "CL925");
        values.SliceInfo.setDisplayWidth("198.14");
        values.SliceInfo.setDisplayHeight("123.838");
        values.SliceInfo.setLayerHeight("0.05");
        values.SliceInfo.setExposureTime((short) 30);
        values.SliceInfo.setWaitTimeBeforeCure((short) 4);
        values.SliceInfo.setBottomExposureTime((short) 40);
        values.SliceInfo.setBottomLayersCount((short) 2);
        values.SliceInfo.setBottomLiftHeight((short) 6);
        values.SliceInfo.setBottomLiftSpeed((short) 1);
        values.SliceInfo.setLiftHeight((short) 6);
        values.SliceInfo.setLiftSpeed((short) 1);
        values.SliceInfo.setRetractSpeed((short) 1);
        values.SliceInfoV3.setDistortionCompensationEnabled((byte) 0);
        values.SliceInfoV3.setDistortionCompensationThickness(0);
        values.SliceInfoV3.setDistortionCompensationFocalLength(0);
        values.SliceInfoV3.setXYAxisProfileCompensationEnabled((byte) 0x0);
        values.SliceInfoV3.setXYAxisProfileCompensationValue((short) 0);
        values.SliceInfoV3.setZPenetrationCompensationEnabled((byte) 0);
        values.SliceInfoV3.setZPenetrationCompensationLevel((short) 0);
        values.SliceInfoV3.setAntiAliasEnabled((byte) 0);
        Settings.put(values.getName(), values);
    }
}
