package com.azas.domain.auth.service;

import com.azas.domain.auth.dto.OAuthProfile;
import com.azas.domain.auth.dto.ParentInviteOAuthResult;
import com.azas.domain.auth.entity.OAuthProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ParentInviteOAuthService {

    private final OAuthClientRegistry oauthClientRegistry;
    private final ParentInviteLoginService parentInviteLoginService;

    public ParentInviteOAuthResult login(
            String providerValue,
            String authorizationCode,
            String redirectUri,
            String inviteToken
    ) {
        OAuthProvider provider =
                OAuthProvider.from(providerValue);

        OAuthClient oauthClient =
                oauthClientRegistry.get(provider);

        OAuthProfile profile =
                oauthClient.fetchProfile(
                        authorizationCode,
                        redirectUri
                );

        return parentInviteLoginService.login(
                inviteToken,
                profile
        );
    }
}