package com.azas.domain.member.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PhoneVerification {

    private Long phoneVerificationId;
    private Long memberId;
    private byte[] phoneNumberCiphertext;
    private String phoneNumberHash;
    private String verificationCodeHash;
    private int attemptCount;
    private LocalDateTime expiresAt;
    private LocalDateTime verifiedAt;
    private String verificationTokenHash;
    private LocalDateTime tokenExpiresAt;
    private LocalDateTime tokenConsumedAt;
    private LocalDateTime createdAt;

    public static PhoneVerification issue(
            long memberId,
            byte[] phoneNumberCiphertext,
            String phoneNumberHash,
            String verificationCodeHash,
            LocalDateTime expiresAt,
            LocalDateTime createdAt
    ) {
        PhoneVerification phoneVerification =
                new PhoneVerification();

        phoneVerification.memberId = memberId;
        phoneVerification.phoneNumberCiphertext =
                phoneNumberCiphertext.clone();
        phoneVerification.phoneNumberHash = phoneNumberHash;
        phoneVerification.verificationCodeHash =
                verificationCodeHash;
        phoneVerification.attemptCount = 0;
        phoneVerification.expiresAt = expiresAt;
        phoneVerification.createdAt = createdAt;

        return phoneVerification;
    }

    public byte[] getPhoneNumberCiphertext() {
        return phoneNumberCiphertext == null
                ? null
                : phoneNumberCiphertext.clone();
    }

    public boolean isVerified() {
        return verifiedAt != null;
    }

    public boolean isExpiredAt(LocalDateTime now) {
        return !expiresAt.isAfter(now);
    }

    public boolean hasReachedAttemptLimit(int maxAttempts) {
        return attemptCount >= maxAttempts;
    }

    public boolean isPendingAt(
            LocalDateTime now,
            int maxAttempts
    ) {
        return !isVerified()
                && !isExpiredAt(now)
                && !hasReachedAttemptLimit(maxAttempts);
    }

    public boolean isResendAvailableAt(
            LocalDateTime now,
            Duration resendCooldown
    ) {
        // 재발송 가능 시각은 별도 컬럼 없이 요청 생성 시각에서 계산한다.
        return !createdAt
                .plus(resendCooldown)
                .isAfter(now);
    }

    public boolean isVerificationTokenConsumed() {
        return tokenConsumedAt != null;
    }

    public boolean isVerificationTokenExpiredAt(
            LocalDateTime now
    ) {
        return tokenExpiresAt == null
                || !tokenExpiresAt.isAfter(now);
    }

    public boolean hasUsableVerificationTokenAt(
            LocalDateTime now
    ) {
        return isVerified()
                && verificationTokenHash != null
                && !isVerificationTokenConsumed()
                && !isVerificationTokenExpiredAt(now);
    }
}