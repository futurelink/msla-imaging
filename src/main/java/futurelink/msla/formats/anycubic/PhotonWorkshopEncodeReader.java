package futurelink.msla.formats.anycubic;

import java.io.IOException;
import java.io.InputStream;

public interface PhotonWorkshopEncodeReader {
    InputStream read() throws IOException;
}
