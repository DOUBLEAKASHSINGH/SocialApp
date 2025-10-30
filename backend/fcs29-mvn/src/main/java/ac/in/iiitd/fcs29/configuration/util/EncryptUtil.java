package ac.in.iiitd.fcs29.configuration.util;

import ac.in.iiitd.fcs29.constant.Constants;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

public class EncryptUtil {
    private static final String TRANSFORMATION = Constants.Security.ENCRYPTION_TRANSFORMATION; // ChaCha20-Poly1305
    private static final String ALGORITHM = Constants.Security.ENCRYPTION_ALGORITHM; // ChaCha20
    private static final String SECRET_KEY = Constants.Security.ENCRYPTION_KEY;
    private static final int KEY_SIZE = Constants.Security.ENCRYPTION_KEY_SIZE; // ChaCha20 uses 256-bit keys
    private static final int NONCE_SIZE = Constants.Security.ENCRYPTION_NONCE_SIZE; // Recommended 12-byte nonce

    private static SecretKey getSecretKey() {
        final byte[] key = Base64.getDecoder().decode(SECRET_KEY);
        return new SecretKeySpec(key, ALGORITHM);
    }

    public static String encrypt(String value) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            byte[] nonce = new byte[NONCE_SIZE];
            new SecureRandom().nextBytes(nonce);

            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(), new IvParameterSpec(nonce));
            byte[] encryptedValue = cipher.doFinal(value.getBytes());

            byte[] combined = new byte[nonce.length + encryptedValue.length];
            System.arraycopy(nonce, 0, combined, 0, nonce.length);
            System.arraycopy(encryptedValue, 0, combined, nonce.length, encryptedValue.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException("Error while encrypting", e);
        }
    }

    public static byte[] encrypt(byte[] data) {
        try {
            byte[] nonce = new byte[NONCE_SIZE];
            new SecureRandom().nextBytes(nonce);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(), new IvParameterSpec(nonce));
            byte[] encryptedData = cipher.doFinal(data);

            byte[] combined = new byte[nonce.length + encryptedData.length];
            System.arraycopy(nonce, 0, combined, 0, nonce.length);
            System.arraycopy(encryptedData, 0, combined, nonce.length, encryptedData.length);
            return combined;
        } catch (Exception e) {
            throw new RuntimeException("Error while encrypting file", e);
        }
    }

    public static String decrypt(String encryptedValue) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            byte[] decoded = Base64.getDecoder().decode(encryptedValue);
            byte[] nonce = Arrays.copyOfRange(decoded, 0, NONCE_SIZE);
            byte[] encryptedData = Arrays.copyOfRange(decoded, NONCE_SIZE, decoded.length);

            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), new IvParameterSpec(nonce));
            return new String(cipher.doFinal(encryptedData));
        } catch (Exception e) {
            throw new RuntimeException("Error while decrypting", e);
        }
    }

    public static byte[] decrypt(byte[] encryptedData) {
        try {
            byte[] nonce = Arrays.copyOfRange(encryptedData, 0, NONCE_SIZE);
            byte[] actualData = Arrays.copyOfRange(encryptedData, NONCE_SIZE, encryptedData.length);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), new IvParameterSpec(nonce));
            return cipher.doFinal(actualData);
        } catch (Exception e) {
            throw new RuntimeException("Error while decrypting file", e);
        }
    }
}
