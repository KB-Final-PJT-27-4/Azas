package com.azas.domain.auth.service;

import com.azas.domain.auth.dto.OAuthMemberResult;
import com.azas.domain.auth.dto.OAuthProfile;
import com.azas.domain.auth.entity.OAuthProvider;
import com.azas.domain.auth.entity.SocialAccount;
import com.azas.domain.auth.mapper.SocialAccountMapper;
import com.azas.domain.member.entity.Member;
import com.azas.domain.member.entity.MemberStatus;
import com.azas.domain.member.entity.MemberType;
import com.azas.domain.member.mapper.MemberMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OAuthMemberServiceTest {

    @Mock
    private MemberMapper memberMapper;

    @Mock
    private SocialAccountMapper socialAccountMapper;

    @InjectMocks
    private OAuthMemberService oauthMemberService;

    @Test
    void returnsMemberConnectedToExistingSocialAccount() {
        OAuthProfile profile = new OAuthProfile(
                OAuthProvider.GOOGLE,
                "google-user-id",
                "parent@example.com",
                "김하나",
                null
        );

        SocialAccount socialAccount =
                SocialAccount.create(
                        1L,
                        OAuthProvider.GOOGLE,
                        "google-user-id"
                );

        Member member = Member.createParent(
                "parent@example.com",
                "김하나",
                null
        );

        when(
                socialAccountMapper.findByProviderAndSubject(
                        OAuthProvider.GOOGLE,
                        "google-user-id"
                )
        ).thenReturn(socialAccount);

        when(memberMapper.findById(1L))
                .thenReturn(member);

        OAuthMemberResult result =
                oauthMemberService.findOrCreate(profile);

        assertSame(member, result.getMember());
        assertFalse(result.isNewMember());

        verify(memberMapper, never())
                .insert(any(Member.class));
        verify(socialAccountMapper, never())
                .insert(any(SocialAccount.class));
    }

    @Test
    void linksNewSocialAccountToMemberWithSameEmail() {
        OAuthProfile profile = new OAuthProfile(
                OAuthProvider.KAKAO,
                "kakao-user-id",
                "parent@example.com",
                "김하나",
                null
        );

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

        when(
                socialAccountMapper.findByProviderAndSubject(
                        OAuthProvider.KAKAO,
                        "kakao-user-id"
                )
        ).thenReturn(null);

        when(
                memberMapper.findByEmail(
                        "parent@example.com"
                )
        ).thenReturn(member);

        OAuthMemberResult result =
                oauthMemberService.findOrCreate(profile);

        assertSame(member, result.getMember());
        assertFalse(result.isNewMember());

        verify(memberMapper, never())
                .insert(any(Member.class));

        ArgumentCaptor<SocialAccount> socialAccountCaptor =
                ArgumentCaptor.forClass(SocialAccount.class);

        verify(socialAccountMapper)
                .insert(socialAccountCaptor.capture());

        SocialAccount linkedSocialAccount =
                socialAccountCaptor.getValue();

        assertEquals(
                1L,
                linkedSocialAccount.getMemberId()
        );
        assertEquals(
                OAuthProvider.KAKAO,
                linkedSocialAccount.getProvider()
        );
        assertEquals(
                "kakao-user-id",
                linkedSocialAccount.getProviderSubject()
        );
    }

    @Test
    void createsParentMemberAndSocialAccountForNewUser() {
        OAuthProfile profile = new OAuthProfile(
                OAuthProvider.GOOGLE,
                "new-google-user-id",
                "new-parent@example.com",
                "김신규",
                "https://example.com/profile.jpg"
        );

        when(
                socialAccountMapper.findByProviderAndSubject(
                        OAuthProvider.GOOGLE,
                        "new-google-user-id"
                )
        ).thenReturn(null);

        when(
                memberMapper.findByEmail(
                        "new-parent@example.com"
                )
        ).thenReturn(null);

        doAnswer(invocation -> {
            Member insertedMember =
                    invocation.getArgument(0);

            // 실제 환경에서는 MyBatis가 INSERT 후 생성된 PK를 객체에 넣는다.
            ReflectionTestUtils.setField(
                    insertedMember,
                    "memberId",
                    10L
            );

            return 1;
        }).when(memberMapper)
                .insert(any(Member.class));

        Member savedMember = Member.createParent(
                "new-parent@example.com",
                "김신규",
                "https://example.com/profile.jpg"
        );

        ReflectionTestUtils.setField(
                savedMember,
                "memberId",
                10L
        );
        ReflectionTestUtils.setField(
                savedMember,
                "createdAt",
                LocalDateTime.of(2026, 8, 2, 6, 0)
        );

        when(memberMapper.findById(10L))
                .thenReturn(savedMember);

        OAuthMemberResult result =
                oauthMemberService.findOrCreate(profile);

        assertTrue(result.isNewMember());
        assertSame(savedMember, result.getMember());

        assertEquals(
                10L,
                result.getMember().getMemberId()
        );
        assertEquals(
                "new-parent@example.com",
                result.getMember().getEmail()
        );
        assertEquals(
                MemberType.PARENT,
                result.getMember().getMemberType()
        );
        assertEquals(
                MemberStatus.ACTIVE,
                result.getMember().getStatus()
        );
        assertEquals(
                LocalDateTime.of(2026, 8, 2, 6, 0),
                result.getMember().getCreatedAt()
        );

        verify(memberMapper)
                .insert(any(Member.class));

        verify(memberMapper)
                .findById(10L);

        ArgumentCaptor<SocialAccount> socialAccountCaptor =
                ArgumentCaptor.forClass(SocialAccount.class);

        verify(socialAccountMapper)
                .insert(socialAccountCaptor.capture());

        SocialAccount linkedSocialAccount =
                socialAccountCaptor.getValue();

        assertEquals(
                10L,
                linkedSocialAccount.getMemberId()
        );
        assertEquals(
                OAuthProvider.GOOGLE,
                linkedSocialAccount.getProvider()
        );
        assertEquals(
                "new-google-user-id",
                linkedSocialAccount.getProviderSubject()
        );
    }

    @Test
    void rejectsWithdrawnMemberConnectedToSocialAccount() {
        OAuthProfile profile = new OAuthProfile(
                OAuthProvider.GOOGLE,
                "withdrawn-google-user-id",
                "withdrawn@example.com",
                "김탈퇴",
                null
        );

        SocialAccount socialAccount =
                SocialAccount.create(
                        20L,
                        OAuthProvider.GOOGLE,
                        "withdrawn-google-user-id"
                );

        Member withdrawnMember = Member.createParent(
                "withdrawn@example.com",
                "김탈퇴",
                null
        );

        ReflectionTestUtils.setField(
                withdrawnMember,
                "status",
                MemberStatus.WITHDRAWN
        );

        when(
                socialAccountMapper.findByProviderAndSubject(
                        OAuthProvider.GOOGLE,
                        "withdrawn-google-user-id"
                )
        ).thenReturn(socialAccount);

        when(memberMapper.findById(20L))
                .thenReturn(withdrawnMember);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> oauthMemberService.findOrCreate(profile)
        );

        assertEquals(
                ErrorCode.WITHDRAWN_MEMBER,
                exception.getErrorCode()
        );

        verify(memberMapper, never())
                .insert(any(Member.class));
        verify(socialAccountMapper, never())
                .insert(any(SocialAccount.class));
    }

    @Test
    void returnsExistingChildWithoutChangingMemberType() {
        OAuthProfile profile = new OAuthProfile(
                OAuthProvider.KAKAO,
                "child-kakao-user-id",
                "child@example.com",
                "김자녀",
                null
        );

        SocialAccount socialAccount =
                SocialAccount.create(
                        30L,
                        OAuthProvider.KAKAO,
                        "child-kakao-user-id"
                );

        Member childMember = Member.createParent(
                "child@example.com",
                "김자녀",
                null
        );

        // 기존 CHILD 회원을 DB에서 조회한 상황을 만든다.
        ReflectionTestUtils.setField(
                childMember,
                "memberType",
                MemberType.CHILD
        );

        when(
                socialAccountMapper.findByProviderAndSubject(
                        OAuthProvider.KAKAO,
                        "child-kakao-user-id"
                )
        ).thenReturn(socialAccount);

        when(memberMapper.findById(30L))
                .thenReturn(childMember);

        OAuthMemberResult result =
                oauthMemberService.findOrCreate(profile);

        assertSame(childMember, result.getMember());
        assertFalse(result.isNewMember());
        assertEquals(
                MemberType.CHILD,
                result.getMember().getMemberType()
        );

        verify(memberMapper, never())
                .insert(any(Member.class));
        verify(socialAccountMapper, never())
                .insert(any(SocialAccount.class));
    }

    @Test
    void createsChildMemberAndSocialAccountForNewChild() {
        OAuthProfile profile = new OAuthProfile(
                OAuthProvider.KAKAO,
                "new-child-subject",
                "child@example.com",
                "김자녀",
                null
        );

        when(
                socialAccountMapper.findByProviderAndSubject(
                        OAuthProvider.KAKAO,
                        "new-child-subject"
                )
        ).thenReturn(null);

        when(memberMapper.findByEmail("child@example.com"))
                .thenReturn(null);

        doAnswer(invocation -> {
            Member insertedMember =
                    invocation.getArgument(0);

            ReflectionTestUtils.setField(
                    insertedMember,
                    "memberId",
                    40L
            );

            return 1;
        }).when(memberMapper)
                .insert(any(Member.class));

        Member savedMember = Member.createChild(
                "child@example.com",
                "김자녀",
                null
        );

        ReflectionTestUtils.setField(
                savedMember,
                "memberId",
                40L
        );

        when(memberMapper.findById(40L))
                .thenReturn(savedMember);

        OAuthMemberResult result =
                oauthMemberService.findOrCreateChild(profile);

        assertTrue(result.isNewMember());
        assertSame(savedMember, result.getMember());
        assertEquals(
                MemberType.CHILD,
                result.getMember().getMemberType()
        );

        verify(memberMapper)
                .insert(any(Member.class));

        verify(socialAccountMapper)
                .insert(any(SocialAccount.class));
    }

    @Test
    void rejectsParentMemberDuringChildInviteLogin() {
        OAuthProfile profile = new OAuthProfile(
                OAuthProvider.GOOGLE,
                "parent-subject",
                "parent@example.com",
                "김부모",
                null
        );

        SocialAccount socialAccount =
                SocialAccount.create(
                        50L,
                        OAuthProvider.GOOGLE,
                        "parent-subject"
                );

        Member parent = Member.createParent(
                "parent@example.com",
                "김부모",
                null
        );

        when(
                socialAccountMapper.findByProviderAndSubject(
                        OAuthProvider.GOOGLE,
                        "parent-subject"
                )
        ).thenReturn(socialAccount);

        when(memberMapper.findById(50L))
                .thenReturn(parent);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> oauthMemberService.findOrCreateChild(profile)
        );

        assertEquals(
                ErrorCode.MEMBER_TYPE_CONFLICT,
                exception.getErrorCode()
        );

        verify(memberMapper, never())
                .insert(any(Member.class));
    }

    @Test
    void linksNewSocialAccountToExistingChildMember() {
        OAuthProfile profile = new OAuthProfile(
                OAuthProvider.KAKAO,
                "child-kakao-subject",
                "child@example.com",
                "김자녀",
                null
        );

        Member child = Member.createChild(
                "child@example.com",
                "김자녀",
                null
        );

        ReflectionTestUtils.setField(
                child,
                "memberId",
                60L
        );

        when(
                socialAccountMapper.findByProviderAndSubject(
                        OAuthProvider.KAKAO,
                        "child-kakao-subject"
                )
        ).thenReturn(null);

        when(memberMapper.findByEmail("child@example.com"))
                .thenReturn(child);

        OAuthMemberResult result =
                oauthMemberService.findOrCreateChild(profile);

        assertFalse(result.isNewMember());
        assertSame(child, result.getMember());

        verify(memberMapper, never())
                .insert(any(Member.class));

        verify(socialAccountMapper)
                .insert(any(SocialAccount.class));
    }
}
