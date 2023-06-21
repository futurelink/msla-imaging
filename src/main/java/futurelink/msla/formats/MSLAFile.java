package futurelink.msla.formats;

import futurelink.msla.formats.anycubic.PhotonWorkshopDecodeWriter;
import futurelink.msla.formats.anycubic.PhotonWorkshopEncodeReader;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public interface MSLAFile {
    float getDPI();
    void addLayer(PhotonWorkshopEncodeReader reader) throws IOException;
    void readLayer(FileInputStream iStream, int layer, PhotonWorkshopDecodeWriter writer) throws IOException;
    void read(FileInputStream iStream) throws IOException;
    void write(FileOutputStream stream) throws IOException;
}
