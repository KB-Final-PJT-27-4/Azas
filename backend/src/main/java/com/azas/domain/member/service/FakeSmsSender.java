package com.azas.domain.member.service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class FakeSmsSender implements SmsSender {

    private final Map<String, String> verificationCodes =
            new ConcurrentHashMap<>();

    @Override
    public void sendVerificationCode(
            String phoneNumber,
            String verificationCode
    ) {
        verificationCodes.put(
                phoneNumber,
                verificationCode
        );
    }

    public Optional<String> findLatestVerificationCode(
            String phoneNumber
    ) {
        return Optional.ofNullable(
                verificationCodes.get(phoneNumber)
        );
    }

    public void clear() {
        verificationCodes.clear();
    }
}