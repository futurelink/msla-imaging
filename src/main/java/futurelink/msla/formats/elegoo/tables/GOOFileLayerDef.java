package futurelink.msla.formats.elegoo.tables;

import futurelink.msla.formats.iface.MSLAFileBlockFields;
import futurelink.msla.formats.iface.annotations.MSLAFileField;
import lombok.Getter;

@Getter
public class GOOFileLayerDef extends GOOFileTable {
    private final Fields fields = new Fields();

    @SuppressWarnings("unused")
    static public class Fields implements MSLAFileBlockFields {
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
        @MSLAFileField(order = 17, length = 2) public byte[] Delimiter1 = new byte[]{ 0x0d, 0x0a };
        @MSLAFileField(order = 18) int DataLength = 0;
        @MSLAFileField(order = 19, lengthAt = "DataLength") @Getter byte[] Data;
        @MSLAFileField(order = 20, length = 2) public byte[] Delimiter2 = new byte[]{ 0x0d, 0x0a };
    }

    @Override
    public int getDataLength() { return 72 + fields.DataLength; }

    @Override
    public String toString() {
        return "Layer { " +
                "DataLength = " + fields.DataLength + ", " +
                "Pause = " + fields.Pause + ", " +
                "PausePositionZ = " + fields.PausePositionZ + ", " +
                "PositionZ = " + fields.PositionZ + ", " +
                "ExposureTime = " + fields.ExposureTime + ", " +
                "LightOffDelay = " + fields.LightOffDelay + ", " +
                "WaitTimeAfterCure = " + fields.WaitTimeAfterCure + ", " +
                "WaitTimeAfterLift = " + fields.WaitTimeAfterLift + ", " +
                "WaitTimeBeforeCure = " + fields.WaitTimeBeforeCure + ", " +
                "LiftHeight = " + fields.LiftHeight + ", " +
                "LiftSpeed = " + fields.LiftSpeed + ", " +
                "LiftHeight2 = " + fields.LiftHeight2 + ", " +
                "LiftSpeed2 = " + fields.LiftSpeed2 + ", " +
                "RetractHeight = " + fields.RetractHeight + ", " +
                "RetractSpeed = " + fields.RetractSpeed + ", " +
                "RetractHeight2 = " + fields.RetractHeight2 + ", " +
                "RetractSpeed2 = " + fields.RetractSpeed2 + ", " +
                "LightPWM = " + fields.LightPWM + ", " +
                " } \n";
    }
}
