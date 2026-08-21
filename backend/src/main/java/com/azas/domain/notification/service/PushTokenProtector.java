package com.azas.domain.notification.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class PushTokenProtector {

    private static final String CIPHER_TRANSFORMATION =
            "AES/GCM/NoPadding";
    private static final int AES_256_KEY_LENGTH = 32;
    private static final int IV_LENGTH = 12;
    private static final int AUTHENTICATION_TAG_LENGTH_BITS = 128;
    private static final byte FORMAT_VERSION = 1;

    private final SecretKey secretKey;
    private final SecureRandom secureRandom;

    @Autowired
    public PushTokenProtector(
            @Value("${PUSH_TOKEN_ENCRYPTION_KEY_BASE64:${ACCOUNT_NUMBER_ENCRYPTION_KEY_BASE64}}")
            String encryptionKeyBase64
    ) {
        this(encryptionKeyBase64, new SecureRandom());
    }

    PushTokenProtector(
            String encryptionKeyBase64,
            SecureRandom secureRandom
    ) {
        this.secretKey = createSecretKey(encryptionKeyBase64);
        this.secureRandom = secureRandom;
    }

    public byte[] encrypt(String pushToken) {
        try {
            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    secretKey,
                    new GCMParameterSpec(
                            AUTHENTICATION_TAG_LENGTH_BITS,
                            iv
                    )
            );
            cipher.updateAAD(new byte[]{FORMAT_VERSION});

            byte[] ciphertext = cipher.doFinal(
                    pushToken.getBytes(StandardCharsets.UTF_8)
            );

            return ByteBuffer.allocate(1 + IV_LENGTH + ciphertext.length)
                    .put(FORMAT_VERSION)
                    .put(iv)
                    .put(ciphertext)
                    .array();
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException(
                    "푸시 토큰을 암호화할 수 없습니다.",
                    exception
            );
        }
    }

    public String decrypt(byte[] encryptedPushToken) {
        validateEncryptedPushToken(encryptedPushToken);

        try {
            ByteBuffer buffer = ByteBuffer.wrap(encryptedPushToken);
            byte formatVersion = buffer.get();

            if (formatVersion != FORMAT_VERSION) {
                throw new IllegalArgumentException(
                        "지원하지 않는 푸시 토큰 암호문 형식입니다."
                );
            }

            byte[] iv = new byte[IV_LENGTH];
            buffer.get(iv);
            byte[] ciphertext = new byte[buffer.remaining()];
            buffer.get(ciphertext);

            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(
                    Cipher.DECRYPT_MODE,
                    secretKey,
                    new GCMParameterSpec(
                            AUTHENTICATION_TAG_LENGTH_BITS,
                            iv
                    )
            );
            cipher.updateAAD(new byte[]{formatVersion});

            return new String(
                    cipher.doFinal(ciphertext),
                    StandardCharsets.UTF_8
            );
        } catch (GeneralSecurityException exception) {
            throw new IllegalArgumentException(
                    "푸시 토큰 암호문이 유효하지 않습니다.",
                    exception
            );
        }
    }

    private SecretKey createSecretKey(String encryptionKeyBase64) {
        if (encryptionKeyBase64 == null || encryptionKeyBase64.isBlank()) {
            throw new IllegalArgumentException(
                    "푸시 토큰 암호화 키가 설정되지 않았습니다."
            );
        }

        final byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(encryptionKeyBase64);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "푸시 토큰 암호화 키는 Base64 형식이어야 합니다.",
                    exception
            );
        }

        if (keyBytes.length != AES_256_KEY_LENGTH) {
            throw new IllegalArgumentException(
                    "푸시 토큰 암호화 키는 32바이트여야 합니다."
            );
        }

        return new SecretKeySpec(keyBytes, "AES");
    }

    private void validateEncryptedPushToken(byte[] encryptedPushToken) {
        int minimumLength = 1
                + IV_LENGTH
                + AUTHENTICATION_TAG_LENGTH_BITS / 8;

        if (encryptedPushToken == null
                || encryptedPushToken.length <= minimumLength) {
            throw new IllegalArgumentException(
                    "푸시 토큰 암호문이 유효하지 않습니다."
            );
        }
    }
}
