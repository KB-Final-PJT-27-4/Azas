package com.azas.domain.member.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

@Component
public class PhoneVerificationTokenGenerator {

    private static final int TOKEN_BYTE_LENGTH = 32;

    private final SecureRandom secureRandom;

    public PhoneVerificationTokenGenerator() {
        this(new SecureRandom());
    }

    PhoneVerificationTokenGenerator(
            SecureRandom secureRandom
    ) {
        this.secureRandom = secureRandom;
    }

    public String generate() {
        byte[] randomBytes =
                new byte[TOKEN_BYTE_LENGTH];

        secureRandom.nextBytes(randomBytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);
    }
}