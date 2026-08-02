package com.azas.domain.auth.dto;

import com.azas.domain.member.entity.Member;
import com.azas.domain.member.entity.MemberType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class OAuthLoginMemberResponse {

    @JsonProperty("member_id")
    private final Long memberId;

    private final String email;

    private final String name;

    @JsonProperty("profile_image_url")
    private final String profileImageUrl;

    @JsonProperty("member_type")
    private final MemberType memberType;

    @JsonProperty("birth_date")
    private final LocalDate birthDate;

    @JsonProperty("phone_number")
    private final String phoneNumber;

    @JsonProperty("phone_verified")
    private final boolean phoneVerified;

    @JsonProperty("phone_verified_at")
    private final Instant phoneVerifiedAt;

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
