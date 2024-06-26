package io.msla.formats.elegoo;

import io.msla.formats.CommonTestRoutines;
import io.msla.formats.MSLAException;
import io.msla.formats.iface.options.MSLAOptionName;
import io.msla.utils.FileFactory;
import io.msla.tools.ImageReader;
import io.msla.tools.ImageWriter;
import io.msla.utils.options.FileOptionMapper;
import io.msla.utils.defaults.MachineDefaults;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class GOOFileTest extends CommonTestRoutines {
    @Test
    void CreateFileTest() throws MSLAException, InterruptedException {
        var outFile = temp_dir + "elegoo_test.goo";
        var machine = "ELEGOO Mars 4 Max";
        var defaults = MachineDefaults.getInstance().getMachineDefaults(machine)
                .orElseThrow(() -> new MSLAException("Machine is not supported"));
        var file = FileFactory.instance.create(machine);
        assertEquals(GOOFile.class, file.getClass());
        assertEquals("5760 x 3600", file.getResolution().toString());
        assertEquals(34.0f, file.getPixelSize());
        assertEquals("ELEGOO Mars 4 Max", file.getMachineName());

        var options = new FileOptionMapper(file, defaults);
        for (var option : options.available()) options.get(option);
        assertFalse(Boolean.parseBoolean(options.get(MSLAOptionName.LayerSettings)));

        var files = new String[]{
                "test_data/ElegooFileTest/Example_Layer_0.png",
                "test_data/ElegooFileTest/Example_Layer_1.png",
                "test_data/ElegooFileTest/Example_Layer_2.png"
        };
        var layerBytes = new int[3];
        var encoders = file.getEncodersPool();
        try {
            for (var f : files) {
                file.addLayer(new ImageReader(file, resourceFile(f)), (layer, data) -> {
                    layerBytes[layer] = data.size();
                    System.out.println("Layer " + layer + " encoded data: " + Arrays.toString(Arrays.copyOfRange((byte[]) data.data(), 0, 50)));
                });
            }
            while (encoders.isEncoding()) Thread.sleep(10); // Wait while reading-encoding is done
        } catch (IOException e) {
            throw new MSLAException("Error adding layers", e);
        }

        // Check if layers were generated properly
        assertEquals(6173, layerBytes[0]);
        assertEquals(6235, layerBytes[1]);
        assertEquals(6266, layerBytes[2]);

        // Write output file
        writeMSLAFile(outFile, file);

        // Check if file is created
        assertFileExactSize(outFile, 214378);

        // Extract layers
        var file2 = FileFactory.instance.load(outFile);
        assertEquals(3, file.getLayers().count());
        assertTrue(file.isValid());

        // Asynchronously extract image files
        var layerPixels2 = new int[3];
        var layerFiles2 = new String[3];
        var writer = new ImageWriter(file2, temp_dir, "png", (layerNumber, fileName, pixels) -> {
            layerPixels2[layerNumber] = pixels;
            layerFiles2[layerNumber] = fileName;
        });
        file2.readLayer(writer, 0);
        file2.readLayer(writer, 1);
        file2.readLayer(writer, 2);
        while (file2.getDecodersPool().isDecoding()) Thread.sleep(10); // Wait while decoding-writing is done

        // Check pixels were written
        assertEquals(179726, layerPixels2[0]);
        assertEquals(183091, layerPixels2[1]);
        assertEquals(186446, layerPixels2[2]);

        // Check files exist
        assertFileExactSize(layerFiles2[0], 22711);
        assertFileExactSize(layerFiles2[1], 23019);
        assertFileExactSize(layerFiles2[2], 23026);
    }

    @Test
    void ReadTestFile() throws InterruptedException, MSLAException, IOException {
        var defaults = MachineDefaults.getInstance().getMachineDefaults("ELEGOO Mars 4 Max")
                .orElseThrow(() -> new MSLAException("Machine is not supported"));
        var file = FileFactory.instance.load(
                resourceFile("test_data/ElegooFileTest/Example_GOO.goo")
        );
        System.out.println(file);
        assertTrue(file.isValid());
        var options = new FileOptionMapper(file, defaults);
        System.out.println(options.get(MSLAOptionName.NormalLayersLiftSpeed));
        assertFalse(Boolean.parseBoolean(options.get(MSLAOptionName.LayerSettings)));

        ImageIO.write(file.getPreview(0).getImage(), "png", new File(temp_dir + "elegoo_preview_small.png"));
        assertFileExactSize(temp_dir + "elegoo_preview_small.png", 1781);

        ImageIO.write(file.getPreview(1).getImage(), "png", new File(temp_dir + "elegoo_preview_large.png"));
        assertFileExactSize(temp_dir + "elegoo_preview_large.png", 6559);

        // Asynchronously extract image files
        var layerPixels = new int[3];
        var layerFiles = new String[3];
        var writer = new ImageWriter(file, temp_dir, "png", (layerNumber, fileName, pixels) -> {
            layerPixels[layerNumber] = pixels;
            layerFiles[layerNumber] = fileName;
        });
        file.readLayer(writer, 0);
        file.readLayer(writer, 1);
        file.readLayer(writer, 2);
        while (file.getDecodersPool().isDecoding()) Thread.sleep(10); // Wait while decoding-writing is done

        // Check pixels were written
        assertEquals(179726, layerPixels[0]);
        assertEquals(183091, layerPixels[1]);
        assertEquals(186446, layerPixels[2]);

        // Check files exist
        assertFileMinSize(layerFiles[0], 10000);
        assertFileMinSize(layerFiles[1], 10000);
        assertFileMinSize(layerFiles[2], 10000);
    }
}
