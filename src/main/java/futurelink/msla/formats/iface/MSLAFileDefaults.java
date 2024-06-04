package futurelink.msla.formats.iface;

import futurelink.msla.utils.Size;
import lombok.Getter;

import java.util.HashMap;

@SuppressWarnings("unused")
public interface MSLAFileDefaults extends MSLADefaults {
    MSLALayerDefaults getLayerDefaults();

    String getMachineFullName();
    String getMachineManufacturer();
    String getMachineName();
    String getFileExtension();
    Class<? extends MSLAFile<?>> getFileClass();

    Size getResolution();
    float getPixelSizeUm();

    MSLAFileProps getFileProps();
}
