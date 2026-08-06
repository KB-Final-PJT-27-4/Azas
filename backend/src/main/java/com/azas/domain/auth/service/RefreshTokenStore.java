package com.azas.domain.auth.service;

import com.azas.domain.auth.entity.RefreshToken;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenStore {

    // 인증 로직이 MySQL이나 Redis 같은 저장 기술에 직접 의존하지 않게 한다.
    void save(RefreshToken refreshToken);

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    boolean revokeIfActive(
            String tokenHash,
            LocalDateTime revokedAt
    );
}
