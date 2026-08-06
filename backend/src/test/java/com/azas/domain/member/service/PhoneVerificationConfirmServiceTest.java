package com.azas.domain.member.service;

import com.azas.domain.member.dto.PhoneVerificationConfirmResult;
import com.azas.domain.member.entity.PhoneVerification;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PhoneVerificationConfirmServiceTest {

    private static final long MEMBER_ID = 1L;
    private static final long VERIFICATION_ID = 10L;
    private static final String VERIFICATION_CODE =
            "482193";
    private static final String PHONE_NUMBER_HASH =
            "phone-number-hash";
    private static final String RAW_TOKEN =
            "raw-verification-token";
    private static final String TOKEN_HASH =
            "verification-token-hash";

    @Mock
    private PhoneVerificationStore
            phoneVerificationStore;

    @Mock
    private PhoneVerificationHasher
            phoneVerificationHasher;

    @Mock
    private PhoneNumberProtector
            phoneNumberProtector;

    @Mock
    private PhoneVerificationTokenGenerator
            phoneVerificationTokenGenerator;

    private PhoneVerificationConfirmService service;

    @BeforeEach
    void setUp() {
        service = new PhoneVerificationConfirmService(
                phoneVerificationStore,
                phoneVerificationHasher,
                phoneNumberProtector,
                phoneVerificationTokenGenerator
        );
    }

    @Test
    void confirmsCodeAndIssuesVerificationToken() {
        PhoneVerification phoneVerification =
                pendingVerification(0);

        when(
                phoneVerificationStore
                        .findByIdAndMemberId(
                                VERIFICATION_ID,
                                MEMBER_ID
                        )
        ).thenReturn(
                Optional.of(phoneVerification)
        );

        when(
                phoneVerificationHasher
                        .matchesVerificationCode(
                                "verification-code-hash",
                                MEMBER_ID,
                                PHONE_NUMBER_HASH,
                                VERIFICATION_CODE
                        )
        ).thenReturn(true);

        when(
                phoneNumberProtector.decrypt(
                        phoneVerification
                                .getPhoneNumberCiphertext()
                )
        ).thenReturn("01012345678");

        when(
                phoneVerificationTokenGenerator.generate()
        ).thenReturn(RAW_TOKEN);

        when(
                phoneVerificationHasher
                        .hashVerificationToken(RAW_TOKEN)
        ).thenReturn(TOKEN_HASH);

        when(
                phoneVerificationStore
                        .markVerifiedIfPending(
                                eq(VERIFICATION_ID),
                                eq(MEMBER_ID),
                                eq(TOKEN_HASH),
                                any(LocalDateTime.class),
                                any(LocalDateTime.class),
                                eq(5)
                        )
        ).thenReturn(true);

        PhoneVerificationConfirmResult result =
                service.confirm(
                        MEMBER_ID,
                        VERIFICATION_ID,
                        VERIFICATION_CODE
                );

        assertEquals(
                VERIFICATION_ID,
                result.getVerificationId()
        );

        assertEquals(
                "010-****-5678",
                result.getMaskedPhoneNumber()
        );

        assertEquals(
                RAW_TOKEN,
                result.getVerificationToken()
        );

        assertEquals(
                result.getVerifiedAt().plusMinutes(10),
                result.getTokenExpiresAt()
        );
    }

    @Test
    void increasesAttemptCountForInvalidCode() {
        PhoneVerification phoneVerification =
                pendingVerification(0);

        when(
                phoneVerificationStore
                        .findByIdAndMemberId(
                                VERIFICATION_ID,
                                MEMBER_ID
                        )
        ).thenReturn(
                Optional.of(phoneVerification)
        );

        when(
                phoneVerificationHasher
                        .matchesVerificationCode(
                                "verification-code-hash",
                                MEMBER_ID,
                                PHONE_NUMBER_HASH,
                                "000000"
                        )
        ).thenReturn(false);

        when(
                phoneVerificationStore
                        .increaseAttemptCountIfPending(
                                eq(VERIFICATION_ID),
                                eq(MEMBER_ID),
                                any(LocalDateTime.class),
                                eq(5)
                        )
        ).thenReturn(true);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.confirm(
                                MEMBER_ID,
                                VERIFICATION_ID,
                                "000000"
                        )
                );

        assertEquals(
                ErrorCode.INVALID_PHONE_VERIFICATION_CODE,
                exception.getErrorCode()
        );

        verify(phoneVerificationStore)
                .increaseAttemptCountIfPending(
                        eq(VERIFICATION_ID),
                        eq(MEMBER_ID),
                        any(LocalDateTime.class),
                        eq(5)
                );
    }

    @Test
    void rejectsFifthInvalidAttempt() {
        PhoneVerification phoneVerification =
                pendingVerification(4);

        when(
                phoneVerificationStore
                        .findByIdAndMemberId(
                                VERIFICATION_ID,
                                MEMBER_ID
                        )
        ).thenReturn(
                Optional.of(phoneVerification)
        );

        when(
                phoneVerificationHasher
                        .matchesVerificationCode(
                                "verification-code-hash",
                                MEMBER_ID,
                                PHONE_NUMBER_HASH,
                                "000000"
                        )
        ).thenReturn(false);

        when(
                phoneVerificationStore
                        .increaseAttemptCountIfPending(
                                eq(VERIFICATION_ID),
                                eq(MEMBER_ID),
                                any(LocalDateTime.class),
                                eq(5)
                        )
        ).thenReturn(true);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.confirm(
                                MEMBER_ID,
                                VERIFICATION_ID,
                                "000000"
                        )
                );

        assertEquals(
                ErrorCode
                        .PHONE_VERIFICATION_ATTEMPT_LIMIT_EXCEEDED,
                exception.getErrorCode()
        );
    }

    @Test
    void rejectsMissingVerification() {
        when(
                phoneVerificationStore
                        .findByIdAndMemberId(
                                VERIFICATION_ID,
                                MEMBER_ID
                        )
        ).thenReturn(Optional.empty());

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.confirm(
                                MEMBER_ID,
                                VERIFICATION_ID,
                                VERIFICATION_CODE
                        )
                );

        assertEquals(
                ErrorCode.PHONE_VERIFICATION_NOT_AVAILABLE,
                exception.getErrorCode()
        );
    }

    @Test
    void rejectsExpiredVerification() {
        PhoneVerification phoneVerification =
                pendingVerification(0);

        ReflectionTestUtils.setField(
                phoneVerification,
                "expiresAt",
                LocalDateTime.now(ZoneOffset.UTC)
                        .minusSeconds(1)
        );

        when(
                phoneVerificationStore
                        .findByIdAndMemberId(
                                VERIFICATION_ID,
                                MEMBER_ID
                        )
        ).thenReturn(
                Optional.of(phoneVerification)
        );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.confirm(
                                MEMBER_ID,
                                VERIFICATION_ID,
                                VERIFICATION_CODE
                        )
                );

        assertEquals(
                ErrorCode.PHONE_VERIFICATION_NOT_AVAILABLE,
                exception.getErrorCode()
        );
    }

    @Test
    void rejectsAlreadyVerifiedRequest() {
        PhoneVerification phoneVerification =
                pendingVerification(0);

        ReflectionTestUtils.setField(
                phoneVerification,
                "verifiedAt",
                LocalDateTime.now(ZoneOffset.UTC)
        );

        when(
                phoneVerificationStore
                        .findByIdAndMemberId(
                                VERIFICATION_ID,
                                MEMBER_ID
                        )
        ).thenReturn(
                Optional.of(phoneVerification)
        );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.confirm(
                                MEMBER_ID,
                                VERIFICATION_ID,
                                VERIFICATION_CODE
                        )
                );

        assertEquals(
                ErrorCode.PHONE_VERIFICATION_NOT_AVAILABLE,
                exception.getErrorCode()
        );
    }

    @Test
    void rejectsRequestThatAlreadyReachedAttemptLimit() {
        PhoneVerification phoneVerification =
                pendingVerification(5);

        when(
                phoneVerificationStore
                        .findByIdAndMemberId(
                                VERIFICATION_ID,
                                MEMBER_ID
                        )
        ).thenReturn(
                Optional.of(phoneVerification)
        );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.confirm(
                                MEMBER_ID,
                                VERIFICATION_ID,
                                VERIFICATION_CODE
                        )
                );

        assertEquals(
                ErrorCode
                        .PHONE_VERIFICATION_ATTEMPT_LIMIT_EXCEEDED,
                exception.getErrorCode()
        );
    }

    @Test
    void rejectsWhenConcurrentUpdateWins() {
        PhoneVerification phoneVerification =
                pendingVerification(0);

        when(
                phoneVerificationStore
                        .findByIdAndMemberId(
                                VERIFICATION_ID,
                                MEMBER_ID
                        )
        ).thenReturn(
                Optional.of(phoneVerification)
        );

        when(
                phoneVerificationHasher
                        .matchesVerificationCode(
                                "verification-code-hash",
                                MEMBER_ID,
                                PHONE_NUMBER_HASH,
                                VERIFICATION_CODE
                        )
        ).thenReturn(true);

        when(
                phoneNumberProtector.decrypt(
                        phoneVerification
                                .getPhoneNumberCiphertext()
                )
        ).thenReturn("01012345678");

        when(
                phoneVerificationTokenGenerator.generate()
        ).thenReturn(RAW_TOKEN);

        when(
                phoneVerificationHasher
                        .hashVerificationToken(RAW_TOKEN)
        ).thenReturn(TOKEN_HASH);

        when(
                phoneVerificationStore
                        .markVerifiedIfPending(
                                eq(VERIFICATION_ID),
                                eq(MEMBER_ID),
                                eq(TOKEN_HASH),
                                any(LocalDateTime.class),
                                any(LocalDateTime.class),
                                eq(5)
                        )
        ).thenReturn(false);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.confirm(
                                MEMBER_ID,
                                VERIFICATION_ID,
                                VERIFICATION_CODE
                        )
                );

        assertEquals(
                ErrorCode.PHONE_VERIFICATION_NOT_AVAILABLE,
                exception.getErrorCode()
        );
    }

    private PhoneVerification pendingVerification(
            int attemptCount
    ) {
        LocalDateTime now =
                LocalDateTime.now(ZoneOffset.UTC);

        PhoneVerification phoneVerification =
                PhoneVerification.issue(
                        MEMBER_ID,
                        new byte[]{1, 2, 3},
                        PHONE_NUMBER_HASH,
                        "verification-code-hash",
                        now.plusMinutes(3),
                        now
                );

        ReflectionTestUtils.setField(
                phoneVerification,
                "phoneVerificationId",
                VERIFICATION_ID
        );

        ReflectionTestUtils.setField(
                phoneVerification,
                "attemptCount",
                attemptCount
        );

        return phoneVerification;
    }
}