package futurelink.msla.formats.anycubic.tables;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.iface.*;
import futurelink.msla.formats.iface.MSLAFileField;
import futurelink.msla.formats.iface.options.MSLAOption;
import futurelink.msla.formats.iface.options.MSLAOptionName;
import futurelink.msla.formats.io.FileFieldsException;
import futurelink.msla.formats.io.FileFieldsIO;
import futurelink.msla.formats.io.FileFieldsReader;
import futurelink.msla.utils.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;

import java.io.DataInputStream;
import java.io.OutputStream;

/**
 * "HEADER" section representation.
 */
@Getter
public class PhotonWorkshopFileHeaderTable extends PhotonWorkshopFileTable {
    @Delegate private final Fields blockFields;

    /**
     * Header section internal fields.
     */
    @Getter @Setter
    @SuppressWarnings("unused")
    static class Fields implements MSLAFileBlockFields {
        private final PhotonWorkshopFileTable parent;
        private Size Resolution = null;

        @MSLAFileField(length = MarkLength, dontCount = true) private String Name() { return "HEADER"; }
        // Validation setter checks for what's been read from file
        // and throws an exception when that is something unexpected.
        private void setName(String name) throws MSLAException {
            if (!"HEADER".equals(name)) throw new MSLAException("Table name '" + name + "' is invalid");
        }
        @MSLAFileField(order = 1, dontCount = true) private Integer TableLength() { return parent.calculateTableLength(); }
        private void setTableLength(Integer length) { parent.TableLength = length; }
        @MSLAFileField(order = 2) private Float PixelSize;
        @MSLAFileField(order = 3) @MSLAOption(MSLAOptionName.LayerHeight) private Float LayerHeight;
        @MSLAFileField(order = 4) @MSLAOption(MSLAOptionName.NormalLayersExposureTime) private Float ExposureTime;
        @MSLAFileField(order = 5) @MSLAOption(MSLAOptionName.WaitBeforeCure) private Float WaitTimeBeforeCure;
        @MSLAFileField(order = 6) @MSLAOption(MSLAOptionName.BottomLayersExposureTime) private Float BottomExposureTime;
        @MSLAFileField(order = 7) @MSLAOption(MSLAOptionName.BottomLayersCount) private Float BottomLayersCount;
        @MSLAFileField(order = 8) @MSLAOption(MSLAOptionName.LiftHeight) private Float LiftHeight;
        @MSLAFileField(order = 9) @MSLAOption(MSLAOptionName.LiftSpeed) private Float LiftSpeed;// = SpeedConverter.Convert(DefaultLiftSpeed, CoreSpeedUnit, SpeedUnit.MillimetersPerSecond); // mm/s
        @MSLAFileField(order = 10) @MSLAOption(MSLAOptionName.RetractSpeed) private Float RetractSpeed;// = SpeedConverter.Convert(DefaultRetractSpeed, CoreSpeedUnit, SpeedUnit.MillimetersPerSecond); // mm/s
        @MSLAFileField(order = 11) @MSLAOption(MSLAOptionName.Volume) private Float VolumeMl;
        @MSLAFileField(order = 12) @MSLAOption(MSLAOptionName.AntialiasLevel) private Integer AntiAliasing;
        @MSLAFileField(order = 13) private Integer ResolutionX() { return Resolution != null ? Resolution.getWidth() : 0; }
        private void setResolutionX(Integer width) { Resolution = new Size(width, ResolutionY()); }
        @MSLAFileField(order = 14) private Integer ResolutionY() { return Resolution != null ? Resolution.getHeight() : 0; }
        private void setResolutionY(Integer height) { Resolution = new Size(ResolutionX(), height); }
        @MSLAFileField(order = 15) @MSLAOption(MSLAOptionName.Weight) private Float WeightG;
        @MSLAFileField(order = 16) @MSLAOption(MSLAOptionName.Price) private Float Price;
        @MSLAFileField(order = 17) @MSLAOption(MSLAOptionName.Currency) private Integer PriceCurrencySymbol; /// 24 00 00 00 $ or ¥ C2 A5 00 00 or € = E2 82 AC 00
        @MSLAFileField(order = 18) @MSLAOption(MSLAOptionName.LayerSettings) private Integer PerLayerOverride; // boolean (80 - true, 00 - false)
        @MSLAFileField(order = 19) @MSLAOption(MSLAOptionName.PrintTime) private Integer PrintTime;

        /* Version 2.4 fields */
        @MSLAFileField(order = 20) @MSLAOption(MSLAOptionName.TransitionLayersCount) private Integer TransitionLayerCount;
        @MSLAFileField(order = 21) @MSLAOption(MSLAOptionName.TransitionLayersType) private Integer TransitionLayerType;
        @MSLAFileField(order = 22) @MSLAOption(MSLAOptionName.AdvancedMode) private Integer AdvancedMode; /// 0 = Basic mode | 1 = Advanced mode which allows TSMC

        /* Version 2.5 fields */
        @MSLAFileField(order = 23) @MSLAOption(MSLAOptionName.Grey) private Short Grey;
        @MSLAFileField(order = 24) @MSLAOption(MSLAOptionName.ImageBlurLevel) private Short BlurLevel;
        @MSLAFileField(order = 25) @MSLAOption(MSLAOptionName.ResinType) private Integer ResinType;

        /* Version 2.6 fields */
        // boolean, when true, normal exposure time will be auto set, use false for traditional way
        @MSLAFileField(order = 26) @MSLAOption(MSLAOptionName.IntelligentMode) private Integer IntelligentMode;

        public Fields(PhotonWorkshopFileHeaderTable parent) { this.parent = parent; }

        @Override
        public boolean isFieldExcluded(String fieldName) {
            if (parent.VersionMajor < 2) {
                return "IntelligentMode".equals(fieldName) || "ResinType".equals(fieldName) ||
                        "BlurLevel".equals(fieldName) || "Grey".equals(fieldName) ||
                        "AdvancedMode".equals(fieldName);
            } else {
                if (parent.VersionMinor < 6 && "IntelligentMode".equals(fieldName)) return true;
                if (parent.VersionMinor < 5) {
                    if ("ResinType".equals(fieldName) || "BlurLevel".equals(fieldName) || "Grey".equals(fieldName))
                        return true;
                }
                if (parent.VersionMinor < 4 && "AdvancedMode".equals(fieldName)) return true;
            }
            return false;
        }
    }

    public PhotonWorkshopFileHeaderTable(byte versionMajor, byte versionMinor) {
        super(versionMajor, versionMinor);
        this.Name = "Header";
        this.blockFields = new Fields(this);
    }

    @Override
    public long read(DataInputStream stream, long position) throws MSLAException {
        try {
            var reader = new FileFieldsReader(stream, FileFieldsIO.Endianness.LittleEndian);
            var dataRead = reader.read(this, position);
            if (dataRead != TableLength) throw new MSLAException(
                "Header was not completely read out (" + dataRead + " of " + TableLength + "), some extra data left unread"
            );
            return dataRead;
        } catch (FileFieldsException e) { throw new MSLAException("Error reading Header table", e); }
    }

    public int calculateTableLength() {
        if (VersionMajor >= 2) {
            return switch (VersionMinor) {
                case 4 -> 84;
                case 5 -> 92;
                case 6 -> 96;
                default -> 80;
            };
        }
        return 80;
    }

    @Override
    public final void write(OutputStream stream) throws MSLAException {
        TableLength = calculateTableLength();
        super.write(stream);
    }

    @Override public String toString() { return "-- Header data --\n" + blockFields.fieldsAsString(" = ", "\n"); }
}
