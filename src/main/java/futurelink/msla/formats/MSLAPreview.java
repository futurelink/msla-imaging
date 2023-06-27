package futurelink.msla.formats;

import futurelink.msla.formats.utils.Size;

import java.awt.image.BufferedImage;

public interface MSLAPreview {
    BufferedImage getImage();
    Size getResolution();
}
