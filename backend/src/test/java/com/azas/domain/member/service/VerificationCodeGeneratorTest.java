package com.azas.domain.member.service;

import org.junit.jupiter.api.Test;

import java.security.SecureRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VerificationCodeGeneratorTest {

    @Test
    void generatesSixDigitCodeWithLeadingZeros() {
        SecureRandom secureRandom =
                mock(SecureRandom.class);

        when(secureRandom.nextInt(1_000_000))
                .thenReturn(42);

        VerificationCodeGenerator generator =
                new VerificationCodeGenerator(
                        secureRandom
                );

        String verificationCode =
                generator.generate();

        assertEquals("000042", verificationCode);
    }

    @Test
    void generatedCodeContainsOnlySixDigits() {
        VerificationCodeGenerator generator =
                new VerificationCodeGenerator();

        for (int index = 0; index < 100; index++) {
            String verificationCode =
                    generator.generate();

            assertTrue(
                    verificationCode.matches("\\d{6}")
            );
        }
    }
}