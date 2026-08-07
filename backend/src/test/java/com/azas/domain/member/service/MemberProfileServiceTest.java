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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberProfileServiceTest {

    @Mock
    private MemberMapper memberMapper;

    @Mock
    private SocialAccountMapper socialAccountMapper;

    private MemberProfileService memberProfileService;

    @BeforeEach
    void setUp() {
        memberProfileService = new MemberProfileService(
                memberMapper,
                socialAccountMapper
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
}