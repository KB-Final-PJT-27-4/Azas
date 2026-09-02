package com.azas.domain.member.dto;

import com.azas.domain.auth.entity.SocialAccount;
import com.azas.domain.member.entity.Member;
import lombok.Getter;

import java.util.List;

@Getter
public final class MemberProfileResult {

    private final Member member;
    private final List<SocialAccount> socialAccounts;
    private final String maskedPhoneNumber;

    public MemberProfileResult(
            Member member,
            List<SocialAccount> socialAccounts,
            String maskedPhoneNumber
    ) {
        this.member = member;
        this.socialAccounts = List.copyOf(
                socialAccounts
        );
        this.maskedPhoneNumber = maskedPhoneNumber;
    }
}