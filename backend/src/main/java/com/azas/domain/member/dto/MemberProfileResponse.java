package com.azas.domain.member.dto;

import com.azas.domain.member.entity.Member;
import com.azas.domain.member.entity.MemberStatus;
import com.azas.domain.member.entity.MemberType;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@ApiModel(description = "내 회원 정보")
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MemberProfileResponse {

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
            value = "회원 상태",
            required = true,
            allowableValues = "ACTIVE,WITHDRAWN",
            example = "ACTIVE"
    )
    private final MemberStatus status;

    @ApiModelProperty(
            value = "생년월일. 등록되지 않은 경우 null",
            example = "1992-04-15"
    )
    @JsonProperty("birth_date")
    private final LocalDate birthDate;

    @ApiModelProperty(
            value = "마스킹된 휴대폰 번호. 등록되지 않은 경우 null",
            example = "010-****-5678"
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
            value = "휴대폰 인증 완료 시각. 미인증인 경우 null",
            example = "2026-07-24T01:00:00Z"
    )
    @JsonProperty("phone_verified_at")
    private final Instant phoneVerifiedAt;

    @ApiModelProperty(
            value = "연결된 소셜 계정 목록",
            required = true
    )
    @JsonProperty("social_accounts")
    private final List<MemberSocialAccountResponse> socialAccounts;

    @ApiModelProperty(
            value = "회원 생성 시각",
            required = true,
            example = "2026-07-23T03:00:00Z"
    )
    @JsonProperty("created_at")
    private final Instant createdAt;

    @ApiModelProperty(
            value = "회원 수정 시각",
            required = true,
            example = "2026-07-24T01:00:00Z"
    )
    @JsonProperty("updated_at")
    private final Instant updatedAt;

    public static MemberProfileResponse from(
            MemberProfileResult result
    ) {
        Member member = result.getMember();

        Instant phoneVerifiedAt =
                toInstant(member.getPhoneVerifiedAt());

        List<MemberSocialAccountResponse> socialAccounts =
                result.getSocialAccounts()
                        .stream()
                        .map(MemberSocialAccountResponse::from)
                        .toList();

        String phoneNumber =
                result.getMaskedPhoneNumber();

        return new MemberProfileResponse(
                member.getMemberId(),
                member.getEmail(),
                member.getName(),
                member.getProfileImageUrl(),
                member.getMemberType(),
                member.getStatus(),
                member.getBirthDate(),
                phoneNumber,
                phoneVerifiedAt != null,
                phoneVerifiedAt,
                socialAccounts,
                toInstant(member.getCreatedAt()),
                toInstant(member.getUpdatedAt())
        );
    }

    private static Instant toInstant(
            LocalDateTime dateTime
    ) {
        return dateTime == null
                ? null
                : dateTime.toInstant(ZoneOffset.UTC);
    }
}