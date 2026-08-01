package com.azas.domain.auth.dto;

import com.azas.domain.member.entity.Member;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class OAuthMemberResult {

    private final Member member;
    private final boolean newMember;
}
