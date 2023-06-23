package futurelink.msla.tools;

import futurelink.msla.formats.MSLADecodeWriter;
import futurelink.msla.formats.MSLAFileCodec;
import futurelink.msla.formats.anycubic.PhotonWorkshopFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class ImageTools {
    public static void exportLayers(String fileName, String destinationDir, String format) throws IOException {
        try (var fis = new FileInputStream(fileName)) {
            var wsFile = new PhotonWorkshopFile(fis);

            System.out.println(wsFile.getDescriptor());
            System.out.println(wsFile.getHeader());
            System.out.println(wsFile.getPreview());
            System.out.println(wsFile.getMachine());
            System.out.println(wsFile.getExtra());
            System.out.println(wsFile.getLayerDef());

            if ((wsFile.getHeader() != null) && (wsFile.getLayerDef() != null)) {
                var decodeWriter = new MSLADecodeWriter() {
                    private final ConcurrentHashMap<Integer, BufferedImage> img = new ConcurrentHashMap<>();
                    @Override public MSLAFileCodec getCodec() {
                        return wsFile.getCodec();
                    }
                    @Override public void pixels(int layerNumber, int color, int linearPos, int count) {
                        for (int i = linearPos; i < linearPos + count; i++) {
                            img.get(layerNumber).getRaster().getDataBuffer().setElem(i, color);
                        }
                    }

                    @Override public void onStart(int layerNumber) {
                        img.put(layerNumber,new BufferedImage(
                                wsFile.getHeader().getResolutionX(),
                                wsFile.getHeader().getResolutionY(),
                                BufferedImage.TYPE_BYTE_GRAY
                        ));
                    }

                    @Override public void onFinish(int layerNumber, int pixels) throws IOException {
                        System.out.println("Layer " + layerNumber + " done pixels: " + pixels);
                        var d = new File(destinationDir);
                        if (!d.exists())
                            if (!d.mkdirs()) throw new IOException("Unable to create destination directory!");
                        try (var f = new FileOutputStream(destinationDir + "/" + layerNumber + "." + format)) {
                            ImageIO.write(img.get(layerNumber), format, f);
                            f.flush();
                        }
                        img.remove(layerNumber);
                    }

                    @Override
                    public void onError(int layerNumber, String error) {
                        System.out.println("Layer " + layerNumber + " error ");
                        img.remove(layerNumber);
                    }
                };

                // Start decoding layers
                for (int i = 0; i < wsFile.getLayerDef().getLayerCount(); i++)
                    wsFile.readLayer(fis, i, decodeWriter);
            }
        }
    }
}
