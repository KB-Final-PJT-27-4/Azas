package com.azas.domain.member.service;

import com.azas.domain.member.entity.PhoneVerification;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PhoneVerificationStore {

    // 인증 서비스가 MySQL 같은 저장 기술에 직접 의존하지 않게 한다.
    void save(PhoneVerification phoneVerification);

    Optional<PhoneVerification> findByIdAndMemberId(
            long phoneVerificationId,
            long memberId
    );

    Optional<PhoneVerification> findLatestByMemberId(
            long memberId
    );

    Optional<PhoneVerification> findLatestByPhoneNumberHash(
            String phoneNumberHash
    );

    Optional<PhoneVerification>
    findByVerificationTokenHash(
            String verificationTokenHash
    );

    int expireUnverifiedByMemberId(
            long memberId,
            LocalDateTime expiredAt
    );

    boolean increaseAttemptCountIfPending(
            long phoneVerificationId,
            long memberId,
            LocalDateTime attemptedAt,
            int maxAttempts
    );

    boolean markVerifiedIfPending(
            long phoneVerificationId,
            long memberId,
            String verificationTokenHash,
            LocalDateTime verifiedAt,
            LocalDateTime tokenExpiresAt,
            int maxAttempts
    );

    boolean consumeVerificationTokenIfUsable(
            long phoneVerificationId,
            long memberId,
            String verificationTokenHash,
            LocalDateTime consumedAt
    );
}