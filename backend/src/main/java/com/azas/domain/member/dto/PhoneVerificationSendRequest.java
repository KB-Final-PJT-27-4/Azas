package com.azas.domain.member.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@ApiModel(description = "휴대폰 인증번호 발송 요청")
@Getter
@NoArgsConstructor
public class PhoneVerificationSendRequest {

    @ApiModelProperty(
            value = "인증번호를 받을 휴대폰 번호",
            required = true,
            example = "010-1234-5678"
    )
    @NotBlank
    @JsonProperty("phone_number")
    private String phoneNumber;
}