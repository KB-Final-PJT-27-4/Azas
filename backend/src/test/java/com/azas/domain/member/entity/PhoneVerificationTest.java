package com.azas.domain.member.entity;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PhoneVerificationTest {

    private static final LocalDateTime NOW =
            LocalDateTime.of(2026, 8, 6, 12, 0);

    private static final int MAX_ATTEMPTS = 5;

    private static final Duration RESEND_COOLDOWN =
            Duration.ofSeconds(60);

    @Test
    void issuesPendingPhoneVerification() {
        byte[] ciphertext = {1, 2, 3};

        PhoneVerification phoneVerification =
                PhoneVerification.issue(
                        1L,
                        ciphertext,
                        "phone-number-hash",
                        "verification-code-hash",
                        NOW.plusMinutes(3),
                        NOW
                );

        assertEquals(1L, phoneVerification.getMemberId());
        assertEquals(
                "phone-number-hash",
                phoneVerification.getPhoneNumberHash()
        );
        assertEquals(
                "verification-code-hash",
                phoneVerification.getVerificationCodeHash()
        );
        assertEquals(0, phoneVerification.getAttemptCount());
        assertArrayEquals(
                ciphertext,
                phoneVerification.getPhoneNumberCiphertext()
        );

        assertTrue(
                phoneVerification.isPendingAt(
                        NOW,
                        MAX_ATTEMPTS
                )
        );
        assertFalse(phoneVerification.isVerified());
        assertFalse(phoneVerification.isExpiredAt(NOW));
    }

    @Test
    void expiresAtExpirationBoundary() {
        PhoneVerification phoneVerification =
                createPhoneVerification();

        assertTrue(
                phoneVerification.isExpiredAt(
                        NOW.plusMinutes(3)
                )
        );

        assertFalse(
                phoneVerification.isPendingAt(
                        NOW.plusMinutes(3),
                        MAX_ATTEMPTS
                )
        );
    }

    @Test
    void doesNotAllowResendBeforeCooldown() {
        PhoneVerification phoneVerification =
                createPhoneVerification();

        assertFalse(
                phoneVerification.isResendAvailableAt(
                        NOW.plusSeconds(59),
                        RESEND_COOLDOWN
                )
        );
    }

    @Test
    void allowsResendAtCooldownBoundary() {
        PhoneVerification phoneVerification =
                createPhoneVerification();

        assertTrue(
                phoneVerification.isResendAvailableAt(
                        NOW.plusSeconds(60),
                        RESEND_COOLDOWN
                )
        );
    }

    @Test
    void doesNotExposeMutableCiphertextArray() {
        byte[] ciphertext = {1, 2, 3};

        PhoneVerification phoneVerification =
                PhoneVerification.issue(
                        1L,
                        ciphertext,
                        "phone-number-hash",
                        "verification-code-hash",
                        NOW.plusMinutes(3),
                        NOW
                );

        ciphertext[0] = 9;

        byte[] returnedCiphertext =
                phoneVerification.getPhoneNumberCiphertext();
        returnedCiphertext[1] = 9;

        assertArrayEquals(
                new byte[]{1, 2, 3},
                phoneVerification.getPhoneNumberCiphertext()
        );
    }

    @Test
    void issuedVerificationDoesNotHaveUsableToken() {
        PhoneVerification phoneVerification =
                createPhoneVerification();

        assertFalse(
                phoneVerification
                        .hasUsableVerificationTokenAt(NOW)
        );

        assertFalse(
                phoneVerification
                        .isVerificationTokenConsumed()
        );

        assertTrue(
                phoneVerification
                        .isVerificationTokenExpiredAt(NOW)
        );
    }

    private PhoneVerification createPhoneVerification() {
        return PhoneVerification.issue(
                1L,
                new byte[]{1, 2, 3},
                "phone-number-hash",
                "verification-code-hash",
                NOW.plusMinutes(3),
                NOW
        );
    }
}