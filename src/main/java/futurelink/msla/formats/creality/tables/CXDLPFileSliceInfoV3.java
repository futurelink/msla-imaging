package futurelink.msla.formats.creality.tables;

import futurelink.msla.formats.MSLAFileBlockFields;
import futurelink.msla.formats.MSLAOption;
import futurelink.msla.formats.MSLAOptionContainer;
import lombok.Getter;
import lombok.Setter;

import java.io.*;

@MSLAOptionContainer(className= CXDLPFileSliceInfoV3.Fields.class)
public class CXDLPFileSliceInfoV3 extends CXDLPFileTable {
    public static class Fields implements MSLAFileBlockFields {
        @MSLAOption(type=String.class) @Getter private String SoftwareName = "v1.9.4";
        @MSLAOption(type=String.class) @Getter private String MaterialName = "normal";
        @MSLAOption @Getter @Setter private Byte DistortionCompensationEnabled = 0;
        @MSLAOption @Getter @Setter private Integer DistortionCompensationThickness = 600;
        @MSLAOption @Getter @Setter private Integer DistortionCompensationFocalLength = 300000;
        @MSLAOption @Getter @Setter private Byte XYAxisProfileCompensationEnabled = 1;
        @MSLAOption @Getter @Setter private Short XYAxisProfileCompensationValue = 0;
        @MSLAOption @Getter @Setter private Byte ZPenetrationCompensationEnabled = 0;
        @MSLAOption @Getter @Setter private Short ZPenetrationCompensationLevel = 1000;
        @MSLAOption @Getter @Setter private Byte AntiAliasEnabled = 1;
        @MSLAOption @Getter private Byte AntiAliasGreyMinValue = 1;
        @MSLAOption @Getter private Byte AntiAliasGreyMaxValue = (byte) 0xff;
        @MSLAOption @Getter @Setter private Byte ImageBlurEnabled = 0;
        @MSLAOption @Getter @Setter private Byte ImageBlurLevel = 2;

        public int getDataLength() { return (SoftwareName.length() + 1) + (MaterialName.length() + 1) + 28; }
        public void setMaterialName(String name) {
            MaterialName = name;
        }
        public void setSoftwareName(String name) {
            SoftwareName = name;
        }
    }

    private final Fields fields = new Fields();

    @Override
    public int getDataLength() { return fields.getDataLength() + 2; }

    @Override
    public void read(FileInputStream stream, int position) throws IOException {
        var l = 0;
        var fc = stream.getChannel();
        fc.position(position);

        var dis = new DataInputStream(stream);
        l = dis.readInt();
        fields.SoftwareName = new String(dis.readNBytes(l)).trim();
        l = dis.readInt();
        fields.MaterialName = new String(dis.readNBytes(l)).trim();
        fields.DistortionCompensationEnabled = dis.readByte();
        fields.DistortionCompensationThickness = dis.readInt();
        fields.DistortionCompensationFocalLength = dis.readInt();
        fields.XYAxisProfileCompensationEnabled = dis.readByte();
        fields.XYAxisProfileCompensationValue = dis.readShort();
        fields.ZPenetrationCompensationEnabled = dis.readByte();
        fields.ZPenetrationCompensationLevel = dis.readShort();
        fields.AntiAliasEnabled = dis.readByte();
        fields.AntiAliasGreyMinValue = dis.readByte();
        fields.AntiAliasGreyMaxValue = dis.readByte();
        fields.ImageBlurEnabled = dis.readByte();
        fields.ImageBlurLevel = dis.readByte();
        dis.readNBytes(2); // Page break (0x0d, 0x0a)
    }

    @Override
    public void write(OutputStream stream) throws IOException {
        var dos = new DataOutputStream(stream);
        dos.writeInt(fields.SoftwareName.length()+1);
        dos.write(fields.SoftwareName.getBytes()); dos.write(0);
        dos.writeInt(fields.MaterialName.length()+1);
        dos.write(fields.MaterialName.getBytes()); dos.write(0);
        dos.writeByte(fields.DistortionCompensationEnabled);
        dos.writeInt(fields.DistortionCompensationThickness);
        dos.writeInt(fields.DistortionCompensationFocalLength);
        dos.writeByte(fields.XYAxisProfileCompensationEnabled);
        dos.writeShort(fields.XYAxisProfileCompensationValue);
        dos.writeByte(fields.ZPenetrationCompensationEnabled);
        dos.writeShort(fields.ZPenetrationCompensationLevel);
        dos.writeByte(fields.AntiAliasEnabled);
        dos.writeByte(fields.AntiAliasGreyMinValue);
        dos.writeByte(fields.AntiAliasGreyMaxValue);
        dos.writeByte(fields.ImageBlurEnabled);
        dos.writeByte(fields.ImageBlurLevel);
        dos.writeByte(0x0d);
        dos.writeByte(0x0a);
    }

    @Override
    public String toString() {
        return "-- SliceInfoV3 -- \n" +
                "SoftwareName: " + fields.SoftwareName + "\n" +
                "MaterialName: " + fields.MaterialName + "\n" +
                "DistortionCompensationEnabled: " + fields.DistortionCompensationEnabled + "\n" +
                "DistortionCompensationThickness: " + fields.DistortionCompensationThickness + "\n" +
                "DistortionCompensationFocalLength: " + fields.DistortionCompensationFocalLength + "\n" +
                "XYAxisProfileCompensationEnabled: " + fields.XYAxisProfileCompensationEnabled + "\n" +
                "XYAxisProfileCompensationValue: " + fields.XYAxisProfileCompensationValue + "\n" +
                "ZPenetrationCompensationEnabled: " + fields.ZPenetrationCompensationEnabled + "\n" +
                "ZPenetrationCompensationLevel: " + fields.ZPenetrationCompensationLevel + "\n" +
                "AntiAliasEnabled: " + fields.AntiAliasEnabled + "\n" +
                "AntiAliasGreyMinValue: " + fields.AntiAliasGreyMinValue + "\n" +
                "AntiAliasGreyMaxValue: " + fields.AntiAliasGreyMaxValue + "\n" +
                "ImageBlurEnabled: " + fields.ImageBlurEnabled + "\n" +
                "ImageBlurLevel: " + fields.ImageBlurLevel + "\n";
    }
}
