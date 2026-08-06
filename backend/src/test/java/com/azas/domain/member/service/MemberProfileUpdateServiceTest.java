package com.azas.domain.member.service;

import com.azas.domain.member.dto.MemberProfileUpdateCommand;
import com.azas.domain.member.entity.Member;
import com.azas.domain.member.entity.MemberStatus;
import com.azas.domain.member.entity.PhoneVerification;
import com.azas.domain.member.mapper.MemberMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberProfileUpdateServiceTest {

    private static final long MEMBER_ID = 1L;

    @Mock
    private MemberMapper memberMapper;

    @Mock
    private PhoneVerificationStore
            phoneVerificationStore;

    @Mock
    private PhoneVerificationHasher
            phoneVerificationHasher;

    @InjectMocks
    private MemberProfileUpdateService
            memberProfileUpdateService;

    @Test
    void updatesBirthDateAndProfileImageUrl() {
        Member member = activeMember();

        when(memberMapper.findById(MEMBER_ID))
                .thenReturn(member);
        when(memberMapper.updateProfile(member))
                .thenReturn(1);

        MemberProfileUpdateCommand command =
                new MemberProfileUpdateCommand(
                        true,
                        LocalDate.of(1992, 4, 15),
                        true,
                        "https://example.com/profile.png",
                        false,
                        null
                );

        memberProfileUpdateService.updateMyProfile(
                MEMBER_ID,
                command
        );

        assertEquals(
                LocalDate.of(1992, 4, 15),
                member.getBirthDate()
        );
        assertEquals(
                "https://example.com/profile.png",
                member.getProfileImageUrl()
        );

        verify(memberMapper).updateProfile(member);
        verifyNoInteractions(
                phoneVerificationStore,
                phoneVerificationHasher
        );
    }

    @Test
    void clearsNullableProfileFields() {
        Member member = activeMember();

        ReflectionTestUtils.setField(
                member,
                "birthDate",
                LocalDate.of(1992, 4, 15)
        );
        ReflectionTestUtils.setField(
                member,
                "profileImageUrl",
                "https://example.com/profile.png"
        );

        when(memberMapper.findById(MEMBER_ID))
                .thenReturn(member);
        when(memberMapper.updateProfile(member))
                .thenReturn(1);

        MemberProfileUpdateCommand command =
                new MemberProfileUpdateCommand(
                        true,
                        null,
                        true,
                        null,
                        false,
                        null
                );

        memberProfileUpdateService.updateMyProfile(
                MEMBER_ID,
                command
        );

        assertEquals(null, member.getBirthDate());
        assertEquals(null, member.getProfileImageUrl());
    }

    @Test
    void appliesPhoneNumberFromUsableVerificationToken() {
        Member member = activeMember();

        PhoneVerification phoneVerification =
                usablePhoneVerification(MEMBER_ID);

        when(memberMapper.findById(MEMBER_ID))
                .thenReturn(member);
        when(
                phoneVerificationHasher
                        .hashVerificationToken(
                                "raw-verification-token"
                        )
        ).thenReturn("verification-token-hash");
        when(
                phoneVerificationStore
                        .findByVerificationTokenHash(
                                "verification-token-hash"
                        )
        ).thenReturn(Optional.of(phoneVerification));
        when(
                phoneVerificationStore
                        .consumeVerificationTokenIfUsable(
                                eq(10L),
                                eq(MEMBER_ID),
                                eq("verification-token-hash"),
                                any(LocalDateTime.class)
                        )
        ).thenReturn(true);
        when(memberMapper.updateProfile(member))
                .thenReturn(1);

        MemberProfileUpdateCommand command =
                new MemberProfileUpdateCommand(
                        false,
                        null,
                        false,
                        null,
                        true,
                        "raw-verification-token"
                );

        memberProfileUpdateService.updateMyProfile(
                MEMBER_ID,
                command
        );

        assertArrayEquals(
                new byte[]{1, 2, 3},
                member.getPhoneNumberCiphertext()
        );
        assertEquals(
                "phone-number-hash",
                member.getPhoneNumberHash()
        );
        assertNotNull(member.getPhoneVerifiedAt());

        verify(phoneVerificationStore)
                .consumeVerificationTokenIfUsable(
                        10L,
                        MEMBER_ID,
                        "verification-token-hash",
                        member.getPhoneVerifiedAt()
                );
        verify(memberMapper).updateProfile(member);
    }

    @Test
    void rejectsEmptyUpdateCommand() {
        MemberProfileUpdateCommand command =
                new MemberProfileUpdateCommand(
                        false,
                        null,
                        false,
                        null,
                        false,
                        null
                );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> memberProfileUpdateService
                        .updateMyProfile(
                                MEMBER_ID,
                                command
                        )
        );

        assertEquals(
                ErrorCode.BADREQUEST,
                exception.getErrorCode()
        );
        verifyNoInteractions(
                memberMapper,
                phoneVerificationStore,
                phoneVerificationHasher
        );
    }

    @Test
    void rejectsFutureBirthDate() {
        MemberProfileUpdateCommand command =
                new MemberProfileUpdateCommand(
                        true,
                        LocalDate.now(ZoneOffset.UTC)
                                .plusDays(1),
                        false,
                        null,
                        false,
                        null
                );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> memberProfileUpdateService
                        .updateMyProfile(
                                MEMBER_ID,
                                command
                        )
        );

        assertEquals(
                ErrorCode.BADREQUEST,
                exception.getErrorCode()
        );
        verifyNoInteractions(memberMapper);
    }

    @Test
    void rejectsInvalidProfileImageUrl() {
        Member member = activeMember();

        when(memberMapper.findById(MEMBER_ID))
                .thenReturn(member);

        MemberProfileUpdateCommand command =
                new MemberProfileUpdateCommand(
                        false,
                        null,
                        true,
                        "not-a-url",
                        false,
                        null
                );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> memberProfileUpdateService
                        .updateMyProfile(
                                MEMBER_ID,
                                command
                        )
        );

        assertEquals(
                ErrorCode.BADREQUEST,
                exception.getErrorCode()
        );
        verify(memberMapper, never())
                .updateProfile(any(Member.class));
    }

    @Test
    void rejectsVerificationTokenOwnedByOtherMember() {
        Member member = activeMember();

        PhoneVerification phoneVerification =
                usablePhoneVerification(2L);

        when(memberMapper.findById(MEMBER_ID))
                .thenReturn(member);
        when(
                phoneVerificationHasher
                        .hashVerificationToken("raw-token")
        ).thenReturn("token-hash");
        when(
                phoneVerificationStore
                        .findByVerificationTokenHash(
                                "token-hash"
                        )
        ).thenReturn(Optional.of(phoneVerification));

        MemberProfileUpdateCommand command =
                phoneTokenCommand("raw-token");

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> memberProfileUpdateService
                        .updateMyProfile(
                                MEMBER_ID,
                                command
                        )
        );

        assertEquals(
                ErrorCode.INVALID_PHONE_VERIFICATION_TOKEN,
                exception.getErrorCode()
        );
        verify(phoneVerificationStore, never())
                .consumeVerificationTokenIfUsable(
                        any(Long.class),
                        any(Long.class),
                        any(String.class),
                        any(LocalDateTime.class)
                );
    }

    @Test
    void rejectsTokenConsumedByConcurrentRequest() {
        Member member = activeMember();

        PhoneVerification phoneVerification =
                usablePhoneVerification(MEMBER_ID);

        when(memberMapper.findById(MEMBER_ID))
                .thenReturn(member);
        when(
                phoneVerificationHasher
                        .hashVerificationToken("raw-token")
        ).thenReturn("token-hash");
        when(
                phoneVerificationStore
                        .findByVerificationTokenHash(
                                "token-hash"
                        )
        ).thenReturn(Optional.of(phoneVerification));
        when(
                phoneVerificationStore
                        .consumeVerificationTokenIfUsable(
                                eq(10L),
                                eq(MEMBER_ID),
                                eq("token-hash"),
                                any(LocalDateTime.class)
                        )
        ).thenReturn(false);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> memberProfileUpdateService
                        .updateMyProfile(
                                MEMBER_ID,
                                phoneTokenCommand("raw-token")
                        )
        );

        assertEquals(
                ErrorCode.INVALID_PHONE_VERIFICATION_TOKEN,
                exception.getErrorCode()
        );
        verify(memberMapper, never())
                .updateProfile(any(Member.class));
    }

    @Test
    void convertsDuplicatePhoneNumberToBusinessError() {
        Member member = activeMember();

        when(memberMapper.findById(MEMBER_ID))
                .thenReturn(member);
        when(memberMapper.updateProfile(member))
                .thenThrow(
                        new DuplicateKeyException(
                                "duplicate phone hash"
                        )
                );

        MemberProfileUpdateCommand command =
                new MemberProfileUpdateCommand(
                        true,
                        LocalDate.of(1992, 4, 15),
                        false,
                        null,
                        false,
                        null
                );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> memberProfileUpdateService
                        .updateMyProfile(
                                MEMBER_ID,
                                command
                        )
        );

        assertEquals(
                ErrorCode.PHONE_NUMBER_ALREADY_IN_USE,
                exception.getErrorCode()
        );
    }

    @Test
    void rejectsWithdrawnMember() {
        Member member = activeMember();

        ReflectionTestUtils.setField(
                member,
                "status",
                MemberStatus.WITHDRAWN
        );

        when(memberMapper.findById(MEMBER_ID))
                .thenReturn(member);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> memberProfileUpdateService
                        .updateMyProfile(
                                MEMBER_ID,
                                birthDateCommand()
                        )
        );

        assertEquals(
                ErrorCode.WITHDRAWN_MEMBER,
                exception.getErrorCode()
        );
        verify(memberMapper, never())
                .updateProfile(any(Member.class));
    }

    private Member activeMember() {
        Member member = Member.createParent(
                "parent@example.com",
                "김하나",
                null
        );

        ReflectionTestUtils.setField(
                member,
                "memberId",
                MEMBER_ID
        );

        return member;
    }

    private PhoneVerification usablePhoneVerification(
            long ownerMemberId
    ) {
        LocalDateTime now =
                LocalDateTime.now(ZoneOffset.UTC);

        PhoneVerification phoneVerification =
                PhoneVerification.issue(
                        ownerMemberId,
                        new byte[]{1, 2, 3},
                        "phone-number-hash",
                        "verification-code-hash",
                        now.plusMinutes(3),
                        now.minusMinutes(1)
                );

        ReflectionTestUtils.setField(
                phoneVerification,
                "phoneVerificationId",
                10L
        );
        ReflectionTestUtils.setField(
                phoneVerification,
                "verifiedAt",
                now.minusSeconds(30)
        );
        ReflectionTestUtils.setField(
                phoneVerification,
                "verificationTokenHash",
                "verification-token-hash"
        );
        ReflectionTestUtils.setField(
                phoneVerification,
                "tokenExpiresAt",
                now.plusMinutes(10)
        );

        return phoneVerification;
    }

    private MemberProfileUpdateCommand
    phoneTokenCommand(String token) {
        return new MemberProfileUpdateCommand(
                false,
                null,
                false,
                null,
                true,
                token
        );
    }

    private MemberProfileUpdateCommand
    birthDateCommand() {
        return new MemberProfileUpdateCommand(
                true,
                LocalDate.of(1992, 4, 15),
                false,
                null,
                false,
                null
        );
    }
}