package com.azas.domain.auth.service;

import com.azas.domain.auth.dto.AuthTokenPair;
import com.azas.domain.auth.dto.OAuthMemberResult;
import com.azas.domain.auth.dto.OAuthProfile;
import com.azas.domain.auth.dto.ParentInviteOAuthResult;
import com.azas.domain.auth.entity.FamilyInvitation;
import com.azas.domain.auth.entity.FamilyInviteeType;
import com.azas.domain.auth.mapper.ParentInviteMapper;
import com.azas.domain.child.entity.Child;
import com.azas.domain.child.entity.RelationType;
import com.azas.domain.member.entity.Member;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class ParentInviteAcceptanceService {

    private final TokenHashEncoder tokenHashEncoder;
    private final FamilyInvitationStore familyInvitationStore;
    private final OAuthMemberService oauthMemberService;
    private final ParentInviteMapper parentInviteMapper;
    private final AuthTokenService authTokenService;

    @Transactional
    public ParentInviteOAuthResult accept(
            String inviteToken,
            RelationType relationType,
            OAuthProfile profile
    ) {
        LocalDateTime acceptedAt =
                LocalDateTime.now(ZoneOffset.UTC);

        String inviteTokenHash =
                tokenHashEncoder.encode(inviteToken);

        FamilyInvitation invitation =
                familyInvitationStore
                        .findByInviteTokenHash(inviteTokenHash)
                        .filter(value -> value.isUsableFor(
                                FamilyInviteeType.PARENT,
                                acceptedAt
                        ))
                        .orElseThrow(
                                this::invalidInvitation
                        );

        Child child =
                parentInviteMapper.findActiveChildById(
                        invitation.getChildId()
                );

        if (child == null) {
            throw invalidInvitation();
        }

        OAuthMemberResult memberResult =
                oauthMemberService.findOrCreateParent(profile);

        Member member = memberResult.getMember();

        validateRelationNotExists(
                child.getChildId(),
                member.getMemberId()
        );

        insertRelation(
                child.getChildId(),
                member.getMemberId(),
                relationType
        );

        boolean invitationAccepted =
                familyInvitationStore.acceptIfPending(
                        invitation.getFamilyInvitationId(),
                        member.getMemberId(),
                        relationType,
                        acceptedAt
                );

        if (!invitationAccepted) {
            throw invalidInvitation();
        }

        AuthTokenPair tokenPair =
                authTokenService.issue(member);

        return new ParentInviteOAuthResult(
                tokenPair,
                member,
                memberResult.isNewMember(),
                child.getChildId(),
                child.getName(),
                relationType,
                invitation.getFamilyInvitationId(),
                acceptedAt
        );
    }

    private void validateRelationNotExists(
            Long childId,
            Long memberId
    ) {
        int relationCount =
                parentInviteMapper.countChildParentRelation(
                        childId,
                        memberId
                );

        if (relationCount > 0) {
            throw memberAlreadyLinked();
        }
    }

    private void insertRelation(
            Long childId,
            Long memberId,
            RelationType relationType
    ) {
        try {
            int insertedCount =
                    parentInviteMapper
                            .insertChildParentRelation(
                                    childId,
                                    memberId,
                                    relationType
                            );

            if (insertedCount != 1) {
                throw new BusinessException(
                        ErrorCode.INTERNAL_SERVER_ERROR
                );
            }
        } catch (DuplicateKeyException exception) {
            // 사전 조회 이후 동시 요청이 들어와도 DB UNIQUE 제약으로 중복 관계를 막는다.
            throw memberAlreadyLinked();
        }
    }

    private BusinessException invalidInvitation() {
        return new BusinessException(
                ErrorCode.INVALID_FAMILY_INVITATION
        );
    }

    private BusinessException memberAlreadyLinked() {
        return new BusinessException(
                ErrorCode.FAMILY_MEMBER_ALREADY_LINKED
        );
    }
}