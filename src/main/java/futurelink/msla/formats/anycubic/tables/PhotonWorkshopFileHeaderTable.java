package futurelink.msla.formats.anycubic.tables;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.iface.*;
import futurelink.msla.formats.iface.annotations.MSLAFileField;
import futurelink.msla.formats.iface.annotations.MSLAOption;
import futurelink.msla.formats.iface.annotations.MSLAOptionContainer;
import futurelink.msla.formats.utils.FileFieldsIO;
import futurelink.msla.formats.utils.FileFieldsReader;
import futurelink.msla.formats.utils.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * "HEADER" section representation.
 */
@Getter
@MSLAOptionContainer(PhotonWorkshopFileHeaderTable.Fields.class)
public class PhotonWorkshopFileHeaderTable extends PhotonWorkshopFileTable {
    public static final String Name = "HEADER";
    @Delegate private final Fields fields;

    @Getter @Setter
    @SuppressWarnings("unused")
    public static class Fields implements MSLAFileBlockFields {
        private final PhotonWorkshopFileTable parent;
        private Size Resolution = new Size(0, 0);

        @MSLAFileField(length = MarkLength, dontCount = true) private String Name() { return PhotonWorkshopFileHeaderTable.Name; }
        // Validation setter checks for what's been read from file
        // and throws an exception when that is something unexpected.
        private void setName(String name) throws MSLAException {
            if (!PhotonWorkshopFileHeaderTable.Name.equals(name))
                throw new MSLAException("Table name '" + name + "' is invalid");
        }
        @MSLAFileField(order = 1, dontCount = true) private Integer TableLength() { return parent.calculateTableLength(); }
        private void setTableLength(Integer length) { parent.TableLength = length; }
        @MSLAFileField(order = 2) private Float PixelSizeUm;
        @MSLAFileField(order = 3) @MSLAOption(MSLAOption.LayerHeight) private Float LayerHeight;
        @MSLAFileField(order = 4) @MSLAOption(MSLAOption.ExposureTime) private Float ExposureTime;
        @MSLAFileField(order = 5) @MSLAOption(MSLAOption.WaitBeforeCure) private Float WaitTimeBeforeCure1;
        @MSLAFileField(order = 6) @MSLAOption(MSLAOption.BottomExposureTime) private Float BottomExposureTime;
        @MSLAFileField(order = 7) @MSLAOption(MSLAOption.BottomLayersCount) private Integer BottomLayersCount;
        @MSLAFileField(order = 8) @MSLAOption(MSLAOption.LiftHeight) private Float LiftHeight = DefaultLiftHeight;
        @MSLAFileField(order = 9) @MSLAOption(MSLAOption.LiftSpeed) private Float LiftSpeed;// = SpeedConverter.Convert(DefaultLiftSpeed, CoreSpeedUnit, SpeedUnit.MillimetersPerSecond); // mm/s
        @MSLAFileField(order = 10) @MSLAOption(MSLAOption.RetractSpeed) private Float RetractSpeed;// = SpeedConverter.Convert(DefaultRetractSpeed, CoreSpeedUnit, SpeedUnit.MillimetersPerSecond); // mm/s
        @MSLAFileField(order = 11) @MSLAOption(MSLAOption.Volume) private Float VolumeMl = 0.0F;
        @MSLAFileField(order = 12) @MSLAOption(MSLAOption.Antialias) private Integer AntiAliasing = 1;
        @MSLAFileField(order = 13) private Integer ResolutionX() { return Resolution.getWidth(); }
        private void setResolutionX(Integer width) { Resolution = new Size(width, Resolution.getHeight()); }
        @MSLAFileField(order = 14) private Integer ResolutionY() { return Resolution.getHeight(); }
        private void setResolutionY(Integer height) { Resolution = new Size(Resolution.getWidth(), height); }
        @MSLAFileField(order = 15) @MSLAOption("Weight") private Float WeightG = 0.0F;
        @MSLAFileField(order = 16) @MSLAOption("Price") private Float Price = 0.0F;
        @MSLAFileField(order = 17) @MSLAOption("Currency") private Integer PriceCurrencySymbol; /// 24 00 00 00 $ or ¥ C2 A5 00 00 or € = E2 82 AC 00
        @MSLAFileField(order = 18) @MSLAOption("Layer overrides") private Integer PerLayerOverride = 0; // boolean (80 - true, 00 - false)
        @MSLAFileField(order = 19) @MSLAOption("Print time") private Integer PrintTime = 0;

        /* Version 2.4 fields */
        @MSLAFileField(order = 20) @MSLAOption("Transition layers count") private Integer TransitionLayerCount = 0;
        @MSLAFileField(order = 21) @MSLAOption("Transition layer type") private Integer TransitionLayerType = 0;
        @MSLAFileField(order = 22) @MSLAOption("Advanced mode") private Integer AdvancedMode = 0; /// 0 = Basic mode | 1 = Advanced mode which allows TSMC

        /* Version 2.5 fields */
        @MSLAFileField(order = 23) @MSLAOption("Grey level") private Short Grey = 0;
        @MSLAFileField(order = 24) @MSLAOption(MSLAOption.BlurLevel) private Short BlurLevel = 0;
        @MSLAFileField(order = 25) @MSLAOption("Resin type") private Integer ResinType = 0;

        /* Version 2.6 fields */
        // boolean, when true, normal exposure time will be auto set, use false for traditional way
        @MSLAFileField(order = 26) @MSLAOption("Intelligent mode") private int IntelligentMode = 0;

        public Fields(PhotonWorkshopFileTable parent) { this.parent = parent; }

        @Override
        public boolean isFieldExcluded(String fieldName) {
            if ((TableLength() < 96) && "IntelligentMode".equals(fieldName)) return true;
            if ((TableLength() < 92) && "ResinType".equals(fieldName)) return true;
            if ((TableLength() < 88) && "BlurLevel".equals(fieldName)) return true;
            if ((TableLength() < 86) && "Grey".equals(fieldName)) return true;
            if ((TableLength() < 84) && "AdvancedMode".equals(fieldName)) return true;
            return false;
        }
    }

    public PhotonWorkshopFileHeaderTable(byte versionMajor, byte versionMinor) {
        super(versionMajor, versionMinor);
        this.fields = new Fields(this);
    }

    public PhotonWorkshopFileHeaderTable(
            MSLAFileDefaults defaults,
            byte versionMajor,
            byte versionMinor) throws MSLAException
    {
        this(versionMajor, versionMinor);
        defaults.setFields("Header", fields);
    }

    @Override
    public long read(FileInputStream stream, long position) throws MSLAException {
        try {
            var reader = new FileFieldsReader(stream, FileFieldsIO.Endianness.LittleEndian);
            var dataRead = reader.read(this);
            if (dataRead != TableLength) throw new MSLAException(
                "Header was not completely read out (" + dataRead + " of " + TableLength + "), some extra data left unread"
            );
            return dataRead;
        } catch (IOException e) { throw new MSLAException("Error reading Header table", e); }
    }

    public int calculateTableLength() {
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
    public final void write(OutputStream stream) throws MSLAException {
        TableLength = calculateTableLength();
        super.write(stream);
    }

    @Override public String toString() { return fields.fieldsAsString(" = ", "\n"); }
}
