package futurelink.msla.tools;

import futurelink.msla.formats.utils.Size;
import lombok.Setter;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 *                   Pattern example (one character 0.1mm width):
 *   6 layers, 2 seconds each layer the last one appear on all layer images,
 *   the first one is displayed only once in 1st layer. Distance between copper lines
 *   is 0.1mm minimum and 0.3mm max, enough to test.
 *   ---------------------------------------------------------------------------------
 *   | | | ||  ||  ||  |||
 *   | | | ||  ||  ||  |||
 *   | | | ||  ||  ||  |||
 *   | | | ||  ||  ||  |||
 *   | | | ||  ||  ||  |||
 *   ||| | | ||  |  |   ||
 *   ||| | | ||  |  |   ||
 *   ||| | | ||  |  |   ||
 *   ||| | | ||  |  |   ||
 *   ||| | | ||  |  |   ||
 *   ||| ||| ||  | ||  |||
 *   ||| ||| ||  | ||  |||
 *   ||| ||| ||  | ||  |||
 *   ||| ||| ||  | ||  |||
 *   ||| ||| ||  | ||  |||
 *             5s                   7s                 9s                 11s
 *   ---------------------------------------------------------------------------------
 */
public class PCBCalibrationPattern {
    @Setter int height; // in mm
    float pixelSize;
    Size resolution;

    @Setter float startTime = 5.0f;
    @Setter float interval = 1.0f;

    // Pattern widths (tracks) - evens are line widths, odds are gap widths
    float[][] pattern = {
            { 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.2f, 0.2f, 0.2f, 0.2f, 0.2f, 0.2f, 0.3f },
            { 0.3f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.2f, 0.2f, 0.1f, 0.2f, 0.1f, 0.3f, 0.2f },
            { 0.3f, 0.1f, 0.3f, 0.1f, 0.2f, 0.2f, 0.1f, 0.1f, 0.2f, 0.2f, 0.3f }
    };

    // Circles (THT pads) - first is outer diameter, second is inner diameter
    float[][] circles = {
            {0.3f, 0.15f}, {0.4f, 0.2f}, {0.5f, 0.25f}, { 0.6f, 0.3f },
            { 0.8f, 0.4f }, { 0.9f, 0.45f }, { 1.0f, 0.5f }
    };

    public PCBCalibrationPattern(Size resolution, float pixelSize) {
        this.resolution = resolution;
        this.pixelSize = pixelSize;
    }

    public final BufferedImage generate(int size, int repetition, int repetitions) {
        var image = new BufferedImage(resolution.getWidth(), resolution.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        var g = image.getGraphics();

        boolean vertical = resolution.getWidth() < resolution.getHeight();

        // Draw top one third of pattern 0.1 + 0.1 + 0.2 + 0.2 + 0.3mm = 0.9mm total
        var distancePX = 120;
        var sizePX = distancePX * repetitions;
        int posX, posY;
        if (vertical) {
            posX = resolution.getWidth() / 2 - size / 2;
            posY = resolution.getHeight() / 2 - sizePX / 2;
        } else {
            posX = resolution.getWidth() / 2 - sizePX / 2;
            posY = resolution.getHeight() / 2 - size / 2;
        }
        int fragmentSize = size / 3;
        for (int i = 0; i < repetitions - repetition; i++) {
            // Draw lines
            g.setColor(Color.WHITE);
            for (int p = 0; p < 3; p++) {
                var offset = i * distancePX;
                for (int j = 0; j < pattern[p].length; j++) {
                    var w = (int) (pattern[p][j] / pixelSize * 1000);
                    if ((j % 2) == 0) { // Draw road
                        if (vertical) g.fillRect(posX + fragmentSize * p, posY + offset, fragmentSize, w);
                        else g.fillRect(offset + posX, posY + fragmentSize * p, w, fragmentSize);
                    }
                    offset += w;
                }
            }

            // Draw circles
            for (int p = 0; p < circles.length; p++) {
                var ro = (int) Math.floor(circles[p][0] * 500 / pixelSize); // A circle radius
                int x, y;
                if (vertical) {
                    y = posY + i * distancePX + 80 - ro;
                    x = posX + p * 50 - ro + 100;
                } else {
                    x = posX + i * distancePX + 80 - ro;
                    y = posY + p * 50 - ro + 100;
                }
                g.setColor(Color.WHITE);
                g.fillOval(x, y, ro * 2, ro * 2);

                var ri = (int) Math.floor(circles[p][1] * 500 / pixelSize); // A hole radius
                g.setColor(Color.BLACK);
                g.fillOval(x + ro - ri, y + ro - ri, ri * 2, ri * 2);
            }
            // Add text
            g.setColor(Color.WHITE);
            g.setFont(g.getFont().deriveFont(40.0f));
            if (vertical) {
                g.drawString((startTime + interval * (repetitions - i - 1)) + "s", posX + size + 10, posY + i * distancePX + 40);
            } else {
                g.drawString((startTime + interval * (repetitions - i - 1)) + "s", posX + i * distancePX, posY + size + 40);
            }
        }

        return image;
    }
}
