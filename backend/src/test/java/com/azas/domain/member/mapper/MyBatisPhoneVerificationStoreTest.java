package com.azas.domain.member.mapper;

import com.azas.domain.member.entity.PhoneVerification;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MyBatisPhoneVerificationStoreTest {

    private static final LocalDateTime NOW =
            LocalDateTime.of(2026, 8, 6, 12, 0);

    private static final int MAX_ATTEMPTS = 5;

    @Mock
    private PhoneVerificationMapper
            phoneVerificationMapper;

    @InjectMocks
    private MyBatisPhoneVerificationStore
            phoneVerificationStore;

    @Test
    void savesPhoneVerification() {
        PhoneVerification phoneVerification =
                createPhoneVerification();

        phoneVerificationStore.save(phoneVerification);

        verify(phoneVerificationMapper)
                .insert(phoneVerification);
    }

    @Test
    void returnsVerificationWhenMapperFindsByIdAndMember() {
        PhoneVerification phoneVerification =
                createPhoneVerification();

        when(
                phoneVerificationMapper
                        .findByIdAndMemberId(10L, 1L)
        ).thenReturn(phoneVerification);

        Optional<PhoneVerification> result =
                phoneVerificationStore
                        .findByIdAndMemberId(10L, 1L);

        assertTrue(result.isPresent());
        assertSame(phoneVerification, result.get());
    }

    @Test
    void returnsEmptyWhenMapperDoesNotFindVerification() {
        when(
                phoneVerificationMapper
                        .findByIdAndMemberId(10L, 1L)
        ).thenReturn(null);

        Optional<PhoneVerification> result =
                phoneVerificationStore
                        .findByIdAndMemberId(10L, 1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void returnsLatestVerificationForMember() {
        PhoneVerification phoneVerification =
                createPhoneVerification();

        when(
                phoneVerificationMapper
                        .findLatestByMemberId(1L)
        ).thenReturn(phoneVerification);

        Optional<PhoneVerification> result =
                phoneVerificationStore
                        .findLatestByMemberId(1L);

        assertTrue(result.isPresent());
        assertSame(phoneVerification, result.get());
    }

    @Test
    void returnsLatestVerificationForPhoneNumber() {
        PhoneVerification phoneVerification =
                createPhoneVerification();

        when(
                phoneVerificationMapper
                        .findLatestByPhoneNumberHash(
                                "phone-number-hash"
                        )
        ).thenReturn(phoneVerification);

        Optional<PhoneVerification> result =
                phoneVerificationStore
                        .findLatestByPhoneNumberHash(
                                "phone-number-hash"
                        );

        assertTrue(result.isPresent());
        assertSame(phoneVerification, result.get());
    }

    @Test
    void returnsExpiredVerificationCount() {
        when(
                phoneVerificationMapper
                        .expireUnverifiedByMemberId(
                                1L,
                                NOW
                        )
        ).thenReturn(2);

        int expiredCount =
                phoneVerificationStore
                        .expireUnverifiedByMemberId(
                                1L,
                                NOW
                        );

        assertEquals(2, expiredCount);
    }

    @Test
    void returnsTrueWhenAttemptCountIsIncreased() {
        when(
                phoneVerificationMapper
                        .increaseAttemptCountIfPending(
                                10L,
                                1L,
                                NOW,
                                MAX_ATTEMPTS
                        )
        ).thenReturn(1);

        boolean increased =
                phoneVerificationStore
                        .increaseAttemptCountIfPending(
                                10L,
                                1L,
                                NOW,
                                MAX_ATTEMPTS
                        );

        assertTrue(increased);
    }

    @Test
    void returnsFalseWhenAttemptCountIsNotIncreased() {
        when(
                phoneVerificationMapper
                        .increaseAttemptCountIfPending(
                                10L,
                                1L,
                                NOW,
                                MAX_ATTEMPTS
                        )
        ).thenReturn(0);

        boolean increased =
                phoneVerificationStore
                        .increaseAttemptCountIfPending(
                                10L,
                                1L,
                                NOW,
                                MAX_ATTEMPTS
                        );

        assertFalse(increased);
    }

    @Test
    void returnsTrueWhenVerificationIsMarkedVerified() {
        LocalDateTime tokenExpiresAt =
                NOW.plusMinutes(10);

        when(
                phoneVerificationMapper
                        .markVerifiedIfPending(
                                10L,
                                1L,
                                "verification-token-hash",
                                NOW,
                                tokenExpiresAt,
                                MAX_ATTEMPTS
                        )
        ).thenReturn(1);

        boolean verified =
                phoneVerificationStore
                        .markVerifiedIfPending(
                                10L,
                                1L,
                                "verification-token-hash",
                                NOW,
                                tokenExpiresAt,
                                MAX_ATTEMPTS
                        );

        assertTrue(verified);
    }

    @Test
    void returnsFalseWhenVerificationIsNotMarkedVerified() {
        LocalDateTime tokenExpiresAt =
                NOW.plusMinutes(10);

        when(
                phoneVerificationMapper
                        .markVerifiedIfPending(
                                10L,
                                1L,
                                "verification-token-hash",
                                NOW,
                                tokenExpiresAt,
                                MAX_ATTEMPTS
                        )
        ).thenReturn(0);

        boolean verified =
                phoneVerificationStore
                        .markVerifiedIfPending(
                                10L,
                                1L,
                                "verification-token-hash",
                                NOW,
                                tokenExpiresAt,
                                MAX_ATTEMPTS
                        );

        assertFalse(verified);
    }

    private PhoneVerification createPhoneVerification() {
        return PhoneVerification.issue(
                1L,
                new byte[]{1, 2, 3},
                "phone-number-hash",
                "verification-code-hash",
                NOW.plusMinutes(3),
                NOW
        );
    }
}