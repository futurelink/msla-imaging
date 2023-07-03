package futurelink.msla.formats.creality.tables;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CXDLPFileLayerTest {
    @Test
    void LayerLineTest() {
        byte[] lineBytes = {1, 2, 3, 4, 5, (byte) 128};
        var line1 = CXDLPFileLayer.LayerLine.fromByteArray(lineBytes);
        System.out.println(line1);
        var line2 = new CXDLPFileLayer.LayerLine(line1.getStartY(), line1.getEndY(), line1.getStartX(), line1.getGray());
        System.out.println(line2);
        assertEquals(line1, line2);

        var line3 = new CXDLPFileLayer.LayerLine((short) 20, (short) 50, (short) 10, (byte) 100);
        System.out.println(line3);
        var bytes = Arrays.copyOf(line3.getCoordinates(), 6);
        bytes[5] = line3.getGray();
        var line4 = CXDLPFileLayer.LayerLine.fromByteArray(bytes);
        System.out.println(line4);

        assertEquals(line3, line4);
    }

    @Test
    void LayerFromBufferedImageTest() throws IOException {
        var img = new BufferedImage(200, 100, BufferedImage.TYPE_BYTE_GRAY);
        img.getGraphics().drawLine(0, 10, 0, 50);
        img.getGraphics().drawLine( 0,55, 0, 75);
        img.getGraphics().drawLine( 20, 55, 20, 75);
        var layer = new CXDLPFileLayer(img);
        System.out.print(layer);
        assertEquals(3, layer.LineCount);
        assertEquals(24, layer.getLines()[0].getStartY());
        assertEquals(44, layer.getLines()[0].getEndY());
        assertEquals(0, layer.getLines()[0].getStartX());
        assertEquals(49, layer.getLines()[1].getStartY());
        assertEquals(89, layer.getLines()[1].getEndY());
        assertEquals(0, layer.getLines()[1].getStartX());
        assertEquals(24, layer.getLines()[2].getStartY());
        assertEquals(44, layer.getLines()[2].getEndY());
        assertEquals(20, layer.getLines()[2].getStartX());
    }
}
