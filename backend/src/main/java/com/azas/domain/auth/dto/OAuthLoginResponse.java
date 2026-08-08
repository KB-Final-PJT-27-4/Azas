package com.azas.domain.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@ApiModel(description = "소셜 로그인 응답")
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class OAuthLoginResponse {

    private static final String TOKEN_TYPE = "Bearer";

    @ApiModelProperty(
            value = "Azas API 요청에 사용하는 Access Token",
            required = true,
            example = "eyJhbGciOiJIUzI1NiJ9..."
    )
    @JsonProperty("access_token")
    private final String accessToken;

    @ApiModelProperty(
            value = "Access Token 재발급에 사용하는 Refresh Token",
            required = true,
            example = "eyJhbGciOiJIUzI1NiJ9..."
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
            value = "Access Token 만료까지 남은 시간(초)",
            required = true,
            example = "3600"
    )
    @JsonProperty("expires_in")
    private final long expiresIn;

    @ApiModelProperty(
            value = "이번 요청에서 새로 생성된 회원인지 여부",
            required = true,
            example = "true"
    )
    @JsonProperty("is_new_member")
    private final boolean newMember;

    @ApiModelProperty(
            value = "로그인한 회원 정보",
            required = true
    )
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
