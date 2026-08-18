package com.azas.domain.member.service;

import com.azas.domain.member.dto.MemberProfileUpdateCommand;
import com.azas.domain.member.entity.Member;
import com.azas.domain.member.entity.MemberStatus;
import com.azas.domain.member.entity.PhoneVerification;
import com.azas.domain.member.mapper.MemberMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class MemberProfileUpdateService {

    private static final int
            MAX_PROFILE_IMAGE_URL_LENGTH = 1000;

    private final MemberMapper memberMapper;
    private final PhoneVerificationStore
            phoneVerificationStore;
    private final PhoneVerificationHasher
            phoneVerificationHasher;

    @Transactional
    public void updateMyProfile(
            long memberId,
            MemberProfileUpdateCommand command
    ) {
        validateCommand(command);

        Member member = findActiveMember(memberId);

        applyBirthDate(
                member,
                command
        );

        applyProfileImageUrl(
                member,
                command
        );

        applyVerifiedPhoneNumber(
                member,
                memberId,
                command
        );

        updateMember(member);
    }

    private void validateCommand(
            MemberProfileUpdateCommand command
    ) {
        if (
                command == null
                        || !command.hasAnyUpdate()
        ) {
            throw invalidRequest();
        }

        if (
                command.isBirthDateProvided()
                        && command.getBirthDate() != null
                        && command.getBirthDate().isAfter(
                        LocalDate.now(ZoneOffset.UTC)
                )
        ) {
            throw invalidRequest();
        }

        if (
                command.isPhoneVerificationTokenProvided()
                        && (
                        command.getPhoneVerificationToken()
                                == null
                                || command
                                .getPhoneVerificationToken()
                                .isBlank()
                )
        ) {
            throw invalidRequest();
        }
    }

    private Member findActiveMember(long memberId) {
        Member member = memberMapper.findById(memberId);

        if (member == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_ACCESS_TOKEN
            );
        }

        if (member.getStatus() == MemberStatus.WITHDRAWN) {
            throw new BusinessException(
                    ErrorCode.WITHDRAWN_MEMBER
            );
        }

        return member;
    }

    private void applyBirthDate(
            Member member,
            MemberProfileUpdateCommand command
    ) {
        if (!command.isBirthDateProvided()) {
            return;
        }

        member.changeBirthDate(
                command.getBirthDate()
        );
    }

    private void applyProfileImageUrl(
            Member member,
            MemberProfileUpdateCommand command
    ) {
        if (!command.isProfileImageUrlProvided()) {
            return;
        }

        member.changeProfileImageUrl(
                validateProfileImageUrl(
                        command.getProfileImageUrl()
                )
        );
    }

    private String validateProfileImageUrl(
            String profileImageUrl
    ) {
        if (profileImageUrl == null) {
            return null;
        }

        String normalizedUrl =
                profileImageUrl.trim();

        if (
                normalizedUrl.isEmpty()
                        || normalizedUrl.length()
                        > MAX_PROFILE_IMAGE_URL_LENGTH
        ) {
            throw invalidRequest();
        }

        try {
            URI uri = new URI(normalizedUrl);

            String scheme = uri.getScheme();

            boolean supportedScheme =
                    scheme != null
                            && (
                            scheme.toLowerCase(Locale.ROOT)
                                    .equals("http")
                                    || scheme
                                    .toLowerCase(Locale.ROOT)
                                    .equals("https")
                    );

            if (
                    !supportedScheme
                            || uri.getHost() == null
            ) {
                throw invalidRequest();
            }

            return normalizedUrl;
        } catch (URISyntaxException exception) {
            throw invalidRequest(exception);
        }
    }

    private void applyVerifiedPhoneNumber(
            Member member,
            long memberId,
            MemberProfileUpdateCommand command
    ) {
        if (
                !command
                        .isPhoneVerificationTokenProvided()
        ) {
            return;
        }

        LocalDateTime now =
                LocalDateTime.now(ZoneOffset.UTC);

        String verificationTokenHash =
                phoneVerificationHasher
                        .hashVerificationToken(
                                command
                                        .getPhoneVerificationToken()
                        );

        PhoneVerification phoneVerification =
                phoneVerificationStore
                        .findByVerificationTokenHash(
                                verificationTokenHash
                        )
                        .orElseThrow(
                                this::invalidVerificationToken
                        );

        validateVerificationToken(
                phoneVerification,
                memberId,
                now
        );

        boolean consumed =
                phoneVerificationStore
                        .consumeVerificationTokenIfUsable(
                                phoneVerification
                                        .getPhoneVerificationId(),
                                memberId,
                                verificationTokenHash,
                                now
                        );

        if (!consumed) {
            throw invalidVerificationToken();
        }

        member.applyVerifiedPhoneNumber(
                phoneVerification
                        .getPhoneNumberCiphertext(),
                phoneVerification
                        .getPhoneNumberHash(),
                now
        );
    }

    private void validateVerificationToken(
            PhoneVerification phoneVerification,
            long memberId,
            LocalDateTime now
    ) {
        if (
                phoneVerification.getMemberId() == null
                        || phoneVerification.getMemberId()
                        != memberId
                        || !phoneVerification
                        .hasUsableVerificationTokenAt(now)
        ) {
            throw invalidVerificationToken();
        }
    }

    private void updateMember(Member member) {
        try {
            int updatedCount =
                    memberMapper.updateProfile(member);

            if (updatedCount != 1) {
                throw new BusinessException(
                        ErrorCode.WITHDRAWN_MEMBER
                );
            }
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(
                    ErrorCode.PHONE_NUMBER_ALREADY_IN_USE,
                    exception
            );
        }
    }

    private BusinessException invalidRequest() {
        return new BusinessException(
                ErrorCode.BADREQUEST
        );
    }

    private BusinessException invalidRequest(
            Throwable cause
    ) {
        return new BusinessException(
                ErrorCode.BADREQUEST,
                cause
        );
    }

    private BusinessException
    invalidVerificationToken() {
        return new BusinessException(
                ErrorCode
                        .INVALID_PHONE_VERIFICATION_TOKEN
        );
    }
}