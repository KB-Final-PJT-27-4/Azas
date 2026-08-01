package com.azas.domain.auth.mapper;

import com.azas.domain.auth.entity.RefreshToken;
import com.azas.domain.auth.service.RefreshTokenStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MyBatisRefreshTokenStore implements RefreshTokenStore {

    private final RefreshTokenMapper refreshTokenMapper;

    @Override
    public void save(RefreshToken refreshToken) {
        refreshTokenMapper.insert(refreshToken);
    }
}
