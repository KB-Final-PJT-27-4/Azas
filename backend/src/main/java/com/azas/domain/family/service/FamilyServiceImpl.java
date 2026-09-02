package com.azas.domain.family.service;

import com.azas.domain.auth.entity.FamilyInvitation;
import com.azas.domain.auth.entity.FamilyInvitationStatus;
import com.azas.domain.auth.entity.FamilyInviteeType;
import com.azas.domain.auth.mapper.ChildInviteMapper;
import com.azas.domain.auth.mapper.ParentInviteMapper;
import com.azas.domain.auth.service.FamilyInvitationStore;
import com.azas.domain.auth.service.TokenHashEncoder;
import com.azas.domain.child.entity.Child;
import com.azas.domain.child.entity.RelationType;
import com.azas.domain.family.dto.AllowanceRequestResponse;
import com.azas.domain.family.dto.ChildMemberLinkResponse;
import com.azas.domain.family.dto.FamilyGuardianListResponse;
import com.azas.domain.family.dto.FamilyGuardianResponse;
import com.azas.domain.family.dto.FamilyInvitationAcceptRequest;
import com.azas.domain.family.dto.FamilyInvitationAcceptResponse;
import com.azas.domain.family.dto.FamilyInvitationChildResponse;
import com.azas.domain.family.dto.FamilyInvitationCreateRequest;
import com.azas.domain.family.dto.FamilyInvitationCreateResponse;
import com.azas.domain.family.dto.FamilyInvitationInfoProjection;
import com.azas.domain.family.dto.FamilyInvitationInfoResponse;
import com.azas.domain.family.dto.FamilyInvitationInsertCommand;
import com.azas.domain.family.mapper.FamilyMapper;
import com.azas.domain.member.entity.Member;
import com.azas.domain.member.entity.MemberStatus;
import com.azas.domain.member.entity.MemberType;
import com.azas.domain.member.mapper.MemberMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FamilyServiceImpl implements FamilyService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int INVITATION_TOKEN_BYTE_LENGTH = 32;

    private final FamilyMapper familyMapper;
    private final TokenHashEncoder tokenHashEncoder;
    private final FamilyInvitationStore familyInvitationStore;
    private final ParentInviteMapper parentInviteMapper;
    private final ChildInviteMapper childInviteMapper;
    private final MemberMapper memberMapper;

    @Value("${FAMILY_INVITATION_URL_BASE:http://localhost:5173/family-invitations}")
    private String familyInvitationUrlBase;

    @Value("${FAMILY_INVITATION_SHARE_URL_BASE:http://localhost:8080/family-invitations}")
    private String familyInvitationShareUrlBase;

    @Value("${FAMILY_INVITATION_DEFAULT_EXPIRE_HOURS:24}")
    private int defaultExpirationHours;

    @Override
    @Transactional(readOnly = true)
    public FamilyGuardianListResponse getFamilyMembers(Long memberId, Long childId) {
        validateChildAccess(memberId, childId);

        List<FamilyGuardianResponse> items = familyMapper.findFamilyMembers(
                childId,
                memberId
        );

        return new FamilyGuardianListResponse(items);
    }

    @Override
    @Transactional(readOnly = true)
    public ChildMemberLinkResponse getChildMemberLink(Long memberId, Long childId) {
        validateChildAccess(memberId, childId);

        ChildMemberLinkResponse response =
                familyMapper.findChildMemberLinkByChildId(childId);

        if (response == null) {
            throw new BusinessException(ErrorCode.CHILD_NOT_FOUND);
        }

        return response;
    }


    @Override
    @Transactional
    public FamilyInvitationCreateResponse createParentFamilyInvitation(
            Long memberId,
            FamilyInvitationCreateRequest request
    ) {
        List<FamilyInvitationChildResponse> invitationChildren =
                familyMapper.findActiveChildrenManagedByMember(memberId);

        if (invitationChildren.isEmpty()) {
            throw new BusinessException(ErrorCode.CHILD_NOT_FOUND);
        }

        return createFamilyInvitation(
                memberId,
                invitationChildren.get(0).getChildId(),
                FamilyInviteeType.PARENT,
                invitationChildren,
                request
        );
    }

    @Override
    @Transactional
    public FamilyInvitationCreateResponse createChildFamilyInvitation(
            Long memberId,
            Long childId,
            FamilyInvitationCreateRequest request
    ) {
        Long lockedChildId = familyMapper.lockActiveChild(childId);

        if (lockedChildId == null) {
            throw new BusinessException(ErrorCode.CHILD_NOT_FOUND);
        }

        if (familyMapper.countChildAccess(childId, memberId) == 0) {
            throw new BusinessException(ErrorCode.CHILD_ACCESS_DENIED);
        }

        ChildMemberLinkResponse memberLink =
                familyMapper.findChildMemberLinkByChildId(childId);

        if (memberLink != null && Boolean.TRUE.equals(memberLink.getLinked())) {
            throw new BusinessException(
                    ErrorCode.FAMILY_MEMBER_ALREADY_LINKED
            );
        }

        return createFamilyInvitation(
                memberId,
                childId,
                FamilyInviteeType.CHILD,
                List.of(new FamilyInvitationChildResponse(
                        childId,
                        findActiveChildName(childId)
                )),
                request
        );
    }

    private FamilyInvitationCreateResponse createFamilyInvitation(
            Long memberId,
            Long childId,
            FamilyInviteeType inviteeType,
            List<FamilyInvitationChildResponse> invitationChildren,
            FamilyInvitationCreateRequest request
    ) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        familyMapper.expirePendingFamilyInvitations(
                childId,
                memberId,
                inviteeType,
                now
        );

        // Invite tokens are stored only as hashes, so an existing raw URL cannot
        // be recovered safely. Reissuing invalidates any usable prior link and
        // gives the requester a fresh URL that can be displayed immediately.
        familyMapper.expireUsableFamilyInvitations(
                childId,
                memberId,
                inviteeType,
                now
        );

        int expirationHours = request == null || request.getExpiresInHours() == null
                ? defaultExpirationHours
                : request.getExpiresInHours();

        LocalDateTime expiresAt = now.plusHours(expirationHours);
        String inviteToken = createSecureInvitationToken();

        FamilyInvitationInsertCommand command =
                new FamilyInvitationInsertCommand(
                        null,
                        childId,
                        memberId,
                        inviteeType,
                        tokenHashEncoder.encode(inviteToken),
                        expiresAt
                );

        if (familyMapper.insertFamilyInvitation(command) != 1) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        if (familyMapper.insertFamilyInvitationChildren(
                command.getFamilyInvitationId(),
                invitationChildren
        ) != invitationChildren.size()) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        return new FamilyInvitationCreateResponse(
                command.getFamilyInvitationId(),
                childId,
                invitationChildren,
                invitationChildren.size(),
                inviteeType,
                inviteToken,
                buildInvitationShareUrl(inviteToken),
                FamilyInvitationStatus.PENDING,
                toInstant(expiresAt),
                toInstant(now)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public FamilyInvitationInfoResponse getFamilyInvitationInfo(
            String inviteToken
    ) {
        FamilyInvitationInfoProjection invitation =
                familyMapper.findFamilyInvitationInfoByTokenHash(
                        tokenHashEncoder.encode(inviteToken)
                );

        if (invitation == null) {
            throw new BusinessException(
                    ErrorCode.FAMILY_INVITATION_NOT_FOUND
            );
        }

        if (!isInvitationUsable(
                invitation.getStatus(),
                invitation.getExpiresAt(),
                LocalDateTime.now(ZoneOffset.UTC)
        )) {
            throw new BusinessException(ErrorCode.FAMILY_INVITATION_GONE);
        }

        List<FamilyInvitationChildResponse> invitationChildren =
                familyMapper.findInvitationChildren(
                        invitation.getFamilyInvitationId()
                );

        return new FamilyInvitationInfoResponse(
                invitation.getChildName(),
                invitationChildren,
                invitationChildren.size(),
                invitation.getInviterName(),
                invitation.getInviteeType(),
                invitation.getStatus(),
                toInstant(invitation.getExpiresAt())
        );
    }

    @Override
    @Transactional
    public FamilyInvitationAcceptResponse acceptFamilyInvitation(
            Long memberId,
            String inviteToken,
            FamilyInvitationAcceptRequest request
    ) {
        LocalDateTime acceptedAt = LocalDateTime.now(ZoneOffset.UTC);

        FamilyInvitation invitation = familyInvitationStore
                .findByInviteTokenHash(tokenHashEncoder.encode(inviteToken))
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.FAMILY_INVITATION_NOT_FOUND
                ));

        if (!isInvitationUsable(
                invitation.getStatus(),
                invitation.getExpiresAt(),
                acceptedAt
        )) {
            throw new BusinessException(ErrorCode.FAMILY_INVITATION_GONE);
        }

        Member member = memberMapper.findById(memberId);

        if (member == null || member.getStatus() != MemberStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.INVALID_ACCESS_TOKEN);
        }

        Child child = findActiveChild(invitation);
        List<FamilyInvitationChildResponse> invitationChildren =
                familyMapper.findInvitationChildren(
                        invitation.getFamilyInvitationId()
                );

        if (invitationChildren.isEmpty()) {
            throw new BusinessException(ErrorCode.CHILD_NOT_FOUND);
        }
        RelationType relationType = request == null
                ? null
                : request.getRelationType();

        if (invitation.getInviteeType() == FamilyInviteeType.PARENT) {
            acceptParentInvitation(invitationChildren, member, relationType);
        } else {
            acceptChildInvitation(child, member, relationType);
        }

        boolean accepted = familyInvitationStore.acceptIfPending(
                invitation.getFamilyInvitationId(),
                memberId,
                relationType,
                acceptedAt
        );

        if (!accepted) {
            throw new BusinessException(ErrorCode.FAMILY_INVITATION_GONE);
        }

        return new FamilyInvitationAcceptResponse(
                invitation.getFamilyInvitationId(),
                FamilyInvitationStatus.ACCEPTED,
                invitation.getInviteeType(),
                toInstant(acceptedAt),
                new FamilyInvitationChildResponse(
                        child.getChildId(),
                        child.getName()
                ),
                invitationChildren,
                invitationChildren.size(),
                relationType
        );
    }

    private void acceptParentInvitation(
            List<FamilyInvitationChildResponse> invitationChildren,
            Member member,
            RelationType relationType
    ) {
        if (relationType == null) {
            throw new BusinessException(
                    ErrorCode.FAMILY_INVITATION_RELATION_TYPE_REQUIRED
            );
        }

        if (member.getMemberType() != MemberType.PARENT) {
            throw new BusinessException(ErrorCode.MEMBER_TYPE_CONFLICT);
        }

        List<Long> childIds = invitationChildren.stream()
                .map(FamilyInvitationChildResponse::getChildId)
                .collect(Collectors.toList());

        for (Long childId : childIds) {
            if (parentInviteMapper.countChildParentRelation(
                    childId,
                    member.getMemberId()
            ) > 0) {
                throw new BusinessException(
                        ErrorCode.FAMILY_MEMBER_ALREADY_LINKED
                );
            }
        }

        try {
            int insertedCount = parentInviteMapper.insertChildParentRelations(
                    childIds,
                    member.getMemberId(),
                    relationType
            );

            if (insertedCount != childIds.size()) {
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
            }
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(
                    ErrorCode.FAMILY_MEMBER_ALREADY_LINKED
            );
        }
    }

    private void acceptChildInvitation(
            Child child,
            Member member,
            RelationType relationType
    ) {
        if (relationType != null) {
            throw new BusinessException(ErrorCode.BADREQUEST);
        }

        if (member.getMemberType() != MemberType.CHILD) {
            throw new BusinessException(ErrorCode.MEMBER_TYPE_CONFLICT);
        }

        if (child.getMemberId() != null
                || childInviteMapper.findByMemberId(member.getMemberId()) != null) {
            throw new BusinessException(
                    ErrorCode.FAMILY_MEMBER_ALREADY_LINKED
            );
        }

        if (childInviteMapper.linkMemberIfUnlinked(
                child.getChildId(),
                member.getMemberId()
        ) != 1) {
            throw new BusinessException(
                    ErrorCode.FAMILY_MEMBER_ALREADY_LINKED
            );
        }
    }

    private Child findActiveChild(FamilyInvitation invitation) {
        Child child = invitation.getInviteeType() == FamilyInviteeType.PARENT
                ? parentInviteMapper.findActiveChildById(invitation.getChildId())
                : childInviteMapper.findActiveById(invitation.getChildId());

        if (child == null) {
            throw new BusinessException(ErrorCode.CHILD_NOT_FOUND);
        }

        return child;
    }

    private String findActiveChildName(Long childId) {
        Child child = parentInviteMapper.findActiveChildById(childId);

        if (child == null) {
            throw new BusinessException(ErrorCode.CHILD_NOT_FOUND);
        }

        return child.getName();
    }

    private void validateChildAccess(Long memberId, Long childId) {
        if (familyMapper.countChildAccess(childId, memberId) == 0) {
            throw new BusinessException(ErrorCode.CHILD_NOT_FOUND);
        }
    }


    private boolean isInvitationUsable(
            FamilyInvitationStatus status,
            LocalDateTime expiresAt,
            LocalDateTime now
    ) {
        return status == FamilyInvitationStatus.PENDING
                && expiresAt != null
                && expiresAt.isAfter(now);
    }

    private String createSecureInvitationToken() {
        byte[] tokenBytes = new byte[INVITATION_TOKEN_BYTE_LENGTH];
        SECURE_RANDOM.nextBytes(tokenBytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(tokenBytes);
    }

    private String buildInvitationShareUrl(String inviteToken) {
        return familyInvitationShareUrlBase.replaceAll("/+$", "")
                + "/"
                + inviteToken
                + "/share";
    }

    private Instant toInstant(LocalDateTime dateTime) {
        return dateTime.toInstant(ZoneOffset.UTC);
    }
}
