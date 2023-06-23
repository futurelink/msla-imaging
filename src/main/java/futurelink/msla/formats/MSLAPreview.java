package futurelink.msla.formats;

import java.awt.image.BufferedImage;

public interface MSLAPreview {
    BufferedImage getImage();
    int getResolutionX();
    int getResolutionY();
}
