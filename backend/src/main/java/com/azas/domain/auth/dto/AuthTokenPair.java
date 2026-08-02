package com.azas.domain.auth.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AuthTokenPair {

    private final String accessToken;
    private final String refreshToken;
    private final long accessTokenExpiresInSeconds;
}
