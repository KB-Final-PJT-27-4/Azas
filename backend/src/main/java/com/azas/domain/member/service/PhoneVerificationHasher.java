package com.azas.domain.member.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HexFormat;

@Component
public class PhoneVerificationHasher {

    private static final String HMAC_ALGORITHM =
            "HmacSHA256";

    private static final int MINIMUM_SECRET_LENGTH = 32;

    private final SecretKeySpec secretKey;

    public PhoneVerificationHasher(
            @Value("${PHONE_VERIFICATION_SECRET_BASE64}")
            String secretBase64
    ) {
        this.secretKey = createSecretKey(secretBase64);
    }

    public String hashPhoneNumber(
            String normalizedPhoneNumber
    ) {
        return encode(
                "phone-number\u0000"
                        + normalizedPhoneNumber
        );
    }

    public String hashVerificationCode(
            long memberId,
            String phoneNumberHash,
            String verificationCode
    ) {
        return encode(
                "verification-code\u0000"
                        + memberId
                        + "\u0000"
                        + phoneNumberHash
                        + "\u0000"
                        + verificationCode
        );
    }

    public boolean matchesVerificationCode(
            String storedVerificationCodeHash,
            long memberId,
            String phoneNumberHash,
            String verificationCode
    ) {
        if (storedVerificationCodeHash == null) {
            return false;
        }

        String calculatedHash =
                hashVerificationCode(
                        memberId,
                        phoneNumberHash,
                        verificationCode
                );

        return MessageDigest.isEqual(
                storedVerificationCodeHash.getBytes(
                        StandardCharsets.US_ASCII
                ),
                calculatedHash.getBytes(
                        StandardCharsets.US_ASCII
                )
        );
    }

    public String hashVerificationToken(
            String verificationToken
    ) {
        return encode(
                "verification-token\u0000"
                        + verificationToken
        );
    }

    private String encode(String value) {
        try {
            // Mac은 thread-safe하지 않아 호출할 때마다 새로 생성한다.
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(secretKey);

            byte[] hashBytes = mac.doFinal(
                    value.getBytes(
                            StandardCharsets.UTF_8
                    )
            );

            return HexFormat.of()
                    .formatHex(hashBytes);
        } catch (
                NoSuchAlgorithmException
                | InvalidKeyException exception
        ) {
            throw new IllegalStateException(
                    "휴대폰 인증 해시를 생성할 수 없습니다.",
                    exception
            );
        }
    }

    private SecretKeySpec createSecretKey(
            String secretBase64
    ) {
        byte[] secretBytes;

        try {
            secretBytes = Base64.getDecoder()
                    .decode(secretBase64);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "휴대폰 인증 비밀키는 Base64 형식이어야 합니다.",
                    exception
            );
        }

        if (secretBytes.length < MINIMUM_SECRET_LENGTH) {
            throw new IllegalArgumentException(
                    "휴대폰 인증 비밀키는 32바이트 이상이어야 합니다."
            );
        }

        return new SecretKeySpec(
                secretBytes,
                HMAC_ALGORITHM
        );
    }
}