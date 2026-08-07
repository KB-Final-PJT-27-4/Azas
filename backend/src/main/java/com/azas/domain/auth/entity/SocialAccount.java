package com.azas.domain.auth.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SocialAccount {

    private Long socialAccountId;
    private Long memberId;
    private OAuthProvider provider;
    private String providerSubject;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static SocialAccount create(
            long memberId,
            OAuthProvider provider,
            String providerSubject
    ) {
        SocialAccount socialAccount = new SocialAccount();
        socialAccount.memberId = memberId;
        socialAccount.provider = provider;
        socialAccount.providerSubject = providerSubject;
        return socialAccount;
    }
}
