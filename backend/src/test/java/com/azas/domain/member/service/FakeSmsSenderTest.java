package com.azas.domain.member.service;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FakeSmsSenderTest {

    private final FakeSmsSender fakeSmsSender =
            new FakeSmsSender();

    @Test
    void storesLatestVerificationCodeForPhoneNumber() {
        fakeSmsSender.sendVerificationCode(
                "01012345678",
                "482193"
        );

        Optional<String> verificationCode =
                fakeSmsSender
                        .findLatestVerificationCode(
                                "01012345678"
                        );

        assertTrue(verificationCode.isPresent());
        assertEquals(
                "482193",
                verificationCode.get()
        );
    }

    @Test
    void replacesPreviousCodeForSamePhoneNumber() {
        fakeSmsSender.sendVerificationCode(
                "01012345678",
                "111111"
        );

        fakeSmsSender.sendVerificationCode(
                "01012345678",
                "222222"
        );

        assertEquals(
                "222222",
                fakeSmsSender
                        .findLatestVerificationCode(
                                "01012345678"
                        )
                        .orElseThrow()
        );
    }

    @Test
    void clearsStoredVerificationCodes() {
        fakeSmsSender.sendVerificationCode(
                "01012345678",
                "482193"
        );

        fakeSmsSender.clear();

        assertTrue(
                fakeSmsSender
                        .findLatestVerificationCode(
                                "01012345678"
                        )
                        .isEmpty()
        );
    }
}