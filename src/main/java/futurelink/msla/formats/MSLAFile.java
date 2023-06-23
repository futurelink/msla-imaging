package futurelink.msla.formats;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

public interface MSLAFile {
    MSLAPreview getPreview();
    void updatePreviewImage() throws IOException;
    float getDPI();
    void addLayer(MSLAEncodeReader reader) throws IOException;
    void addLayer(MSLAEncodeReader reader, float layerHeight, float exposureTime,
                  float liftSpeed, float liftHeight) throws IOException;
    void readLayer(FileInputStream iStream, int layer, MSLADecodeWriter writer) throws IOException;
    void read(FileInputStream iStream) throws IOException;
    void write(OutputStream stream) throws IOException;
}
