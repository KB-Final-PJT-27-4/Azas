package com.azas.domain.member.dto;

import com.azas.domain.auth.entity.SocialAccount;
import com.azas.domain.member.entity.Member;
import lombok.Getter;

import java.util.List;

@Getter
public final class MemberProfileResult {

    private final Member member;
    private final List<SocialAccount> socialAccounts;

    public MemberProfileResult(
            Member member,
            List<SocialAccount> socialAccounts
    ) {
        this.member = member;
        this.socialAccounts = List.copyOf(
                socialAccounts
        );
    }
}