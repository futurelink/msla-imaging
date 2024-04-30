package futurelink.msla.formats.anycubic.tables;

import com.google.common.io.LittleEndianDataInputStream;
import com.google.common.io.LittleEndianDataOutputStream;
import futurelink.msla.formats.iface.MSLAFileBlockFields;
import futurelink.msla.formats.iface.MSLAOption;
import futurelink.msla.formats.iface.MSLAOptionContainer;
import futurelink.msla.formats.utils.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * "HEADER" section representation.
 */
@MSLAOptionContainer(className=PhotonWorkshopFileHeaderTable.Fields.class)
public class PhotonWorkshopFileHeaderTable extends PhotonWorkshopFileTable {
    public static final String Name = "HEADER";
    public static class Fields implements MSLAFileBlockFields {
        @Getter private Float PixelSizeUm;
        @MSLAOption @Setter @Getter private Float LayerHeight;
        @MSLAOption @Setter @Getter private Float ExposureTime;
        @MSLAOption @Setter @Getter private Float WaitTimeBeforeCure1;
        @MSLAOption @Setter @Getter private Float BottomExposureTime;
        @MSLAOption @Setter @Getter private Integer BottomLayersCount;
        @MSLAOption @Setter @Getter private Float LiftHeight = DefaultLiftHeight;
        @MSLAOption @Setter @Getter private Float LiftSpeed;// = SpeedConverter.Convert(DefaultLiftSpeed, CoreSpeedUnit, SpeedUnit.MillimetersPerSecond); // mm/s
        @MSLAOption @Setter @Getter private Float RetractSpeed;// = SpeedConverter.Convert(DefaultRetractSpeed, CoreSpeedUnit, SpeedUnit.MillimetersPerSecond); // mm/s
        @MSLAOption @Setter @Getter private Float VolumeMl;
        @MSLAOption @Setter @Getter private Integer AntiAliasing = 1;
        @Getter private Size Resolution;
        @MSLAOption @Setter @Getter private Float WeightG;
        @MSLAOption @Setter @Getter private Float Price;
        @MSLAOption @Setter @Getter private Integer PriceCurrencySymbol; /// 24 00 00 00 $ or ¥ C2 A5 00 00 or € = E2 82 AC 00
        @MSLAOption @Setter @Getter private Integer PerLayerOverride; // boolean (80 - true, 00 - false)
        @MSLAOption @Setter @Getter private Integer PrintTime;

        /* Version 2.4 fields */
        @MSLAOption @Setter @Getter private Integer TransitionLayerCount;
        @MSLAOption @Setter @Getter private Integer TransitionLayerType;
        @MSLAOption @Setter @Getter private Integer AdvancedMode; /// 0 = Basic mode | 1 = Advanced mode which allows TSMC

        /* Version 2.5 fields */
        @MSLAOption @Setter @Getter private Short Grey;
        @MSLAOption @Setter @Getter private Short BlurLevel;
        @MSLAOption @Setter @Getter private Integer ResinType;

        /* Version 2.6 fields */
        @MSLAOption @Getter @Setter private int IntelligentMode; // boolean, when true, normal exposure time will be auto set, use false for traditional way

        public Fields(Float PixelSizeUm, Size Resolution) {
            this.PixelSizeUm = PixelSizeUm;
            this.Resolution = new Size(Resolution);
        }

        public Fields(Fields source) {
            PixelSizeUm = source.PixelSizeUm;
            Resolution = new Size(source.Resolution);
            LayerHeight = source.LayerHeight;
            ExposureTime = source.ExposureTime;
            WaitTimeBeforeCure1 = source.WaitTimeBeforeCure1;
            BottomExposureTime = source.BottomExposureTime;
            BottomLayersCount = source.BottomLayersCount;
            LiftHeight = source.LiftHeight;
            LiftSpeed = source.LiftSpeed;
            RetractSpeed = source.RetractSpeed;
            VolumeMl = source.VolumeMl;
            AntiAliasing = source.AntiAliasing;
            WeightG = source.WeightG;
            Price = source.Price;
            PriceCurrencySymbol = source.PriceCurrencySymbol;
            PerLayerOverride = source.PerLayerOverride;
            PrintTime = source.PrintTime;
            TransitionLayerCount = source.TransitionLayerCount;
            TransitionLayerType = source.TransitionLayerType;
            AdvancedMode = source.AdvancedMode;
            Grey = source.Grey;
            BlurLevel = source.BlurLevel;
            ResinType = source.ResinType;
            IntelligentMode = source.IntelligentMode;
        }
    }

    @Delegate private final Fields fields;

    public PhotonWorkshopFileHeaderTable(byte versionMajor, byte versionMinor) {
        super(versionMajor, versionMinor);
        fields = new Fields(0.0f, new Size(0, 0));
    }

    public PhotonWorkshopFileHeaderTable(MSLAFileBlockFields defaults, byte versionMajor, byte versionMinor) {
        super(versionMajor, versionMinor);
        fields = new Fields((Fields) defaults);
    }

    @Override
    public void read(FileInputStream stream, int position) throws IOException {
        var fc = stream.getChannel(); fc.position(position);
        var dis = new LittleEndianDataInputStream(stream);

        int dataRead;
        var headerMark = stream.readNBytes(Name.length());
        if (!Arrays.equals(headerMark, Name.getBytes())) {
            throw new IOException("Header mark not found! Corrupted data.");
        }
        stream.readNBytes(MarkLength - Name.length()); // Skip section name zeroes
        TableLength = dis.readInt();
        fields.PixelSizeUm = dis.readFloat();
        fields.LayerHeight = dis.readFloat();
        fields.ExposureTime = dis.readFloat();
        fields.WaitTimeBeforeCure1 = dis.readFloat();
        fields.BottomExposureTime = dis.readFloat();
        fields.BottomLayersCount = dis.readInt();
        fields.LiftHeight = dis.readFloat();
        fields.LiftSpeed = dis.readFloat();
        fields.RetractSpeed = dis.readFloat();
        fields.VolumeMl = dis.readFloat();
        fields.AntiAliasing = dis.readInt();
        fields.Resolution = new Size(dis.readInt(), dis.readInt());
        fields.WeightG = dis.readFloat();
        fields.Price = dis.readFloat();
        fields.PriceCurrencySymbol = dis.readInt();
        fields.PerLayerOverride = dis.readInt(); // boolean (80 - true, 00 - false)
        fields.PrintTime = dis.readInt(); // in seconds
        fields.TransitionLayerCount = dis.readInt();
        fields.TransitionLayerType = dis.readInt();
        dataRead = 80; // Assume we read 20 fields x 4 bytes

        // Version 2.4 has this table length
        if (TableLength >= 84) { fields.AdvancedMode = dis.readInt(); dataRead += 4; }

        // Version 2.5 and greater has this table length
        if (TableLength >= 86) { fields.Grey = dis.readShort(); dataRead += 2; }
        if (TableLength >= 88) { fields.BlurLevel = dis.readShort(); dataRead += 2; }
        if (TableLength >= 92) { fields.ResinType = dis.readInt();  dataRead += 4; }
        if (TableLength >= 96) { fields.IntelligentMode = dis.readInt(); dataRead += 4; }

        if (dataRead != TableLength) {
            throw new IOException("Header was not completely read out (" + dataRead + " of " + TableLength + "), some extra data left unread");
        }
    }

    public int calculateTableLength(byte versionMajor, byte versionMinor) {
        if (versionMajor >= 2) {
            return switch (versionMinor) {
                case 4 -> 84;
                case 5 -> 92;
                case 6 -> 96;
                default -> 80;
            };
        }
        return 80;
    }

    @Override
    public final void write(OutputStream stream) throws IOException {
        var dos = new LittleEndianDataOutputStream(stream);
        dos.write(Name.getBytes());
        dos.write(new byte[PhotonWorkshopFileTable.MarkLength - Name.length()]);
        TableLength = calculateTableLength(versionMajor, versionMinor);
        dos.writeInt(TableLength);
        dos.writeFloat(fields.PixelSizeUm);
        dos.writeFloat(fields.LayerHeight);
        dos.writeFloat(fields.ExposureTime);
        dos.writeFloat(fields.WaitTimeBeforeCure1);
        dos.writeFloat(fields.BottomExposureTime);
        dos.writeFloat(fields.BottomLayersCount);
        dos.writeFloat(fields.LiftHeight);
        dos.writeFloat(fields.LiftSpeed);
        dos.writeFloat(fields.RetractSpeed);
        dos.writeFloat(fields.VolumeMl);
        dos.writeInt(fields.AntiAliasing);
        dos.writeInt(fields.Resolution.getWidth());
        dos.writeInt(fields.Resolution.getHeight());
        dos.writeFloat(fields.WeightG);
        dos.writeFloat(fields.Price);
        dos.writeInt(fields.PriceCurrencySymbol);
        dos.writeInt(fields.PerLayerOverride); // boolean (80 - true, 00 - false)
        dos.writeInt(fields.PrintTime); // in seconds
        dos.writeInt(fields.TransitionLayerCount);
        dos.writeInt(fields.TransitionLayerType);

        // Version 2.4 has this table length
        if (TableLength >= 84) { dos.writeInt(fields.AdvancedMode); }

        // Version 2.5 and greater has this table length
        if (TableLength >= 86) { dos.writeShort(fields.Grey); }
        if (TableLength >= 88) { dos.writeShort(fields.BlurLevel); }
        if (TableLength >= 92) { dos.writeInt(fields.ResinType); }
        if (TableLength >= 96) { dos.writeInt(fields.IntelligentMode); }
        stream.flush();
    }

    @Override
    public String toString() {
        var b = new StringBuilder();
        b.append("-- Header data --\n");
        b.append("TableLength: ").append(TableLength).append("\n");
        b.append("PixelSizeUm: ").append(fields.PixelSizeUm).append("\n");
        b.append("LayerHeight: ").append(fields.LayerHeight).append("\n");
        b.append("ExposureTime: ").append(fields.ExposureTime).append("\n");
        b.append("WaitTimeBeforeCure1: ").append(fields.WaitTimeBeforeCure1).append("\n");
        b.append("BottomExposureTime: ").append(fields.BottomExposureTime).append("\n");
        b.append("BottomLayersCount: ").append(fields.BottomLayersCount).append("\n");
        b.append("LiftHeight: ").append(fields.LiftHeight).append("\n");
        b.append("LiftSpeed: ").append(fields.LiftSpeed).append("\n");
        b.append("RetractSpeed: ").append(fields.RetractSpeed).append("\n");
        b.append("VolumeMl: ").append(fields.VolumeMl).append("\n");

        b.append("AntiAliasing: ").append(fields.AntiAliasing).append("\n");
        b.append("Resolution: ").append(fields.Resolution).append("\n");
        b.append("WeightG: ").append(fields.WeightG).append("\n");
        b.append("Price: ").append(fields.Price).append("\n");
        b.append("PriceCurrencySymbol: ").append(fields.PriceCurrencySymbol).append("\n");
        b.append("PerLayerOverride: ").append(fields.PerLayerOverride).append("\n");
        b.append("PrintTime (s): ").append(fields.PrintTime).append("\n");
        b.append("TransitionLayerCount: ").append(fields.TransitionLayerCount).append("\n");
        b.append("TransitionLayerType: ").append(fields.TransitionLayerType).append("\n");

        if (TableLength >= 84) b.append("AdvancedMode: ").append(fields.AdvancedMode).append("\n");
        if (TableLength >= 86) b.append("Grey: ").append(fields.Grey).append("\n");
        if (TableLength >= 88) b.append("BlurLevel: ").append(fields.BlurLevel).append("\n");
        if (TableLength >= 92) b.append("ResinType: ").append(fields.ResinType).append("\n");
        if (TableLength >= 96) b.append("IntelligentMode: ").append(fields.IntelligentMode).append("\n");

        return b.toString();
    }
}
