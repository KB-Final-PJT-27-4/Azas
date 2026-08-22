package com.azas.domain.family.service;

import com.azas.domain.auth.entity.FamilyInvitation;
import com.azas.domain.auth.entity.FamilyInvitationStatus;
import com.azas.domain.auth.entity.FamilyInviteeType;
import com.azas.domain.auth.mapper.ChildInviteMapper;
import com.azas.domain.auth.mapper.ParentInviteMapper;
import com.azas.domain.auth.service.FamilyInvitationStore;
import com.azas.domain.auth.service.TokenHashEncoder;
import com.azas.domain.child.entity.Child;
import com.azas.domain.child.entity.ChildStatus;
import com.azas.domain.child.entity.RelationType;
import com.azas.domain.family.dto.ChildMemberLinkResponse;
import com.azas.domain.family.dto.FamilyInvitationAcceptRequest;
import com.azas.domain.family.dto.FamilyInvitationAcceptResponse;
import com.azas.domain.family.dto.FamilyInvitationCreateRequest;
import com.azas.domain.family.dto.FamilyInvitationCreateResponse;
import com.azas.domain.family.dto.FamilyInvitationChildResponse;
import com.azas.domain.family.dto.FamilyInvitationInfoProjection;
import com.azas.domain.family.mapper.FamilyMapper;
import com.azas.domain.member.entity.Member;
import com.azas.domain.member.mapper.MemberMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.ZoneOffset;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FamilyInvitationServiceImplTest {

    @Mock private FamilyMapper familyMapper;
    @Mock private TokenHashEncoder tokenHashEncoder;
    @Mock private FamilyInvitationStore familyInvitationStore;
    @Mock private ParentInviteMapper parentInviteMapper;
    @Mock private ChildInviteMapper childInviteMapper;
    @Mock private MemberMapper memberMapper;

    @InjectMocks
    private FamilyServiceImpl familyService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(
                familyService,
                "familyInvitationUrlBase",
                "http://localhost:5173/family-invitations"
        );
        ReflectionTestUtils.setField(
                familyService,
                "defaultExpirationHours",
                24
        );
        lenient().when(familyMapper.findActiveChildrenManagedByMember(any()))
                .thenReturn(List.of(invitationChild(10L)));
        lenient().when(familyMapper.findInvitationChildren(any()))
                .thenReturn(List.of(invitationChild(10L)));
        lenient().when(familyMapper.insertFamilyInvitationChildren(any(), any()))
                .thenAnswer(invocation ->
                        ((List<?>) invocation.getArgument(1)).size()
                );
        lenient().when(parentInviteMapper.findActiveChildById(any()))
                .thenReturn(child(10L, null));
        lenient().when(parentInviteMapper.insertChildParentRelations(
                any(), any(), any()
        )).thenAnswer(invocation ->
                ((List<?>) invocation.getArgument(0)).size()
        );
    }

    @Test
    void createsParentInvitationAfterExpiringOldPendingInvitation() {
        when(tokenHashEncoder.encode(any())).thenReturn("token-hash");
        assignInvitationIdOnInsert(30L);

        FamilyInvitationCreateResponse response =
                familyService.createParentFamilyInvitation(7L, createRequest(null));

        assertEquals(30L, response.getFamilyInvitationId());
        assertEquals(FamilyInviteeType.PARENT, response.getInviteeType());
        assertEquals(FamilyInvitationStatus.PENDING, response.getStatus());
        assertNotNull(response.getInviteToken());
        assertEquals(
                "http://localhost:5173/family-invitations/"
                        + response.getInviteToken(),
                response.getInviteUrl()
        );
        verify(familyMapper).expirePendingFamilyInvitations(
                eq(10L), eq(7L), eq(FamilyInviteeType.PARENT), any()
        );
        verify(familyMapper).expireUsableFamilyInvitations(
                eq(10L), eq(7L), eq(FamilyInviteeType.PARENT), any()
        );
    }

    @Test
    void allowsChildInvitationWhenParentInvitationExists() {
        when(familyMapper.lockActiveChild(10L)).thenReturn(10L);
        when(familyMapper.countChildAccess(10L, 7L)).thenReturn(1);
        when(familyMapper.findChildMemberLinkByChildId(10L))
                .thenReturn(childMemberLink(false));
        when(tokenHashEncoder.encode(any())).thenReturn("token-hash");
        assignInvitationIdOnInsert(31L);

        FamilyInvitationCreateResponse response =
                familyService.createChildFamilyInvitation(7L, 10L, createRequest(24));

        assertEquals(31L, response.getFamilyInvitationId());
        assertEquals(FamilyInviteeType.CHILD, response.getInviteeType());
    }

    @Test
    void rejectsInvitationCreationWhenRequesterCannotManageChild() {
        when(familyMapper.lockActiveChild(10L)).thenReturn(10L);
        when(familyMapper.countChildAccess(10L, 7L)).thenReturn(0);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> familyService.createChildFamilyInvitation(
                        7L,
                        10L,
                        createRequest(24)
                )
        );

        assertEquals(ErrorCode.CHILD_ACCESS_DENIED, exception.getErrorCode());
        verify(familyMapper, never()).insertFamilyInvitation(any());
    }

    @Test
    void rejectsInvitationCreationWhenChildDoesNotExist() {
        when(familyMapper.lockActiveChild(10L)).thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> familyService.createChildFamilyInvitation(
                        7L,
                        10L,
                        createRequest(24)
                )
        );

        assertEquals(ErrorCode.CHILD_NOT_FOUND, exception.getErrorCode());
        verify(familyMapper, never()).insertFamilyInvitation(any());
    }

    @Test
    void reissuesInvitationWhenUsableInvitationOfSameTypeExists() {
        when(tokenHashEncoder.encode(any())).thenReturn("new-token-hash");
        assignInvitationIdOnInsert(32L);

        FamilyInvitationCreateResponse response =
                familyService.createParentFamilyInvitation(7L, createRequest(24));

        assertEquals(32L, response.getFamilyInvitationId());
        assertEquals(FamilyInvitationStatus.PENDING, response.getStatus());
        verify(familyMapper).expireUsableFamilyInvitations(
                eq(10L), eq(7L), eq(FamilyInviteeType.PARENT), any()
        );
        verify(familyMapper).insertFamilyInvitation(any());
    }

    @Test
    void createsParentInvitationForEveryChildManagedAtIssuance() {
        List<FamilyInvitationChildResponse> targets = List.of(
                invitationChild(10L),
                invitationChild(20L)
        );
        when(familyMapper.findActiveChildrenManagedByMember(7L))
                .thenReturn(targets);
        when(tokenHashEncoder.encode(any())).thenReturn("token-hash");
        assignInvitationIdOnInsert(33L);

        FamilyInvitationCreateResponse response =
                familyService.createParentFamilyInvitation(7L, createRequest(24));

        assertEquals(2, response.getChildCount());
        assertEquals(List.of(10L, 20L), response.getChildren().stream()
                .map(FamilyInvitationChildResponse::getChildId)
                .collect(Collectors.toList()));
        verify(familyMapper).insertFamilyInvitationChildren(33L, targets);
    }

    @Test
    void rejectsChildInvitationWhenChildMemberAlreadyLinked() {
        when(familyMapper.lockActiveChild(10L)).thenReturn(10L);
        when(familyMapper.countChildAccess(10L, 7L)).thenReturn(1);
        when(familyMapper.findChildMemberLinkByChildId(10L))
                .thenReturn(childMemberLink(true));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> familyService.createChildFamilyInvitation(
                        7L,
                        10L,
                        createRequest(24)
                )
        );

        assertEquals(
                ErrorCode.FAMILY_MEMBER_ALREADY_LINKED,
                exception.getErrorCode()
        );
        verify(familyMapper, never()).insertFamilyInvitation(any());
        verify(familyMapper, never()).expirePendingFamilyInvitations(
                eq(10L), eq(7L), eq(FamilyInviteeType.CHILD), any()
        );
        verify(familyMapper, never()).expireUsableFamilyInvitations(
                eq(10L), eq(7L), eq(FamilyInviteeType.CHILD), any()
        );
    }

    @Test
    void returnsPublicPreviewForUsableInvitation() {
        when(tokenHashEncoder.encode("raw-token")).thenReturn("token-hash");
        when(familyMapper.findFamilyInvitationInfoByTokenHash("token-hash"))
                .thenReturn(preview(
                        "깨비",
                        "김하나",
                        FamilyInviteeType.PARENT,
                        FamilyInvitationStatus.PENDING,
                        LocalDateTime.now().plusHours(1)
                ));

        var response = familyService.getFamilyInvitationInfo("raw-token");

        assertEquals("깨비", response.getChildName());
        assertEquals("김하나", response.getInviterName());
        assertEquals(FamilyInviteeType.PARENT, response.getInviteeType());
    }

    @Test
    void rejectsMissingInvitationPreview() {
        when(tokenHashEncoder.encode("missing-token")).thenReturn("token-hash");
        when(familyMapper.findFamilyInvitationInfoByTokenHash("token-hash"))
                .thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> familyService.getFamilyInvitationInfo("missing-token")
        );

        assertEquals(
                ErrorCode.FAMILY_INVITATION_NOT_FOUND,
                exception.getErrorCode()
        );
    }

    @Test
    void rejectsExpiredInvitationPreview() {
        when(tokenHashEncoder.encode("expired-token")).thenReturn("token-hash");
        when(familyMapper.findFamilyInvitationInfoByTokenHash("token-hash"))
                .thenReturn(preview(
                        "깨비",
                        "김하나",
                        FamilyInviteeType.PARENT,
                        FamilyInvitationStatus.PENDING,
                        LocalDateTime.now(ZoneOffset.UTC).minusMinutes(1)
                ));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> familyService.getFamilyInvitationInfo("expired-token")
        );

        assertEquals(ErrorCode.FAMILY_INVITATION_GONE, exception.getErrorCode());
    }

    @Test
    void rejectsAcceptedInvitationPreview() {
        when(tokenHashEncoder.encode("accepted-token")).thenReturn("token-hash");
        when(familyMapper.findFamilyInvitationInfoByTokenHash("token-hash"))
                .thenReturn(preview(
                        "깨비",
                        "김하나",
                        FamilyInviteeType.PARENT,
                        FamilyInvitationStatus.ACCEPTED,
                        LocalDateTime.now().plusHours(1)
                ));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> familyService.getFamilyInvitationInfo("accepted-token")
        );

        assertEquals(ErrorCode.FAMILY_INVITATION_GONE, exception.getErrorCode());
    }

    @Test
    void acceptsParentInvitationAndCreatesRelation() {
        FamilyInvitation invitation = pendingInvitation(FamilyInviteeType.PARENT);

        when(tokenHashEncoder.encode("parent-token")).thenReturn("token-hash");
        when(familyInvitationStore.findByInviteTokenHash("token-hash"))
                .thenReturn(Optional.of(invitation));
        when(memberMapper.findById(8L)).thenReturn(parentMember(8L));
        when(parentInviteMapper.findActiveChildById(10L))
                .thenReturn(child(10L, null));
        when(parentInviteMapper.countChildParentRelation(10L, 8L))
                .thenReturn(0);
        when(parentInviteMapper.insertChildParentRelations(
                List.of(10L), 8L, RelationType.FATHER
        )).thenReturn(1);
        when(familyInvitationStore.acceptIfPending(
                eq(30L), eq(8L), eq(RelationType.FATHER), any()
        )).thenReturn(true);

        var response = familyService.acceptFamilyInvitation(
                8L,
                "parent-token",
                acceptRequest(RelationType.FATHER)
        );

        assertEquals(FamilyInvitationStatus.ACCEPTED, response.getStatus());
        assertEquals(RelationType.FATHER, response.getRelationType());
        assertEquals(10L, response.getChild().getChildId());
    }

    @Test
    void acceptsParentInvitationAndCreatesRelationsForAllInvitationChildren() {
        FamilyInvitation invitation = pendingInvitation(FamilyInviteeType.PARENT);
        List<FamilyInvitationChildResponse> targets = List.of(
                invitationChild(10L),
                invitationChild(20L)
        );

        when(tokenHashEncoder.encode("parent-token")).thenReturn("token-hash");
        when(familyInvitationStore.findByInviteTokenHash("token-hash"))
                .thenReturn(Optional.of(invitation));
        when(memberMapper.findById(8L)).thenReturn(parentMember(8L));
        when(familyMapper.findInvitationChildren(30L)).thenReturn(targets);
        when(parentInviteMapper.countChildParentRelation(10L, 8L)).thenReturn(0);
        when(parentInviteMapper.countChildParentRelation(20L, 8L)).thenReturn(0);
        when(parentInviteMapper.insertChildParentRelations(
                List.of(10L, 20L), 8L, RelationType.FATHER
        )).thenReturn(2);
        when(familyInvitationStore.acceptIfPending(
                eq(30L), eq(8L), eq(RelationType.FATHER), any()
        )).thenReturn(true);

        FamilyInvitationAcceptResponse response =
                familyService.acceptFamilyInvitation(
                        8L,
                        "parent-token",
                        acceptRequest(RelationType.FATHER)
                );

        assertEquals(2, response.getChildCount());
        assertEquals(List.of(10L, 20L), response.getChildren().stream()
                .map(FamilyInvitationChildResponse::getChildId)
                .collect(Collectors.toList()));
        verify(parentInviteMapper).insertChildParentRelations(
                List.of(10L, 20L), 8L, RelationType.FATHER
        );
    }

    @Test
    void rejectsParentInvitationWithoutRelationType() {
        setUpParentAcceptance("parent-token", parentMember(8L));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> familyService.acceptFamilyInvitation(
                        8L,
                        "parent-token",
                        new FamilyInvitationAcceptRequest()
                )
        );

        assertEquals(
                ErrorCode.FAMILY_INVITATION_RELATION_TYPE_REQUIRED,
                exception.getErrorCode()
        );
    }

    @Test
    void rejectsParentInvitationAcceptedByChildMember() {
        setUpParentAcceptance("parent-token", childMember(9L));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> familyService.acceptFamilyInvitation(
                        9L,
                        "parent-token",
                        acceptRequest(RelationType.FATHER)
                )
        );

        assertEquals(ErrorCode.MEMBER_TYPE_CONFLICT, exception.getErrorCode());
    }

    @Test
    void acceptsChildInvitationAndLinksChildMember() {
        FamilyInvitation invitation = pendingInvitation(FamilyInviteeType.CHILD);

        when(tokenHashEncoder.encode("child-token")).thenReturn("token-hash");
        when(familyInvitationStore.findByInviteTokenHash("token-hash"))
                .thenReturn(Optional.of(invitation));
        when(memberMapper.findById(9L)).thenReturn(childMember(9L));
        when(childInviteMapper.findActiveById(10L))
                .thenReturn(child(10L, null));
        when(childInviteMapper.findByMemberId(9L)).thenReturn(null);
        when(childInviteMapper.linkMemberIfUnlinked(10L, 9L)).thenReturn(1);
        when(familyInvitationStore.acceptIfPending(
                eq(30L), eq(9L), eq(null), any()
        )).thenReturn(true);

        var response = familyService.acceptFamilyInvitation(
                9L,
                "child-token",
                new FamilyInvitationAcceptRequest()
        );

        assertEquals(FamilyInviteeType.CHILD, response.getInviteeType());
        assertEquals(null, response.getRelationType());
        verify(childInviteMapper).linkMemberIfUnlinked(10L, 9L);
    }

    @Test
    void rejectsChildInvitationAcceptedByParentMember() {
        setUpChildAcceptance("child-token", parentMember(8L), child(10L, null));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> familyService.acceptFamilyInvitation(
                        8L,
                        "child-token",
                        new FamilyInvitationAcceptRequest()
                )
        );

        assertEquals(ErrorCode.MEMBER_TYPE_CONFLICT, exception.getErrorCode());
    }

    @Test
    void rejectsChildInvitationWhenChildProfileAlreadyLinked() {
        setUpChildAcceptance("child-token", childMember(9L), child(10L, 99L));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> familyService.acceptFamilyInvitation(
                        9L,
                        "child-token",
                        new FamilyInvitationAcceptRequest()
                )
        );

        assertEquals(
                ErrorCode.FAMILY_MEMBER_ALREADY_LINKED,
                exception.getErrorCode()
        );
    }

    @Test
    void rejectsChildInvitationWhenMemberIsAlreadyLinkedToAnotherChild() {
        setUpChildAcceptance("child-token", childMember(9L), child(10L, null));
        when(childInviteMapper.findByMemberId(9L)).thenReturn(child(20L, 9L));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> familyService.acceptFamilyInvitation(
                        9L,
                        "child-token",
                        new FamilyInvitationAcceptRequest()
                )
        );

        assertEquals(
                ErrorCode.FAMILY_MEMBER_ALREADY_LINKED,
                exception.getErrorCode()
        );
    }

    @Test
    void rejectsAlreadyAcceptedInvitation() {
        when(tokenHashEncoder.encode("accepted-token")).thenReturn("token-hash");
        when(familyInvitationStore.findByInviteTokenHash("token-hash"))
                .thenReturn(Optional.of(invitation(
                        FamilyInviteeType.PARENT,
                        FamilyInvitationStatus.ACCEPTED,
                        LocalDateTime.now().plusHours(1)
                )));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> familyService.acceptFamilyInvitation(
                        8L,
                        "accepted-token",
                        acceptRequest(RelationType.FATHER)
                )
        );

        assertEquals(ErrorCode.FAMILY_INVITATION_GONE, exception.getErrorCode());
    }

    @Test
    void rejectsMissingInvitationAcceptance() {
        when(tokenHashEncoder.encode("missing-token")).thenReturn("token-hash");
        when(familyInvitationStore.findByInviteTokenHash("token-hash"))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> familyService.acceptFamilyInvitation(
                        8L,
                        "missing-token",
                        acceptRequest(RelationType.FATHER)
                )
        );

        assertEquals(
                ErrorCode.FAMILY_INVITATION_NOT_FOUND,
                exception.getErrorCode()
        );
    }

    @Test
    void rejectsRelationTypeForChildInvitation() {
        setUpChildAcceptance("child-token", childMember(9L), child(10L, null));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> familyService.acceptFamilyInvitation(
                        9L,
                        "child-token",
                        acceptRequest(RelationType.MOTHER)
                )
        );

        assertEquals(ErrorCode.BADREQUEST, exception.getErrorCode());
    }

    @Test
    void rejectsSecondAcceptanceWhenAnotherRequestAlreadyAcceptedInvitation() {
        setUpParentAcceptance("parent-token", parentMember(8L));
        when(parentInviteMapper.countChildParentRelation(10L, 8L))
                .thenReturn(0);
        when(parentInviteMapper.insertChildParentRelations(
                List.of(10L), 8L, RelationType.FATHER
        )).thenReturn(1);
        when(familyInvitationStore.acceptIfPending(
                eq(30L), eq(8L), eq(RelationType.FATHER), any()
        )).thenReturn(false);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> familyService.acceptFamilyInvitation(
                        8L,
                        "parent-token",
                        acceptRequest(RelationType.FATHER)
                )
        );

        assertEquals(ErrorCode.FAMILY_INVITATION_GONE, exception.getErrorCode());
    }

    @Test
    void convertsParentRelationDuplicateToConflict() {
        setUpParentAcceptance("parent-token", parentMember(8L));
        when(parentInviteMapper.countChildParentRelation(10L, 8L))
                .thenReturn(0);
        when(parentInviteMapper.insertChildParentRelations(
                List.of(10L), 8L, RelationType.FATHER
        )).thenThrow(new DuplicateKeyException("duplicate"));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> familyService.acceptFamilyInvitation(
                        8L,
                        "parent-token",
                        acceptRequest(RelationType.FATHER)
                )
        );

        assertEquals(
                ErrorCode.FAMILY_MEMBER_ALREADY_LINKED,
                exception.getErrorCode()
        );
    }

    private void setUpParentAcceptance(String token, Member member) {
        when(tokenHashEncoder.encode(token)).thenReturn("token-hash");
        when(familyInvitationStore.findByInviteTokenHash("token-hash"))
                .thenReturn(Optional.of(pendingInvitation(FamilyInviteeType.PARENT)));
        when(memberMapper.findById(member.getMemberId())).thenReturn(member);
        when(parentInviteMapper.findActiveChildById(10L))
                .thenReturn(child(10L, null));
    }

    private void setUpChildAcceptance(
            String token,
            Member member,
            Child child
    ) {
        when(tokenHashEncoder.encode(token)).thenReturn("token-hash");
        when(familyInvitationStore.findByInviteTokenHash("token-hash"))
                .thenReturn(Optional.of(pendingInvitation(FamilyInviteeType.CHILD)));
        when(memberMapper.findById(member.getMemberId())).thenReturn(member);
        when(childInviteMapper.findActiveById(10L)).thenReturn(child);
    }

    private void assignInvitationIdOnInsert(Long invitationId) {
        when(familyMapper.insertFamilyInvitation(any())).thenAnswer(invocation -> {
            Object command = invocation.getArgument(0);
            ReflectionTestUtils.setField(
                    command,
                    "familyInvitationId",
                    invitationId
            );
            return 1;
        });
    }

    private FamilyInvitationCreateRequest createRequest(Integer expiresInHours) {
        FamilyInvitationCreateRequest request =
                new FamilyInvitationCreateRequest();
        ReflectionTestUtils.setField(
                request,
                "expiresInHours",
                expiresInHours
        );
        return request;
    }

    private FamilyInvitationAcceptRequest acceptRequest(
            RelationType relationType
    ) {
        FamilyInvitationAcceptRequest request =
                new FamilyInvitationAcceptRequest();
        ReflectionTestUtils.setField(request, "relationType", relationType);
        return request;
    }

    private ChildMemberLinkResponse childMemberLink(boolean linked) {
        ChildMemberLinkResponse response = new ChildMemberLinkResponse();
        ReflectionTestUtils.setField(response, "childId", 10L);
        ReflectionTestUtils.setField(response, "linked", linked);
        return response;
    }

    private FamilyInvitationInfoProjection preview(
            String childName,
            String inviterName,
            FamilyInviteeType inviteeType,
            FamilyInvitationStatus status,
            LocalDateTime expiresAt
    ) {
        return new FamilyInvitationInfoProjection(
                30L,
                childName,
                inviterName,
                inviteeType,
                status,
                expiresAt
        );
    }

    private FamilyInvitation pendingInvitation(FamilyInviteeType inviteeType) {
        return invitation(
                inviteeType,
                FamilyInvitationStatus.PENDING,
                LocalDateTime.now().plusHours(1)
        );
    }

    private FamilyInvitationChildResponse invitationChild(Long childId) {
        return new FamilyInvitationChildResponse(childId, "깨비");
    }

    private FamilyInvitation invitation(
            FamilyInviteeType inviteeType,
            FamilyInvitationStatus status,
            LocalDateTime expiresAt
    ) {
        return FamilyInvitation.builder()
                .familyInvitationId(30L)
                .childId(10L)
                .inviterMemberId(7L)
                .inviteeType(inviteeType)
                .inviteTokenHash("token-hash")
                .status(status)
                .expiresAt(expiresAt)
                .createdAt(LocalDateTime.now().minusMinutes(1))
                .updatedAt(LocalDateTime.now().minusMinutes(1))
                .build();
    }

    private Child child(Long childId, Long memberId) {
        Child child = new Child();
        child.setChildId(childId);
        child.setMemberId(memberId);
        child.setName("깨비");
        child.setChildStatus(ChildStatus.ACTIVE);
        return child;
    }

    private Member parentMember(Long memberId) {
        Member member = Member.createParent(
                "parent@example.com",
                "김부모",
                null
        );
        ReflectionTestUtils.setField(member, "memberId", memberId);
        return member;
    }

    private Member childMember(Long memberId) {
        Member member = Member.createChild(
                "child@example.com",
                "김자녀",
                null
        );
        ReflectionTestUtils.setField(member, "memberId", memberId);
        return member;
    }
}
