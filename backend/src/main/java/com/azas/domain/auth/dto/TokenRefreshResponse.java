package com.azas.domain.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@ApiModel(description = "Access Token 재발급 응답")
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class TokenRefreshResponse {

    private static final String TOKEN_TYPE = "Bearer";

    @ApiModelProperty(
            value = "새로 발급된 Access Token",
            required = true,
            example = "eyJhbGciOiJIUzI1NiJ9..."
    )
    @JsonProperty("access_token")
    private final String accessToken;

    @ApiModelProperty(
            value = "새로 발급된 Refresh Token",
            required = true,
            example = "N4mQ2sR8vX1kL6pT9cA3eF7hJ0uW5yZbD2gH8nKqM1E"
    )
    @JsonProperty("refresh_token")
    private final String refreshToken;

    @ApiModelProperty(
            value = "인증 방식",
            required = true,
            example = "Bearer"
    )
    @JsonProperty("token_type")
    private final String tokenType;

    @ApiModelProperty(
            value = "새 Access Token의 유효시간(초)",
            required = true,
            example = "3600"
    )
    @JsonProperty("expires_in")
    private final long expiresIn;

    public static TokenRefreshResponse from(
            AuthTokenPair tokenPair
    ) {
        return new TokenRefreshResponse(
                tokenPair.getAccessToken(),
                tokenPair.getRefreshToken(),
                TOKEN_TYPE,
                tokenPair.getAccessTokenExpiresInSeconds()
        );
    }
}
