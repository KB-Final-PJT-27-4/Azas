package com.azas.domain.member.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {
    private Long memberId;
    private String email;
    private String name;
    private String profileImageUrl;
    private MemberType memberType;
    private MemberStatus status;
    private LocalDate birthDate;
    private byte[] phoneNumberCiphertext;
    private String phoneNumberHash;
    private LocalDateTime phoneVerifiedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static Member createParent(
            String email,
            String name,
            String profileImageUrl
    ) {
        Member member = new Member();
        member.email = email;
        member.name = name;
        member.profileImageUrl = profileImageUrl;
        member.memberType = MemberType.PARENT;
        member.status = MemberStatus.ACTIVE;
        return member;
    }

    public static Member createChild(
            String email,
            String name,
            String profileImageUrl
    ) {
        Member member = new Member();
        member.email = email;
        member.name = name;
        member.profileImageUrl = profileImageUrl;
        member.memberType = MemberType.CHILD;
        member.status = MemberStatus.ACTIVE;
        return member;
    }

    public void changeBirthDate(
            LocalDate birthDate
    ) {
        this.birthDate = birthDate;
    }

    public void changeProfileImageUrl(
            String profileImageUrl
    ) {
        this.profileImageUrl = profileImageUrl;
    }

    public void applyVerifiedPhoneNumber(
            byte[] phoneNumberCiphertext,
            String phoneNumberHash,
            LocalDateTime phoneVerifiedAt
    ) {
        if (
                phoneNumberCiphertext == null
                        || phoneNumberHash == null
                        || phoneVerifiedAt == null
        ) {
            throw new IllegalArgumentException(
                    "인증된 휴대폰 정보가 필요합니다."
            );
        }

        this.phoneNumberCiphertext =
                phoneNumberCiphertext.clone();
        this.phoneNumberHash = phoneNumberHash;
        this.phoneVerifiedAt = phoneVerifiedAt;
    }
}
