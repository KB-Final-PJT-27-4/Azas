package com.azas.domain.auth.service;

import com.azas.domain.auth.entity.RefreshToken;

public interface RefreshTokenStore {

    // 인증 로직이 MySQL이나 Redis 같은 저장 기술에 직접 의존하지 않게 한다.
    void save(RefreshToken refreshToken);
}
