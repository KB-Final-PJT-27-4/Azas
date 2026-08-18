package com.azas.domain.member.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public final class PhoneVerificationSendResult {

    private final long verificationId;
    private final LocalDateTime expiresAt;
    private final LocalDateTime resendAvailableAt;

    public PhoneVerificationSendResult(
            long verificationId,
            LocalDateTime expiresAt,
            LocalDateTime resendAvailableAt
    ) {
        this.verificationId = verificationId;
        this.expiresAt = expiresAt;
        this.resendAvailableAt = resendAvailableAt;
    }
}