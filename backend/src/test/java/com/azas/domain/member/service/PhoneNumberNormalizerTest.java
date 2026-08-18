package com.azas.domain.member.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PhoneNumberNormalizerTest {

    private final PhoneNumberNormalizer normalizer =
            new PhoneNumberNormalizer();

    @Test
    void removesHyphensAndSpaces() {
        assertEquals(
                "01012345678",
                normalizer.normalize(
                        "010-1234-5678"
                )
        );

        assertEquals(
                "01012345678",
                normalizer.normalize(
                        "010 1234 5678"
                )
        );
    }

    @Test
    void rejectsNullPhoneNumber() {
        assertThrows(
                IllegalArgumentException.class,
                () -> normalizer.normalize(null)
        );
    }

    @Test
    void rejectsInvalidPhoneNumber() {
        assertThrows(
                IllegalArgumentException.class,
                () -> normalizer.normalize(
                        "02-1234-5678"
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> normalizer.normalize(
                        "010-1234-ABCD"
                )
        );
    }
}