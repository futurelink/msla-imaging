package futurelink.msla.formats.chitubox.tables;

import futurelink.msla.formats.MSLAFileBlock;
import futurelink.msla.formats.MSLAOption;
import futurelink.msla.formats.MSLAOptionContainer;
import lombok.Getter;

import java.io.*;
import java.nio.charset.StandardCharsets;

@MSLAOptionContainer(className=ChituBoxDLPFileSliceInfo.class)
public class ChituBoxDLPFileSliceInfo implements MSLAFileBlock {
    @Getter private Integer DisplayWidthLength;
    @Getter private String DisplayWidth;            // UTF-16
    @Getter private Integer DisplayHeightLength;
    @Getter private String DisplayHeight;           // UTF-16
    @Getter private Integer LayerHeightLength = 8;
    @MSLAOption @Getter private String LayerHeight = "0.05";    // UTF-16
    @MSLAOption @Getter private Short ExposureTime;
    @MSLAOption @Getter private Short WaitTimeBeforeCure = 1;   // 1 as minimum or it won't print!
    @MSLAOption @Getter private Short BottomExposureTime;
    @MSLAOption @Getter private Short BottomLayersCount;
    @MSLAOption @Getter private Short BottomLiftHeight;
    @MSLAOption @Getter private Short BottomLiftSpeed;
    @MSLAOption @Getter private Short LiftHeight;
    @MSLAOption @Getter private Short LiftSpeed;
    @MSLAOption @Getter private Short RetractSpeed;
    @MSLAOption @Getter private Short BottomLightPWM = 255;
    @MSLAOption @Getter private Short LightPWM = 255;

    @Override
    public int getDataLength() {
        return (DisplayWidthLength + DisplayHeightLength + LayerHeightLength) + 22 + 12;
    }

    @Override
    public void read(FileInputStream stream, int position) throws IOException {
        var fc = stream.getChannel();
        fc.position(position);

        var dis = new DataInputStream(stream);
        DisplayWidthLength = dis.readInt();
        DisplayWidth = new String(dis.readNBytes(DisplayWidthLength), StandardCharsets.UTF_16);
        DisplayHeightLength = dis.readInt();
        DisplayHeight = new String(dis.readNBytes(DisplayHeightLength), StandardCharsets.UTF_16);
        LayerHeightLength = dis.readInt();
        LayerHeight = new String(dis.readNBytes(LayerHeightLength), StandardCharsets.UTF_16);
        ExposureTime = dis.readShort();
        WaitTimeBeforeCure = dis.readShort();
        BottomExposureTime = dis.readShort();
        BottomLayersCount = dis.readShort();
        BottomLiftHeight = dis.readShort();
        BottomLiftSpeed = dis.readShort();
        LiftHeight = dis.readShort();
        LiftSpeed = dis.readShort();
        RetractSpeed = dis.readShort();
        BottomLightPWM = dis.readShort();
        LightPWM = dis.readShort();
    }

    @Override
    public void write(OutputStream stream) throws IOException {

    }

    @Override
    public String toString() {
        return "-- Slicer Info --\n" +
                "DisplayWidthLength: " + DisplayWidthLength + "\n" +
                "DisplayWidth: " + DisplayWidth + "\n" +
                "DisplayHeightLength: " + DisplayHeightLength + "\n" +
                "DisplayHeight: " + DisplayHeight + "\n" +
                "LayerHeightLength: " + LayerHeightLength + "\n" +
                "LayerHeight: " + LayerHeight + "\n" +
                "ExposureTime: " + ExposureTime + "\n" +
                "WaitTimeBeforeCure: " + WaitTimeBeforeCure + "\n" +
                "BottomExposureTime: " + BottomExposureTime + "\n" +
                "BottomLayersCount: " + BottomLayersCount + "\n" +
                "BottomLiftHeight: " + BottomLiftHeight + "\n" +
                "BottomLiftSpeed: " + BottomLiftSpeed + "\n" +
                "LiftHeight: " + LiftHeight + "\n" +
                "LiftSpeed: " + LiftSpeed + "\n" +
                "RetractSpeed: " + RetractSpeed + "\n" +
                "BottomLightPWM: " + BottomLightPWM + "\n" +
                "LightPWM: " + LightPWM + "\n";
    }
}
