package futurelink.msla.formats.anycubic;

import futurelink.msla.formats.anycubic.tables.PhotonWorkshopFileExtraTable;
import futurelink.msla.formats.anycubic.tables.PhotonWorkshopFileHeaderTable;
import futurelink.msla.formats.anycubic.tables.PhotonWorkshopFileMachineTable;
import lombok.Getter;

public class PhotonWorkshopFileDefaults {
    public static class Values {
        @Getter String FileExtension;
        @Getter byte VersionMajor;
        @Getter byte VersionMinor;
        @Getter PhotonWorkshopFileMachineTable.Fields Machine;
        @Getter PhotonWorkshopFileHeaderTable.Fields Header;
    }
    public static final Values PhotonMonoX6K = new Values();
    static {
        PhotonMonoX6K.FileExtension = "pwmb";
        PhotonMonoX6K.VersionMajor = 0x02;
        PhotonMonoX6K.VersionMinor = 0x04;
        PhotonMonoX6K.Machine = new PhotonWorkshopFileMachineTable.Fields();
        PhotonMonoX6K.Header = new PhotonWorkshopFileHeaderTable.Fields();

        PhotonMonoX6K.Machine.setMachineName("Anycubic Photon Mono X 6K");
        PhotonMonoX6K.Machine.setLayerImageFormat("pw0Img");
        PhotonMonoX6K.Machine.setMaxAntialiasingLevel(16);
        PhotonMonoX6K.Machine.setPropertyFields(7);
        PhotonMonoX6K.Machine.setDisplayWidth(198.144f);
        PhotonMonoX6K.Machine.setDisplayHeight(123.84f);
        PhotonMonoX6K.Machine.setMachineZ(245.0f);
        PhotonMonoX6K.Machine.setMaxFileVersion(516);
        PhotonMonoX6K.Machine.setMachineBackground(6506241);

        PhotonMonoX6K.Header.setPixelSizeUm(34.399998f);
        PhotonMonoX6K.Header.setLayerHeight(0.05f);
        PhotonMonoX6K.Header.setExposureTime(2.0f);
        PhotonMonoX6K.Header.setWaitTimeBeforeCure1(0.0f);
        PhotonMonoX6K.Header.setBottomExposureTime(28.0f);
        PhotonMonoX6K.Header.setBottomLayersCount(6.0f);
        PhotonMonoX6K.Header.setLiftHeight(8.0f);
        PhotonMonoX6K.Header.setLiftSpeed(2.0f);
        PhotonMonoX6K.Header.setRetractSpeed(2.0f);
        PhotonMonoX6K.Header.setAntiAliasing(2);
        PhotonMonoX6K.Header.setResolutionX(5760);
        PhotonMonoX6K.Header.setResolutionY(3600);
        PhotonMonoX6K.Header.setPriceCurrencySymbol(36);
        PhotonMonoX6K.Header.setPerLayerOverride(0);
        PhotonMonoX6K.Header.setTransitionLayerCount(10);
        PhotonMonoX6K.Header.setTransitionLayerType(0);
        PhotonMonoX6K.Header.setAdvancedMode(1);
    }

    public static final Values PhotonMono4K = new Values();
    static {
        PhotonMono4K.FileExtension = "pwma";
        PhotonMono4K.VersionMajor = 0x02;
        PhotonMono4K.VersionMinor = 0x04;
        PhotonMono4K.Machine = new PhotonWorkshopFileMachineTable.Fields();
        PhotonMono4K.Header = new PhotonWorkshopFileHeaderTable.Fields();

        PhotonMono4K.Machine.setMachineName("Anycubic Photon Mono 4K");
        PhotonMono4K.Machine.setLayerImageFormat("pw0Img");
        PhotonMono4K.Machine.setMaxAntialiasingLevel(16);
        PhotonMono4K.Machine.setPropertyFields(7);
        PhotonMono4K.Machine.setDisplayWidth(134.4f);
        PhotonMono4K.Machine.setDisplayHeight(84.0f);
        PhotonMono4K.Machine.setMachineZ(165.0f);
        PhotonMono4K.Machine.setMaxFileVersion(516);
        PhotonMono4K.Machine.setMachineBackground(6506241);

        PhotonMono4K.Header.setPixelSizeUm(35.0f);
        PhotonMono4K.Header.setLayerHeight(0.05f);
        PhotonMono4K.Header.setExposureTime(2.0f);
        PhotonMono4K.Header.setWaitTimeBeforeCure1(0.5f);
        PhotonMono4K.Header.setBottomExposureTime(40.0f);
        PhotonMono4K.Header.setBottomLayersCount(6.0f);
        PhotonMono4K.Header.setLiftHeight(6.0f);
        PhotonMono4K.Header.setLiftSpeed(2.0f);
        PhotonMono4K.Header.setRetractSpeed(2.0f);
        PhotonMono4K.Header.setAntiAliasing(1);
        PhotonMono4K.Header.setResolutionX(3840);
        PhotonMono4K.Header.setResolutionY(2400);
        PhotonMono4K.Header.setPriceCurrencySymbol(36);
        PhotonMono4K.Header.setPerLayerOverride(0);
        PhotonMono4K.Header.setTransitionLayerCount(0);
        PhotonMono4K.Header.setTransitionLayerType(0);
        PhotonMono4K.Header.setAdvancedMode(1);
    }

    public static final Values PhotonMonoX = new Values();
    static {
        PhotonMonoX.FileExtension = "pwmx";
        PhotonMonoX.VersionMajor = 0x02;
        PhotonMonoX.VersionMinor = 0x04;
        PhotonMonoX.Machine = new PhotonWorkshopFileMachineTable.Fields();
        PhotonMonoX.Header = new PhotonWorkshopFileHeaderTable.Fields();

        PhotonMonoX.Machine.setMachineName("Anycubic Photon Mono X");
        PhotonMonoX.Machine.setLayerImageFormat("pw0Img");
        PhotonMonoX.Machine.setMaxAntialiasingLevel(16);
        PhotonMonoX.Machine.setPropertyFields(7);
        PhotonMonoX.Machine.setDisplayWidth(192.0f);
        PhotonMonoX.Machine.setDisplayHeight(120.0f);
        PhotonMonoX.Machine.setMachineZ(245.0f);
        PhotonMonoX.Machine.setMaxFileVersion(516);
        PhotonMonoX.Machine.setMachineBackground(6506241);

        PhotonMonoX.Header.setPixelSizeUm(50.0f);
        PhotonMonoX.Header.setLayerHeight(0.05f);
        PhotonMonoX.Header.setExposureTime(2.0f);
        PhotonMonoX.Header.setWaitTimeBeforeCure1(0.5f);
        PhotonMonoX.Header.setBottomExposureTime(28.0f);
        PhotonMonoX.Header.setBottomLayersCount(4.0f);
        PhotonMonoX.Header.setLiftHeight(8.0f);
        PhotonMonoX.Header.setLiftSpeed(1.0f);
        PhotonMonoX.Header.setRetractSpeed(1.5f);
        PhotonMonoX.Header.setAntiAliasing(1);
        PhotonMonoX.Header.setResolutionX(3840);
        PhotonMonoX.Header.setResolutionY(2400);
        PhotonMonoX.Header.setPriceCurrencySymbol(36);
        PhotonMonoX.Header.setPerLayerOverride(0);
        PhotonMonoX.Header.setTransitionLayerCount(10);
        PhotonMonoX.Header.setTransitionLayerType(0);
        PhotonMonoX.Header.setAdvancedMode(1);
    }

    public static final Values PhotonM3 = new Values();
    static {
        PhotonM3.FileExtension = "pm3";
        PhotonM3.VersionMajor = 0x02;
        PhotonM3.VersionMinor = 0x04;
        PhotonM3.Machine = new PhotonWorkshopFileMachineTable.Fields();
        PhotonM3.Header = new PhotonWorkshopFileHeaderTable.Fields();

        PhotonM3.Machine.setMachineName("Anycubic Photon M3");
        PhotonM3.Machine.setLayerImageFormat("pw0Img");
        PhotonM3.Machine.setMaxAntialiasingLevel(16);
        PhotonM3.Machine.setPropertyFields(7);
        PhotonM3.Machine.setDisplayWidth(163.92f);
        PhotonM3.Machine.setDisplayHeight(102.4f);
        PhotonM3.Machine.setMachineZ(180.0f);
        PhotonM3.Machine.setMaxFileVersion(516);
        PhotonM3.Machine.setMachineBackground(6506241);

        PhotonM3.Header.setPixelSizeUm(40.0f);
        PhotonM3.Header.setLayerHeight(0.05f);
        PhotonM3.Header.setExposureTime(2.0f);
        PhotonM3.Header.setWaitTimeBeforeCure1(0.5f);
        PhotonM3.Header.setBottomExposureTime(23.0f);
        PhotonM3.Header.setBottomLayersCount(4.0f);
        PhotonM3.Header.setLiftHeight(6.0f);
        PhotonM3.Header.setLiftSpeed(3.0f);
        PhotonM3.Header.setRetractSpeed(4.0f);
        PhotonM3.Header.setAntiAliasing(1);
        PhotonM3.Header.setResolutionX(4096);
        PhotonM3.Header.setResolutionY(2560);
        PhotonM3.Header.setPriceCurrencySymbol(36);
        PhotonM3.Header.setPerLayerOverride(0);
        PhotonM3.Header.setTransitionLayerCount(10);
        PhotonM3.Header.setTransitionLayerType(0);
        PhotonM3.Header.setAdvancedMode(0);
    }

    public static final Values PhotonM3Max = new Values();
    static {
        PhotonM3.FileExtension = "pm3m";
        PhotonM3.VersionMajor = 0x02;
        PhotonM3.VersionMinor = 0x04;
        PhotonM3.Machine = new PhotonWorkshopFileMachineTable.Fields();
        PhotonM3.Header = new PhotonWorkshopFileHeaderTable.Fields();

        PhotonM3.Machine.setMachineName("Anycubic Photon M3 Max");
        PhotonM3.Machine.setLayerImageFormat("pw0Img");
        PhotonM3.Machine.setMaxAntialiasingLevel(16);
        PhotonM3.Machine.setPropertyFields(7);
        PhotonM3.Machine.setDisplayWidth(298.08f);
        PhotonM3.Machine.setDisplayHeight(165.6f);
        PhotonM3.Machine.setMachineZ(300.0f);
        PhotonM3.Machine.setMaxFileVersion(516);
        PhotonM3.Machine.setMachineBackground(6506241);

        PhotonM3.Header.setPixelSizeUm(45.999996f);
        PhotonM3.Header.setLayerHeight(0.05f);
        PhotonM3.Header.setExposureTime(3.0f);
        PhotonM3.Header.setWaitTimeBeforeCure1(2.0f);
        PhotonM3.Header.setBottomExposureTime(30.0f);
        PhotonM3.Header.setBottomLayersCount(6.0f);
        PhotonM3.Header.setLiftHeight(10.0f);
        PhotonM3.Header.setLiftSpeed(4.0f);
        PhotonM3.Header.setRetractSpeed(4.0f);
        PhotonM3.Header.setAntiAliasing(1);
        PhotonM3.Header.setResolutionX(6480);
        PhotonM3.Header.setResolutionY(3600);
        PhotonM3.Header.setPriceCurrencySymbol(36);
        PhotonM3.Header.setPerLayerOverride(0);
        PhotonM3.Header.setTransitionLayerCount(10);
        PhotonM3.Header.setTransitionLayerType(0);
        PhotonM3.Header.setAdvancedMode(0);
    }
}
