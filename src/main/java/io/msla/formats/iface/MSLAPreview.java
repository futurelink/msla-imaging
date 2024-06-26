package io.msla.formats.iface;

import io.msla.utils.Size;

import java.awt.image.BufferedImage;

public interface MSLAPreview {
    /**
     * Preview image data.
     */
    BufferedImage getImage();

    /**
     * Sets image.
     * @param image {@link BufferedImage} containing preview image data
     */
    void setImage(BufferedImage image);

    /**
     * Preview image resolution.
     */
    Size getResolution();
}
