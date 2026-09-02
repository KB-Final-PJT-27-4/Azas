package com.azas.domain.member.dto;

import com.azas.domain.auth.entity.OAuthProvider;
import com.azas.domain.auth.entity.SocialAccount;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.time.ZoneOffset;

@ApiModel(description = "회원에게 연결된 소셜 계정")
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MemberSocialAccountResponse {

    @ApiModelProperty(
            value = "소셜 로그인 제공자",
            required = true,
            allowableValues = "GOOGLE,KAKAO",
            example = "GOOGLE"
    )
    private final OAuthProvider provider;

    @ApiModelProperty(
            value = "소셜 계정 연결 시각",
            required = true,
            example = "2026-07-23T03:00:00Z"
    )
    @JsonProperty("connected_at")
    private final Instant connectedAt;

    public static MemberSocialAccountResponse from(
            SocialAccount socialAccount
    ) {
        Instant connectedAt =
                socialAccount.getCreatedAt() == null
                        ? null
                        : socialAccount.getCreatedAt()
                        .toInstant(ZoneOffset.UTC);

        return new MemberSocialAccountResponse(
                socialAccount.getProvider(),
                connectedAt
        );
    }
}