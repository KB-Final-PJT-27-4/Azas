package com.azas.domain.member.service;

import org.springframework.stereotype.Component;

@Component
public class PhoneNumberNormalizer {

    private static final String KOREAN_MOBILE_PATTERN =
            "^010\\d{8}$";

    public String normalize(String phoneNumber) {
        if (phoneNumber == null) {
            throw new IllegalArgumentException(
                    "휴대폰번호는 필수입니다."
            );
        }

        String normalized = phoneNumber
                .replaceAll("[\\s-]", "");

        if (!normalized.matches(KOREAN_MOBILE_PATTERN)) {
            throw new IllegalArgumentException(
                    "올바른 휴대폰번호 형식이 아닙니다."
            );
        }

        return normalized;
    }
}