package futurelink.msla.formats.iface;

import futurelink.msla.utils.Size;
import futurelink.msla.utils.defaults.props.MachineProperty;
import lombok.Getter;

import java.util.HashMap;

/**
 * mSla file properties structure.
 */
@Getter
@SuppressWarnings("unused")
public class MSLAFileProps extends HashMap<String, MSLADefaultsParams> {
    public Integer getInt(String name) { return get(name) != null ? get(name).getInt() : null; }
    public Byte getByte(String name) { return get(name) != null ? get(name).getByte() : null; }
    public Short getShort(String name) { return get(name) != null ? get(name).getShort() : null; }
    public String getString(String name) { return get(name) != null ? get(name).getString() : null; }
    public Boolean getBoolean(String name) { return get(name) != null ? get(name).getBoolean() : null; }
    public Float getFloat(String name) { return get(name) != null ? get(name).getFloat() : null; }
    public Double getDouble(String name) { return get(name) != null ? get(name).getDouble() : null; }

    @Override
    public MSLADefaultsParams get(Object option) {
        if ("ResolutionX".equals(option)) {
            var res = get("Resolution");
            if (res == null) return null;
            return new MachineProperty(((Size) res.getAsType("Size")).getWidth().toString());
        } else if ("ResolutionY".equals(option)) {
            var res = get("Resolution");
            if (res == null) return null;
            return new MachineProperty(((Size) res.getAsType("Size")).getHeight().toString());
        }
        return super.get(option);
    }
}
