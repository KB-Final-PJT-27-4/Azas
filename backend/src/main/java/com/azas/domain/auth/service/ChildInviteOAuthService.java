package com.azas.domain.auth.service;

import com.azas.domain.auth.dto.ChildInviteOAuthResult;
import com.azas.domain.auth.dto.OAuthProfile;
import com.azas.domain.auth.entity.OAuthProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChildInviteOAuthService {

    private final OAuthClientRegistry oauthClientRegistry;
    private final ChildInviteLoginService childInviteLoginService;

    public ChildInviteOAuthResult login(
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

        return childInviteLoginService.login(
                inviteToken,
                profile
        );
    }
}