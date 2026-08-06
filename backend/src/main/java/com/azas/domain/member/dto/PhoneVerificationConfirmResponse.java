package com.azas.domain.member.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@ApiModel(description = "휴대폰 인증번호 확인 결과")
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class PhoneVerificationConfirmResponse {

    @ApiModelProperty(
            value = "휴대폰 인증 요청 ID",
            required = true,
            example = "1"
    )
    @JsonProperty("verification_id")
    private final long verificationId;

    @ApiModelProperty(
            value = "마스킹된 인증 휴대폰 번호",
            required = true,
            example = "010-****-5678"
    )
    @JsonProperty("phone_number")
    private final String phoneNumber;

    @ApiModelProperty(
            value = "인증 완료 시각",
            required = true,
            example = "2026-08-06T03:01:30Z"
    )
    @JsonProperty("verified_at")
    private final Instant verifiedAt;

    @ApiModelProperty(
            value = "회원 정보에 휴대폰 번호를 반영할 때 사용하는 일회용 인증 토큰",
            required = true,
            example = "nJ2qJ5R7sD9xK4pL8vM1cF6wT3aH0eYz"
    )
    @JsonProperty("phone_verification_token")
    private final String phoneVerificationToken;

    @ApiModelProperty(
            value = "휴대폰 인증 토큰 만료 시각",
            required = true,
            example = "2026-08-06T03:11:30Z"
    )
    @JsonProperty("token_expires_at")
    private final Instant tokenExpiresAt;

    public static PhoneVerificationConfirmResponse from(
            PhoneVerificationConfirmResult result
    ) {
        return new PhoneVerificationConfirmResponse(
                result.getVerificationId(),
                result.getMaskedPhoneNumber(),
                toInstant(result.getVerifiedAt()),
                result.getVerificationToken(),
                toInstant(result.getTokenExpiresAt())
        );
    }

    private static Instant toInstant(
            LocalDateTime dateTime
    ) {
        return dateTime.toInstant(ZoneOffset.UTC);
    }
}