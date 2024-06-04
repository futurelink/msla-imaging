package futurelink.msla.formats.iface;

import lombok.Getter;

import java.util.HashMap;

@Getter
public class MSLAFileProps extends HashMap<String, String> {
    public Integer getInt(String name) { return Integer.parseInt(get(name)); }
    public Byte getByte(String name) { return Byte.parseByte(get(name)); }
    public Short getShort(String name) { return Short.parseShort(get(name)); }
    public String getString(String name) { return get(name); }
}
