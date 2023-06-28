package futurelink.msla.formats;

import futurelink.msla.formats.utils.Size;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

public interface MSLAFile {
    MSLAFileCodec getCodec();
    MSLAPreview getPreview();
    void updatePreviewImage() throws IOException;
    float getDPI();
    void addLayer(MSLAEncodeReader reader) throws IOException;
    void addLayer(MSLAEncodeReader reader, float layerHeight, float exposureTime,
                  float liftSpeed, float liftHeight) throws IOException;
    void readLayer(int layer, MSLADecodeWriter writer) throws IOException;
    void write(OutputStream stream) throws IOException;
    Size getResolution();
    float getPixelSizeUm();
    int getLayerCount();
    boolean isValid();
    void setOption(String option, Serializable value) throws IOException;
}
