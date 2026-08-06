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

@ApiModel(description = "휴대폰 인증번호 발송 결과")
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class PhoneVerificationSendResponse {

    @ApiModelProperty(
            value = "휴대폰 인증 요청 ID",
            required = true,
            example = "1"
    )
    @JsonProperty("verification_id")
    private final long verificationId;

    @ApiModelProperty(
            value = "인증번호 만료 시각",
            required = true,
            example = "2026-08-06T03:03:00Z"
    )
    @JsonProperty("expires_at")
    private final Instant expiresAt;

    @ApiModelProperty(
            value = "재발송 가능 시각",
            required = true,
            example = "2026-08-06T03:01:00Z"
    )
    @JsonProperty("resend_available_at")
    private final Instant resendAvailableAt;

    public static PhoneVerificationSendResponse from(
            PhoneVerificationSendResult result
    ) {
        return new PhoneVerificationSendResponse(
                result.getVerificationId(),
                toInstant(result.getExpiresAt()),
                toInstant(result.getResendAvailableAt())
        );
    }

    private static Instant toInstant(
            LocalDateTime dateTime
    ) {
        return dateTime.toInstant(ZoneOffset.UTC);
    }
}