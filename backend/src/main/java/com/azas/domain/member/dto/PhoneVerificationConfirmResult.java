package com.azas.domain.member.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public final class PhoneVerificationConfirmResult {

    private final long verificationId;
    private final String maskedPhoneNumber;
    private final LocalDateTime verifiedAt;
    private final String verificationToken;
    private final LocalDateTime tokenExpiresAt;

    public PhoneVerificationConfirmResult(
            long verificationId,
            String maskedPhoneNumber,
            LocalDateTime verifiedAt,
            String verificationToken,
            LocalDateTime tokenExpiresAt
    ) {
        this.verificationId = verificationId;
        this.maskedPhoneNumber = maskedPhoneNumber;
        this.verifiedAt = verifiedAt;
        this.verificationToken = verificationToken;
        this.tokenExpiresAt = tokenExpiresAt;
    }
}