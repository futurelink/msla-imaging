package futurelink.msla.formats.elegoo.tables;

import futurelink.msla.formats.iface.MSLAFileBlockFields;
import futurelink.msla.formats.iface.MSLAFileField;
import lombok.Getter;

@Getter
public class GOOFileLayerDef extends GOOFileTable {
    private final Fields fields = new Fields();

    @SuppressWarnings("unused")
    static class Fields implements MSLAFileBlockFields {
        @MSLAFileField short Pause;
        @MSLAFileField(order = 1) float PausePositionZ;
        @MSLAFileField(order = 2) float PositionZ;
        @MSLAFileField(order = 3) float ExposureTime;
        @MSLAFileField(order = 4) float LightOffDelay;
        @MSLAFileField(order = 5) float WaitTimeAfterCure;
        @MSLAFileField(order = 6) float WaitTimeAfterLift;
        @MSLAFileField(order = 7) float WaitTimeBeforeCure;
        @MSLAFileField(order = 8) float LiftHeight;
        @MSLAFileField(order = 9) float LiftSpeed;
        @MSLAFileField(order = 10) float LiftHeight2;
        @MSLAFileField(order = 11) float LiftSpeed2;
        @MSLAFileField(order = 12) float RetractHeight;
        @MSLAFileField(order = 13) float RetractSpeed;
        @MSLAFileField(order = 14) float RetractHeight2;
        @MSLAFileField(order = 15) float RetractSpeed2;
        @MSLAFileField(order = 16) short LightPWM;
        @MSLAFileField(order = 17, length = 2) public byte[] DelimiterData = new byte[]{ 0x0d, 0x0a };
        @MSLAFileField(order = 18) int DataLength;
    }

    @Override
    public int getDataLength() { return 0; }
}
