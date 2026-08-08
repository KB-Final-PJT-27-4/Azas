package com.azas.domain.auth.service;

import com.azas.domain.auth.dto.AuthTokenPair;
import com.azas.domain.auth.dto.OAuthMemberResult;
import com.azas.domain.auth.dto.OAuthProfile;
import com.azas.domain.auth.dto.ParentInviteOAuthResult;
import com.azas.domain.auth.entity.FamilyInvitation;
import com.azas.domain.auth.entity.FamilyInvitationStatus;
import com.azas.domain.auth.entity.FamilyInviteeType;
import com.azas.domain.auth.entity.OAuthProvider;
import com.azas.domain.auth.mapper.ParentInviteMapper;
import com.azas.domain.child.entity.Child;
import com.azas.domain.child.entity.ChildStatus;
import com.azas.domain.child.entity.RelationType;
import com.azas.domain.member.entity.Member;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParentInviteAcceptanceServiceTest {

    @Mock
    private TokenHashEncoder tokenHashEncoder;

    @Mock
    private FamilyInvitationStore familyInvitationStore;

    @Mock
    private OAuthMemberService oauthMemberService;

    @Mock
    private ParentInviteMapper parentInviteMapper;

    @Mock
    private AuthTokenService authTokenService;

    @InjectMocks
    private ParentInviteAcceptanceService service;

    @Test
    void acceptsInvitationAndCreatesParentRelation() {
        OAuthProfile profile = parentProfile();
        Member member = parentMember(20L);
        Child child = child(10L);

        when(tokenHashEncoder.encode("raw-token"))
                .thenReturn("hashed-token");

        when(
                familyInvitationStore.findByInviteTokenHash(
                        "hashed-token"
                )
        ).thenReturn(
                Optional.of(parentInvitation())
        );

        when(parentInviteMapper.findActiveChildById(10L))
                .thenReturn(child);

        when(oauthMemberService.findOrCreateParent(profile))
                .thenReturn(
                        new OAuthMemberResult(
                                member,
                                true
                        )
                );

        when(
                parentInviteMapper.countChildParentRelation(
                        10L,
                        20L
                )
        ).thenReturn(0);

        when(
                parentInviteMapper.insertChildParentRelation(
                        10L,
                        20L,
                        RelationType.GUARDIAN
                )
        ).thenReturn(1);

        when(
                familyInvitationStore.acceptIfPending(
                        eq(30L),
                        eq(20L),
                        eq(RelationType.GUARDIAN),
                        any(LocalDateTime.class)
                )
        ).thenReturn(true);

        AuthTokenPair tokenPair =
                new AuthTokenPair(
                        "access-token",
                        "refresh-token",
                        3600L
                );

        when(authTokenService.issue(member))
                .thenReturn(tokenPair);

        ParentInviteOAuthResult result =
                service.accept(
                        "raw-token",
                        RelationType.GUARDIAN,
                        profile
                );

        assertSame(tokenPair, result.getTokenPair());
        assertSame(member, result.getMember());
        assertEquals(10L, result.getChildId());
        assertEquals("김자녀", result.getChildName());
        assertEquals(
                RelationType.GUARDIAN,
                result.getRelationType()
        );
        assertEquals(30L, result.getFamilyInvitationId());
    }

    @Test
    void rejectsChildInvitationOnParentEndpoint() {
        when(tokenHashEncoder.encode("child-token"))
                .thenReturn("child-hash");

        when(
                familyInvitationStore.findByInviteTokenHash(
                        "child-hash"
                )
        ).thenReturn(
                Optional.of(
                        FamilyInvitation.builder()
                                .familyInvitationId(30L)
                                .childId(10L)
                                .inviteeType(
                                        FamilyInviteeType.CHILD
                                )
                                .status(
                                        FamilyInvitationStatus.PENDING
                                )
                                .expiresAt(
                                        LocalDateTime.now()
                                                .plusDays(1)
                                )
                                .build()
                )
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.accept(
                        "child-token",
                        RelationType.GUARDIAN,
                        parentProfile()
                )
        );

        assertEquals(
                ErrorCode.INVALID_FAMILY_INVITATION,
                exception.getErrorCode()
        );
    }

    @Test
    void rejectsExistingParentRelation() {
        OAuthProfile profile = parentProfile();
        Member member = parentMember(20L);

        prepareValidInvitation(profile, member);

        when(
                parentInviteMapper.countChildParentRelation(
                        10L,
                        20L
                )
        ).thenReturn(1);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.accept(
                        "raw-token",
                        RelationType.GUARDIAN,
                        profile
                )
        );

        assertEquals(
                ErrorCode.FAMILY_MEMBER_ALREADY_LINKED,
                exception.getErrorCode()
        );

        verify(parentInviteMapper, never())
                .insertChildParentRelation(
                        any(Long.class),
                        any(Long.class),
                        any(RelationType.class)
                );
    }

    @Test
    void rejectsConcurrentDuplicateRelation() {
        OAuthProfile profile = parentProfile();
        Member member = parentMember(20L);

        prepareValidInvitation(profile, member);

        when(
                parentInviteMapper.countChildParentRelation(
                        10L,
                        20L
                )
        ).thenReturn(0);

        when(
                parentInviteMapper.insertChildParentRelation(
                        10L,
                        20L,
                        RelationType.GUARDIAN
                )
        ).thenThrow(
                new DuplicateKeyException("duplicate")
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.accept(
                        "raw-token",
                        RelationType.GUARDIAN,
                        profile
                )
        );

        assertEquals(
                ErrorCode.FAMILY_MEMBER_ALREADY_LINKED,
                exception.getErrorCode()
        );
    }

    @Test
    void rejectsInvitationChangedByConcurrentRequest() {
        OAuthProfile profile = parentProfile();
        Member member = parentMember(20L);

        prepareValidInvitation(profile, member);

        when(
                parentInviteMapper.countChildParentRelation(
                        10L,
                        20L
                )
        ).thenReturn(0);

        when(
                parentInviteMapper.insertChildParentRelation(
                        10L,
                        20L,
                        RelationType.GUARDIAN
                )
        ).thenReturn(1);

        when(
                familyInvitationStore.acceptIfPending(
                        eq(30L),
                        eq(20L),
                        eq(RelationType.GUARDIAN),
                        any(LocalDateTime.class)
                )
        ).thenReturn(false);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.accept(
                        "raw-token",
                        RelationType.GUARDIAN,
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

    private void prepareValidInvitation(
            OAuthProfile profile,
            Member member
    ) {
        when(tokenHashEncoder.encode("raw-token"))
                .thenReturn("hashed-token");

        when(
                familyInvitationStore.findByInviteTokenHash(
                        "hashed-token"
                )
        ).thenReturn(
                Optional.of(parentInvitation())
        );

        when(parentInviteMapper.findActiveChildById(10L))
                .thenReturn(child(10L));

        when(oauthMemberService.findOrCreateParent(profile))
                .thenReturn(
                        new OAuthMemberResult(
                                member,
                                false
                        )
                );
    }

    private FamilyInvitation parentInvitation() {
        return FamilyInvitation.builder()
                .familyInvitationId(30L)
                .childId(10L)
                .inviterMemberId(1L)
                .inviteeType(FamilyInviteeType.PARENT)
                .status(FamilyInvitationStatus.PENDING)
                .expiresAt(
                        LocalDateTime.now()
                                .plusDays(1)
                )
                .build();
    }

    private OAuthProfile parentProfile() {
        return new OAuthProfile(
                OAuthProvider.GOOGLE,
                "google-parent-subject",
                "parent@example.com",
                "김부모",
                null
        );
    }

    private Member parentMember(Long memberId) {
        Member member = Member.createParent(
                "parent@example.com",
                "김부모",
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