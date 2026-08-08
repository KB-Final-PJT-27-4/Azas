package com.azas.domain.auth.service;

import com.azas.domain.auth.dto.AuthTokenPair;
import com.azas.domain.auth.entity.RefreshToken;
import com.azas.domain.member.entity.Member;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class AuthTokenService {

    private final JwtAccessTokenProvider accessTokenProvider;
    private final RefreshTokenGenerator refreshTokenGenerator;
    private final TokenHashEncoder tokenHashEncoder;
    private final RefreshTokenStore refreshTokenStore;
    private final long refreshTokenExpirationSeconds;

    public AuthTokenService(
            JwtAccessTokenProvider accessTokenProvider,
            RefreshTokenGenerator refreshTokenGenerator,
            TokenHashEncoder tokenHashEncoder,
            RefreshTokenStore refreshTokenStore,
            @Value("${REFRESH_TOKEN_EXPIRATION_SECONDS:1209600}")
            long refreshTokenExpirationSeconds
    ) {
        this.accessTokenProvider = accessTokenProvider;
        this.refreshTokenGenerator = refreshTokenGenerator;
        this.tokenHashEncoder = tokenHashEncoder;
        this.refreshTokenStore = refreshTokenStore;
        this.refreshTokenExpirationSeconds =
                refreshTokenExpirationSeconds;
    }

    @Transactional
    public AuthTokenPair issue(Member member) {
        String accessToken =
                accessTokenProvider.issue(member);

        String refreshToken =
                refreshTokenGenerator.generate();

        String refreshTokenHash =
                tokenHashEncoder.encode(refreshToken);

        Instant refreshTokenExpiresAt =
                Instant.now().plusSeconds(
                        refreshTokenExpirationSeconds
                );

        // DB에는 서버 전체에서 일관된 UTC 기준 만료 시각을 저장한다.
        LocalDateTime storedExpiresAt =
                LocalDateTime.ofInstant(
                        refreshTokenExpiresAt,
                        ZoneOffset.UTC
                );

        RefreshToken storedRefreshToken =
                RefreshToken.issue(
                        member.getMemberId(),
                        refreshTokenHash,
                        storedExpiresAt
                );

        refreshTokenStore.save(storedRefreshToken);

        return new AuthTokenPair(
                accessToken,
                refreshToken,
                accessTokenProvider.getExpirationSeconds()
        );
    }
}
