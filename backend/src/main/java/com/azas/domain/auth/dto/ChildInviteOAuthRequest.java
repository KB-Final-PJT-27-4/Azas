package com.azas.domain.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@ApiModel(description = "자녀 초대코드 기반 소셜 로그인 요청")
@Getter
@NoArgsConstructor
public class ChildInviteOAuthRequest {

    @ApiModelProperty(
            value = "소셜 제공자가 발급한 일회용 인가 코드",
            required = true,
            example = "authorization-code"
    )
    @NotBlank
    @JsonProperty("authorization_code")
    private String authorizationCode;

    @ApiModelProperty(
            value = "인가 코드 발급 시 사용한 자녀 초대 전용 Redirect URI",
            required = true,
            example = "http://localhost:5173/auth/kakao/child-invite/callback"
    )
    @NotBlank
    @JsonProperty("redirect_uri")
    private String redirectUri;

    @ApiModelProperty(
            value = "가족 초대 링크에 포함된 원본 자녀 초대 토큰",
            required = true,
            example = "raw-child-invite-token"
    )
    @NotBlank
    @JsonProperty("invite_token")
    private String inviteToken;
}