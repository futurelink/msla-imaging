package futurelink.msla.utils;

import lombok.Getter;

import java.io.Serializable;
import java.util.Objects;

/**
 * Size helper class.
 * Widely used in order to describe machine resolution and image sizes.
 */
@Getter
public class Size implements Serializable {
    private final Integer width;
    private final Integer height;
    public Size(Size size) {
        this.width = size.width;
        this.height = size.height;
    }
    public Size(int width, int height) { this.width = width; this.height = height; }
    public int length() { return width * height; }

    public static Size parseSize(String s) {
        var p = s.split("x");
        if (p.length != 2) throw new RuntimeException("Can't parse size");
        return new Size(Integer.parseInt(p[0]), Integer.parseInt(p[1]));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Size)
            return Objects.equals(this.width, ((Size) obj).width) &&
                    Objects.equals(this.height, ((Size) obj).height);
        return false;
    }

    @Override
    public String toString() {
        return width + " x " + height;
    }
}
