package io.msla.formats.iface;

import io.msla.formats.iface.options.MSLAOptionName;
import io.msla.utils.Size;

@SuppressWarnings("unused")
public interface MSLAFileDefaults extends MSLADefaults {
    MSLALayerDefaults getLayerDefaults();

    String getMachineFullName();
    String getMachineManufacturer();
    String getMachineName();
    String getFileExtension();
    Class<? extends MSLAFile<?>> getFileClass();

    Size getResolution();
    Float getPixelSize();

    MSLADefaultsParams getFileOption(MSLAOptionName name);
    MSLAFileProps getFileProps();
    boolean hasFileOption(MSLAOptionName name);
}
