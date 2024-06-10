package futurelink.msla.formats.iface;

import futurelink.msla.utils.Size;

@SuppressWarnings("unused")
public interface MSLAFileDefaults extends MSLADefaults {
    MSLALayerDefaults getLayerDefaults();

    String getMachineFullName();
    String getMachineManufacturer();
    String getMachineName();
    String getFileExtension();
    Class<? extends MSLAFile<?>> getFileClass();

    Size getResolution();
    float getPixelSize();

    MSLADefaultsParams getFileOption(String name);
    MSLAFileProps getFileProps();
}
