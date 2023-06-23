package futurelink.msla.tools;

import lombok.Setter;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

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
    int resolutionX;
    int resolutionY;

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

    public PCBCalibrationPattern(int resolutionX, int resolutionY, float pixelSize) {
        this.resolutionX = resolutionX;
        this.resolutionY = resolutionY;
        this.pixelSize = pixelSize;
    }

    public final BufferedImage generate(int height, int repetition, int repetitions) {
        var image = new BufferedImage(resolutionX, resolutionY, BufferedImage.TYPE_BYTE_GRAY);
        var g = image.getGraphics();

        // Draw top one third of pattern 0.1 + 0.1 + 0.2 + 0.2 + 0.3mm = 0.9mm total
        var distancePX = 120;
        var widthPX = distancePX * repetitions;
        var posX = resolutionX / 2 - widthPX / 2;
        var posY = resolutionY / 2 - height / 2;
        var fragmentHeight = height / 3;
        for (int i = 0; i < repetitions - repetition; i++) {
            // Draw lines
            g.setColor(Color.WHITE);
            for (int p = 0; p < 3; p++) {
                var offset = i * distancePX;
                for (int j = 0; j < pattern[p].length; j++) {
                    var w = pattern[p][j] / pixelSize * 1000;
                    if ((j % 2) == 0) { // Draw road
                        g.fillRect(offset + posX, posY + fragmentHeight * p, (int) Math.floor(w), fragmentHeight);
                    }
                    offset += w;
                }
            }

            // Draw circles
            for (int p = 0; p < circles.length; p++) {
                var ro = (int) Math.floor(circles[p][0] * 500 / pixelSize); // A circle radius
                var x = posX + i * distancePX + 80 - ro;
                var y = posY + p * 50 - ro + 100;
                g.setColor(Color.WHITE);
                g.fillOval(x, y, ro * 2, ro * 2);

                var ri = (int) Math.floor(circles[p][1] * 500 / pixelSize); // A hole radius
                g.setColor(Color.BLACK);
                g.fillOval(x + ro - ri, y + ro - ri, ri * 2, ri * 2);
            }
            // Add text
            g.setColor(Color.WHITE);
            g.setFont(g.getFont().deriveFont(40.0f));
            g.drawString((startTime + interval * (repetitions - i - 1) )+ "s", posX + i * distancePX, posY + height + 40);
        }

        return image;
    }
}
