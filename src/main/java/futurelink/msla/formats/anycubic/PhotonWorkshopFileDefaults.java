package futurelink.msla.formats.anycubic;

import futurelink.msla.formats.iface.MSLAFileBlockFields;
import futurelink.msla.formats.iface.MSLAFileDefaults;
import futurelink.msla.formats.anycubic.tables.PhotonWorkshopFileHeaderTable;
import futurelink.msla.formats.anycubic.tables.PhotonWorkshopFileMachineTable;
import futurelink.msla.formats.utils.Size;
import lombok.Getter;

import java.util.HashMap;
import java.util.Set;

/**
 * Anycubic printers default settings.
 */
public class PhotonWorkshopFileDefaults {
    private static class Values implements MSLAFileDefaults {
        @Getter private String Name;
        @Getter private String FileExtension;
        @Getter private byte VersionMajor;
        @Getter private byte VersionMinor;
        @Getter PhotonWorkshopFileMachineTable.Fields Machine;
        @Getter PhotonWorkshopFileHeaderTable.Fields Header;

        Values(String name, String fileExtension, byte versionMajor, byte versionMinor,  Float PixelSizeUm, Size Resolution) {
            Name = name;
            FileExtension = fileExtension;
            VersionMajor = versionMajor;
            VersionMinor = versionMinor;

            Machine = new PhotonWorkshopFileMachineTable.Fields();
            Machine.setMachineName(name);
            Header = new PhotonWorkshopFileHeaderTable.Fields(PixelSizeUm, Resolution);
            Header.setVolumeMl(0.0f);
            Header.setWeightG(0.0f);
            Header.setPrice(0.0f);
            Header.setPriceCurrencySymbol(0x24000000);
            Header.setPrintTime(0);
            Header.setPriceCurrencySymbol(36);
            Header.setPerLayerOverride(0);
        }
        @Override
        public float getPixelSizeUm() { return Header.getPixelSizeUm(); }
        @Override
        public Integer getOptionInt(String name) { return null; }
        @Override
        public Byte getOptionByte(String name) {
            if ("VersionMajor".equals(name)) return getVersionMajor();
            if ("VersionMinor".equals(name)) return getVersionMinor();
            return null;
        }
        @Override
        public Short getOptionShort(String name) { return null; }

        @Override
        public String getOptionString(String name) {
            if ("FileExtension".equals(name)) return getFileExtension();
            return null;
        }

        @Override
        public MSLAFileBlockFields getOptionBlock(String name) {
            if ("Machine".equals(name)) return Machine;
            if ("Header".equals(name)) return Header;
            return null;
        }
    }
    public static Set<String> getSupported() {
        return Settings.keySet();
    }
    public static Values get(String machineName) {
        return Settings.get(machineName);
    }

    private static final HashMap<String, Values> Settings = new HashMap<>();
    static {
        var values = new Values("Anycubic Photon Mono X 6K", "pwmb", (byte) 0x02, (byte )0x04,
                34.399998f, new Size(5760,3600));
        values.Machine.setLayerImageFormat("pw0Img");
        values.Machine.setMaxAntialiasingLevel(16);
        values.Machine.setPropertyFields(7);
        values.Machine.setDisplayWidth(198.144f);
        values.Machine.setDisplayHeight(123.84f);
        values.Machine.setMachineZ(245.0f);
        values.Machine.setMaxFileVersion(516);
        values.Machine.setMachineBackground(6506241);

        values.Header.setPriceCurrencySymbol(36);
        values.Header.setLayerHeight(0.05f);
        values.Header.setExposureTime(2.0f);
        values.Header.setWaitTimeBeforeCure1(0.0f);
        values.Header.setBottomExposureTime(28.0f);
        values.Header.setBottomLayersCount(6);
        values.Header.setLiftHeight(8.0f);
        values.Header.setLiftSpeed(2.0f);
        values.Header.setRetractSpeed(2.0f);
        values.Header.setAntiAliasing(2);
        values.Header.setTransitionLayerCount(10);
        values.Header.setTransitionLayerType(0);
        values.Header.setAdvancedMode(1);
        Settings.put(values.getName(), values);

        Settings.put("Anycubic Photon M3 Plus", values);
    }

    static {
        var values = new Values("Anycubic Photon Mono 4K", "pwma", (byte) 0x02, (byte )0x04,
                35.0f, new Size(3840, 2400));

        values.Machine.setLayerImageFormat("pw0Img");
        values.Machine.setMaxAntialiasingLevel(16);
        values.Machine.setPropertyFields(7);
        values.Machine.setDisplayWidth(134.4f);
        values.Machine.setDisplayHeight(84.0f);
        values.Machine.setMachineZ(165.0f);
        values.Machine.setMaxFileVersion(516);
        values.Machine.setMachineBackground(6506241);

        values.Header.setLayerHeight(0.05f);
        values.Header.setExposureTime(2.0f);
        values.Header.setWaitTimeBeforeCure1(0.5f);
        values.Header.setBottomExposureTime(40.0f);
        values.Header.setBottomLayersCount(6);
        values.Header.setLiftHeight(6.0f);
        values.Header.setLiftSpeed(2.0f);
        values.Header.setRetractSpeed(2.0f);
        values.Header.setAntiAliasing(1);
        values.Header.setTransitionLayerCount(0);
        values.Header.setTransitionLayerType(0);
        values.Header.setAdvancedMode(1);
        Settings.put(values.getName(), values);
    }

    static {
        var values = new Values("Anycubic Photon Mono X", "pwmx", (byte) 0x02, (byte )0x04,
                50.0f, new Size(3840, 2400));

        values.Machine.setLayerImageFormat("pw0Img");
        values.Machine.setMaxAntialiasingLevel(16);
        values.Machine.setPropertyFields(7);
        values.Machine.setDisplayWidth(192.0f);
        values.Machine.setDisplayHeight(120.0f);
        values.Machine.setMachineZ(245.0f);
        values.Machine.setMaxFileVersion(516);
        values.Machine.setMachineBackground(6506241);

        values.Header.setLayerHeight(0.05f);
        values.Header.setExposureTime(2.0f);
        values.Header.setWaitTimeBeforeCure1(0.5f);
        values.Header.setBottomExposureTime(28.0f);
        values.Header.setBottomLayersCount(4);
        values.Header.setLiftHeight(8.0f);
        values.Header.setLiftSpeed(1.0f);
        values.Header.setRetractSpeed(1.5f);
        values.Header.setAntiAliasing(1);
        values.Header.setTransitionLayerCount(10);
        values.Header.setTransitionLayerType(0);
        values.Header.setAdvancedMode(1);
        Settings.put(values.getName(), values);
    }

    static {
        var values = new Values("Anycubic Photon M3", "pm3", (byte) 0x02, (byte )0x04,
                40.0f, new Size(4096, 2560));

        values.Machine.setLayerImageFormat("pw0Img");
        values.Machine.setMaxAntialiasingLevel(16);
        values.Machine.setPropertyFields(7);
        values.Machine.setDisplayWidth(163.92f);
        values.Machine.setDisplayHeight(102.4f);
        values.Machine.setMachineZ(180.0f);
        values.Machine.setMaxFileVersion(516);
        values.Machine.setMachineBackground(6506241);

        values.Header.setLayerHeight(0.05f);
        values.Header.setExposureTime(2.0f);
        values.Header.setWaitTimeBeforeCure1(0.5f);
        values.Header.setBottomExposureTime(23.0f);
        values.Header.setBottomLayersCount(4);
        values.Header.setLiftHeight(6.0f);
        values.Header.setLiftSpeed(3.0f);
        values.Header.setRetractSpeed(4.0f);
        values.Header.setAntiAliasing(1);
        values.Header.setTransitionLayerCount(10);
        values.Header.setTransitionLayerType(0);
        values.Header.setAdvancedMode(0);
        Settings.put(values.getName(), values);
    }

    static {
        var values = new Values("Anycubic Photon M3 Max", "pm3m", (byte) 0x02, (byte )0x04,
                45.999996f, new Size(6480, 3600));

        values.Machine.setLayerImageFormat("pw0Img");
        values.Machine.setMaxAntialiasingLevel(16);
        values.Machine.setPropertyFields(7);
        values.Machine.setDisplayWidth(298.08f);
        values.Machine.setDisplayHeight(165.6f);
        values.Machine.setMachineZ(300.0f);
        values.Machine.setMaxFileVersion(516);
        values.Machine.setMachineBackground(6506241);


        values.Header.setLayerHeight(0.05f);
        values.Header.setExposureTime(3.0f);
        values.Header.setWaitTimeBeforeCure1(2.0f);
        values.Header.setBottomExposureTime(30.0f);
        values.Header.setBottomLayersCount(6);
        values.Header.setLiftHeight(10.0f);
        values.Header.setLiftSpeed(4.0f);
        values.Header.setRetractSpeed(4.0f);
        values.Header.setAntiAliasing(1);
        values.Header.setTransitionLayerCount(10);
        values.Header.setTransitionLayerType(0);
        values.Header.setAdvancedMode(0);
        Settings.put(values.getName(), values);
    }
}
