package com.azas.domain.auth.entity;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RefreshTokenTest {

    private static final LocalDateTime NOW =
            LocalDateTime.of(2026, 8, 4, 12, 0);

    @Test
    void issuedTokenIsActiveBeforeExpiration() {
        RefreshToken refreshToken = RefreshToken.issue(
                1L,
                "token-hash",
                NOW.plusSeconds(1)
        );

        assertFalse(refreshToken.isRevoked());
        assertFalse(refreshToken.isExpiredAt(NOW));
        assertTrue(refreshToken.isActiveAt(NOW));
    }

    @Test
    void tokenIsExpiredAfterExpiration() {
        RefreshToken refreshToken = RefreshToken.issue(
                1L,
                "token-hash",
                NOW.minusNanos(1)
        );

        assertTrue(refreshToken.isExpiredAt(NOW));
        assertFalse(refreshToken.isActiveAt(NOW));
    }

    @Test
    void tokenIsExpiredAtExactExpirationTime() {
        RefreshToken refreshToken = RefreshToken.issue(
                1L,
                "token-hash",
                NOW
        );

        assertTrue(refreshToken.isExpiredAt(NOW));
        assertFalse(refreshToken.isActiveAt(NOW));
    }

    @Test
    void revokedTokenIsNotActiveBeforeExpiration() {
        RefreshToken refreshToken = RefreshToken.issue(
                1L,
                "token-hash",
                NOW.plusDays(1)
        );

        ReflectionTestUtils.setField(
                refreshToken,
                "revokedAt",
                NOW.minusSeconds(1)
        );

        assertTrue(refreshToken.isRevoked());
        assertFalse(refreshToken.isExpiredAt(NOW));
        assertFalse(refreshToken.isActiveAt(NOW));
    }
}
