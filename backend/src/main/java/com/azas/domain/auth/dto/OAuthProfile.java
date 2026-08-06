package com.azas.domain.auth.dto;

import com.azas.domain.auth.entity.OAuthProvider;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class OAuthProfile {

    private final OAuthProvider provider;
    private final String providerSubject;
    private final String email;
    private final String name;
    private final String profileImageUrl;
}
