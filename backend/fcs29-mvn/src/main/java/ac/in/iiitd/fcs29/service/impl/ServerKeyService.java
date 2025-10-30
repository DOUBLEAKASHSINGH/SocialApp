package ac.in.iiitd.fcs29.service.impl;

import ac.in.iiitd.fcs29.configuration.util.SharedKeyUtil;
import ac.in.iiitd.fcs29.entity.User;
import ac.in.iiitd.fcs29.entity.UserPairKeys;
import ac.in.iiitd.fcs29.entity.embeddable.UserPairId;
import ac.in.iiitd.fcs29.repository.UserPairKeysRepository;
import ac.in.iiitd.fcs29.repository.UserRepository;
import ac.in.iiitd.fcs29.service.ChatGroupService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

@Service
public class ServerKeyService {

//    private static final String PUBLIC_KEY_FILE = "server_public.key";
//    private static final String PRIVATE_KEY_FILE = "server_private.key";
    @Value("${security.pki.public-key-path}")
    private String publicKeyPath;
    @Value("${security.pki.private-key-path}")
    private String privateKeyPath;
    private final UserPairKeysRepository userPairKeysRepository;
    private final ChatGroupService chatGroupService;
    private final UserRepository userRepository;

    private PublicKey publicKey;
    @Getter
    private PrivateKey privateKey;
    @Getter
    private String publicKeyBase64;

    public ServerKeyService(UserPairKeysRepository userPairKeysRepository, ChatGroupService chatGroupService,
                            UserRepository userRepository) {
        this.userPairKeysRepository = userPairKeysRepository;
        this.chatGroupService = chatGroupService;
        this.userRepository = userRepository;
    }

    public static PublicKey importPublicKeyFromBase64(String base64Key) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return factory.generatePublic(spec);
    }

    @PostConstruct
    public void init() throws Exception {
        File pubKeyFile = new File(publicKeyPath);
        File privKeyFile = new File(privateKeyPath);

        if (pubKeyFile.exists() && privKeyFile.exists()) {
            this.publicKey = loadPublicKey(Files.readAllBytes(pubKeyFile.toPath()));
            this.privateKey = loadPrivateKey(Files.readAllBytes(privKeyFile.toPath()));
        } else {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair keyPair = generator.generateKeyPair();

            this.publicKey = keyPair.getPublic();
            this.privateKey = keyPair.getPrivate();

            Files.write(pubKeyFile.toPath(), this.publicKey.getEncoded());
            Files.write(privKeyFile.toPath(), this.privateKey.getEncoded());

            if (pubKeyFile.setReadOnly() && privKeyFile.setReadOnly()) {
                System.out.println("Public and Private keys are set to read-only.");
            } else {
                System.out.println("Failed to set Public and Private keys to read-only.");
            }
        }
        this.publicKeyBase64 = Base64.getEncoder().encodeToString(this.publicKey.getEncoded());

        System.out.println("Public Key: " + this.publicKeyBase64);
    }

    private PublicKey loadPublicKey(byte[] data) throws Exception {
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return factory.generatePublic(new X509EncodedKeySpec(data));
    }

    private PrivateKey loadPrivateKey(byte[] data) throws Exception {
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return factory.generatePrivate(new PKCS8EncodedKeySpec(data));
    }

    public String decryptBase64(String base64Ciphertext) throws Exception {
        byte[] encryptedBytes = Base64.getDecoder().decode(base64Ciphertext);

        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        OAEPParameterSpec oaepParams = new OAEPParameterSpec(
                "SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT
        );
        cipher.init(Cipher.DECRYPT_MODE, getPrivateKey(), oaepParams);

        byte[] decrypted = cipher.doFinal(encryptedBytes);
        return new String(decrypted, StandardCharsets.UTF_8);  // Specify UTF-8 encoding
    }

    public <T> T decryptPayload(Map<String, String> payloadMap, Class<T> clazz) throws Exception {
        String decryptedJson = decryptBase64(payloadMap.get("payload"));
        return new ObjectMapper().readValue(decryptedJson, clazz);
    }

    public String encryptForUser(String json, String base64Key) throws Exception {
        PublicKey userPublicKey = importPublicKeyFromBase64(base64Key);
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        OAEPParameterSpec oaepParams = new OAEPParameterSpec(
                "SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT
        );
        cipher.init(Cipher.ENCRYPT_MODE, userPublicKey, oaepParams);

        byte[] encrypted = cipher.doFinal(json.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public String getUserSharedKey(String userId1, String userId2, String userPublicKey) throws Exception {
        System.out.println("getUserSharedKey = " + userId1 + " userId2 = " + userId2);
        User user1 = userRepository.findById(userId1).orElseThrow(() -> new EntityNotFoundException("User not found"));
        User user2 = userRepository.findById(userId2).orElseThrow(() -> new EntityNotFoundException("User not found"));

        UserPairId pairId = new UserPairId(user1.getEmail(), user2.getEmail());

        String key;
        Optional<UserPairKeys> optional = userPairKeysRepository.findById(pairId);
        if (optional.isPresent()) {
            key = optional.get().getSharedKey(); // Already stored
        } else {

            String newKey = SharedKeyUtil.generateAesKeyBase64();
            UserPairKeys pair = new UserPairKeys();
            pair.setId(pairId);
            pair.setUser1(user1);
            pair.setUser2(user2);
            pair.setSharedKey(newKey);

            userPairKeysRepository.save(pair);

            key = newKey;
        }
        System.out.println("User Shared Key: " + key);
        return encryptForUser(key, userPublicKey);
    }

    public String getGroupSharedKey(String groupId, String userId, boolean isAdminReq, String userPublicKey) throws Exception {
        System.out.println("getGroupSharedKey = " + groupId + " userId = " + userId + " isAdminReq = " + isAdminReq);
        if (!isAdminReq) {
            if (!chatGroupService.isUserInGroup(userId, groupId)) {
                throw new EntityNotFoundException("User is not part of the group");
            }
        }
        String key;
        if (chatGroupService.groupKeyExists(groupId)) {
            key = chatGroupService.getGroupKey(groupId);
        } else {
            String newKey = SharedKeyUtil.generateAesKeyBase64();
            chatGroupService.setGroupKey(groupId, newKey);
            key = newKey;
        }
        System.out.println("getGroupSharedKey = " + groupId + " key = " + key);
        return encryptForUser(key, userPublicKey);
    }
}
