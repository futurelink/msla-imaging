package futurelink.msla.formats.elegoo.tables;

import futurelink.msla.formats.iface.*;
import futurelink.msla.formats.iface.MSLAFileField;
import futurelink.msla.formats.iface.options.MSLAOption;
import futurelink.msla.formats.iface.options.MSLAOptionName;
import futurelink.msla.utils.About;
import futurelink.msla.utils.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;

import java.text.SimpleDateFormat;
import java.util.Date;

@Getter
public class GOOFileHeader extends GOOFileTable {
    enum DelayModes {
        LightOff((byte) 0), WaitTime((byte) 1);
        public final byte value; DelayModes(byte value) { this.value = value; }
    }
    private static final int HEADER_LENGTH = 195477;
    private static final byte DefaultBottomLightPWM = 0x01;
    private static final byte DefaultLightPWM = 0x02;
    @Delegate private final Fields blockFields = new Fields();

    @Getter
    @SuppressWarnings("unused")
    public static class Fields implements MSLAFileBlockFields {
        private Size Resolution = null;
        @MSLAFileField(length = 4) private final String Version = "V3.0";
        @MSLAFileField(length = 8, order = 1) private byte[] Magic = { 0x07, 0x00, 0x00, 0x00, 0x44, 0x4C, 0x50, 0x00 };
        @MSLAFileField(length = 32, order = 2) private String SoftwareName = About.Name;
        @MSLAFileField(length = 24, order = 3) private String SoftwareVersion = About.Version;
        @MSLAFileField(length = 24, order = 4) private String FileCreateTime = formatDate(new Date());
        @MSLAFileField(length = 32, order = 5) @Setter private String MachineName;
        @MSLAFileField(length = 32, order = 6) private String MachineType = "DLP";
        @MSLAFileField(length = 32, order = 7) private String ProfileName = "DEFAULT";
        @MSLAFileField(order = 8) @MSLAOption(MSLAOptionName.AntialiasLevel) private Short AntiAliasingLevel;
        @MSLAFileField(order = 9) @MSLAOption(MSLAOptionName.GreyLevel) private Short GreyLevel;
        @MSLAFileField(order = 10) @MSLAOption(MSLAOptionName.ImageBlurLevel) private Short BlurLevel;
        @MSLAFileField(order = 11) private GOOFilePreview SmallPreview = new GOOFilePreview(new Size(116, 116));
        @MSLAFileField(order = 11) private GOOFilePreview BigPreview = new GOOFilePreview(new Size(290, 290));
        @MSLAFileField(order = 12) @Setter private Integer LayerCount;
        @MSLAFileField(order = 13) Short ResolutionX() { return Resolution != null ? Resolution.getWidth().shortValue() : 0; }
        private void setResolutionX(Short width) { Resolution = new Size(width, ResolutionY()); }
        @MSLAFileField(order = 14) Short ResolutionY() { return Resolution != null ? Resolution.getHeight().shortValue() : 0; }
        private void setResolutionY(Short height) { Resolution = new Size(ResolutionX(), height); }
        @MSLAFileField(order = 15) @MSLAOption(MSLAOptionName.MirrorX) private Boolean MirrorX;
        @MSLAFileField(order = 16) @MSLAOption(MSLAOptionName.MirrorY) private Boolean MirrorY;
        @MSLAFileField(order = 17) private Float DisplayWidth;
        @MSLAFileField(order = 18) private Float DisplayHeight;
        @MSLAFileField(order = 19) private Float MachineZ;
        @MSLAFileField(order = 20) @MSLAOption(MSLAOptionName.LayerHeight) private Float LayerHeight;
        @MSLAFileField(order = 21) @MSLAOption(MSLAOptionName.NormalLayersExposureTime) private Float ExposureTime;
        @MSLAFileField(order = 22) private Byte DelayMode = DelayModes.WaitTime.value;
        @MSLAFileField(order = 23) @MSLAOption(MSLAOptionName.LightOffTime) private Float LightOffDelay;
        @MSLAFileField(order = 24) @MSLAOption(MSLAOptionName.BottomLayersWaitAfterCure) private Float BottomWaitTimeAfterCure;
        @MSLAFileField(order = 25) @MSLAOption(MSLAOptionName.BottomLayersWaitAfterLift) private Float BottomWaitTimeAfterLift;
        @MSLAFileField(order = 26) @MSLAOption(MSLAOptionName.BottomLayersWaitBeforeCure) private Float BottomWaitTimeBeforeCure;
        @MSLAFileField(order = 27) @MSLAOption(MSLAOptionName.NormalLayersWaitAfterCure) private Float WaitTimeAfterCure;
        @MSLAFileField(order = 28) @MSLAOption(MSLAOptionName.NormalLayersWaitAfterLift) private Float WaitTimeAfterLift;
        @MSLAFileField(order = 29) @MSLAOption(MSLAOptionName.NormalLayersWaitBeforeCure) private Float WaitTimeBeforeCure;
        @MSLAFileField(order = 30) @MSLAOption(MSLAOptionName.BottomLayersExposureTime) private Float BottomExposureTime;
        @MSLAFileField(order = 31) @MSLAOption(MSLAOptionName.BottomLayersCount) private Integer BottomLayerCount;
        @MSLAFileField(order = 32) @MSLAOption(MSLAOptionName.BottomLayersLiftHeight) private Float BottomLiftHeight;
        @MSLAFileField(order = 33) @MSLAOption(MSLAOptionName.BottomLayersLiftSpeed) private Float BottomLiftSpeed;
        @MSLAFileField(order = 34) @MSLAOption(MSLAOptionName.NormalLayersLiftHeight) private Float LiftHeight;
        @MSLAFileField(order = 35) @MSLAOption(MSLAOptionName.NormalLayersLiftSpeed) private Float LiftSpeed;
        @MSLAFileField(order = 36) @MSLAOption(MSLAOptionName.BottomLayersRetractHeight) private Float BottomRetractHeight;
        @MSLAFileField(order = 37) @MSLAOption(MSLAOptionName.BottomLayersRetractSpeed) private Float BottomRetractSpeed;
        @MSLAFileField(order = 38) @MSLAOption(MSLAOptionName.NormalLayersRetractHeight) private Float RetractHeight;
        @MSLAFileField(order = 39) @MSLAOption(MSLAOptionName.NormalLayersRetractSpeed) private Float RetractSpeed;
        @MSLAFileField(order = 40) @MSLAOption(MSLAOptionName.BottomLayersLiftHeight2) private Float BottomLiftHeight2;
        @MSLAFileField(order = 41) @MSLAOption(MSLAOptionName.BottomLayersLiftSpeed2) private Float BottomLiftSpeed2;
        @MSLAFileField(order = 42) @MSLAOption(MSLAOptionName.NormalLayersLiftHeight2) private Float LiftHeight2;
        @MSLAFileField(order = 43) @MSLAOption(MSLAOptionName.NormalLayersLiftSpeed2) private Float LiftSpeed2;
        @MSLAFileField(order = 44) @MSLAOption(MSLAOptionName.BottomLayersRetractHeight2) private Float BottomRetractHeight2;
        @MSLAFileField(order = 45) @MSLAOption(MSLAOptionName.BottomLayersRetractSpeed2) private Float BottomRetractSpeed2;
        @MSLAFileField(order = 46) @MSLAOption(MSLAOptionName.NormalLayersRetractHeight2) private Float RetractHeight2;
        @MSLAFileField(order = 47) @MSLAOption(MSLAOptionName.NormalLayersRetractSpeed2) private Float RetractSpeed2;
        @MSLAFileField(order = 48) @MSLAOption(MSLAOptionName.BottomLayersLightPWM) private Short BottomLightPWM = DefaultBottomLightPWM;
        @MSLAFileField(order = 49) @MSLAOption(MSLAOptionName.NormalLayersLightPWM) private Short LightPWM = DefaultLightPWM;
        @MSLAFileField(order = 50) @MSLAOption(MSLAOptionName.LayerSettings) private Boolean PerLayerSettings; // 0: Normal mode, 1: Advance mode, printing use the value of "Layer Definition Content"
        @MSLAFileField(order = 51) @MSLAOption(MSLAOptionName.PrintTime) private Integer PrintTime;
        @MSLAFileField(order = 52) @MSLAOption(MSLAOptionName.Volume) private Float Volume; // The volume of all parts. unit: mm3
        @MSLAFileField(order = 53) @MSLAOption(MSLAOptionName.Weight) private Float MaterialGrams; // The weight of all parts. unit: g
        @MSLAFileField(order = 54) @MSLAOption(MSLAOptionName.Price) private Float MaterialCost;
        @MSLAFileField(length = 8, order = 55) @MSLAOption(MSLAOptionName.Currency) private String PriceCurrencySymbol = "$"; // 8 bytes
        @MSLAFileField(order = 56) private Integer LayerDefAddress = 195477; // Always after the header at 195477
        @MSLAFileField(order = 57) private Byte GrayScaleLevel = 1; // 0：The range of pixel's gray value is from 0x0 ~ 0xf, 1：The range of pixel's gray value is from 0x0 ~ 0xff
        @MSLAFileField(order = 58) @MSLAOption(MSLAOptionName.TransitionLayersCount) private Short TransitionLayerCount;

        private String formatDate(Date date) {
            var dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            return dateFormat.format(date);
        }
    }

    @Override public String getName() { return "Header"; }
    @Override public int getDataLength() { return HEADER_LENGTH; }
    @Override public String toString() { return "-- Header --\n" + blockFields.fieldsAsString(" = ", "\n"); }

}
