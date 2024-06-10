package futurelink.msla.formats.anycubic.tables;

import futurelink.msla.formats.iface.MSLAFileBlockFields;
import futurelink.msla.formats.iface.MSLAFileField;
import futurelink.msla.utils.About;
import lombok.Getter;

/**
 * "SOFTWARE" section representation.
 */
@Getter
public class PhotonWorkshopFileSoftwareTable extends PhotonWorkshopFileTable {
    private final Fields blockFields = new Fields();

    @SuppressWarnings("unused")
    @Getter
    static class Fields implements MSLAFileBlockFields {
        @MSLAFileField(length = 32) private final String SoftwareName = About.Name;
        @MSLAFileField(order = 1) private final Integer TableLength = 64;
        @MSLAFileField(order = 2, length = 32) private final String Version = About.Version;
        @MSLAFileField(order = 3, length = 64) private final String OperatingSystem  = System.getProperty("os.name");
        @MSLAFileField(order = 4, length = 32) private final String OpenGLVersion = "3.3-CoreProfile";
    }

    public PhotonWorkshopFileSoftwareTable(byte versionMajor, byte versionMinor) {
        super(versionMajor, versionMinor);
    }

    @Override
    int calculateTableLength() {
        return 164;
    }

    @Override
    public String toString() { return blockFields.fieldsAsString(" = ", "\n"); }
}
