package futurelink.msla.formats.anycubic.tables;

import futurelink.msla.formats.iface.MSLAFileBlock;
import futurelink.msla.formats.iface.annotations.MSLAOption;
import futurelink.msla.formats.iface.annotations.MSLAOptionContainer;
import lombok.Getter;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Common class for section representation.
 */
abstract public class PhotonWorkshopFileTable implements MSLAFileBlock {
    public static final int MarkLength = 12;
    public static final float DefaultLiftHeight = 100;

    @Getter protected String Name;
    @Getter protected int TableLength;
    protected byte versionMajor;
    protected byte versionMinor;

    public PhotonWorkshopFileTable(byte versionMajor, byte versionMinor) {
        setVersion(versionMajor, versionMinor);
    }

    public void setVersion(byte versionMajor, byte versionMinor) {
        this.versionMajor = versionMajor;
        this.versionMinor = versionMinor;
    }

    abstract int calculateTableLength();

    @Override
    public int getDataLength() { return calculateTableLength() + MarkLength + 4; }

    public HashMap<String, Class<?>> getOptions() {
        var a = getClass().getAnnotation(MSLAOptionContainer.class);
        if (a != null) {
            var optionsMap = new HashMap<String, Class<?>>();
            Arrays.stream(a.className().getDeclaredFields())
                    .filter((f) -> f.getAnnotation(MSLAOption.class) != null)
                    .forEach((f) -> optionsMap.put(f.getName(), f.getType()));
            return optionsMap;
        }
        return null;
    }
}
