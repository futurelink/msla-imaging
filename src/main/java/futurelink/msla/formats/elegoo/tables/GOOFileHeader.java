package futurelink.msla.formats.elegoo.tables;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.iface.*;
import futurelink.msla.formats.iface.annotations.MSLAFileField;
import futurelink.msla.formats.iface.annotations.MSLAOption;
import futurelink.msla.formats.iface.annotations.MSLAOptionContainer;
import futurelink.msla.formats.utils.About;
import futurelink.msla.formats.utils.Size;
import lombok.Getter;
import lombok.experimental.Delegate;

import java.text.SimpleDateFormat;
import java.util.Date;

@Getter
@MSLAOptionContainer(GOOFileHeader.Fields.class)
public class GOOFileHeader extends GOOFileTable {
    enum DelayModes {
        LightOff((byte) 0), WaitTime((byte) 1);
        public final byte value; DelayModes(byte value) { this.value = value; }
    }
    private static final int HEADER_LENGTH = 195477;
    private static final byte DefaultBottomLightPWM = 0x01;
    private static final byte DefaultLightPWM = 0x02;
    @Delegate private final Fields fields;

    @Getter
    @SuppressWarnings("unused")
    public static class Fields implements MSLAFileBlockFields {
        private float PixelSizeUm;
        private Size Resolution = new Size(0, 0);
        @MSLAFileField(length = 4) private final String Version = "V3.0";
        @MSLAFileField(length = 8, order = 1) byte[] Magic = { 0x07, 0x00, 0x00, 0x00, 0x44, 0x4C, 0x50, 0x00 };
        @MSLAFileField(length = 32, order = 2) String SoftwareName = About.Name;
        @MSLAFileField(length = 24, order = 3) String SoftwareVersion = About.Version;
        @MSLAFileField(length = 24, order = 4) String FileCreateTime = formatDate(new Date());
        @MSLAFileField(length = 32, order = 5) String MachineName = "DEFAULT";
        @MSLAFileField(length = 32, order = 6) String MachineType = "DLP";
        @MSLAFileField(length = 32, order = 7) String ProfileName = "DEFAULT";
        @MSLAFileField(order = 8) @MSLAOption(MSLAOption.AntialiasLevel) Short AntiAliasingLevel = 8;
        @MSLAFileField(order = 9) @MSLAOption(MSLAOption.GreyLevel) Short GreyLevel = 1;
        @MSLAFileField(order = 10) @MSLAOption(MSLAOption.BlurLevel) Short BlurLevel = 0;
        @MSLAFileField(order = 11) GOOFilePreview SmallPreview = new GOOFilePreview(new Size(116, 116));
        @MSLAFileField(order = 11) GOOFilePreview BigPreview = new GOOFilePreview(new Size(290, 290));
        @MSLAFileField(order = 12) int LayerCount;
        @MSLAFileField(order = 13) short ResolutionX() { return (short) Resolution.getWidth(); }
        private void setResolutionX(short width) { Resolution = new Size(width, Resolution.getHeight()); }
        @MSLAFileField(order = 14) short ResolutionY() { return (short) Resolution.getHeight(); }
        private void setResolutionY(short height) { Resolution = new Size(Resolution.getWidth(), height); }
        @MSLAFileField(order = 15) @MSLAOption("Mirror X") boolean MirrorX;
        @MSLAFileField(order = 16) @MSLAOption("Mirror Y") boolean MirrorY;
        @MSLAFileField(order = 17) float DisplayWidth;
        @MSLAFileField(order = 18) float DisplayHeight;
        @MSLAFileField(order = 19) float MachineZ;
        @MSLAFileField(order = 20) @MSLAOption(MSLAOption.LayerHeight) float LayerHeight;
        @MSLAFileField(order = 21) @MSLAOption(MSLAOption.ExposureTime) float ExposureTime;
        @MSLAFileField(order = 22) @MSLAOption byte DelayMode = DelayModes.WaitTime.value;
        @MSLAFileField(order = 23) @MSLAOption("Light off delay") float LightOffDelay;
        @MSLAFileField(order = 24) @MSLAOption("Bottom layers wait after cure") float BottomWaitTimeAfterCure;
        @MSLAFileField(order = 25) @MSLAOption("Bottom layers wait after lift") float BottomWaitTimeAfterLift;
        @MSLAFileField(order = 26) @MSLAOption("Bottom layers wait before cure") float BottomWaitTimeBeforeCure;
        @MSLAFileField(order = 27) @MSLAOption("Normal layers wait after cure") float WaitTimeAfterCure;
        @MSLAFileField(order = 28) @MSLAOption("Normal layers wait after lift") float WaitTimeAfterLift;
        @MSLAFileField(order = 29) @MSLAOption(MSLAOption.WaitBeforeCure) float WaitTimeBeforeCure;
        @MSLAFileField(order = 30) @MSLAOption(MSLAOption.BottomExposureTime) float BottomExposureTime;
        @MSLAFileField(order = 31) @MSLAOption(MSLAOption.BottomLayersCount) int BottomLayerCount;
        @MSLAFileField(order = 32) @MSLAOption(MSLAOption.BottomLiftHeight) float BottomLiftHeight;
        @MSLAFileField(order = 33) @MSLAOption(MSLAOption.BottomLiftSpeed) float BottomLiftSpeed;
        @MSLAFileField(order = 34) @MSLAOption(MSLAOption.LiftHeight) float LiftHeight;
        @MSLAFileField(order = 35) @MSLAOption(MSLAOption.LiftSpeed) float LiftSpeed;
        @MSLAFileField(order = 36) @MSLAOption("Bottom layers retract height") float BottomRetractHeight;
        @MSLAFileField(order = 37) @MSLAOption("Bottom layers retract speed") float BottomRetractSpeed;
        @MSLAFileField(order = 38) @MSLAOption(MSLAOption.RetractHeight) float RetractHeight;
        @MSLAFileField(order = 39) @MSLAOption(MSLAOption.RetractSpeed) float RetractSpeed;

        @MSLAFileField(order = 40) @MSLAOption float BottomLiftHeight2;
        @MSLAFileField(order = 41) @MSLAOption float BottomLiftSpeed2;
        @MSLAFileField(order = 42) @MSLAOption float LiftHeight2;
        @MSLAFileField(order = 43) @MSLAOption float LiftSpeed2;
        @MSLAFileField(order = 44) @MSLAOption float BottomRetractHeight2;
        @MSLAFileField(order = 45) @MSLAOption float BottomRetractSpeed2;
        @MSLAFileField(order = 46) @MSLAOption float RetractHeight2;
        @MSLAFileField(order = 47) @MSLAOption float RetractSpeed2;

        @MSLAFileField(order = 48) @MSLAOption("Bottom layers PWM") short BottomLightPWM = DefaultBottomLightPWM;
        @MSLAFileField(order = 49) @MSLAOption("Normal layers PWM") short LightPWM = DefaultLightPWM;
        @MSLAFileField(order = 50) @MSLAOption boolean PerLayerSettings; // 0: Normal mode, 1: Advance mode, printing use the value of "Layer Definition Content"
        @MSLAFileField(order = 51) int PrintTime;
        @MSLAFileField(order = 52) float Volume; // The volume of all parts. unit: mm3
        @MSLAFileField(order = 53) float MaterialGrams; // The weight of all parts. unit: g
        @MSLAFileField(order = 54) @MSLAOption float MaterialCost;
        @MSLAFileField(length = 8, order = 55) @MSLAOption String PriceCurrencySymbol = "$"; // 8 bytes
        @MSLAFileField(order = 56) int LayerDefAddress; // Always after the header at 195477
        @MSLAFileField(order = 57) byte GrayScaleLevel = 1; // 0：The range of pixel's gray value is from 0x0 ~ 0xf, 1：The range of pixel's gray value is from 0x0 ~ 0xff
        @MSLAFileField(order = 58) @MSLAOption("Transition layers count") short TransitionLayerCount;

        private String formatDate(Date date) {
            var dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            return dateFormat.format(date);
        }
    }

    public GOOFileHeader() { fields = new Fields(); }
    public GOOFileHeader(MSLAFileDefaults defaults) throws MSLAException {
        this();
        defaults.setFields("Header", fields);
        fields.MachineName = defaults.getName();
    }

    @Override public int getDataLength() { return HEADER_LENGTH; }
    @Override public String toString() { return "-- Header --\n" + fields.fieldsAsString(" = ", "\n"); }

}
