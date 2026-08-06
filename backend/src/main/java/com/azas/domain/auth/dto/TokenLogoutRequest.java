package com.azas.domain.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@ApiModel(description = "로그아웃 요청")
@Getter
@NoArgsConstructor
public class TokenLogoutRequest {

    @ApiModelProperty(
            value = "현재 기기에서 사용 중인 Refresh Token",
            required = true,
            example = "K8jVb0fNq3xYw7mP2sR6aC9uE1dL4hZTg5iO0kWnXQA"
    )
    @NotBlank
    @JsonProperty("refresh_token")
    private String refreshToken;
}