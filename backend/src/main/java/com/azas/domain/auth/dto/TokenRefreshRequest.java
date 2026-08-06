package com.azas.domain.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@ApiModel(description = "Access Token 재발급 요청")
@Getter
@NoArgsConstructor
public class TokenRefreshRequest {

    @ApiModelProperty(
            value = "로그인 또는 이전 재발급 시 발급받은 Refresh Token",
            required = true,
            example = "K8jVb0fNq3xYw7mP2sR6aC9uE1dL4hZTg5iO0kWnXQA"
    )
    @NotBlank
    @JsonProperty("refresh_token")
    private String refreshToken;
}
