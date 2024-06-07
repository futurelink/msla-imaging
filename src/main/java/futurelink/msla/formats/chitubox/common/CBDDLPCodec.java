package futurelink.msla.formats.chitubox.common;

import futurelink.msla.formats.MSLAException;
import futurelink.msla.formats.iface.*;

public class CBDDLPCodec implements MSLALayerCodec<byte[][]> {
    @Override
    public MSLALayerEncodeOutput<byte[][]> Encode(int layerNumber, MSLALayerEncodeReader input)
            throws MSLAException
    {
        return null;
    }

    @Override
    public int Decode(int layerNumber,
                      MSLALayerDecodeInput<byte[][]> data,
                      MSLALayerDecodeWriter writer) throws MSLAException
    {
        byte[] span = new byte[10]; // temporary set!!!
        var imageLength = data.size();
        var encodedRLE = data.data();
        var antialias_level = 1; // parent.AntiAliasing
        for (byte antialias_bit = 0; antialias_bit < antialias_level; antialias_bit++) {
            var layer = encodedRLE[antialias_bit];
            int n = 0;
            for (byte b : layer) {
                // Lower 7 bits is the repeat count for the bit (0..127)
                int reps = b & 0x7f;

                // We only need to set the non-zero pixels
                // High bit is on for white, off for black
                if ((b & 0x80) != 0) for (int i = 0; i < reps; i++) span[n + i]++;

                n += reps;
                if (n == imageLength) break;
                if (n > imageLength) throw new MSLAException("Error image ran off the end");
            }
        }

        for (int i = 0; i < imageLength; i++) {
            int newC = span[i] * (256 / antialias_level);
            if (newC > 0) newC--;
            span[i] = (byte) newC;
        }

        return 0;
    }
}
