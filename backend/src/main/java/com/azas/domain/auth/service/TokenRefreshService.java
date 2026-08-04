package com.azas.domain.auth.service;

import com.azas.domain.auth.dto.AuthTokenPair;
import com.azas.domain.auth.entity.RefreshToken;
import com.azas.domain.member.entity.Member;
import com.azas.domain.member.entity.MemberStatus;
import com.azas.domain.member.mapper.MemberMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class TokenRefreshService {

    private final TokenHashEncoder tokenHashEncoder;
    private final RefreshTokenStore refreshTokenStore;
    private final MemberMapper memberMapper;
    private final AuthTokenService authTokenService;

    @Transactional
    public AuthTokenPair refresh(String rawRefreshToken) {
        String tokenHash =
                tokenHashEncoder.encode(rawRefreshToken);

        RefreshToken storedToken =
                refreshTokenStore
                        .findByTokenHash(tokenHash)
                        .orElseThrow(
                                this::invalidRefreshToken
                        );

        LocalDateTime now =
                LocalDateTime.now(ZoneOffset.UTC);

        if (!storedToken.isActiveAt(now)) {
            throw invalidRefreshToken();
        }

        Member member = memberMapper.findById(
                storedToken.getMemberId()
        );

        if (member == null) {
            throw invalidRefreshToken();
        }

        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw new BusinessException(
                    ErrorCode.WITHDRAWN_MEMBER
            );
        }

        boolean revoked =
                refreshTokenStore.revokeIfActive(
                        tokenHash,
                        now
                );

        if (!revoked) {
            throw invalidRefreshToken();
        }

        return authTokenService.issue(member);
    }

    private BusinessException invalidRefreshToken() {
        return new BusinessException(
                ErrorCode.INVALID_REFRESH_TOKEN
        );
    }
}
