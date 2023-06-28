package futurelink.msla.formats.chitubox.tables;

import futurelink.msla.formats.MSLAFileBlock;
import futurelink.msla.formats.MSLAOption;
import futurelink.msla.formats.MSLAOptionContainer;
import lombok.Getter;

import java.io.*;

@MSLAOptionContainer(className=ChituBoxDLPFileSliceInfoV3.class)
public class ChituBoxDLPFileSliceInfoV3 implements MSLAFileBlock {
    @Getter private Integer SoftwareNameLength;
    @MSLAOption @Getter private String SoftwareName = "mMSLA toolbox";
    @Getter private Integer MaterialNameLength;
    @MSLAOption @Getter private String MaterialName = "";
    @MSLAOption @Getter private Byte DistortionCompensationEnabled;
    @MSLAOption @Getter private Integer DistortionCompensationThickness = 600;
    @MSLAOption @Getter private Integer DistortionCompensationFocalLength = 300000;
    @MSLAOption @Getter private Byte XYAxisProfileCompensationEnabled = 1;
    @MSLAOption @Getter private Short XYAxisProfileCompensationValue;
    @MSLAOption @Getter private Byte ZPenetrationCompensationEnabled;
    @MSLAOption @Getter private Short ZPenetrationCompensationLevel = 1000;
    @MSLAOption @Getter private Byte AntiAliasEnabled = 1;
    @MSLAOption @Getter private Byte AntiAliasGreyMinValue = 1;
    @MSLAOption @Getter private Byte AntiAliasGreyMaxValue = (byte) 0xff;
    @MSLAOption @Getter private Byte ImageBlurEnabled = 0;
    @MSLAOption @Getter private Byte ImageBlurLevel  = 2;

    @Override
    public int getDataLength() {
        return (SoftwareNameLength + MaterialNameLength) + 28 + 2;
    }

    @Override
    public void read(FileInputStream stream, int position) throws IOException {
        var fc = stream.getChannel();
        fc.position(position);

        var dis = new DataInputStream(stream);
        SoftwareNameLength = dis.readInt();
        SoftwareName = new String(dis.readNBytes(SoftwareNameLength)).trim();
        MaterialNameLength = dis.readInt();
        MaterialName = new String(dis.readNBytes(MaterialNameLength)).trim();
        DistortionCompensationEnabled = dis.readByte();
        DistortionCompensationThickness = dis.readInt();
        DistortionCompensationFocalLength = dis.readInt();
        XYAxisProfileCompensationEnabled = dis.readByte();
        XYAxisProfileCompensationValue = dis.readShort();
        ZPenetrationCompensationEnabled = dis.readByte();
        ZPenetrationCompensationLevel = dis.readShort();
        AntiAliasEnabled = dis.readByte();
        AntiAliasGreyMinValue = dis.readByte();
        AntiAliasGreyMaxValue = dis.readByte();
        ImageBlurEnabled = dis.readByte();
        ImageBlurLevel = dis.readByte();
        dis.readNBytes(2); // Page break (0x0d, 0x0a)
    }

    @Override
    public void write(OutputStream stream) throws IOException {

    }

    @Override
    public String toString() {
        return "-- SliceInfoV3 -- \n" +
                "SoftwareNameLength: " + SoftwareNameLength + "\n" +
                "SoftwareName: " + SoftwareName + "\n" +
                "MaterialNameLength: " + MaterialNameLength + "\n" +
                "MaterialName: " + MaterialName + "\n" +
                "DistortionCompensationEnabled: " + DistortionCompensationEnabled + "\n" +
                "DistortionCompensationThickness: " + DistortionCompensationThickness + "\n" +
                "DistortionCompensationFocalLength: " + DistortionCompensationFocalLength + "\n" +
                "XYAxisProfileCompensationEnabled: " + XYAxisProfileCompensationEnabled + "\n" +
                "XYAxisProfileCompensationValue: " + XYAxisProfileCompensationValue + "\n" +
                "ZPenetrationCompensationEnabled: " + ZPenetrationCompensationEnabled + "\n" +
                "ZPenetrationCompensationLevel: " + ZPenetrationCompensationLevel + "\n" +
                "AntiAliasEnabled: " + AntiAliasEnabled + "\n" +
                "AntiAliasGreyMinValue: " + AntiAliasGreyMinValue + "\n" +
                "AntiAliasGreyMaxValue: " + AntiAliasGreyMaxValue + "\n" +
                "ImageBlurEnabled: " + ImageBlurEnabled + "\n" +
                "ImageBlurLevel: " + ImageBlurLevel + "\n";
    }
}
