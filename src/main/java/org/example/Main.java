package org.example;

import futurelink.msla.formats.anycubic.PhotonWorkshopEncodeReader;
import futurelink.msla.formats.anycubic.PhotonWorkshopFile;
import futurelink.msla.formats.anycubic.PhotonWorkshopFileDefaults;

import javax.imageio.ImageIO;
import java.awt.image.*;
import java.io.*;

public class Main {
    public static class RasterBytesInputStream extends InputStream {
        private final Raster raster;
        private int position;

        public RasterBytesInputStream(Raster raster) {
            this.raster = raster;
        }

        @Override
        public int available() {
            return raster.getDataBuffer().getSize() - position;
        }

        @Override
        public int read() throws IOException {
            return raster.getDataBuffer().getElem(position++);
        }
    }

    public static void main(String[] args) throws IOException {
        testAddLayer();
        //testReadFileLayer("sample_data/PCB.pwmb");
        //testReadFileLayer("sample_data/Example_Photon_Mono_X_4K.pwma");
        //testReadFileLayer("sample_data/Example_Photon_Mono_X.pwmx");
        //testReadFileLayer("sample_data/Example_Photon_M3.pm3");
        //testReadFileLayer("sample_data/Example_Photon_M3_Max.pm3m");
        //testReadFileLayer("sample_data/Example_Photon_Mono.pwmo");
    }

    public static void testAddLayer() throws IOException  {
        var defaults = PhotonWorkshopFileDefaults.PhotonMono4K;
        try (var fos = new FileOutputStream("sample_data/PCB_Mono_4K." + defaults.getFileExtension())) {
            var wsFile = new PhotonWorkshopFile(defaults);
            wsFile.addLayer(() -> {
                try (var stream = new FileInputStream("sample_data/Test-PCB.png")) {
                    var raster = ImageIO.read(stream).getRaster();
                    return new RasterBytesInputStream(raster);
                }
            });
            wsFile.write(fos);
        }
    }

    public static void testReadFileLayer(String fileName) throws IOException {
        try (var fis = new FileInputStream(fileName)) {
            var wsFile = new PhotonWorkshopFile(fis);

            System.out.println(wsFile.getDescriptor());
            System.out.println(wsFile.getHeader());
            System.out.println(wsFile.getPreview());
            System.out.println(wsFile.getMachine());
            System.out.println(wsFile.getExtra());
            System.out.println(wsFile.getLayerDef());

            // 737.47 DPI on Photon Mono X 6K
            if (wsFile.getHeader() != null) {
                var img = new BufferedImage(
                        wsFile.getHeader().getResolutionX(),
                        wsFile.getHeader().getResolutionY(),
                        BufferedImage.TYPE_BYTE_GRAY
                );
                wsFile.readLayer(fis, 0, (color, linearPos, count) -> {
                    for (int i = linearPos; i < linearPos + count; i++) {
                        img.getRaster().getDataBuffer().setElem(i, color);
                    }
                });
                try (var f = new FileOutputStream("sample_data/test.png")) {
                    ImageIO.write(img, "png", f);
                    f.flush();
                }
            }
        }
    }
}
