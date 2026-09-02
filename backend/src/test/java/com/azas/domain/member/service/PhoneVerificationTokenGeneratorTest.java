package com.azas.domain.member.service;

import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PhoneVerificationTokenGeneratorTest {

    private final PhoneVerificationTokenGenerator generator =
            new PhoneVerificationTokenGenerator();

    @Test
    void generatesUrlSafeRandomToken() {
        String firstToken = generator.generate();
        String secondToken = generator.generate();

        assertEquals(43, firstToken.length());
        assertTrue(
                firstToken.matches(
                        "[A-Za-z0-9_-]+"
                )
        );
        assertFalse(firstToken.contains("="));
        assertNotEquals(firstToken, secondToken);

        byte[] decoded =
                Base64.getUrlDecoder()
                        .decode(firstToken);

        assertEquals(32, decoded.length);
    }
}