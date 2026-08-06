package com.azas.domain.member.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@ApiModel(description = "휴대폰 인증번호 확인 요청")
@Getter
@NoArgsConstructor
public class PhoneVerificationConfirmRequest {

    @ApiModelProperty(
            value = "SMS로 받은 6자리 인증번호",
            required = true,
            example = "123456"
    )
    @NotBlank
    @JsonProperty("verification_code")
    private String verificationCode;
}