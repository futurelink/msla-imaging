package futurelink.msla.formats.elegoo.tables;

import com.google.common.io.LittleEndianDataOutputStream;
import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.anycubic.tables.PhotonWorkshopFileTable;
import futurelink.msla.formats.iface.*;
import futurelink.msla.formats.utils.FileFieldsReader;
import futurelink.msla.formats.utils.FileFieldsWriter;
import futurelink.msla.formats.utils.Size;
import lombok.Getter;
import lombok.experimental.Delegate;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

@MSLAOptionContainer(className = GOOFileHeader.Fields.class)
public class GOOFileHeader implements MSLAFileBlock {
    enum DelayModes {
        LightOff((byte) 0), WaitTime((byte) 1);
        public final byte value; DelayModes(byte value) { this.value = value; }
    }
    private static final byte DefaultBottomLightPWM = 0x01;
    private static final byte DefaultLightPWM = 0x02;
    @Delegate private final Fields fields;

    @Getter
    public static class Fields implements MSLAFileBlockFields {
        float PixelSizeUm;
        Size Resolution; // Goes to file as short values: ResolutionX and ResolutionY
        @MSLAFileField(length = 4) private final String Version = "V3.0"; // 4 bytes
        @MSLAFileField(length = 8, order = 1) byte[] Magic = { 0x07, 0x00, 0x00, 0x00, 0x44, 0x4C, 0x50, 0x00 }; // 8 bytes
        @MSLAFileField(length = 32, order = 2) String SoftwareName = "mSLA-imaging library"; // 32 bytes
        @MSLAFileField(length = 24, order = 3) String SoftwareVersion = "1.0"; // 24 bytes
        @MSLAFileField(length = 24, order = 4) String FileCreateTime = formatDate(new Date());
        @MSLAFileField(length = 32, order = 5) String MachineName = ""; // 32 bytes
        @MSLAFileField(length = 32, order = 6) String MachineType = "DLP"; // 32 bytes
        @MSLAFileField(length = 32, order = 7) String ProfileName = ""; // 32 bytes
        @MSLAFileField(order = 8) @MSLAOption Short AntiAliasingLevel = 8;
        @MSLAFileField(order = 9) @MSLAOption Short GreyLevel = 1;
        @MSLAFileField(order = 10) @MSLAOption Short BlurLevel = 0;
        @MSLAFileField(order = 11) byte[] SmallPreview565 = new byte[116 * 116 * 2];
        @MSLAFileField(order = 12) byte[] SmallPreviewDelimiter = new byte[]{0x0d, 0x0a};
        @MSLAFileField(order = 13) byte[] BigPreview565 = new byte[290 * 290 * 2];
        @MSLAFileField(order = 14) byte[] BigPreviewDelimiter = new byte[]{0x0d, 0x0a};
        @MSLAFileField(order = 15) int LayerCount;
        @MSLAFileField(order = 16) short ResolutionX() { return (short) Resolution.getWidth(); }
        @MSLAFileField(order = 17) short ResolutionY() { return (short) Resolution.getHeight(); }
        @MSLAFileField(order = 18) @MSLAOption boolean MirrorX;
        @MSLAFileField(order = 19) @MSLAOption boolean MirrorY;
        @MSLAFileField(order = 20) float DisplayWidth;
        @MSLAFileField(order = 21) float DisplayHeight;
        @MSLAFileField(order = 22) float MachineZ;
        @MSLAFileField(order = 23) @MSLAOption float LayerHeight;
        @MSLAFileField(order = 24) @MSLAOption float ExposureTime;
        @MSLAFileField(order = 25) @MSLAOption byte DelayMode = DelayModes.WaitTime.value; // 1 byte, 0: Light off delay mode | 1：Wait time mode
        @MSLAFileField(order = 26) @MSLAOption float LightOffDelay;
        @MSLAFileField(order = 27) @MSLAOption float BottomWaitTimeAfterCure;
        @MSLAFileField(order = 28) @MSLAOption float BottomWaitTimeAfterLift;
        @MSLAFileField(order = 29) @MSLAOption float BottomWaitTimeBeforeCure;
        @MSLAFileField(order = 30) @MSLAOption float WaitTimeAfterCure;
        @MSLAFileField(order = 31) @MSLAOption float WaitTimeAfterLift;
        @MSLAFileField(order = 32) @MSLAOption float WaitTimeBeforeCure;
        @MSLAFileField(order = 33) @MSLAOption float BottomExposureTime;
        @MSLAFileField(order = 34) int BottomLayerCount;
        @MSLAFileField(order = 35) @MSLAOption float BottomLiftHeight;
        @MSLAFileField(order = 36) @MSLAOption float BottomLiftSpeed;
        @MSLAFileField(order = 37) @MSLAOption float LiftHeight;
        @MSLAFileField(order = 38) @MSLAOption float LiftSpeed;
        @MSLAFileField(order = 39) @MSLAOption float BottomRetractHeight;
        @MSLAFileField(order = 40) @MSLAOption float BottomRetractSpeed;
        @MSLAFileField(order = 41) @MSLAOption float RetractHeight;
        @MSLAFileField(order = 42) @MSLAOption float RetractSpeed;
        @MSLAFileField(order = 43) @MSLAOption float BottomLiftHeight2;
        @MSLAFileField(order = 44) @MSLAOption float BottomLiftSpeed2;
        @MSLAFileField(order = 45) @MSLAOption float LiftHeight2;
        @MSLAFileField(order = 46) @MSLAOption float LiftSpeed2;
        @MSLAFileField(order = 47) @MSLAOption float BottomRetractHeight2;
        @MSLAFileField(order = 48) @MSLAOption float BottomRetractSpeed2;
        @MSLAFileField(order = 49) @MSLAOption float RetractHeight2;
        @MSLAFileField(order = 50) @MSLAOption float RetractSpeed2;
        @MSLAFileField(order = 51) @MSLAOption short BottomLightPWM = DefaultBottomLightPWM;
        @MSLAFileField(order = 52) @MSLAOption short LightPWM = DefaultLightPWM;
        @MSLAFileField(order = 53) @MSLAOption boolean PerLayerSettings; // 0: Normal mode, 1: Advance mode, printing use the value of "Layer Definition Content"
        @MSLAFileField(order = 54) int PrintTime;
        @MSLAFileField(order = 55) float Volume; // The volume of all parts. unit: mm3
        @MSLAFileField(order = 56) float MaterialGrams; // The weight of all parts. unit: g
        @MSLAFileField(order = 57) @MSLAOption float MaterialCost;
        @MSLAFileField(length = 8, order = 58) @MSLAOption String PriceCurrencySymbol = "$"; // 8 bytes
        @MSLAFileField(order = 59) int LayerDefAddress; // 195477
        @MSLAFileField(order = 60) byte GrayScaleLevel = 1; // 0：The range of pixel's gray value is from 0x0 ~ 0xf, 1：The range of pixel's gray value is from 0x0 ~ 0xff
        @MSLAFileField(order = 61) @MSLAOption short TransitionLayerCount;

        private String formatDate(Date date) {
            var dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            return dateFormat.format(date);
        }
    }

    public GOOFileHeader() { fields = new Fields(); }
    public GOOFileHeader(MSLAFileDefaults defaults) throws MSLAException {
        this();
        defaults.setFields("Header", fields);
    }

    @Override
    public int getDataLength() {
        return 0;
    }

    @Override
    public void read(FileInputStream stream, int position) throws MSLAException {
        try {
            var reader = new FileFieldsReader(stream, FileFieldsReader.Endianness.BigEndian);
            var dataRead = reader.read(fields);
        } catch (IOException e) { throw new MSLAException("Error reading " + this.getClass().getName() + " table", e); }
    }

    @Override
    public void write(OutputStream stream) throws MSLAException {
        try {
            var writer = new FileFieldsWriter(stream, FileFieldsWriter.Endianness.BigEndian);
            writer.write(fields);
        } catch (IOException e) {
            throw new MSLAException("Error writing " + this.getClass().getName() + " table", e);
        }
    }
}
