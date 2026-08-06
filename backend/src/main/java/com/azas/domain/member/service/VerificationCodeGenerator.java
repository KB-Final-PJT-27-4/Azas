package com.azas.domain.member.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Locale;

@Component
public class VerificationCodeGenerator {

    private static final int CODE_BOUND = 1_000_000;

    private final SecureRandom secureRandom;

    public VerificationCodeGenerator() {
        this(new SecureRandom());
    }

    VerificationCodeGenerator(
            SecureRandom secureRandom
    ) {
        this.secureRandom = secureRandom;
    }

    public String generate() {
        int code = secureRandom.nextInt(CODE_BOUND);

        return String.format(
                Locale.ROOT,
                "%06d",
                code
        );
    }
}