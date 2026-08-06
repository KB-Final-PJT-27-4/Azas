package com.azas.domain.member.service;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PhoneVerificationHasherTest {

    private static final String SECRET_BASE64 =
            Base64.getEncoder().encodeToString(
                    "abcdef0123456789abcdef0123456789"
                            .getBytes(StandardCharsets.UTF_8)
            );

    private final PhoneVerificationHasher hasher =
            new PhoneVerificationHasher(
                    SECRET_BASE64
            );

    @Test
    void hashesPhoneNumberWithHmacSha256() {
        String phoneNumberHash =
                hasher.hashPhoneNumber(
                        "01012345678"
                );

        assertEquals(64, phoneNumberHash.length());
        assertTrue(
                phoneNumberHash.matches(
                        "[0-9a-f]{64}"
                )
        );

        assertEquals(
                phoneNumberHash,
                hasher.hashPhoneNumber(
                        "01012345678"
                )
        );

        assertNotEquals(
                phoneNumberHash,
                hasher.hashPhoneNumber(
                        "01087654321"
                )
        );
    }

    @Test
    void verifiesMatchingVerificationCode() {
        String phoneNumberHash =
                hasher.hashPhoneNumber(
                        "01012345678"
                );

        String verificationCodeHash =
                hasher.hashVerificationCode(
                        1L,
                        phoneNumberHash,
                        "482193"
                );

        assertTrue(
                hasher.matchesVerificationCode(
                        verificationCodeHash,
                        1L,
                        phoneNumberHash,
                        "482193"
                )
        );

        assertFalse(
                hasher.matchesVerificationCode(
                        verificationCodeHash,
                        1L,
                        phoneNumberHash,
                        "000000"
                )
        );
    }

    @Test
    void doesNotMatchHashForDifferentMember() {
        String phoneNumberHash =
                hasher.hashPhoneNumber(
                        "01012345678"
                );

        String verificationCodeHash =
                hasher.hashVerificationCode(
                        1L,
                        phoneNumberHash,
                        "482193"
                );

        assertFalse(
                hasher.matchesVerificationCode(
                        verificationCodeHash,
                        2L,
                        phoneNumberHash,
                        "482193"
                )
        );
    }
}