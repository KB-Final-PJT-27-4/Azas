package com.azas.domain.auth.service;

import com.azas.domain.auth.dto.AuthTokenPair;
import com.azas.domain.auth.dto.ChildInviteOAuthResult;
import com.azas.domain.auth.dto.OAuthMemberResult;
import com.azas.domain.auth.dto.OAuthProfile;
import com.azas.domain.auth.entity.FamilyInvitation;
import com.azas.domain.auth.entity.FamilyInvitationStatus;
import com.azas.domain.auth.entity.FamilyInviteeType;
import com.azas.domain.auth.entity.OAuthProvider;
import com.azas.domain.auth.mapper.ChildInviteMapper;
import com.azas.domain.child.entity.Child;
import com.azas.domain.child.entity.ChildStatus;
import com.azas.domain.member.entity.Member;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChildInviteLoginServiceTest {

    @Mock
    private TokenHashEncoder tokenHashEncoder;

    @Mock
    private FamilyInvitationStore familyInvitationStore;

    @Mock
    private OAuthMemberService oauthMemberService;

    @Mock
    private ChildInviteMapper childInviteMapper;

    @Mock
    private AuthTokenService authTokenService;

    @InjectMocks
    private ChildInviteLoginService service;

    @Test
    void logsInWithoutLinkingChildOrAcceptingInvitation() {
        OAuthProfile profile = childProfile();
        LocalDateTime expiresAt =
                LocalDateTime.now().plusDays(1);

        FamilyInvitation invitation =
                childInvitation(expiresAt);

        Child child = child(10L);
        Member member = childMember(20L);

        AuthTokenPair tokenPair =
                new AuthTokenPair(
                        "access-token",
                        "refresh-token",
                        3600L
                );

        when(tokenHashEncoder.encode("raw-token"))
                .thenReturn("hashed-token");

        when(
                familyInvitationStore.findByInviteTokenHash(
                        "hashed-token"
                )
        ).thenReturn(Optional.of(invitation));

        when(childInviteMapper.findActiveById(10L))
                .thenReturn(child);

        when(oauthMemberService.findOrCreateChild(profile))
                .thenReturn(
                        new OAuthMemberResult(member, true)
                );

        when(authTokenService.issue(member))
                .thenReturn(tokenPair);

        ChildInviteOAuthResult result =
                service.login("raw-token", profile);

        assertSame(tokenPair, result.getTokenPair());
        assertSame(member, result.getMember());
        assertEquals(10L, result.getChildId());
        assertEquals("김자녀", result.getChildName());
        assertEquals(30L, result.getFamilyInvitationId());
        assertEquals(expiresAt, result.getExpiresAt());

        verify(childInviteMapper, never())
                .linkMemberIfUnlinked(
                        any(Long.class),
                        any(Long.class)
                );

        verify(familyInvitationStore, never())
                .acceptIfPending(
                        any(Long.class),
                        any(Long.class),
                        any(),
                        any(LocalDateTime.class)
                );
    }

    @Test
    void rejectsMissingInvitationBeforeCreatingMember() {
        when(tokenHashEncoder.encode("invalid-token"))
                .thenReturn("invalid-hash");

        when(
                familyInvitationStore.findByInviteTokenHash(
                        "invalid-hash"
                )
        ).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.login(
                        "invalid-token",
                        childProfile()
                )
        );

        assertEquals(
                ErrorCode.INVALID_FAMILY_INVITATION,
                exception.getErrorCode()
        );

        verify(oauthMemberService, never())
                .findOrCreateChild(any(OAuthProfile.class));
    }

    @Test
    void rejectsParentInvitationOnChildLogin() {
        FamilyInvitation invitation =
                FamilyInvitation.builder()
                        .familyInvitationId(30L)
                        .childId(10L)
                        .inviteeType(FamilyInviteeType.PARENT)
                        .status(FamilyInvitationStatus.PENDING)
                        .expiresAt(
                                LocalDateTime.now().plusDays(1)
                        )
                        .build();

        when(tokenHashEncoder.encode("raw-token"))
                .thenReturn("hashed-token");

        when(
                familyInvitationStore.findByInviteTokenHash(
                        "hashed-token"
                )
        ).thenReturn(Optional.of(invitation));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.login(
                        "raw-token",
                        childProfile()
                )
        );

        assertEquals(
                ErrorCode.INVALID_FAMILY_INVITATION,
                exception.getErrorCode()
        );

        verify(oauthMemberService, never())
                .findOrCreateChild(any(OAuthProfile.class));
    }

    @Test
    void rejectsInvitationWhenTargetChildIsMissing() {
        when(tokenHashEncoder.encode("raw-token"))
                .thenReturn("hashed-token");

        when(
                familyInvitationStore.findByInviteTokenHash(
                        "hashed-token"
                )
        ).thenReturn(
                Optional.of(
                        childInvitation(
                                LocalDateTime.now().plusDays(1)
                        )
                )
        );

        when(childInviteMapper.findActiveById(10L))
                .thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.login(
                        "raw-token",
                        childProfile()
                )
        );

        assertEquals(
                ErrorCode.INVALID_FAMILY_INVITATION,
                exception.getErrorCode()
        );

        verify(oauthMemberService, never())
                .findOrCreateChild(any(OAuthProfile.class));
    }

    private FamilyInvitation childInvitation(
            LocalDateTime expiresAt
    ) {
        return FamilyInvitation.builder()
                .familyInvitationId(30L)
                .childId(10L)
                .inviteeType(FamilyInviteeType.CHILD)
                .status(FamilyInvitationStatus.PENDING)
                .expiresAt(expiresAt)
                .build();
    }

    private OAuthProfile childProfile() {
        return new OAuthProfile(
                OAuthProvider.GOOGLE,
                "google-child-subject",
                "child@example.com",
                "김자녀",
                null
        );
    }

    private Member childMember(Long memberId) {
        Member member = Member.createChild(
                "child@example.com",
                "김자녀",
                null
        );

        ReflectionTestUtils.setField(
                member,
                "memberId",
                memberId
        );

        return member;
    }

    private Child child(Long childId) {
        Child child = new Child();

        ReflectionTestUtils.setField(
                child,
                "childId",
                childId
        );
        ReflectionTestUtils.setField(
                child,
                "name",
                "김자녀"
        );
        ReflectionTestUtils.setField(
                child,
                "childStatus",
                ChildStatus.ACTIVE
        );

        return child;
    }
}