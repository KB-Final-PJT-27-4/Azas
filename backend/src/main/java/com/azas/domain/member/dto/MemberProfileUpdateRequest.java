package com.azas.domain.member.dto;

import com.fasterxml.jackson.annotation.JsonSetter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDate;

@ApiModel(description = "내 회원 정보 수정 요청")
public final class MemberProfileUpdateRequest {

    @ApiModelProperty(
            value = "생년월일. null을 전달하면 기존 값을 삭제",
            example = "1992-04-15"
    )
    private LocalDate birthDate;

    @ApiModelProperty(
            value = "프로필 이미지 URL. null을 전달하면 기존 값을 삭제",
            example = "https://example.com/profile.png"
    )
    private String profileImageUrl;

    @ApiModelProperty(
            value = "휴대폰 인증번호 확인 API에서 발급받은 인증 토큰",
            example = "raw-phone-verification-token"
    )
    private String phoneVerificationToken;

    private boolean birthDateProvided;
    private boolean profileImageUrlProvided;
    private boolean phoneVerificationTokenProvided;

    @JsonSetter("birth_date")
    public void setBirthDate(LocalDate birthDate) {
        this.birthDateProvided = true;
        this.birthDate = birthDate;
    }

    @JsonSetter("profile_image_url")
    public void setProfileImageUrl(
            String profileImageUrl
    ) {
        this.profileImageUrlProvided = true;
        this.profileImageUrl = profileImageUrl;
    }

    @JsonSetter("phone_verification_token")
    public void setPhoneVerificationToken(
            String phoneVerificationToken
    ) {
        this.phoneVerificationTokenProvided = true;
        this.phoneVerificationToken =
                phoneVerificationToken;
    }

    public MemberProfileUpdateCommand toCommand() {
        return new MemberProfileUpdateCommand(
                birthDateProvided,
                birthDate,
                profileImageUrlProvided,
                profileImageUrl,
                phoneVerificationTokenProvided,
                phoneVerificationToken
        );
    }
}