package com.azas.domain.auth.service;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Component
public class TokenHashEncoder {

    private static final String HASH_ALGORITHM =
            "SHA-256";

    public String encode(String token) {
        try {
            // MessageDigest는 thread-safe하지 않아 호출할 때마다 새로 생성한다.
            MessageDigest messageDigest =
                    MessageDigest.getInstance(
                            HASH_ALGORITHM
                    );

            byte[] tokenBytes =
                    token.getBytes(StandardCharsets.UTF_8);

            byte[] hashBytes =
                    messageDigest.digest(tokenBytes);

            return HexFormat.of()
                    .formatHex(hashBytes);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 알고리즘을 사용할 수 없습니다.",
                    exception
            );
        }
    }
}
