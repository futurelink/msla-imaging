package futurelink.msla.formats.iface;

import futurelink.msla.formats.utils.Size;
import lombok.Getter;

import java.util.HashMap;

@SuppressWarnings("unused")
public interface MSLAFileDefaults extends MSLADefaults {
    @Getter
    class FileProps extends HashMap<String, String> {
        public Integer getInt(String name) { return Integer.parseInt(get(name)); }
        public Byte getByte(String name) { return Byte.parseByte(get(name)); }
        public Short getShort(String name) { return Short.parseShort(get(name)); }
        public String getString(String name) { return get(name); }
    }

    MSLALayerDefaults getLayerDefaults();

    String getMachineFullName();
    String getMachineManufacturer();
    String getMachineName();
    String getFileExtension();

    Size getResolution();
    float getPixelSizeUm();

    FileProps getFileProps();
}
