package com.azas.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class TokenLogoutService {

    private final TokenHashEncoder tokenHashEncoder;
    private final RefreshTokenStore refreshTokenStore;

    @Transactional
    public void logout(String rawRefreshToken) {
        String tokenHash =
                tokenHashEncoder.encode(rawRefreshToken);

        LocalDateTime revokedAt =
                LocalDateTime.now(ZoneOffset.UTC);

        // 이미 폐기됐거나 존재하지 않는 토큰도 로그아웃 성공으로 처리한다.
        refreshTokenStore.revokeIfActive(
                tokenHash,
                revokedAt
        );
    }
}