package com.azas.domain.auth.service;

import com.azas.domain.auth.dto.OAuthProfile;
import com.azas.domain.auth.entity.OAuthProvider;

public interface OAuthClient {

    OAuthProvider getProvider();

    OAuthProfile fetchProfile(
            String authorizationCode,
            String redirectUri
    );
}
