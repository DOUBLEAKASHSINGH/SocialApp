package ac.in.iiitd.fcs29.constant;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class Constants {

    private Constants() {
        // restrict instantiation
    }
    /**
     * Security-related constants
     */
    public static final class Security {
        public static final String JWT_SECRET = "703273357638792F423F4528482B4D6250655368566D597133743677397A2443";
        public static final long TOKEN_EXPIRY_DURATION = (long) 60 * 60 * 1000; // 1 hour in milliseconds
        public static final String BEARER_PREFIX = "Bearer ";
        public static final String OTP_TOKEN = "otp-token";
        public static final String BEARER_HEADER = "authorization";

//        public static final String ENCRYPTION_ALGORITHM = "AES";
        // 16-byte key (for AES-128)
//        public static final String ENCRYPTION_KEY = "1234567890123456";
        public static final String ENCRYPTION_ALGORITHM = "ChaCha20";
        public static final String ENCRYPTION_TRANSFORMATION = "ChaCha20-Poly1305";
        // ChaCha20 uses 256-bit keys
        public static final String ENCRYPTION_KEY = "iLgq0Nw9Nau8gD0DhhYQEqnTsY2eVzcOqIXAWtzcaBA=";
        public static final int ENCRYPTION_KEY_SIZE = 256;
        public static final int ENCRYPTION_NONCE_SIZE = 12; // Recommended 12-byte nonce
    }

    public static final String SUPER_ADMIN = "mp.sharadjain24@gmail.com";

    public static final int MAX_GROUP_SIZE = 20;

    public static final int MAX_PAGE_SIZE = 20;
    public static final int MIN_PAGE_SIZE = 1;

    public static final int MAX_LOGIN_ATTEMPTS = 3;
    public static final int LOGIN_WAIT_TIME = 15; // in minutes

    // OTP (User Verification) constants
    public static final int OTP_DURATION_MIN = 3;
    public static final int OTP_DURATION_SEC = 210;  // 30 sec extra for processing delay
    public static final int OTP_LENGTH = 6;

    // Rate Limiter
    public static final int MAX_IMG_REQUESTS_PER_TIME = 40;
    public static final int IMG_REQUESTS_DURATION_SEC = 10;
    public static final int MAX_GENERAL_REQUESTS_PER_TIME = 100;
    public static final int GENERAL_REQUESTS_DURATION_MIN = 1;

    // Default content-type of files/content
    public static final String CONTENT_TYPE = "Content-Type";
//    public static final String MESSAGE_CONTENT_TYPE = "text/plain";
    public static final String MESSAGE_CONTENT_TYPE = "text/plain";
    public static final String IMAGE_PREFIX = "image";
    public static final String TEXT_PREFIX = "text/plain";
    public static final String MULTIPART_PREFIX = "multipart";
    public static final String PDF_PREFIX = "application/pdf";

    // allowed fields for user sorting
    public static final Set<String> USER_SORT_FIELDS = new HashSet<>(Arrays.asList("firstName", "lastName", "email",
            "isVerified", "role", "createdAt", "lastUpdatedAt", "isCredentialsExpired", "isAccountLocked"));

}
