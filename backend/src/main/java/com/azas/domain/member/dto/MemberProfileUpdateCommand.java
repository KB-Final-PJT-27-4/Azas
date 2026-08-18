package com.azas.domain.member.dto;

import lombok.Getter;

import java.time.LocalDate;

@Getter
public final class MemberProfileUpdateCommand {

    private final boolean birthDateProvided;
    private final LocalDate birthDate;

    private final boolean profileImageUrlProvided;
    private final String profileImageUrl;

    private final boolean phoneVerificationTokenProvided;
    private final String phoneVerificationToken;

    public MemberProfileUpdateCommand(
            boolean birthDateProvided,
            LocalDate birthDate,
            boolean profileImageUrlProvided,
            String profileImageUrl,
            boolean phoneVerificationTokenProvided,
            String phoneVerificationToken
    ) {
        this.birthDateProvided = birthDateProvided;
        this.birthDate = birthDate;
        this.profileImageUrlProvided =
                profileImageUrlProvided;
        this.profileImageUrl = profileImageUrl;
        this.phoneVerificationTokenProvided =
                phoneVerificationTokenProvided;
        this.phoneVerificationToken =
                phoneVerificationToken;
    }

    public boolean hasAnyUpdate() {
        return birthDateProvided
                || profileImageUrlProvided
                || phoneVerificationTokenProvided;
    }
}