package futurelink.msla.formats.anycubic.tables;

import com.google.common.io.LittleEndianDataInputStream;
import com.google.common.io.LittleEndianDataOutputStream;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.Arrays;

public class PhotonWorkshopFileHeaderTable extends PhotonWorkshopFileTable {
    public static final String Name = "HEADER";
    public static class Fields {
        @Getter @Setter private float PixelSizeUm = 47.25f;
        @Getter @Setter private float LayerHeight;
        @Getter @Setter private float ExposureTime;
        @Getter @Setter private float WaitTimeBeforeCure1;
        @Getter @Setter private float BottomExposureTime;
        @Getter @Setter private float BottomLayersCount;
        @Getter @Setter private float LiftHeight = DefaultLiftHeight;
        @Getter @Setter private float LiftSpeed;// = SpeedConverter.Convert(DefaultLiftSpeed, CoreSpeedUnit, SpeedUnit.MillimetersPerSecond); // mm/s
        @Getter @Setter private float RetractSpeed;// = SpeedConverter.Convert(DefaultRetractSpeed, CoreSpeedUnit, SpeedUnit.MillimetersPerSecond); // mm/s
        @Getter @Setter private float VolumeMl;
        @Getter @Setter private int AntiAliasing = 1;
        @Getter @Setter private int ResolutionX;
        @Getter @Setter private int ResolutionY;
        @Getter @Setter private float WeightG;
        @Getter @Setter private float Price;
        @Getter @Setter private int PriceCurrencySymbol = '$'; /// 24 00 00 00 $ or ¥ C2 A5 00 00 or € = E2 82 AC 00
        @Getter @Setter private int PerLayerOverride; // boolean (80 - true, 00 - false)
        @Getter @Setter private int PrintTime;

        /*
            Version 2.4 fields
         */
        @Getter @Setter private int TransitionLayerCount;
        @Getter @Setter private int TransitionLayerType;
        @Getter @Setter private int AdvancedMode; /// 0 = Basic mode | 1 = Advanced mode which allows TSMC

        /*
            Version 2.5 fields
         */
        @Getter @Setter private short Grey;
        @Getter @Setter private short BlurLevel;
        @Getter @Setter private  int ResinType;

        /*
            Version 2.6 fields
         */
        @Getter @Setter private int IntelligentMode; // boolean, when true, normal exposure time will be auto set, use false for traditional way

        public static Fields copyOf(Fields source) {
            var f = new Fields();
            f.PixelSizeUm = source.PixelSizeUm;
            f.LayerHeight = source.LayerHeight;
            f.ExposureTime = source.ExposureTime;
            f.WaitTimeBeforeCure1 = source.WaitTimeBeforeCure1;
            f.BottomExposureTime = source.BottomExposureTime;
            f.BottomLayersCount = source.BottomLayersCount;
            f.LiftHeight = source.LiftHeight;
            f.LiftSpeed = source.LiftSpeed;
            f.RetractSpeed = source.RetractSpeed;
            f.VolumeMl = source.VolumeMl;
            f.AntiAliasing = source.AntiAliasing;
            f.ResolutionX = source.ResolutionX;
            f.ResolutionY = source.ResolutionY;
            f.WeightG = source.WeightG;
            f.Price = source.Price;
            f.PriceCurrencySymbol = source.PriceCurrencySymbol;
            f.PerLayerOverride = source.PerLayerOverride;
            f.PrintTime = source.PrintTime;
            f.TransitionLayerCount = source.TransitionLayerCount;
            f.TransitionLayerType = source.TransitionLayerType;
            f.AdvancedMode = source.AdvancedMode;
            f.Grey = source.Grey;
            f.BlurLevel = source.BlurLevel;
            f.ResinType = source.ResinType;
            f.IntelligentMode = source.IntelligentMode;
            return f;
        }
    }

    private final Fields fields;

    public PhotonWorkshopFileHeaderTable() {
        fields = new Fields();
    }

    public PhotonWorkshopFileHeaderTable(Fields defaults) {
        fields = Fields.copyOf(defaults);
    }

    public int getResolutionX() {
        return fields.getResolutionX();
    }

    public int getResolutionY() {
        return fields.getResolutionY();
    }

    public float getPixelSizeUm() { return fields.getPixelSizeUm(); }

    @Override
    public void read(LittleEndianDataInputStream stream) throws IOException {
        int dataRead;
        var headerMark = stream.readNBytes(Name.length());
        if (!Arrays.equals(headerMark, Name.getBytes())) {
            throw new IOException("Header mark not found! Corrupted data.");
        }
        stream.readNBytes(MarkLength - Name.length()); // Skip section name zeroes
        TableLength = stream.readInt();
        fields.PixelSizeUm = stream.readFloat();
        fields.LayerHeight = stream.readFloat();
        fields.ExposureTime = stream.readFloat();
        fields.WaitTimeBeforeCure1 = stream.readFloat();
        fields.BottomExposureTime = stream.readFloat();
        fields.BottomLayersCount = stream.readFloat();
        fields.LiftHeight = stream.readFloat();
        fields.LiftSpeed = stream.readFloat();
        fields.RetractSpeed = stream.readFloat();
        fields.VolumeMl = stream.readFloat();
        fields.AntiAliasing = stream.readInt();
        fields.ResolutionX = stream.readInt();
        fields.ResolutionY = stream.readInt();
        fields.WeightG = stream.readFloat();
        fields.Price = stream.readFloat();
        fields.PriceCurrencySymbol = stream.readInt();
        fields.PerLayerOverride = stream.readInt(); // boolean (80 - true, 00 - false)
        fields.PrintTime = stream.readInt(); // in seconds
        fields.TransitionLayerCount = stream.readInt();
        fields.TransitionLayerType = stream.readInt();
        dataRead = 80; // Assume we read 20 fields x 4 bytes

        // Version 2.4 has this table length
        if (TableLength >= 84) { fields.AdvancedMode = stream.readInt(); dataRead += 4; }

        // Version 2.5 and greater has this table length
        if (TableLength >= 86) { fields.Grey = stream.readShort(); dataRead += 2; }
        if (TableLength >= 88) { fields.BlurLevel = stream.readShort(); dataRead += 2; }
        if (TableLength >= 92) { fields.ResinType = stream.readInt();  dataRead += 4; }
        if (TableLength >= 96) { fields.IntelligentMode = stream.readInt(); dataRead += 4; }

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

    public void write(LittleEndianDataOutputStream stream, byte versionMajor, byte versionMinor) throws IOException {
        stream.write(Name.getBytes());
        stream.write(new byte[PhotonWorkshopFileTable.MarkLength - Name.length()]);
        TableLength = calculateTableLength(versionMajor, versionMinor);
        stream.writeInt(TableLength);
        stream.writeFloat(fields.PixelSizeUm);
        stream.writeFloat(fields.LayerHeight);
        stream.writeFloat(fields.ExposureTime);
        stream.writeFloat(fields.WaitTimeBeforeCure1);
        stream.writeFloat(fields.BottomExposureTime);
        stream.writeFloat(fields.BottomLayersCount);
        stream.writeFloat(fields.LiftHeight);
        stream.writeFloat(fields.LiftSpeed);
        stream.writeFloat(fields.RetractSpeed);
        stream.writeFloat(fields.VolumeMl);
        stream.writeInt(fields.AntiAliasing);
        stream.writeInt(fields.ResolutionX);
        stream.writeInt(fields.ResolutionY);
        stream.writeFloat(fields.WeightG);
        stream.writeFloat(fields.Price);
        stream.writeInt(fields.PriceCurrencySymbol);
        stream.writeInt(fields.PerLayerOverride); // boolean (80 - true, 00 - false)
        stream.writeInt(fields.PrintTime); // in seconds
        stream.writeInt(fields.TransitionLayerCount);
        stream.writeInt(fields.TransitionLayerType);

        // Version 2.4 has this table length
        if (TableLength >= 84) { stream.writeInt(fields.AdvancedMode); }

        // Version 2.5 and greater has this table length
        if (TableLength >= 86) { stream.writeShort(fields.Grey); }
        if (TableLength >= 88) { stream.writeShort(fields.BlurLevel); }
        if (TableLength >= 92) { stream.writeInt(fields.ResinType); }
        if (TableLength >= 96) { stream.writeInt(fields.IntelligentMode); }
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
        b.append("ResolutionX: ").append(fields.ResolutionX).append("\n");
        b.append("ResolutionY: ").append(fields.ResolutionY).append("\n");
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
