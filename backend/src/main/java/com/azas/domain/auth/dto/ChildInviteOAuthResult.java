package com.azas.domain.auth.dto;

import com.azas.domain.member.entity.Member;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class ChildInviteOAuthResult {

    private final AuthTokenPair tokenPair;
    private final Member member;
    private final boolean newMember;
    private final Long childId;
    private final String childName;
    private final Long familyInvitationId;
    private final LocalDateTime expiresAt;
}