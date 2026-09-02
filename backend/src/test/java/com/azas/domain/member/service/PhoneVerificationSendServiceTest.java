package com.azas.domain.member.service;

import com.azas.domain.member.dto.PhoneVerificationSendResult;
import com.azas.domain.member.entity.PhoneVerification;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PhoneVerificationSendServiceTest {

    private static final long MEMBER_ID = 1L;
    private static final String RAW_PHONE_NUMBER =
            "010-1234-5678";
    private static final String NORMALIZED_PHONE_NUMBER =
            "01012345678";
    private static final String PHONE_NUMBER_HASH =
            "phone-number-hash";
    private static final String VERIFICATION_CODE =
            "482193";
    private static final String VERIFICATION_CODE_HASH =
            "verification-code-hash";

    @Mock
    private PhoneVerificationStore
            phoneVerificationStore;

    @Mock
    private PhoneNumberNormalizer
            phoneNumberNormalizer;

    @Mock
    private PhoneNumberProtector
            phoneNumberProtector;

    @Mock
    private PhoneVerificationHasher
            phoneVerificationHasher;

    @Mock
    private VerificationCodeGenerator
            verificationCodeGenerator;

    @Mock
    private SmsSender smsSender;

    @Mock
    private Environment environment;

    private PhoneVerificationSendService service;

    @BeforeEach
    void setUp() {
        service = new PhoneVerificationSendService(
                phoneVerificationStore,
                phoneNumberNormalizer,
                phoneNumberProtector,
                phoneVerificationHasher,
                verificationCodeGenerator,
                smsSender,
                environment
        );
    }

    @Test
    void usesConfiguredCodeForDemoPhoneNumberInDemoProfile() {
        configureDemoVerification(
                NORMALIZED_PHONE_NUMBER,
                "000000"
        );

        when(
                environment.acceptsProfiles(
                        any(Profiles.class)
                )
        ).thenReturn(true);

        assertEquals(
                "000000",
                service.resolveVerificationCode(
                        NORMALIZED_PHONE_NUMBER
                )
        );

        verify(verificationCodeGenerator, never())
                .generate();
    }

    @Test
    void generatesRandomCodeForOtherPhoneNumberInDemoProfile() {
        configureDemoVerification(
                "01099998888",
                "000000"
        );

        when(
                environment.acceptsProfiles(
                        any(Profiles.class)
                )
        ).thenReturn(true);

        when(
                verificationCodeGenerator.generate()
        ).thenReturn(VERIFICATION_CODE);

        assertEquals(
                VERIFICATION_CODE,
                service.resolveVerificationCode(
                        NORMALIZED_PHONE_NUMBER
                )
        );
    }

    @Test
    void generatesRandomCodeOutsideDemoProfile() {
        configureDemoVerification(
                NORMALIZED_PHONE_NUMBER,
                "000000"
        );

        when(
                environment.acceptsProfiles(
                        any(Profiles.class)
                )
        ).thenReturn(false);

        when(
                verificationCodeGenerator.generate()
        ).thenReturn(VERIFICATION_CODE);

        assertEquals(
                VERIFICATION_CODE,
                service.resolveVerificationCode(
                        NORMALIZED_PHONE_NUMBER
                )
        );
    }

    @Test
    void sendsVerificationCodeAndStoresRequest() {
        byte[] ciphertext = {1, 2, 3};

        prepareSuccessfulDependencies(ciphertext);

        PhoneVerificationSendResult result =
                service.send(
                        MEMBER_ID,
                        RAW_PHONE_NUMBER
                );

        ArgumentCaptor<PhoneVerification> captor =
                ArgumentCaptor.forClass(
                        PhoneVerification.class
                );

        verify(phoneVerificationStore)
                .save(captor.capture());

        PhoneVerification savedVerification =
                captor.getValue();

        assertEquals(
                10L,
                result.getVerificationId()
        );

        assertEquals(
                MEMBER_ID,
                savedVerification.getMemberId()
        );

        assertArrayEquals(
                ciphertext,
                savedVerification
                        .getPhoneNumberCiphertext()
        );

        assertEquals(
                PHONE_NUMBER_HASH,
                savedVerification.getPhoneNumberHash()
        );

        assertEquals(
                VERIFICATION_CODE_HASH,
                savedVerification
                        .getVerificationCodeHash()
        );

        assertEquals(
                savedVerification.getExpiresAt(),
                result.getExpiresAt()
        );

        assertEquals(
                savedVerification.getCreatedAt(),
                result.getResendAvailableAt()
        );

        verify(phoneVerificationStore)
                .expireUnverifiedByMemberId(
                        anyLong(),
                        any(LocalDateTime.class)
                );

        verify(smsSender)
                .sendVerificationCode(
                        NORMALIZED_PHONE_NUMBER,
                        VERIFICATION_CODE
                );
    }

    @Test
    void rejectsInvalidPhoneNumber() {
        when(
                phoneNumberNormalizer.normalize(
                        RAW_PHONE_NUMBER
                )
        ).thenThrow(
                new IllegalArgumentException()
        );

        BusinessException exception =
                org.junit.jupiter.api.Assertions
                        .assertThrows(
                                BusinessException.class,
                                () -> service.send(
                                        MEMBER_ID,
                                        RAW_PHONE_NUMBER
                                )
                        );

        assertEquals(
                ErrorCode.INVALID_PHONE_NUMBER,
                exception.getErrorCode()
        );
    }

    @Test
    void allowsImmediateResendWithoutCheckingPreviousRequests() {
        prepareSuccessfulDependencies(
                new byte[]{1, 2, 3}
        );

        PhoneVerificationSendResult result =
                service.send(
                        MEMBER_ID,
                        RAW_PHONE_NUMBER
                );

        assertEquals(10L, result.getVerificationId());
        verify(phoneVerificationStore, never())
                .findLatestByMemberId(MEMBER_ID);
        verify(phoneVerificationStore, never())
                .findLatestByPhoneNumberHash(
                        PHONE_NUMBER_HASH
                );
    }

    @Test
    void convertsSmsFailureToBusinessException() {
        prepareSuccessfulDependencies(
                new byte[]{1, 2, 3}
        );

        doThrow(new IllegalStateException())
                .when(smsSender)
                .sendVerificationCode(
                        NORMALIZED_PHONE_NUMBER,
                        VERIFICATION_CODE
                );

        BusinessException exception =
                org.junit.jupiter.api.Assertions
                        .assertThrows(
                                BusinessException.class,
                                () -> service.send(
                                        MEMBER_ID,
                                        RAW_PHONE_NUMBER
                                )
                        );

        assertEquals(
                ErrorCode.SMS_DELIVERY_FAILED,
                exception.getErrorCode()
        );
    }

    private void prepareSuccessfulDependencies(
            byte[] ciphertext
    ) {
        when(
                phoneNumberNormalizer.normalize(
                        RAW_PHONE_NUMBER
                )
        ).thenReturn(NORMALIZED_PHONE_NUMBER);

        when(
                phoneVerificationHasher
                        .hashPhoneNumber(
                                NORMALIZED_PHONE_NUMBER
                        )
        ).thenReturn(PHONE_NUMBER_HASH);

        when(
                verificationCodeGenerator.generate()
        ).thenReturn(VERIFICATION_CODE);

        when(
                phoneNumberProtector.encrypt(
                        NORMALIZED_PHONE_NUMBER
                )
        ).thenReturn(ciphertext);

        when(
                phoneVerificationHasher
                        .hashVerificationCode(
                                MEMBER_ID,
                                PHONE_NUMBER_HASH,
                                VERIFICATION_CODE
                        )
        ).thenReturn(VERIFICATION_CODE_HASH);

        doAnswer(invocation -> {
            PhoneVerification phoneVerification =
                    invocation.getArgument(0);

            ReflectionTestUtils.setField(
                    phoneVerification,
                    "phoneVerificationId",
                    10L
            );

            return null;
        }).when(phoneVerificationStore)
                .save(any(PhoneVerification.class));
    }

    private void configureDemoVerification(
            String phoneNumber,
            String code
    ) {
        ReflectionTestUtils.setField(
                service,
                "demoPhoneNumber",
                phoneNumber
        );

        ReflectionTestUtils.setField(
                service,
                "demoCode",
                code
        );
    }
}
