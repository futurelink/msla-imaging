package futurelink.msla.formats.elegoo.tables;

import futurelink.msla.formats.iface.*;
import futurelink.msla.formats.iface.MSLAFileField;
import futurelink.msla.formats.iface.options.MSLAOption;
import futurelink.msla.formats.iface.options.MSLAOptionName;
import futurelink.msla.utils.About;
import futurelink.msla.utils.Size;
import lombok.Getter;
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
        @MSLAFileField(length = 8, order = 1) byte[] Magic = { 0x07, 0x00, 0x00, 0x00, 0x44, 0x4C, 0x50, 0x00 };
        @MSLAFileField(length = 32, order = 2) String SoftwareName = About.Name;
        @MSLAFileField(length = 24, order = 3) String SoftwareVersion = About.Version;
        @MSLAFileField(length = 24, order = 4) String FileCreateTime = formatDate(new Date());
        @MSLAFileField(length = 32, order = 5) String MachineName = "DEFAULT";
        @MSLAFileField(length = 32, order = 6) String MachineType = "DLP";
        @MSLAFileField(length = 32, order = 7) String ProfileName = "DEFAULT";
        @MSLAFileField(order = 8) @MSLAOption(MSLAOptionName.AntialiasLevel) Short AntiAliasingLevel = 8;
        @MSLAFileField(order = 9) @MSLAOption(MSLAOptionName.GreyLevel) Short GreyLevel = 1;
        @MSLAFileField(order = 10) @MSLAOption(MSLAOptionName.BlurLevel) Short BlurLevel = 0;
        @MSLAFileField(order = 11) GOOFilePreview SmallPreview = new GOOFilePreview(new Size(116, 116));
        @MSLAFileField(order = 11) GOOFilePreview BigPreview = new GOOFilePreview(new Size(290, 290));
        @MSLAFileField(order = 12) int LayerCount;
        @MSLAFileField(order = 13) short ResolutionX() { return Resolution != null ? Resolution.getWidth().shortValue() : 0; }
        private void setResolutionX(short width) { Resolution = new Size(width, ResolutionY()); }
        @MSLAFileField(order = 14) short ResolutionY() { return Resolution != null ? Resolution.getHeight().shortValue() : 0; }
        private void setResolutionY(short height) { Resolution = new Size(ResolutionX(), height); }
        @MSLAFileField(order = 15) @MSLAOption(MSLAOptionName.MirrorX) boolean MirrorX;
        @MSLAFileField(order = 16) @MSLAOption(MSLAOptionName.MirrorY) boolean MirrorY;
        @MSLAFileField(order = 17) float DisplayWidth;
        @MSLAFileField(order = 18) float DisplayHeight;
        @MSLAFileField(order = 19) float MachineZ;
        @MSLAFileField(order = 20) @MSLAOption(MSLAOptionName.LayerHeight) float LayerHeight;
        @MSLAFileField(order = 21) @MSLAOption(MSLAOptionName.NormalLayersExposureTime) float ExposureTime;
        @MSLAFileField(order = 22) byte DelayMode = DelayModes.WaitTime.value;
        @MSLAFileField(order = 23) @MSLAOption(MSLAOptionName.LightOffTime) float LightOffDelay;
        @MSLAFileField(order = 24) @MSLAOption(MSLAOptionName.BottomLayersWaitAfterCure) float BottomWaitTimeAfterCure;
        @MSLAFileField(order = 25) @MSLAOption(MSLAOptionName.BottomLayersWaitAfterLift) float BottomWaitTimeAfterLift;
        @MSLAFileField(order = 26) @MSLAOption(MSLAOptionName.BottomLayersWaitBeforeCure) float BottomWaitTimeBeforeCure;
        @MSLAFileField(order = 27) @MSLAOption(MSLAOptionName.NormalLayersWaitAfterCure) float WaitTimeAfterCure;
        @MSLAFileField(order = 28) @MSLAOption(MSLAOptionName.NormalLayersWaitAfterLift) float WaitTimeAfterLift;
        @MSLAFileField(order = 29) @MSLAOption(MSLAOptionName.NormalLayersWaitBeforeCure) float WaitTimeBeforeCure;
        @MSLAFileField(order = 30) @MSLAOption(MSLAOptionName.BottomLayersExposureTime) float BottomExposureTime;
        @MSLAFileField(order = 31) @MSLAOption(MSLAOptionName.BottomLayersCount) int BottomLayerCount;
        @MSLAFileField(order = 32) @MSLAOption(MSLAOptionName.BottomLayersLiftHeight) float BottomLiftHeight;
        @MSLAFileField(order = 33) @MSLAOption(MSLAOptionName.BottomLayersLiftSpeed) float BottomLiftSpeed;
        @MSLAFileField(order = 34) @MSLAOption(MSLAOptionName.NormalLayersLiftHeight) float LiftHeight;
        @MSLAFileField(order = 35) @MSLAOption(MSLAOptionName.NormalLayersLiftSpeed) float LiftSpeed;
        @MSLAFileField(order = 36) @MSLAOption(MSLAOptionName.BottomLayersRetractHeight) float BottomRetractHeight;
        @MSLAFileField(order = 37) @MSLAOption(MSLAOptionName.BottomLayersRetractSpeed) float BottomRetractSpeed;
        @MSLAFileField(order = 38) @MSLAOption(MSLAOptionName.RetractHeight) float RetractHeight;
        @MSLAFileField(order = 39) @MSLAOption(MSLAOptionName.RetractSpeed) float RetractSpeed;

        @MSLAFileField(order = 40) @MSLAOption(MSLAOptionName.BottomLayersLiftHeight2) float BottomLiftHeight2;
        @MSLAFileField(order = 41) @MSLAOption(MSLAOptionName.BottomLayersLiftSpeed2) float BottomLiftSpeed2;
        @MSLAFileField(order = 42) @MSLAOption(MSLAOptionName.NormalLayersLiftHeight2) float LiftHeight2;
        @MSLAFileField(order = 43) @MSLAOption(MSLAOptionName.NormalLayersLiftSpeed2) float LiftSpeed2;
        @MSLAFileField(order = 44) @MSLAOption(MSLAOptionName.BottomLayersRetractHeight2)  float BottomRetractHeight2;
        @MSLAFileField(order = 45) @MSLAOption(MSLAOptionName.BottomLayersRetractSpeed2) float BottomRetractSpeed2;
        @MSLAFileField(order = 46) @MSLAOption(MSLAOptionName.NormalLayersRetractHeight2) float RetractHeight2;
        @MSLAFileField(order = 47) @MSLAOption(MSLAOptionName.NormalLayersRetractSpeed2) float RetractSpeed2;

        @MSLAFileField(order = 48) @MSLAOption(MSLAOptionName.BottomLayersLightPWM) short BottomLightPWM = DefaultBottomLightPWM;
        @MSLAFileField(order = 49) @MSLAOption(MSLAOptionName.NormalLayersLightPWM) short LightPWM = DefaultLightPWM;
        @MSLAFileField(order = 50) @MSLAOption(MSLAOptionName.LayerSettings) boolean PerLayerSettings; // 0: Normal mode, 1: Advance mode, printing use the value of "Layer Definition Content"
        @MSLAFileField(order = 51) @MSLAOption(MSLAOptionName.PrintTime) int PrintTime;
        @MSLAFileField(order = 52) @MSLAOption(MSLAOptionName.Volume) float Volume; // The volume of all parts. unit: mm3
        @MSLAFileField(order = 53) @MSLAOption(MSLAOptionName.Weight) float MaterialGrams; // The weight of all parts. unit: g
        @MSLAFileField(order = 54) @MSLAOption(MSLAOptionName.Price) float MaterialCost;
        @MSLAFileField(length = 8, order = 55) @MSLAOption(MSLAOptionName.Currency) String PriceCurrencySymbol = "$"; // 8 bytes
        @MSLAFileField(order = 56) int LayerDefAddress; // Always after the header at 195477
        @MSLAFileField(order = 57) byte GrayScaleLevel = 1; // 0：The range of pixel's gray value is from 0x0 ~ 0xf, 1：The range of pixel's gray value is from 0x0 ~ 0xff
        @MSLAFileField(order = 58) @MSLAOption(MSLAOptionName.TransitionLayersCount) short TransitionLayerCount;

        private String formatDate(Date date) {
            var dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            return dateFormat.format(date);
        }
    }

    @Override public String getName() { return "Header"; }
    @Override public int getDataLength() { return HEADER_LENGTH; }
    @Override public String toString() { return "-- Header --\n" + blockFields.fieldsAsString(" = ", "\n"); }

}
