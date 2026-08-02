package com.azas.domain.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class OAuthLoginResponse {

    private static final String TOKEN_TYPE = "Bearer";

    @JsonProperty("access_token")
    private final String accessToken;

    @JsonProperty("refresh_token")
    private final String refreshToken;

    @JsonProperty("token_type")
    private final String tokenType;

    @JsonProperty("expires_in")
    private final long expiresIn;

    @JsonProperty("is_new_member")
    private final boolean newMember;

    private final OAuthLoginMemberResponse member;

    public static OAuthLoginResponse from(
            OAuthLoginResult result
    ) {
        AuthTokenPair tokenPair =
                result.getTokenPair();

        return new OAuthLoginResponse(
                tokenPair.getAccessToken(),
                tokenPair.getRefreshToken(),
                TOKEN_TYPE,
                tokenPair.getAccessTokenExpiresInSeconds(),
                result.isNewMember(),
                OAuthLoginMemberResponse.from(
                        result.getMember()
                )
        );
    }
}
