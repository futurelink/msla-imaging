package futurelink.msla.formats.iface;

import futurelink.msla.formats.MSLAException;

public interface MSLAFileDefaults {
    String getName();
    String getFileExtension();
    Integer getOptionInt(String name);
    Byte getOptionByte(String blockName, String name);
    Short getOptionShort(String name);
    String getOptionString(String name);
    float getPixelSizeUm();
    void setFields(String blockName, MSLAFileBlockFields fields) throws MSLAException;
}
