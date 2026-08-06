package com.azas.domain.auth.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenHashEncoderTest {

    private final TokenHashEncoder tokenHashEncoder =
            new TokenHashEncoder();

    @Test
    void encodesRefreshTokenWithSha256() {
        String tokenHash =
                tokenHashEncoder.encode("refresh-token");

        assertEquals(
                "0eb17643d4e9261163783a420859c92c7d212fa9624106a12b510afbec266120",
                tokenHash
        );
        assertEquals(64, tokenHash.length());
        assertTrue(
                tokenHash.matches("[0-9a-f]{64}")
        );

        assertEquals(
                tokenHash,
                tokenHashEncoder.encode("refresh-token")
        );
        assertNotEquals(
                tokenHash,
                tokenHashEncoder.encode("different-token")
        );
    }
}