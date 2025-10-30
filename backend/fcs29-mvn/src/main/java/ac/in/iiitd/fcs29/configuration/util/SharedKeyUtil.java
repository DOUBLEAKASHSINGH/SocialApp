package ac.in.iiitd.fcs29.configuration.util;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.util.Base64;

public class SharedKeyUtil {

    public static SecretKey generateAesKey() throws Exception {
        KeyGenerator generator = KeyGenerator.getInstance("AES");
        generator.init(256); // 256-bit AES
        return generator.generateKey(); // save this in UserPairKeys.sharedKey (Base64)
    }

    public static String encodeKeyBase64(SecretKey key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    public static String generateAesKeyBase64() {
        try {
            SecretKey key = generateAesKey();
            return encodeKeyBase64(key);
        } catch (Exception e) {
            throw new RuntimeException("Error generating AES key", e);
        }
    }
}
