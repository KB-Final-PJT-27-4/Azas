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
    public AllowanceRequestResponse requestAllowance(Long memberId, Long childId) {
        validateChildMemberAccess(memberId, childId);

        LocalDate requestMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate lastRequestMonth =
                familyMapper.findLastAllowanceRequestMonth(childId);

        if (requestMonth.equals(lastRequestMonth)) {
            throw new BusinessException(
                    ErrorCode.ALLOWANCE_REQUEST_ALREADY_EXISTS
            );
        }

        int updatedCount = familyMapper.updateAllowanceRequest(
                childId,
                requestMonth
        );

        if (updatedCount == 0) {
            throw new BusinessException(ErrorCode.CHILD_NOT_FOUND);
        }

        BigDecimal childAvailableAmount =
                familyMapper.findChildAvailableAmount(childId);

        return new AllowanceRequestResponse(
                childId,
                true,
                requestMonth,
                LocalDateTime.now(),
                childAvailableAmount,
                "용돈 요청이 등록되었습니다."
        );
    }

    @Override
    @Transactional
    public FamilyInvitationCreateResponse createFamilyInvitation(
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

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        familyMapper.expirePendingFamilyInvitations(
                childId,
                request.getInviteeType(),
                now
        );

        if (familyMapper.countUsableFamilyInvitations(
                childId,
                request.getInviteeType(),
                now
        ) > 0) {
            throw new BusinessException(
                    ErrorCode.FAMILY_INVITATION_ALREADY_EXISTS
            );
        }

        if (request.getInviteeType() == FamilyInviteeType.CHILD) {
            ChildMemberLinkResponse memberLink =
                    familyMapper.findChildMemberLinkByChildId(childId);

            if (memberLink != null && Boolean.TRUE.equals(memberLink.getLinked())) {
                throw new BusinessException(
                        ErrorCode.FAMILY_MEMBER_ALREADY_LINKED
                );
            }
        }

        int expirationHours = request.getExpiresInHours() == null
                ? defaultExpirationHours
                : request.getExpiresInHours();

        LocalDateTime expiresAt = now.plusHours(expirationHours);
        String inviteToken = createSecureInvitationToken();

        FamilyInvitationInsertCommand command =
                new FamilyInvitationInsertCommand(
                        null,
                        childId,
                        memberId,
                        request.getInviteeType(),
                        tokenHashEncoder.encode(inviteToken),
                        expiresAt
                );

        if (familyMapper.insertFamilyInvitation(command) != 1) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        return new FamilyInvitationCreateResponse(
                command.getFamilyInvitationId(),
                childId,
                request.getInviteeType(),
                inviteToken,
                buildInvitationUrl(inviteToken),
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

        return new FamilyInvitationInfoResponse(
                invitation.getChildName(),
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
        RelationType relationType = request == null
                ? null
                : request.getRelationType();

        if (invitation.getInviteeType() == FamilyInviteeType.PARENT) {
            acceptParentInvitation(child, member, relationType);
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
                relationType
        );
    }

    private void acceptParentInvitation(
            Child child,
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

        if (parentInviteMapper.countChildParentRelation(
                child.getChildId(),
                member.getMemberId()
        ) > 0) {
            throw new BusinessException(
                    ErrorCode.FAMILY_MEMBER_ALREADY_LINKED
            );
        }

        try {
            int insertedCount = parentInviteMapper.insertChildParentRelation(
                    child.getChildId(),
                    member.getMemberId(),
                    relationType
            );

            if (insertedCount != 1) {
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

    private void validateChildAccess(Long memberId, Long childId) {
        if (familyMapper.countChildAccess(childId, memberId) == 0) {
            throw new BusinessException(ErrorCode.CHILD_NOT_FOUND);
        }
    }

    private void validateChildMemberAccess(Long memberId, Long childId) {
        if (familyMapper.countChildMemberAccess(childId, memberId) == 0) {
            throw new BusinessException(ErrorCode.CHILD_ACCESS_DENIED);
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

    private String buildInvitationUrl(String inviteToken) {
        return familyInvitationUrlBase.replaceAll("/+$", "")
                + "/"
                + inviteToken;
    }

    private Instant toInstant(LocalDateTime dateTime) {
        return dateTime.toInstant(ZoneOffset.UTC);
    }
}