package com.azas.domain.member.mapper;

import com.azas.domain.member.entity.PhoneVerification;
import com.azas.domain.member.service.PhoneVerificationStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MyBatisPhoneVerificationStore
        implements PhoneVerificationStore {

    private final PhoneVerificationMapper
            phoneVerificationMapper;

    @Override
    public void save(
            PhoneVerification phoneVerification
    ) {
        phoneVerificationMapper.insert(
                phoneVerification
        );
    }

    @Override
    public Optional<PhoneVerification>
    findByIdAndMemberId(
            long phoneVerificationId,
            long memberId
    ) {
        return Optional.ofNullable(
                phoneVerificationMapper
                        .findByIdAndMemberId(
                                phoneVerificationId,
                                memberId
                        )
        );
    }

    @Override
    public Optional<PhoneVerification>
    findLatestByMemberId(
            long memberId
    ) {
        return Optional.ofNullable(
                phoneVerificationMapper
                        .findLatestByMemberId(memberId)
        );
    }

    @Override
    public Optional<PhoneVerification>
    findLatestByPhoneNumberHash(
            String phoneNumberHash
    ) {
        return Optional.ofNullable(
                phoneVerificationMapper
                        .findLatestByPhoneNumberHash(
                                phoneNumberHash
                        )
        );
    }

    @Override
    public int expireUnverifiedByMemberId(
            long memberId,
            LocalDateTime expiredAt
    ) {
        return phoneVerificationMapper
                .expireUnverifiedByMemberId(
                        memberId,
                        expiredAt
                );
    }

    @Override
    public boolean increaseAttemptCountIfPending(
            long phoneVerificationId,
            long memberId,
            LocalDateTime attemptedAt,
            int maxAttempts
    ) {
        return phoneVerificationMapper
                .increaseAttemptCountIfPending(
                        phoneVerificationId,
                        memberId,
                        attemptedAt,
                        maxAttempts
                ) == 1;
    }

    @Override
    public boolean markVerifiedIfPending(
            long phoneVerificationId,
            long memberId,
            String verificationTokenHash,
            LocalDateTime verifiedAt,
            LocalDateTime tokenExpiresAt,
            int maxAttempts
    ) {
        return phoneVerificationMapper
                .markVerifiedIfPending(
                        phoneVerificationId,
                        memberId,
                        verificationTokenHash,
                        verifiedAt,
                        tokenExpiresAt,
                        maxAttempts
                ) == 1;
    }
}