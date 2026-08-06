package com.azas.domain.member.mapper;

import com.azas.domain.member.entity.PhoneVerification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface PhoneVerificationMapper {

    int insert(PhoneVerification phoneVerification);

    PhoneVerification findByIdAndMemberId(
            @Param("phoneVerificationId")
            long phoneVerificationId,
            @Param("memberId")
            long memberId
    );

    PhoneVerification findLatestByMemberId(
            @Param("memberId")
            long memberId
    );

    PhoneVerification findLatestByPhoneNumberHash(
            @Param("phoneNumberHash")
            String phoneNumberHash
    );

    int expireUnverifiedByMemberId(
            @Param("memberId")
            long memberId,
            @Param("expiredAt")
            LocalDateTime expiredAt
    );

    int increaseAttemptCountIfPending(
            @Param("phoneVerificationId")
            long phoneVerificationId,
            @Param("memberId")
            long memberId,
            @Param("attemptedAt")
            LocalDateTime attemptedAt,
            @Param("maxAttempts")
            int maxAttempts
    );

    int markVerifiedIfPending(
            @Param("phoneVerificationId")
            long phoneVerificationId,
            @Param("memberId")
            long memberId,
            @Param("verificationTokenHash")
            String verificationTokenHash,
            @Param("verifiedAt")
            LocalDateTime verifiedAt,
            @Param("tokenExpiresAt")
            LocalDateTime tokenExpiresAt,
            @Param("maxAttempts")
            int maxAttempts
    );
}