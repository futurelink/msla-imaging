package futurelink.msla.formats.utils;

import lombok.Getter;

public class Size {
    @Getter int width;
    @Getter int height;
    public Size(Size size) {
        this.width = size.width;
        this.height = size.height;
    }
    public Size(int width, int height) { this.width = width; this.height = height; }
    public int length() { return width * height; }

    @Override
    public String toString() {
        return width + " x " + height;
    }
}
