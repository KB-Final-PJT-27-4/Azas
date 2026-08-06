package com.azas.domain.member.service;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PhoneNumberProtectorTest {

    private static final String ENCRYPTION_KEY_BASE64 =
            Base64.getEncoder().encodeToString(
                    "0123456789abcdef0123456789abcdef"
                            .getBytes(StandardCharsets.UTF_8)
            );

    private final PhoneNumberProtector protector =
            new PhoneNumberProtector(
                    ENCRYPTION_KEY_BASE64
            );

    @Test
    void encryptsAndDecryptsPhoneNumber() {
        byte[] ciphertext =
                protector.encrypt("01012345678");

        String phoneNumber =
                protector.decrypt(ciphertext);

        assertEquals(
                "01012345678",
                phoneNumber
        );
    }

    @Test
    void rejectsTamperedCiphertext() {
        byte[] ciphertext =
                protector.encrypt("01012345678");

        ciphertext[ciphertext.length - 1] ^= 1;

        assertThrows(
                IllegalArgumentException.class,
                () -> protector.decrypt(ciphertext)
        );
    }

    @Test
    void rejectsShortEncryptionKey() {
        String shortKeyBase64 =
                Base64.getEncoder().encodeToString(
                        "short-key"
                                .getBytes(
                                        StandardCharsets.UTF_8
                                )
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> new PhoneNumberProtector(
                        shortKeyBase64
                )
        );
    }
}