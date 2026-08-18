package com.azas.domain.member.service;

import com.azas.domain.member.dto.PhoneVerificationConfirmResult;
import com.azas.domain.member.entity.PhoneVerification;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class PhoneVerificationConfirmService {

    private static final int MAX_ATTEMPTS = 5;

    private static final Duration TOKEN_EXPIRATION =
            Duration.ofMinutes(10);

    private final PhoneVerificationStore
            phoneVerificationStore;
    private final PhoneVerificationHasher
            phoneVerificationHasher;
    private final PhoneNumberProtector
            phoneNumberProtector;
    private final PhoneVerificationTokenGenerator
            phoneVerificationTokenGenerator;

    public PhoneVerificationConfirmResult confirm(
            long memberId,
            long phoneVerificationId,
            String verificationCode
    ) {
        PhoneVerification phoneVerification =
                phoneVerificationStore
                        .findByIdAndMemberId(
                                phoneVerificationId,
                                memberId
                        )
                        .orElseThrow(
                                this::notAvailable
                        );

        LocalDateTime now =
                LocalDateTime.now(ZoneOffset.UTC);

        validateAvailable(
                phoneVerification,
                now
        );

        boolean codeMatches =
                phoneVerificationHasher
                        .matchesVerificationCode(
                                phoneVerification
                                        .getVerificationCodeHash(),
                                memberId,
                                phoneVerification
                                        .getPhoneNumberHash(),
                                verificationCode == null
                                        ? ""
                                        : verificationCode
                        );

        if (!codeMatches) {
            handleFailedAttempt(
                    phoneVerification,
                    memberId,
                    now
            );
        }

        String normalizedPhoneNumber =
                phoneNumberProtector.decrypt(
                        phoneVerification
                                .getPhoneNumberCiphertext()
                );

        String verificationToken =
                phoneVerificationTokenGenerator
                        .generate();

        String verificationTokenHash =
                phoneVerificationHasher
                        .hashVerificationToken(
                                verificationToken
                        );

        LocalDateTime tokenExpiresAt =
                now.plus(TOKEN_EXPIRATION);

        boolean verified =
                phoneVerificationStore
                        .markVerifiedIfPending(
                                phoneVerificationId,
                                memberId,
                                verificationTokenHash,
                                now,
                                tokenExpiresAt,
                                MAX_ATTEMPTS
                        );

        if (!verified) {
            throw notAvailable();
        }

        return new PhoneVerificationConfirmResult(
                phoneVerificationId,
                maskPhoneNumber(normalizedPhoneNumber),
                now,
                verificationToken,
                tokenExpiresAt
        );
    }

    private void validateAvailable(
            PhoneVerification phoneVerification,
            LocalDateTime now
    ) {
        if (
                phoneVerification.isVerified()
                        || phoneVerification.isExpiredAt(now)
        ) {
            throw notAvailable();
        }

        if (
                phoneVerification
                        .hasReachedAttemptLimit(
                                MAX_ATTEMPTS
                        )
        ) {
            throw attemptLimitExceeded();
        }
    }

    private void handleFailedAttempt(
            PhoneVerification phoneVerification,
            long memberId,
            LocalDateTime attemptedAt
    ) {
        boolean increased =
                phoneVerificationStore
                        .increaseAttemptCountIfPending(
                                phoneVerification
                                        .getPhoneVerificationId(),
                                memberId,
                                attemptedAt,
                                MAX_ATTEMPTS
                        );

        if (!increased) {
            throw notAvailable();
        }

        if (
                phoneVerification.getAttemptCount() + 1
                        >= MAX_ATTEMPTS
        ) {
            throw attemptLimitExceeded();
        }

        throw new BusinessException(
                ErrorCode.INVALID_PHONE_VERIFICATION_CODE
        );
    }

    private String maskPhoneNumber(
            String normalizedPhoneNumber
    ) {
        return normalizedPhoneNumber.substring(0, 3)
                + "-****-"
                + normalizedPhoneNumber.substring(7);
    }

    private BusinessException notAvailable() {
        return new BusinessException(
                ErrorCode.PHONE_VERIFICATION_NOT_AVAILABLE
        );
    }

    private BusinessException attemptLimitExceeded() {
        return new BusinessException(
                ErrorCode
                        .PHONE_VERIFICATION_ATTEMPT_LIMIT_EXCEEDED
        );
    }
}