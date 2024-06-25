package futurelink.msla.formats.iface;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.iface.options.MSLAOptionName;
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
    Float getPixelSize();

    MSLADefaultsParams getFileOption(MSLAOptionName name);
    MSLAFileProps getFileProps();
    boolean hasFileOption(MSLAOptionName name);
}
