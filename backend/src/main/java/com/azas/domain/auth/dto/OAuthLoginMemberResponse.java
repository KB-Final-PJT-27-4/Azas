package com.azas.domain.auth.dto;

import com.azas.domain.member.entity.Member;
import com.azas.domain.member.entity.MemberType;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

@ApiModel(description = "소셜 로그인 회원 정보")
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class OAuthLoginMemberResponse {

    @ApiModelProperty(
            value = "회원 ID",
            required = true,
            example = "1"
    )
    @JsonProperty("member_id")
    private final Long memberId;

    @ApiModelProperty(
            value = "소셜 제공자에서 받은 회원 이메일",
            required = true,
            example = "parent@example.com"
    )
    private final String email;

    @ApiModelProperty(
            value = "소셜 제공자에서 받은 회원 이름",
            required = true,
            example = "김하나"
    )
    private final String name;

    @ApiModelProperty(
            value = "프로필 이미지 URL. 등록되지 않은 경우 null",
            example = "https://example.com/profile.png"
    )
    @JsonProperty("profile_image_url")
    private final String profileImageUrl;

    @ApiModelProperty(
            value = "회원 유형",
            required = true,
            allowableValues = "PARENT,CHILD,ADMIN",
            example = "PARENT"
    )
    @JsonProperty("member_type")
    private final MemberType memberType;

    @ApiModelProperty(
            value = "생년월일. 등록되지 않은 경우 null",
            example = "2015-03-20"
    )
    @JsonProperty("birth_date")
    private final LocalDate birthDate;

    @ApiModelProperty(
            value = "휴대폰 번호. 인증되지 않은 경우 null",
            example = "01012345678"
    )
    @JsonProperty("phone_number")
    private final String phoneNumber;

    @ApiModelProperty(
            value = "휴대폰 인증 완료 여부",
            required = true,
            example = "false"
    )
    @JsonProperty("phone_verified")
    private final boolean phoneVerified;

    @ApiModelProperty(
            value = "휴대폰 인증 완료 시각. 인증되지 않은 경우 null",
            example = "2026-08-02T09:00:00Z"
    )
    @JsonProperty("phone_verified_at")
    private final Instant phoneVerifiedAt;

    @ApiModelProperty(
            value = "회원 생성 시각",
            required = true,
            example = "2026-08-02T09:00:00Z"
    )
    @JsonProperty("created_at")
    private final Instant createdAt;

    public static OAuthLoginMemberResponse from(
            Member member
    ) {
        Instant phoneVerifiedAt =
                member.getPhoneVerifiedAt() == null
                        ? null
                        : member.getPhoneVerifiedAt()
                                .toInstant(ZoneOffset.UTC);

        Instant createdAt =
                member.getCreatedAt() == null
                        ? null
                        : member.getCreatedAt()
                                .toInstant(ZoneOffset.UTC);

        return new OAuthLoginMemberResponse(
                member.getMemberId(),
                member.getEmail(),
                member.getName(),
                member.getProfileImageUrl(),
                member.getMemberType(),
                member.getBirthDate(),
                null,
                phoneVerifiedAt != null,
                phoneVerifiedAt,
                createdAt
        );
    }
}
