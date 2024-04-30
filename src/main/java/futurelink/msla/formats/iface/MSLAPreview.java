package futurelink.msla.formats.iface;

import futurelink.msla.formats.utils.Size;

import java.awt.image.BufferedImage;

public interface MSLAPreview {
    /**
     * Preview image data.
     * @return
     */
    BufferedImage getImage();

    /**
     * Preview image resolution.
     * @return
     */
    Size getResolution();
}
