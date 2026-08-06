package com.azas.domain.auth.service;

import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RefreshTokenGeneratorTest {

    private final RefreshTokenGenerator refreshTokenGenerator =
            new RefreshTokenGenerator();

    @Test
    void generatesUrlSafeRandomRefreshToken() {
        String firstToken =
                refreshTokenGenerator.generate();
        String secondToken =
                refreshTokenGenerator.generate();

        assertEquals(43, firstToken.length());
        assertTrue(
                firstToken.matches("[A-Za-z0-9_-]+")
        );
        assertFalse(firstToken.contains("="));
        assertNotEquals(firstToken, secondToken);

        byte[] decodedToken =
                Base64.getUrlDecoder()
                        .decode(firstToken);

        assertEquals(32, decodedToken.length);
    }
}