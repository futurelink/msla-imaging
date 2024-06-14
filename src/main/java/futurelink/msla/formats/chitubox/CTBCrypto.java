package futurelink.msla.formats.chitubox;

import futurelink.msla.formats.MSLAException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.logging.Logger;

public class CTBCrypto {
    private static final Logger logger = Logger.getLogger(CTBCrypto.class.getName());
    private static final String Secret0 = "HDgSAB0BEiE/AgpPAhwhM1QAAUwHPT8HTywEGiEjVAoBDwEsJgAKC0wVPDoRTwkDATg3AE9HQhAhNF1VZWYk" +
            "MHYVHQpMEjI3HQEcGFM7OQBPHwkBOD8AGwoIUyAlER1PCBIhN1QKAQ8BLCYABgACX3U6GwwEH191NRsBHBgBND8aHENMATAlAB0GDwc8ORo" +
            "cQ0weOjgbHwAAGi83AAYAAlM0OBBPAQMdeCURARwJUyU5GAYMBRYmdgAHDhhTJSQRGQoCByZ2GxsHCQEmdhIdAAFTNiQRDhsJUzQ4EE8DCR" +
            "IxexIAHRsSJzJUHAAABiE/GwEcTBInOQEBC0wHMDUcAQAAHDIvWmU8GQMlOQYbBgIUdSIcBhxMFTw6EU8JAwE4NwBPBh9TNHYHGwocXjc3F" +
            "wRPChwndkcrTxgWNj4aAAMDFCx2FQELTBU6JFQbBwlTNjkZAhoCGiEvVAAZCQE0OhhBTz8HPDoYQ08NHTF2HQFPDhY9NxgJTwMVdSMHCh0f" +
            "UyIzVA4DABwidgAATx4WNDJYTxwNBTB2FQELTB40OB0fGgASITNUGwcJUzM/GApPChwndgYKGQUWInpUHQoPHCMzBk8LDQc0dhUBC0wXMCI" +
            "RDBtMAyc5FgMKAQB1IhtPAg0YMHYNABpMEDogER0KCFMzJBsCTwEaJiIVBAofUzQ4EE8KHgE6JAdBZTwfMDcHCkNMHjQ9EU8WAwYndgcHBg" +
            "oHdTAGAAJMBz0/B08fHhwxIxcbHEwSOzJUBwoAA3UiHApPXzd1IhEMBwIcOTkTFk8LHHUwGx0YDQExdhUBC0wcJTMaTk8/BiUmGx0bTBwlM" +
            "xpCHAMGJzURTxwDHyAiHQABH191IhwOG0wENC9UGApMEDQ4VAwdCRIhM1QNChgHMCRUHx0DFyA1ABxPChwndgAHCkwQOjgHGgIJASZ4";

    private static final String Secret1 = "0FuOM3HePRrlTyLd31v9lKtdZDqdfr+vQgPzENhSKuo=";
    private static final String Secret2 = "DwEKBQULBgcIBgoMDA0JDw==";

    public static byte[] XORCipher(byte[] text, byte[] key) {
        var output = new byte[text.length];
        for (int i = 0; i < text.length; i++) output[i] = (byte)(text[i] ^ key[i % key.length]);
        return output;
    }

    public interface LayerRLECryptCallback {
        byte getByte(int index);
        void setByte(int index, byte b);
        int getSize();
    }

    public static void LayerRLECrypt(int seed, int layerIndex, LayerRLECryptCallback callback)  {
        if (seed == 0) return;
        int init = seed * 0x2d83cdac + 0xd8a83423;
        int key = (layerIndex * 0x1e1530cd + 0xec3d47cd) * init;
        int index = 0;
        for (int i = 0; i < callback.getSize(); i++) {
            byte k = (byte)((key >> 8 * index) & 0xff);
            if ((++index & 3) == 0) { key += init; index = 0; }
            callback.setByte(i, (byte)(callback.getByte(i) ^ k));
        }
    }

    public static Cipher initCipher(int mode) throws MSLAException {
        try {
            byte[] decodedKey = Base64.getDecoder().decode(Secret1);
            logger.fine("AES key size is " + decodedKey.length * Byte.SIZE); // Must be 256

            byte[] decodedIV = Base64.getDecoder().decode(Secret2);
            logger.fine("IV size is " + decodedIV.length * Byte.SIZE); // Must be 16

            SecretKey key = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
            IvParameterSpec iv = new IvParameterSpec(decodedIV);
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(mode, key, iv);
            return cipher;
        } catch (NoSuchPaddingException | NoSuchAlgorithmException |
                 InvalidKeyException | InvalidAlgorithmParameterException e) {
            throw new MSLAException("Can't init crypt cipher", e);
        }
    }
}
