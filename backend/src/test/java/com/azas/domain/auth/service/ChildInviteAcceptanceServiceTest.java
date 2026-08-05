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
import org.mockito.ArgumentCaptor;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChildInviteAcceptanceServiceTest {

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
    private ChildInviteAcceptanceService service;

    @Test
    void acceptsInvitationAndLinksChildMember() {
        OAuthProfile profile = childProfile();
        FamilyInvitation invitation =
                usableInvitation(FamilyInviteeType.CHILD);
        Child child = child(10L, null);
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
                        new OAuthMemberResult(
                                member,
                                true
                        )
                );

        when(childInviteMapper.findByMemberId(20L))
                .thenReturn(null);

        when(
                childInviteMapper.linkMemberIfUnlinked(
                        10L,
                        20L
                )
        ).thenReturn(1);

        when(
                familyInvitationStore.acceptIfPending(
                        eq(30L),
                        eq(20L),
                        isNull(),
                        any(LocalDateTime.class)
                )
        ).thenReturn(true);

        when(authTokenService.issue(member))
                .thenReturn(tokenPair);

        ChildInviteOAuthResult result =
                service.accept(
                        "raw-token",
                        profile
                );

        assertSame(tokenPair, result.getTokenPair());
        assertSame(member, result.getMember());
        assertEquals(10L, result.getChildId());
        assertEquals("김자녀", result.getChildName());
        assertEquals(30L, result.getFamilyInvitationId());

        ArgumentCaptor<LocalDateTime> acceptedAtCaptor =
                ArgumentCaptor.forClass(
                        LocalDateTime.class
                );

        verify(familyInvitationStore)
                .acceptIfPending(
                        eq(30L),
                        eq(20L),
                        isNull(),
                        acceptedAtCaptor.capture()
                );

        assertEquals(
                acceptedAtCaptor.getValue(),
                result.getAcceptedAt()
        );
    }

    @Test
    void rejectsMissingInvitation() {
        when(tokenHashEncoder.encode("invalid-token"))
                .thenReturn("invalid-hash");

        when(
                familyInvitationStore.findByInviteTokenHash(
                        "invalid-hash"
                )
        ).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.accept(
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
    void rejectsParentInvitationOnChildEndpoint() {
        when(tokenHashEncoder.encode("parent-token"))
                .thenReturn("parent-hash");

        when(
                familyInvitationStore.findByInviteTokenHash(
                        "parent-hash"
                )
        ).thenReturn(
                Optional.of(
                        usableInvitation(
                                FamilyInviteeType.PARENT
                        )
                )
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.accept(
                        "parent-token",
                        childProfile()
                )
        );

        assertEquals(
                ErrorCode.INVALID_FAMILY_INVITATION,
                exception.getErrorCode()
        );
    }

    @Test
    void rejectsChildAlreadyLinkedToMember() {
        when(tokenHashEncoder.encode("raw-token"))
                .thenReturn("hashed-token");

        when(
                familyInvitationStore.findByInviteTokenHash(
                        "hashed-token"
                )
        ).thenReturn(
                Optional.of(
                        usableInvitation(
                                FamilyInviteeType.CHILD
                        )
                )
        );

        when(childInviteMapper.findActiveById(10L))
                .thenReturn(child(10L, 99L));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.accept(
                        "raw-token",
                        childProfile()
                )
        );

        assertEquals(
                ErrorCode.FAMILY_MEMBER_ALREADY_LINKED,
                exception.getErrorCode()
        );

        verify(oauthMemberService, never())
                .findOrCreateChild(any(OAuthProfile.class));
    }

    @Test
    void rejectsMemberAlreadyLinkedToAnotherChild() {
        OAuthProfile profile = childProfile();
        Member member = childMember(20L);

        when(tokenHashEncoder.encode("raw-token"))
                .thenReturn("hashed-token");

        when(
                familyInvitationStore.findByInviteTokenHash(
                        "hashed-token"
                )
        ).thenReturn(
                Optional.of(
                        usableInvitation(
                                FamilyInviteeType.CHILD
                        )
                )
        );

        when(childInviteMapper.findActiveById(10L))
                .thenReturn(child(10L, null));

        when(oauthMemberService.findOrCreateChild(profile))
                .thenReturn(
                        new OAuthMemberResult(
                                member,
                                false
                        )
                );

        when(childInviteMapper.findByMemberId(20L))
                .thenReturn(child(11L, 20L));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.accept(
                        "raw-token",
                        profile
                )
        );

        assertEquals(
                ErrorCode.FAMILY_MEMBER_ALREADY_LINKED,
                exception.getErrorCode()
        );

        verify(childInviteMapper, never())
                .linkMemberIfUnlinked(
                        any(Long.class),
                        any(Long.class)
                );
    }

    @Test
    void rejectsInvitationChangedByConcurrentRequest() {
        OAuthProfile profile = childProfile();
        Member member = childMember(20L);

        when(tokenHashEncoder.encode("raw-token"))
                .thenReturn("hashed-token");

        when(
                familyInvitationStore.findByInviteTokenHash(
                        "hashed-token"
                )
        ).thenReturn(
                Optional.of(
                        usableInvitation(
                                FamilyInviteeType.CHILD
                        )
                )
        );

        when(childInviteMapper.findActiveById(10L))
                .thenReturn(child(10L, null));

        when(oauthMemberService.findOrCreateChild(profile))
                .thenReturn(
                        new OAuthMemberResult(
                                member,
                                false
                        )
                );

        when(childInviteMapper.findByMemberId(20L))
                .thenReturn(null);

        when(
                childInviteMapper.linkMemberIfUnlinked(
                        10L,
                        20L
                )
        ).thenReturn(1);

        when(
                familyInvitationStore.acceptIfPending(
                        eq(30L),
                        eq(20L),
                        isNull(),
                        any(LocalDateTime.class)
                )
        ).thenReturn(false);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.accept(
                        "raw-token",
                        profile
                )
        );

        assertEquals(
                ErrorCode.INVALID_FAMILY_INVITATION,
                exception.getErrorCode()
        );

        verify(authTokenService, never())
                .issue(any(Member.class));
    }

    private OAuthProfile childProfile() {
        return new OAuthProfile(
                OAuthProvider.KAKAO,
                "kakao-child-subject",
                "child@example.com",
                "김자녀",
                null
        );
    }

    private FamilyInvitation usableInvitation(
            FamilyInviteeType inviteeType
    ) {
        return FamilyInvitation.builder()
                .familyInvitationId(30L)
                .childId(10L)
                .inviterMemberId(1L)
                .inviteeType(inviteeType)
                .inviteTokenHash("hashed-token")
                .status(FamilyInvitationStatus.PENDING)
                .expiresAt(
                        LocalDateTime.now()
                                .plusDays(1)
                )
                .build();
    }

    private Child child(
            Long childId,
            Long memberId
    ) {
        Child child = new Child();

        ReflectionTestUtils.setField(
                child,
                "childId",
                childId
        );
        ReflectionTestUtils.setField(
                child,
                "memberId",
                memberId
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
}