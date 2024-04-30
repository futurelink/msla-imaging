package futurelink.msla.formats.creality.tables;

import futurelink.msla.formats.iface.MSLAFileBlockFields;
import futurelink.msla.formats.iface.MSLAOption;
import futurelink.msla.formats.iface.MSLAOptionContainer;
import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.nio.charset.StandardCharsets;

@MSLAOptionContainer(className= CXDLPFileSliceInfo.Fields.class)
public class CXDLPFileSliceInfo extends CXDLPFileTable {
    public static class Fields implements MSLAFileBlockFields {
        @Getter private Integer DisplayWidthLength;
        @Getter private String DisplayWidth;            // UTF-16
        @Getter private Integer DisplayHeightLength;
        @Getter private String DisplayHeight;           // UTF-16
        @Getter private Integer LayerHeightLength = 8;
        @MSLAOption(type=String.class) @Getter private String LayerHeight = "0.05";    // UTF-16
        @MSLAOption @Getter @Setter private Short ExposureTime;
        @MSLAOption @Getter @Setter private Short WaitTimeBeforeCure = 1;   // 1 as minimum or it won't print!
        @MSLAOption @Getter @Setter private Short BottomExposureTime;
        @MSLAOption @Getter @Setter private Short BottomLayersCount;
        @MSLAOption @Getter @Setter private Short BottomLiftHeight;
        @MSLAOption @Getter @Setter private Short BottomLiftSpeed;
        @MSLAOption @Getter @Setter private Short LiftHeight;
        @MSLAOption @Getter @Setter private Short LiftSpeed;
        @MSLAOption @Getter @Setter private Short RetractSpeed;
        @MSLAOption @Getter @Setter private Short BottomLightPWM = 255;
        @MSLAOption @Getter @Setter private Short LightPWM = 255;

        public Fields() {}

        public Fields(Fields defaults) {
            DisplayWidthLength = defaults.DisplayWidthLength;
            DisplayWidth = defaults.DisplayWidth;
            DisplayHeightLength = defaults.DisplayHeightLength;
            DisplayHeight = defaults.DisplayHeight;
            LayerHeightLength = defaults.LayerHeightLength;
            LayerHeight = defaults.LayerHeight;
            ExposureTime = defaults.ExposureTime;
            WaitTimeBeforeCure = defaults.WaitTimeBeforeCure;
            BottomExposureTime = defaults.BottomExposureTime;
            BottomLayersCount = defaults.BottomLayersCount;
            BottomLiftHeight = defaults.BottomLiftHeight;
            BottomLiftSpeed = defaults.BottomLiftSpeed;
            LiftHeight = defaults.LiftHeight;
            LiftSpeed = defaults.LiftSpeed;
            RetractSpeed = defaults.RetractSpeed;
            BottomLightPWM = defaults.BottomLightPWM;
            LightPWM = defaults.LightPWM;
        }

        public int getDataLength() { return DisplayWidthLength + DisplayHeightLength + LayerHeightLength + 22 + 12; }

        public void setDisplayHeight(String displayHeight) {
            DisplayHeight = displayHeight;
            DisplayHeightLength = displayHeight.length() * 2;
        }

        public void setDisplayWidth(String displayWidth) {
            DisplayWidth = displayWidth;
            DisplayWidthLength = displayWidth.length() * 2;
        }

        public void setLayerHeight(String layerHeight) {
            LayerHeight = layerHeight;
            LayerHeightLength = layerHeight.length() * 2;
        }
    }

    private final Fields fields;

    public CXDLPFileSliceInfo() {
        fields = new Fields();
    }

    public CXDLPFileSliceInfo(MSLAFileBlockFields defaults) {
        fields = new Fields((Fields) defaults);
    }

    @Override
    public int getDataLength() { return fields.getDataLength(); }

    @Override
    public void read(FileInputStream stream, int position) throws IOException {
        var fc = stream.getChannel();
        fc.position(position);

        var dis = new DataInputStream(stream);
        fields.DisplayWidthLength = dis.readInt();
        fields.DisplayWidth = new String(dis.readNBytes(fields.DisplayWidthLength), StandardCharsets.UTF_16BE);
        fields.DisplayHeightLength = dis.readInt();
        fields.DisplayHeight = new String(dis.readNBytes(fields.DisplayHeightLength), StandardCharsets.UTF_16BE);
        fields.LayerHeightLength = dis.readInt();
        fields.LayerHeight = new String(dis.readNBytes(fields.LayerHeightLength), StandardCharsets.UTF_16BE);
        fields.ExposureTime = dis.readShort();
        fields.WaitTimeBeforeCure = dis.readShort();
        fields.BottomExposureTime = dis.readShort();
        fields.BottomLayersCount = dis.readShort();
        fields.BottomLiftHeight = dis.readShort();
        fields.BottomLiftSpeed = dis.readShort();
        fields.LiftHeight = dis.readShort();
        fields.LiftSpeed = dis.readShort();
        fields.RetractSpeed = dis.readShort();
        fields.BottomLightPWM = dis.readShort();
        fields.LightPWM = dis.readShort();
    }

    @Override
    public void write(OutputStream stream) throws IOException {
        var dos = new DataOutputStream(stream);
        dos.writeInt(fields.DisplayWidthLength);
        dos.write(fields.DisplayWidth.getBytes(StandardCharsets.UTF_16BE));
        dos.writeInt(fields.DisplayHeightLength);
        dos.write(fields.DisplayHeight.getBytes(StandardCharsets.UTF_16BE));
        dos.writeInt(fields.LayerHeightLength);
        dos.write(fields.LayerHeight.getBytes(StandardCharsets.UTF_16BE));
        dos.writeShort(fields.ExposureTime);
        dos.writeShort(fields.WaitTimeBeforeCure);
        dos.writeShort(fields.BottomExposureTime);
        dos.writeShort(fields.BottomLayersCount);
        dos.writeShort(fields.BottomLiftHeight);
        dos.writeShort(fields.BottomLiftSpeed);
        dos.writeShort(fields.LiftHeight);
        dos.writeShort(fields.LiftSpeed);
        dos.writeShort(fields.RetractSpeed);
        dos.writeShort(fields.BottomLightPWM);
        dos.writeShort(fields.LightPWM);
    }

    @Override
    public String toString() {
        return "-- Slicer Info --\n" +
                "DisplayWidthLength: " + fields.DisplayWidthLength + "\n" +
                "DisplayWidth: " + fields.DisplayWidth + "\n" +
                "DisplayHeightLength: " + fields.DisplayHeightLength + "\n" +
                "DisplayHeight: " + fields.DisplayHeight + "\n" +
                "LayerHeightLength: " + fields.LayerHeightLength + "\n" +
                "LayerHeight: " + fields.LayerHeight + "\n" +
                "ExposureTime: " + fields.ExposureTime + "\n" +
                "WaitTimeBeforeCure: " + fields.WaitTimeBeforeCure + "\n" +
                "BottomExposureTime: " + fields.BottomExposureTime + "\n" +
                "BottomLayersCount: " + fields.BottomLayersCount + "\n" +
                "BottomLiftHeight: " + fields.BottomLiftHeight + "\n" +
                "BottomLiftSpeed: " + fields.BottomLiftSpeed + "\n" +
                "LiftHeight: " + fields.LiftHeight + "\n" +
                "LiftSpeed: " + fields.LiftSpeed + "\n" +
                "RetractSpeed: " + fields.RetractSpeed + "\n" +
                "BottomLightPWM: " + fields.BottomLightPWM + "\n" +
                "LightPWM: " + fields.LightPWM + "\n";
    }
}
