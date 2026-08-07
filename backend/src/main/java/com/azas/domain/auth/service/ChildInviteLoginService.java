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
public class ChildInviteLoginService {

    private final TokenHashEncoder tokenHashEncoder;
    private final FamilyInvitationStore familyInvitationStore;
    private final OAuthMemberService oauthMemberService;
    private final ChildInviteMapper childInviteMapper;
    private final AuthTokenService authTokenService;

    @Transactional
    public ChildInviteOAuthResult login(
            String inviteToken,
            OAuthProfile profile
    ) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        FamilyInvitation invitation =
                findUsableInvitation(inviteToken, now);

        Child child =
                childInviteMapper.findActiveById(
                        invitation.getChildId()
                );

        if (child == null) {
            throw invalidInvitation();
        }

        OAuthMemberResult memberResult =
                oauthMemberService.findOrCreateChild(profile);

        Member member = memberResult.getMember();

        AuthTokenPair tokenPair =
                authTokenService.issue(member);

        return new ChildInviteOAuthResult(
                tokenPair,
                member,
                memberResult.isNewMember(),
                child.getChildId(),
                child.getName(),
                invitation.getFamilyInvitationId(),
                invitation.getExpiresAt()
        );
    }

    private FamilyInvitation findUsableInvitation(
            String inviteToken,
            LocalDateTime now
    ) {
        String inviteTokenHash =
                tokenHashEncoder.encode(inviteToken);

        return familyInvitationStore
                .findByInviteTokenHash(inviteTokenHash)
                .filter(invitation -> invitation.isUsableFor(
                        FamilyInviteeType.CHILD,
                        now
                ))
                .orElseThrow(this::invalidInvitation);
    }

    private BusinessException invalidInvitation() {
        return new BusinessException(
                ErrorCode.INVALID_FAMILY_INVITATION
        );
    }
}