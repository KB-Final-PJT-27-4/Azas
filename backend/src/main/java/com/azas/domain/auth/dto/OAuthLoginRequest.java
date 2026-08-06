package com.azas.domain.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@ApiModel(description = "소셜 로그인 요청")
@Getter
@NoArgsConstructor
public class OAuthLoginRequest {

    @ApiModelProperty(
            value = "소셜 제공자가 발급한 일회용 인가 코드",
            required = true,
            example = "authorization-code"
    )
    @NotBlank
    @JsonProperty("authorization_code")
    private String authorizationCode;

    @ApiModelProperty(
            value = "인가 코드 발급 요청에 사용한 Redirect URI",
            required = true,
            example = "http://localhost:5173/auth/kakao/callback"
    )
    @NotBlank
    @JsonProperty("redirect_uri")
    private String redirectUri;
}
