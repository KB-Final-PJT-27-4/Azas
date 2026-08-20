package com.azas.domain.notification.service;

import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class PushTokenProtectorTest {

    @Test
    void encryptsAndDecryptsPushToken() {
        byte[] keyBytes = new byte[32];
        new SecureRandom().nextBytes(keyBytes);

        PushTokenProtector protector = new PushTokenProtector(
                Base64.getEncoder().encodeToString(keyBytes),
                new SecureRandom()
        );

        String pushToken = "fcm-registration-token";
        byte[] encrypted = protector.encrypt(pushToken);

        assertNotEquals(
                pushToken,
                Base64.getEncoder().encodeToString(encrypted)
        );
        assertEquals(pushToken, protector.decrypt(encrypted));
    }
}
