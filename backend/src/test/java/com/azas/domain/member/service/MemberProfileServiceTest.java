package com.azas.domain.member.service;

import com.azas.domain.auth.entity.OAuthProvider;
import com.azas.domain.auth.entity.SocialAccount;
import com.azas.domain.auth.mapper.SocialAccountMapper;
import com.azas.domain.member.dto.MemberProfileResult;
import com.azas.domain.member.entity.Member;
import com.azas.domain.member.entity.MemberStatus;
import com.azas.domain.member.mapper.MemberMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberProfileServiceTest {

    @Mock
    private MemberMapper memberMapper;

    @Mock
    private SocialAccountMapper socialAccountMapper;

    @Mock
    private PhoneNumberProtector phoneNumberProtector;

    private MemberProfileService memberProfileService;

    @BeforeEach
    void setUp() {
        memberProfileService = new MemberProfileService(
                memberMapper,
                socialAccountMapper,
                phoneNumberProtector
        );
    }

    @Test
    void returnsMemberAndConnectedSocialAccounts() {
        Member member = activeMember();

        SocialAccount googleAccount =
                SocialAccount.create(
                        1L,
                        OAuthProvider.GOOGLE,
                        "google-subject"
                );

        SocialAccount kakaoAccount =
                SocialAccount.create(
                        1L,
                        OAuthProvider.KAKAO,
                        "kakao-subject"
                );

        List<SocialAccount> socialAccounts =
                List.of(
                        googleAccount,
                        kakaoAccount
                );

        when(memberMapper.findById(1L))
                .thenReturn(member);
        when(socialAccountMapper.findAllByMemberId(1L))
                .thenReturn(socialAccounts);

        MemberProfileResult result =
                memberProfileService.getMyProfile(1L);

        assertSame(member, result.getMember());
        assertEquals(
                socialAccounts,
                result.getSocialAccounts()
        );
        assertNull(result.getMaskedPhoneNumber());

        verify(memberMapper).findById(1L);
        verify(socialAccountMapper)
                .findAllByMemberId(1L);
    }

    @Test
    void rejectsAccessTokenForMissingMember() {
        when(memberMapper.findById(1L))
                .thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> memberProfileService.getMyProfile(1L)
        );

        assertEquals(
                ErrorCode.INVALID_ACCESS_TOKEN,
                exception.getErrorCode()
        );
        verifyNoInteractions(socialAccountMapper);
    }

    @Test
    void rejectsWithdrawnMember() {
        Member member = activeMember();

        ReflectionTestUtils.setField(
                member,
                "status",
                MemberStatus.WITHDRAWN
        );

        when(memberMapper.findById(1L))
                .thenReturn(member);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> memberProfileService.getMyProfile(1L)
        );

        assertEquals(
                ErrorCode.WITHDRAWN_MEMBER,
                exception.getErrorCode()
        );
        verifyNoInteractions(socialAccountMapper);
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
                1L
        );

        return member;
    }

    @Test
    void returnsMaskedVerifiedPhoneNumber() {
        Member member = activeMember();

        byte[] ciphertext = new byte[]{1, 2, 3};

        member.applyVerifiedPhoneNumber(
                ciphertext,
                "phone-number-hash",
                java.time.LocalDateTime.of(
                        2026,
                        8,
                        7,
                        1,
                        0
                )
        );

        when(memberMapper.findById(1L))
                .thenReturn(member);
        when(socialAccountMapper.findAllByMemberId(1L))
                .thenReturn(List.of());
        when(
                phoneNumberProtector.decrypt(
                        org.mockito.ArgumentMatchers.any(
                                byte[].class
                        )
                )
        ).thenReturn("01012345678");

        MemberProfileResult result =
                memberProfileService.getMyProfile(1L);

        assertEquals(
                "010-****-5678",
                result.getMaskedPhoneNumber()
        );

        verify(phoneNumberProtector)
                .decrypt(
                        org.mockito.ArgumentMatchers.any(
                                byte[].class
                        )
                );
    }
}