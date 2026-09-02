package com.azas.domain.auth.mapper;

import com.azas.domain.auth.entity.RefreshToken;
import com.azas.domain.auth.service.RefreshTokenStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MyBatisRefreshTokenStore
        implements RefreshTokenStore {

    private final RefreshTokenMapper refreshTokenMapper;

    @Override
    public void save(RefreshToken refreshToken) {
        refreshTokenMapper.insert(refreshToken);
    }

    @Override
    public Optional<RefreshToken> findByTokenHash(
            String tokenHash
    ) {
        return Optional.ofNullable(
                refreshTokenMapper.findByTokenHash(
                        tokenHash
                )
        );
    }

    @Override
    public boolean revokeIfActive(
            String tokenHash,
            LocalDateTime revokedAt
    ) {
        return refreshTokenMapper.revokeIfActive(
                tokenHash,
                revokedAt
        ) == 1;
    }

    @Override
    public int revokeAllActiveByMemberId(
            long memberId,
            LocalDateTime revokedAt
    ) {
        return refreshTokenMapper
                .revokeAllActiveByMemberId(
                        memberId,
                        revokedAt
                );
    }
}