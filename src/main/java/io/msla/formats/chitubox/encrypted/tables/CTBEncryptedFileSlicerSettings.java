package io.msla.formats.chitubox.encrypted.tables;

import io.msla.formats.MSLAException;
import io.msla.formats.chitubox.common.tables.CTBFileBlock;
import io.msla.formats.iface.MSLAFileBlockFields;
import io.msla.formats.iface.MSLAFileProps;
import io.msla.formats.iface.MSLAFileField;
import io.msla.formats.iface.options.MSLAOption;
import io.msla.formats.iface.options.MSLAOptionName;
import io.msla.formats.io.FileFieldsException;
import io.msla.formats.io.FileFieldsIO;
import io.msla.utils.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Random;

@Getter
public class CTBEncryptedFileSlicerSettings extends CTBFileBlock {
    private final Fields blockFields;
    private final Integer DefaultLayerXORKey = 0xEFBEADDE;

    @SuppressWarnings("unused")
    public static class Fields implements MSLAFileBlockFields {
        @Getter private Size Resolution = null;
        @MSLAFileField @Getter Long ChecksumValue = 0xCAFEBABEL;
        @MSLAFileField(order = 1) @Getter @Setter private Integer LayersDefinitionOffset;
        @MSLAFileField(order = 2) @Getter private Float DisplayWidth;
        @MSLAFileField(order = 3) @Getter private Float DisplayHeight;
        @MSLAFileField(order = 4) @Getter private Float MachineZ;
        @MSLAFileField(order = 5) final private Integer Unknown1 = 0;
        @MSLAFileField(order = 6) final private Integer Unknown2 = 0;
        @MSLAFileField(order = 7) @Getter @Setter private Float TotalHeightMillimeter;
        @MSLAFileField(order = 8) @MSLAOption(MSLAOptionName.LayerHeight) @Getter private Float LayerHeight;
        @MSLAFileField(order = 9) @MSLAOption(MSLAOptionName.NormalLayersExposureTime) @Getter private Float ExposureTime;
        @MSLAFileField(order = 10) @MSLAOption(MSLAOptionName.BottomLayersExposureTime) @Getter private Float BottomExposureTime;
        @MSLAFileField(order = 11) @MSLAOption(MSLAOptionName.NormalLayersLightOffDelay) @Getter private Float LightOffDelay;
        @MSLAFileField(order = 12) @MSLAOption(MSLAOptionName.BottomLayersCount) @Getter private Integer BottomLayerCount;
        @MSLAFileField(order = 13) private Integer ResolutionX() { return Resolution != null ? Resolution.getWidth() : 0; }
        private void setResolutionX(Integer width) { Resolution = new Size(width, ResolutionY()); }
        @MSLAFileField(order = 14) private Integer ResolutionY() { return  Resolution != null ? Resolution.getHeight() : 0; }
        private void setResolutionY(Integer height) { Resolution = new Size(ResolutionX(), height); }
        @MSLAFileField(order = 15) @Getter @Setter private Integer LayerCount = 0;
        @MSLAFileField(order = 16) @Getter @Setter private Integer PreviewLargeOffset;
        @MSLAFileField(order = 17) @Getter @Setter private Integer PreviewSmallOffset;
        @MSLAFileField(order = 18) @MSLAOption(MSLAOptionName.PrintTime) @Getter @Setter private Integer PrintTime;
        @MSLAFileField(order = 19) @Getter private Integer ProjectorType = 0;
        @MSLAFileField(order = 20) @MSLAOption(MSLAOptionName.BottomLayersLiftHeight) @Getter private Float BottomLiftHeight;
        @MSLAFileField(order = 21) @MSLAOption(MSLAOptionName.BottomLayersLiftSpeed) @Getter private Float BottomLiftSpeed;
        @MSLAFileField(order = 22) @MSLAOption(MSLAOptionName.NormalLayersLiftHeight) @Getter private Float LiftHeight;
        @MSLAFileField(order = 23) @MSLAOption(MSLAOptionName.NormalLayersLiftSpeed) @Getter private Float LiftSpeed;
        @MSLAFileField(order = 24) @MSLAOption(MSLAOptionName.RetractSpeed) @Getter private Float RetractSpeed;
        @MSLAFileField(order = 25) @MSLAOption(MSLAOptionName.Volume) @Getter private Float MaterialMilliliters;
        @MSLAFileField(order = 26) @MSLAOption(MSLAOptionName.Weight) @Getter private Float MaterialGrams;
        @MSLAFileField(order = 27) @MSLAOption(MSLAOptionName.Price) @Getter private Float MaterialCost;
        @MSLAFileField(order = 28) @MSLAOption(MSLAOptionName.BottomLayersLightOffDelay) @Getter private Float BottomLightOffDelay;
        @MSLAFileField(order = 29) final private Integer Unknown3 = 1;
        @MSLAFileField(order = 30) @MSLAOption(MSLAOptionName.NormalLayersLightPWM) @Getter private Short LightPWM;
        @MSLAFileField(order = 31) @MSLAOption(MSLAOptionName.BottomLayersLightPWM) @Getter private Short BottomLightPWM;
        @MSLAFileField(order = 32) @Getter private Integer EncryptionKey;
        @MSLAFileField(order = 33) @MSLAOption(MSLAOptionName.BottomLayersLiftHeight2) @Getter private Float BottomLiftHeight2;
        @MSLAFileField(order = 34) @MSLAOption(MSLAOptionName.BottomLayersLiftSpeed2) @Getter private Float BottomLiftSpeed2;
        @MSLAFileField(order = 35) @MSLAOption(MSLAOptionName.NormalLayersLiftHeight2) @Getter private Float LiftHeight2;
        @MSLAFileField(order = 36) @MSLAOption(MSLAOptionName.NormalLayersLiftSpeed2) @Getter private Float LiftSpeed2;
        @MSLAFileField(order = 37) @MSLAOption(MSLAOptionName.NormalLayersRetractHeight2) @Getter private Float RetractHeight2;
        @MSLAFileField(order = 38) @MSLAOption(MSLAOptionName.NormalLayersRetractSpeed2) @Getter private Float RetractSpeed2;
        @MSLAFileField(order = 39) @MSLAOption(MSLAOptionName.WaitAfterLift) @Getter private Float RestTimeAfterLift;
        @MSLAFileField(order = 40) @Getter @Setter private Integer MachineNameOffset;
        @MSLAFileField(order = 41) @Getter @Setter private Integer MachineNameSize;
        @MSLAFileField(order = 42) @MSLAOption(MSLAOptionName.Antialias) @Getter private Byte AntiAliasFlag = 0x0F; // 7(0x7) [No AA] / 15(0x0F) [AA]
        @MSLAFileField(order = 43) final private Short Padding = 0;
        @MSLAFileField(order = 44) @MSLAOption(MSLAOptionName.LayerSettings) @Getter private Byte PerLayerSettings; // Not totally understood. 0 to not support, 0x40 to 0x50 to allow per layer parameters
        @MSLAFileField(order = 45) final private Integer Unknown4 = 0;
        @MSLAFileField(order = 46) @MSLAOption(MSLAOptionName.AntialiasLevel) private Integer AntialiasLevel; // Also 1
        @MSLAFileField(order = 47) @MSLAOption(MSLAOptionName.WaitAfterRetract) @Getter private Float RestTimeAfterRetract;
        @MSLAFileField(order = 48) @MSLAOption(MSLAOptionName.WaitAfterLift2) @Getter private Float RestTimeAfterLift2;
        @MSLAFileField(order = 49) @MSLAOption(MSLAOptionName.TransitionLayersCount) @Getter private Integer TransitionLayerCount;
        @MSLAFileField(order = 50) @MSLAOption(MSLAOptionName.BottomLayersRetractSpeed) @Getter private Float BottomRetractSpeed;
        @MSLAFileField(order = 51) @MSLAOption(MSLAOptionName.BottomLayersRetractSpeed2) @Getter private Float BottomRetractSpeed2;
        @MSLAFileField(order = 52) final private Integer Padding1 = 0;
        @MSLAFileField(order = 53) final private Float Four1 = 4.0f;
        @MSLAFileField(order = 54) final private Integer Padding2 = 0;
        @MSLAFileField(order = 55) final private Float Four2 = 4.0f;
        @MSLAFileField(order = 56) @MSLAOption(MSLAOptionName.WaitAfterRetract2) @Getter private Float RestTimeAfterRetract2;
        @MSLAFileField(order = 57) @MSLAOption(MSLAOptionName.WaitAfterLift3) @Getter private Float RestTimeAfterLift3;
        @MSLAFileField(order = 58) @MSLAOption(MSLAOptionName.WaitBeforeLift) @Getter private Float RestTimeBeforeLift;
        @MSLAFileField(order = 59) @MSLAOption(MSLAOptionName.BottomLayersRetractHeight2) @Getter private Float BottomRetractHeight2;
        @MSLAFileField(order = 60) final private Integer Unknown6 = 0;
        @MSLAFileField(order = 61) final private Integer Unknown7 = 0;
        @MSLAFileField(order = 62) final private Integer Unknown8 = 4;
        @MSLAFileField(order = 63) @Getter @Setter private Integer LastLayerIndex;
        @MSLAFileField(order = 64) final private Integer Padding3 = 0;
        @MSLAFileField(order = 65) final private Integer Padding4 = 0;
        @MSLAFileField(order = 66) final private Integer Padding5 = 0;
        @MSLAFileField(order = 67) final private Integer Padding6 = 0;
        @MSLAFileField(order = 68) @Getter @Setter private Integer DisclaimerOffset;
        @MSLAFileField(order = 69) @Getter @Setter private Integer DisclaimerLength;
        @MSLAFileField(order = 70) final private Integer Padding7 = 0;
        @MSLAFileField(order = 71) @Getter @Setter private Integer ResinParametersOffset;
        @MSLAFileField(order = 72) final private Integer Padding8 = 0;
        @MSLAFileField(order = 73) final private Integer Padding9 = 0;
    }

    public CTBEncryptedFileSlicerSettings(Integer version, MSLAFileProps initialProps) throws MSLAException {
        super(version);
        blockFields = new Fields();
        blockFields.EncryptionKey = new Random().nextInt(Integer.MAX_VALUE);
        if (initialProps != null) {
            blockFields.Resolution = Size.parseSize(initialProps.get("Resolution").getString());
            blockFields.DisplayWidth = initialProps.getFloat("DisplayWidth");
            blockFields.DisplayHeight = initialProps.getFloat("DisplayHeight");
            blockFields.MachineZ = initialProps.getFloat("MachineZ");
        }
    }

    @Override public String getName() { return "SlicerSettings"; }

    @Override public int getDataLength() throws FileFieldsException {
        return FileFieldsIO.getBlockLength(getBlockFields());
    }

    @Override public int getDataFieldOffset(String fieldName) throws FileFieldsException {
        return FileFieldsIO.getBlockLength(getBlockFields(), fieldName);
    }

    @Override public String toString() { return blockFields.fieldsAsString(" = ", "\n"); }
}
