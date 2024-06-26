package io.msla.formats.creality;

import io.msla.formats.CommonTestRoutines;
import io.msla.formats.MSLAException;
import io.msla.formats.creality.tables.CXDLPFileLayerLine;
import io.msla.utils.FileFactory;
import io.msla.tools.ImageReader;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CXDLPFileLayerTest extends CommonTestRoutines {
    @Test
    void LayerLineTest() {
        byte[] lineBytes = {1, 2, 3, 4, 5, (byte) 128};
        var line1 = CXDLPFileLayerLine.fromByteArray(lineBytes);
        logger.info(line1.toString());
        var line2 = new CXDLPFileLayerLine(line1.getStartY(), line1.getEndY(), line1.getStartX(), line1.getGray());
        logger.info(line2.toString());
        assertEquals(line1, line2);

        var line3 = new CXDLPFileLayerLine((short) 20, (short) 50, (short) 10, (byte) 100);
        logger.info(line3.toString());
        var bytes = Arrays.copyOf(line3.getCoordinates(), 6);
        bytes[5] = (byte) line3.getGray();
        var line4 = CXDLPFileLayerLine.fromByteArray(bytes);
        logger.info(line4.toString());

        assertEquals(line3, line4);
    }

    @Test
    void LayerFromBufferedImageTest() throws MSLAException, InterruptedException {
        // Prepare graphics
        var img = new BufferedImage(200, 100, BufferedImage.TYPE_BYTE_GRAY);
        img.getGraphics().drawLine(0, 10, 0, 50);
        img.getGraphics().drawLine( 0,55, 0, 75);
        img.getGraphics().drawLine( 20, 55, 20, 75);

        // Prepare file
        var file = (CXDLPFile) FileFactory.instance.create("CREALITY HALOT-ONE");
        file.addLayer(new ImageReader(file, img), null);
        while(file.getEncodersPool().isEncoding())  Thread.sleep(10); // Wait while working

        // Check layer lines
        var layer = file.getLayer(0);
        assertEquals(1254, layer.getLines().get(0).getStartY());
        assertEquals(1274, layer.getLines().get(0).getEndY());
        assertEquals(710, layer.getLines().get(0).getStartX());
        assertEquals(1279, layer.getLines().get(1).getStartY());
        assertEquals(1319, layer.getLines().get(1).getEndY());
        assertEquals(710, layer.getLines().get(1).getStartX());
        assertEquals(1254, layer.getLines().get(2).getStartY());
        assertEquals(1274, layer.getLines().get(2).getEndY());
        assertEquals(730, layer.getLines().get(2).getStartX());
        assertEquals(3, layer.getLines().size());
    }
}
