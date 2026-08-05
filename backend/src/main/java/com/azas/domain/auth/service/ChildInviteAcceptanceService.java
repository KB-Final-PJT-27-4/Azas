package com.azas.domain.auth.service;

import com.azas.domain.auth.dto.AuthTokenPair;
import com.azas.domain.auth.dto.ChildInviteOAuthResult;
import com.azas.domain.auth.dto.OAuthMemberResult;
import com.azas.domain.auth.dto.OAuthProfile;
import com.azas.domain.auth.entity.FamilyInvitation;
import com.azas.domain.auth.entity.FamilyInviteeType;
import com.azas.domain.auth.mapper.ChildInviteMapper;
import com.azas.domain.child.entity.Child;
import com.azas.domain.member.entity.Member;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class ChildInviteAcceptanceService {

    private final TokenHashEncoder tokenHashEncoder;
    private final FamilyInvitationStore familyInvitationStore;
    private final OAuthMemberService oauthMemberService;
    private final ChildInviteMapper childInviteMapper;
    private final AuthTokenService authTokenService;

    @Transactional
    public ChildInviteOAuthResult accept(
            String inviteToken,
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
                                FamilyInviteeType.CHILD,
                                acceptedAt
                        ))
                        .orElseThrow(
                                this::invalidInvitation
                        );

        Child child =
                childInviteMapper.findActiveById(
                        invitation.getChildId()
                );

        if (child == null) {
            throw invalidInvitation();
        }

        if (child.getMemberId() != null) {
            throw memberAlreadyLinked();
        }

        OAuthMemberResult memberResult =
                oauthMemberService.findOrCreateChild(profile);

        Member member = memberResult.getMember();

        Child connectedChild =
                childInviteMapper.findByMemberId(
                        member.getMemberId()
                );

        if (connectedChild != null) {
            throw memberAlreadyLinked();
        }

        int linkedCount =
                childInviteMapper.linkMemberIfUnlinked(
                        child.getChildId(),
                        member.getMemberId()
                );

        if (linkedCount != 1) {
            throw memberAlreadyLinked();
        }

        boolean invitationAccepted =
                familyInvitationStore.acceptIfPending(
                        invitation.getFamilyInvitationId(),
                        member.getMemberId(),
                        null,
                        acceptedAt
                );

        if (!invitationAccepted) {
            throw invalidInvitation();
        }

        AuthTokenPair tokenPair =
                authTokenService.issue(member);

        return new ChildInviteOAuthResult(
                tokenPair,
                member,
                memberResult.isNewMember(),
                child.getChildId(),
                child.getName(),
                invitation.getFamilyInvitationId(),
                acceptedAt
        );
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