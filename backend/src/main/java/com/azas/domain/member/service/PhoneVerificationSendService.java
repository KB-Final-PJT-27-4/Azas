package com.azas.domain.member.service;

import com.azas.domain.member.dto.PhoneVerificationSendResult;
import com.azas.domain.member.entity.PhoneVerification;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PhoneVerificationSendService {

    private static final Duration CODE_EXPIRATION =
            Duration.ofMinutes(3);

    private static final Duration RESEND_COOLDOWN =
            Duration.ofSeconds(60);

    private final PhoneVerificationStore
            phoneVerificationStore;
    private final PhoneNumberNormalizer
            phoneNumberNormalizer;
    private final PhoneNumberProtector
            phoneNumberProtector;
    private final PhoneVerificationHasher
            phoneVerificationHasher;
    private final VerificationCodeGenerator
            verificationCodeGenerator;
    private final SmsSender smsSender;

    @Transactional
    public PhoneVerificationSendResult send(
            long memberId,
            String phoneNumber
    ) {
        String normalizedPhoneNumber =
                normalize(phoneNumber);

        LocalDateTime now =
                LocalDateTime.now(ZoneOffset.UTC);

        String phoneNumberHash =
                phoneVerificationHasher
                        .hashPhoneNumber(
                                normalizedPhoneNumber
                        );

        validateMemberResendCooldown(
                memberId,
                now
        );

        validatePhoneNumberResendCooldown(
                phoneNumberHash,
                now
        );

        String verificationCode =
                verificationCodeGenerator.generate();

        byte[] phoneNumberCiphertext =
                phoneNumberProtector.encrypt(
                        normalizedPhoneNumber
                );

        String verificationCodeHash =
                phoneVerificationHasher
                        .hashVerificationCode(
                                memberId,
                                phoneNumberHash,
                                verificationCode
                        );

        LocalDateTime expiresAt =
                now.plus(CODE_EXPIRATION);

        phoneVerificationStore
                .expireUnverifiedByMemberId(
                        memberId,
                        now
                );

        PhoneVerification phoneVerification =
                PhoneVerification.issue(
                        memberId,
                        phoneNumberCiphertext,
                        phoneNumberHash,
                        verificationCodeHash,
                        expiresAt,
                        now
                );

        phoneVerificationStore.save(
                phoneVerification
        );

        sendSms(
                normalizedPhoneNumber,
                verificationCode
        );

        return new PhoneVerificationSendResult(
                phoneVerification
                        .getPhoneVerificationId(),
                expiresAt,
                now.plus(RESEND_COOLDOWN)
        );
    }

    private String normalize(String phoneNumber) {
        try {
            return phoneNumberNormalizer.normalize(
                    phoneNumber
            );
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(
                    ErrorCode.INVALID_PHONE_NUMBER,
                    exception
            );
        }
    }

    private void validateMemberResendCooldown(
            long memberId,
            LocalDateTime now
    ) {
        validateResendCooldown(
                phoneVerificationStore
                        .findLatestByMemberId(memberId),
                now
        );
    }

    private void validatePhoneNumberResendCooldown(
            String phoneNumberHash,
            LocalDateTime now
    ) {
        validateResendCooldown(
                phoneVerificationStore
                        .findLatestByPhoneNumberHash(
                                phoneNumberHash
                        ),
                now
        );
    }

    private void validateResendCooldown(
            Optional<PhoneVerification>
                    latestVerification,
            LocalDateTime now
    ) {
        boolean resendAllowed =
                latestVerification
                        .map(
                                verification ->
                                        verification
                                                .isResendAvailableAt(
                                                        now,
                                                        RESEND_COOLDOWN
                                                )
                        )
                        .orElse(true);

        if (!resendAllowed) {
            throw new BusinessException(
                    ErrorCode
                            .PHONE_VERIFICATION_RESEND_NOT_ALLOWED
            );
        }
    }

    private void sendSms(
            String phoneNumber,
            String verificationCode
    ) {
        try {
            smsSender.sendVerificationCode(
                    phoneNumber,
                    verificationCode
            );
        } catch (RuntimeException exception) {
            // 발송 실패 시 현재 트랜잭션을 롤백해 사용할 수 없는 인증 요청을 남기지 않는다.
            throw new BusinessException(
                    ErrorCode.SMS_DELIVERY_FAILED,
                    exception
            );
        }
    }
}