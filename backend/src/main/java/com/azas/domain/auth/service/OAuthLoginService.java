package com.azas.domain.auth.service;

import com.azas.domain.auth.dto.AuthTokenPair;
import com.azas.domain.auth.dto.OAuthLoginRequest;
import com.azas.domain.auth.dto.OAuthLoginResult;
import com.azas.domain.auth.dto.OAuthMemberResult;
import com.azas.domain.auth.dto.OAuthProfile;
import com.azas.domain.auth.entity.OAuthProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OAuthLoginService {

    private final OAuthClientRegistry oauthClientRegistry;
    private final OAuthMemberService oauthMemberService;
    private final AuthTokenService authTokenService;

    public OAuthLoginResult login(
            String providerValue,
            OAuthLoginRequest request
    ) {
        OAuthProvider provider =
                OAuthProvider.from(providerValue);

        OAuthClient oauthClient =
                oauthClientRegistry.get(provider);

        OAuthProfile profile =
                oauthClient.fetchProfile(
                        request.getAuthorizationCode(),
                        request.getRedirectUri()
                );

        OAuthMemberResult memberResult =
                oauthMemberService.findOrCreate(profile);

        AuthTokenPair tokenPair =
                authTokenService.issue(
                        memberResult.getMember()
                );

        return new OAuthLoginResult(
                tokenPair,
                memberResult.getMember(),
                memberResult.isNewMember()
        );
    }
}
