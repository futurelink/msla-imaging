package futurelink.msla.formats.chitubox.encrypted.tables;

import futurelink.msla.formats.chitubox.common.tables.CTBFileBlock;
import futurelink.msla.formats.iface.MSLAFileBlockFields;
import futurelink.msla.formats.iface.annotations.MSLAFileField;
import futurelink.msla.formats.iface.annotations.MSLAOption;
import futurelink.msla.formats.io.FileFieldsException;
import futurelink.msla.utils.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
public class CTBEncryptedFileSlicerSettings extends CTBFileBlock {
    private final Fields fileFields;

    @SuppressWarnings("unused")
    public static class Fields implements MSLAFileBlockFields {
        private Size Resolution = null;

        @MSLAFileField @Getter Long ChecksumValue = 0xCAFEBABEL;
        @MSLAFileField(order = 1) @Getter Integer LayersDefinitionOffset;
        @MSLAFileField(order = 2) @MSLAOption @Getter Float DisplayWidth;
        @MSLAFileField(order = 3) @MSLAOption @Getter Float DisplayHeight;
        @MSLAFileField(order = 4) @MSLAOption @Getter Float MachineZ;
        @MSLAFileField(order = 5) final private Integer Unknown1 = 0;
        @MSLAFileField(order = 6) final private Integer Unknown2 = 0;
        @MSLAFileField(order = 7) @MSLAOption @Getter Float TotalHeightMillimeter;
        @MSLAFileField(order = 8) @MSLAOption @Getter Float LayerHeight;
        @MSLAFileField(order = 9) @MSLAOption @Getter Float ExposureTime;
        @MSLAFileField(order = 10) @MSLAOption @Getter Float BottomExposureTime;
        @MSLAFileField(order = 11) @MSLAOption @Getter Float LightOffDelay;
        @MSLAFileField(order = 12) @MSLAOption @Getter Integer BottomLayerCount;
        @MSLAFileField(order = 13) private Integer ResolutionX() { return Resolution != null ? Resolution.getWidth() : 0; }
        private void setResolutionX(Integer width) { Resolution = new Size(width, ResolutionY()); }
        @MSLAFileField(order = 14) private Integer ResolutionY() { return  Resolution != null ? Resolution.getHeight() : 0; }
        private void setResolutionY(Integer height) { Resolution = new Size(ResolutionX(), height); }
        @MSLAFileField(order = 15) @Getter @Setter Integer LayerCount;
        @MSLAFileField(order = 16) @MSLAOption @Getter Integer PreviewLargeOffset;
        @MSLAFileField(order = 17) @MSLAOption @Getter Integer PreviewSmallOffset;
        @MSLAFileField(order = 18) @MSLAOption @Getter Integer PrintTime;
        @MSLAFileField(order = 19) @MSLAOption @Getter Integer ProjectorType;
        @MSLAFileField(order = 20) @MSLAOption @Getter Float BottomLiftHeight;
        @MSLAFileField(order = 21) @MSLAOption @Getter Float BottomLiftSpeed;
        @MSLAFileField(order = 22) @MSLAOption @Getter Float LiftHeight;
        @MSLAFileField(order = 23) @MSLAOption @Getter Float LiftSpeed;
        @MSLAFileField(order = 24) @MSLAOption @Getter Float RetractSpeed;
        @MSLAFileField(order = 25) @MSLAOption @Getter Float MaterialMilliliters;
        @MSLAFileField(order = 26) @MSLAOption @Getter Float MaterialGrams;
        @MSLAFileField(order = 27) @MSLAOption @Getter Float MaterialCost;
        @MSLAFileField(order = 28) @MSLAOption @Getter Float BottomLightOffDelay;
        @MSLAFileField(order = 29) final private Integer Unknown3 = 1;
        @MSLAFileField(order = 30) @MSLAOption @Getter Short LightPWM;
        @MSLAFileField(order = 31) @MSLAOption @Getter Short BottomLightPWM;
        @MSLAFileField(order = 32) @Getter Integer LayerXORKey;
        @MSLAFileField(order = 33) @MSLAOption @Getter Float BottomLiftHeight2;
        @MSLAFileField(order = 34) @MSLAOption @Getter Float BottomLiftSpeed2;
        @MSLAFileField(order = 35) @MSLAOption @Getter Float LiftHeight2;
        @MSLAFileField(order = 36) @MSLAOption @Getter Float LiftSpeed2;
        @MSLAFileField(order = 37) @MSLAOption @Getter Float RetractHeight2;
        @MSLAFileField(order = 38) @MSLAOption @Getter Float RetractSpeed2;
        @MSLAFileField(order = 39) @MSLAOption @Getter Float RestTimeAfterLift;
        @MSLAFileField(order = 40) @MSLAOption @Getter Integer MachineNameOffset;
        @MSLAFileField(order = 41) @Getter Integer MachineNameSize;
        @MSLAFileField(order = 42) @MSLAOption @Getter Byte AntiAliasFlag = 0x0F; // 7(0x7) [No AA] / 15(0x0F) [AA]
        @MSLAFileField(order = 43) final Short Padding = 0;
        @MSLAFileField(order = 44) @MSLAOption @Getter Byte PerLayerSettings; // Not totally understood. 0 to not support, 0x40 to 0x50 to allow per layer parameters
        @MSLAFileField(order = 45) final private Integer Unknown4 = 0;
        @MSLAFileField(order = 46) final private Integer Unknown5 = 8; // Also 1
        @MSLAFileField(order = 47) @MSLAOption @Getter Float RestTimeAfterRetract;
        @MSLAFileField(order = 48) @MSLAOption @Getter Float RestTimeAfterLift2;
        @MSLAFileField(order = 49) @MSLAOption @Getter Integer TransitionLayerCount;
        @MSLAFileField(order = 50) @MSLAOption @Getter Float BottomRetractSpeed;
        @MSLAFileField(order = 51) @MSLAOption @Getter Float BottomRetractSpeed2;
        @MSLAFileField(order = 52) final private Integer Padding1 = 0;
        @MSLAFileField(order = 53) final private Float Four1 = 4.0f;
        @MSLAFileField(order = 54) final private Integer Padding2 = 0;
        @MSLAFileField(order = 55) final private Float Four2 = 4.0f;
        @MSLAFileField(order = 56) @MSLAOption @Getter Float RestTimeAfterRetract2;
        @MSLAFileField(order = 57) @MSLAOption @Getter Float RestTimeAfterLift3;
        @MSLAFileField(order = 58) @MSLAOption @Getter Float RestTimeBeforeLift;
        @MSLAFileField(order = 59) @MSLAOption @Getter Float BottomRetractHeight2;
        @MSLAFileField(order = 60) final private Integer Unknown6 = 0;
        @MSLAFileField(order = 61) final private Integer Unknown7 = 0;
        @MSLAFileField(order = 62) final private Integer Unknown8 = 4;
        @MSLAFileField(order = 63) @Getter Integer LastLayerIndex;
        @MSLAFileField(order = 64) final private Integer Padding3 = 0;
        @MSLAFileField(order = 65) final private Integer Padding4 = 0;
        @MSLAFileField(order = 66) final private Integer Padding5 = 0;
        @MSLAFileField(order = 67) final private Integer Padding6 = 0;
        @MSLAFileField(order = 68) @Getter Integer DisclaimerOffset;
        @MSLAFileField(order = 69) @Getter Integer DisclaimerSize;
        @MSLAFileField(order = 70) final private Integer Padding7 = 0;
        @MSLAFileField(order = 71) @Getter Integer ResinParametersAddress;
        @MSLAFileField(order = 72) final private Integer Padding8 = 0;
        @MSLAFileField(order = 73) final private Integer Padding9 = 0;
    }

    public CTBEncryptedFileSlicerSettings(Integer version) { super(version); fileFields = new Fields(); }

    @Override public String getName() { return "SlicerSettings"; }
    @Override public int getDataLength() throws FileFieldsException { return 0; }
    @Override public int getDataFieldOffset(String fieldName) throws FileFieldsException { return 0; }
}
