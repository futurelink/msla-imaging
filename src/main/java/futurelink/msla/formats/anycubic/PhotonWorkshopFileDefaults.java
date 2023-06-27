package futurelink.msla.formats.anycubic;

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
    public static class Values {
        @Getter String FileExtension;
        @Getter byte VersionMajor;
        @Getter byte VersionMinor;
        @Getter PhotonWorkshopFileMachineTable.Fields Machine;
        @Getter PhotonWorkshopFileHeaderTable.Fields Header;
    }

    public static Set<String> getSupported() {
        return Settings.keySet();
    }

    public static Values get(String machineName) {
        return Settings.get(machineName);
    }

    private static final HashMap<String, Values> Settings = new HashMap<>();
    static {
        var name = "Anycubic Photon Mono X 6K";
        var values = new Values();
        values.FileExtension = "pwmb";
        values.VersionMajor = 0x02;
        values.VersionMinor = 0x04;
        values.Machine = new PhotonWorkshopFileMachineTable.Fields();
        values.Header = new PhotonWorkshopFileHeaderTable.Fields(34.399998f, new Size(5760,3600));

        values.Machine.setMachineName(name);
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
        values.Header.setPerLayerOverride(0);
        values.Header.setTransitionLayerCount(10);
        values.Header.setTransitionLayerType(0);
        values.Header.setAdvancedMode(1);
        values.Header.setVolumeMl(0.0f);
        values.Header.setWeightG(0.0f);
        values.Header.setPrice(0.0f);
        values.Header.setPriceCurrencySymbol(0x240000);
        values.Header.setPrintTime(0);
        Settings.put(name, values);

        var name2 = "Anycubic Photon M3 Plus";
        Settings.put(name2, values);
    }

    static {
        var name = "Anycubic Photon Mono 4K";
        var values = new Values();

        values.FileExtension = "pwma";
        values.VersionMajor = 0x02;
        values.VersionMinor = 0x04;
        values.Machine = new PhotonWorkshopFileMachineTable.Fields();
        values.Header = new PhotonWorkshopFileHeaderTable.Fields(35.0f, new Size(3840, 2400));

        values.Machine.setMachineName(name);
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
        values.Header.setPriceCurrencySymbol(36);
        values.Header.setPerLayerOverride(0);
        values.Header.setTransitionLayerCount(0);
        values.Header.setTransitionLayerType(0);
        values.Header.setAdvancedMode(1);
        values.Header.setVolumeMl(0.0f);
        values.Header.setWeightG(0.0f);
        values.Header.setPrice(0.0f);
        values.Header.setPriceCurrencySymbol(0x240000);
        values.Header.setPrintTime(0);
        Settings.put(name, values);
    }

    static {
        var name = "Anycubic Photon Mono X";
        var values = new Values();
        values.FileExtension = "pwmx";
        values.VersionMajor = 0x02;
        values.VersionMinor = 0x04;
        values.Machine = new PhotonWorkshopFileMachineTable.Fields();
        values.Header = new PhotonWorkshopFileHeaderTable.Fields(50.0f, new Size(3840, 2400));

        values.Machine.setMachineName("Anycubic Photon Mono X");
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
        values.Header.setPriceCurrencySymbol(36);
        values.Header.setPerLayerOverride(0);
        values.Header.setTransitionLayerCount(10);
        values.Header.setTransitionLayerType(0);
        values.Header.setAdvancedMode(1);
        values.Header.setVolumeMl(0.0f);
        values.Header.setWeightG(0.0f);
        values.Header.setPrice(0.0f);
        values.Header.setPriceCurrencySymbol(0x240000);
        values.Header.setPrintTime(0);
        Settings.put(name, values);
    }

    static {
        var name = "Anycubic Photon M3";
        var values = new Values();
        values.FileExtension = "pm3";
        values.VersionMajor = 0x02;
        values.VersionMinor = 0x04;
        values.Machine = new PhotonWorkshopFileMachineTable.Fields();
        values.Header = new PhotonWorkshopFileHeaderTable.Fields(40.0f, new Size(4096, 2560));

        values.Machine.setMachineName(name);
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
        values.Header.setPriceCurrencySymbol(36);
        values.Header.setPerLayerOverride(0);
        values.Header.setTransitionLayerCount(10);
        values.Header.setTransitionLayerType(0);
        values.Header.setAdvancedMode(0);
        values.Header.setVolumeMl(0.0f);
        values.Header.setWeightG(0.0f);
        values.Header.setPrice(0.0f);
        values.Header.setPriceCurrencySymbol(0x240000);
        values.Header.setPrintTime(0);
        Settings.put(name, values);
    }

    static {
        var name = "Anycubic Photon M3 Max";
        var values = new Values();
        values.FileExtension = "pm3m";
        values.VersionMajor = 0x02;
        values.VersionMinor = 0x04;
        values.Machine = new PhotonWorkshopFileMachineTable.Fields();
        values.Header = new PhotonWorkshopFileHeaderTable.Fields(45.999996f, new Size(6480, 3600));

        values.Machine.setMachineName(name);
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
        values.Header.setPriceCurrencySymbol(36);
        values.Header.setPerLayerOverride(0);
        values.Header.setTransitionLayerCount(10);
        values.Header.setTransitionLayerType(0);
        values.Header.setAdvancedMode(0);
        values.Header.setVolumeMl(0.0f);
        values.Header.setWeightG(0.0f);
        values.Header.setPrice(0.0f);
        values.Header.setPriceCurrencySymbol(0x240000);
        values.Header.setPrintTime(0);
        Settings.put(name, values);
    }
}
