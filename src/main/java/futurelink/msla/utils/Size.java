package futurelink.msla.utils;

import lombok.Getter;

@Getter
public class Size {
    int width;
    int height;
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
        if (obj instanceof Size) return this.width == ((Size)obj).width && this.height == ((Size)obj).height;
        return false;
    }

    @Override
    public String toString() {
        return width + " x " + height;
    }
}