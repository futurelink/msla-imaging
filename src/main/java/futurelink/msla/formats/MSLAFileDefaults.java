package futurelink.msla.formats;

public interface MSLAFileDefaults {
    String getName();
    String getFileExtension();
    float getPixelSizeUm();
    Integer getOptionInt(String name);
    Byte getOptionByte(String name);
    Short getOptionShort(String name);
    String getOptionString(String name);
    MSLAFileBlockFields getOptionBlock(String name);
}
