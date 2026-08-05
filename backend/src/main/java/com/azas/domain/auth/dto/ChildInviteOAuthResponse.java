package com.azas.domain.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@ApiModel(description = "자녀 초대코드 기반 소셜 로그인 응답")
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ChildInviteOAuthResponse {

    private static final String TOKEN_TYPE = "Bearer";

    @ApiModelProperty(
            value = "Azas API 요청에 사용하는 Access Token",
            required = true,
            example = "eyJhbGciOiJIUzI1NiJ9..."
    )
    @JsonProperty("access_token")
    private final String accessToken;

    @ApiModelProperty(
            value = "Access Token 재발급용 Refresh Token",
            required = true,
            example = "random-refresh-token"
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
            value = "로그인한 자녀 회원 정보",
            required = true
    )
    private final OAuthLoginMemberResponse member;

    @ApiModelProperty(
            value = "회원 계정과 연결된 자녀 정보",
            required = true
    )
    private final ChildInviteChildResponse child;

    @ApiModelProperty(
            value = "수락 완료된 가족 초대 정보",
            required = true
    )
    private final ChildInviteInvitationResponse invitation;

    public static ChildInviteOAuthResponse from(
            ChildInviteOAuthResult result
    ) {
        AuthTokenPair tokenPair =
                result.getTokenPair();

        return new ChildInviteOAuthResponse(
                tokenPair.getAccessToken(),
                tokenPair.getRefreshToken(),
                TOKEN_TYPE,
                tokenPair.getAccessTokenExpiresInSeconds(),
                result.isNewMember(),
                OAuthLoginMemberResponse.from(
                        result.getMember()
                ),
                ChildInviteChildResponse.from(result),
                ChildInviteInvitationResponse.from(result)
        );
    }
}