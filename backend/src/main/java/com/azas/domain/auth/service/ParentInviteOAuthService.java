package com.azas.domain.auth.service;

import com.azas.domain.auth.dto.OAuthProfile;
import com.azas.domain.auth.dto.ParentInviteOAuthResult;
import com.azas.domain.auth.entity.OAuthProvider;
import com.azas.domain.child.entity.RelationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ParentInviteOAuthService {

    private final OAuthClientRegistry oauthClientRegistry;
    private final ParentInviteAcceptanceService
            parentInviteAcceptanceService;

    public ParentInviteOAuthResult login(
            String providerValue,
            String authorizationCode,
            String redirectUri,
            String inviteToken,
            RelationType relationType
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

        return parentInviteAcceptanceService.accept(
                inviteToken,
                relationType,
                profile
        );
    }
}