package com.azas.domain.auth.service;

import com.azas.domain.auth.dto.AuthTokenPair;
import com.azas.domain.auth.entity.RefreshToken;
import com.azas.domain.member.entity.Member;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthTokenServiceTest {

    private static final long REFRESH_TOKEN_EXPIRATION_SECONDS = 1209600L;

    @Test
    void issuesTokenPairAndStoresHashedRefreshToken() {
        JwtAccessTokenProvider accessTokenProvider =
                mock(JwtAccessTokenProvider.class);
        RefreshTokenGenerator refreshTokenGenerator =
                mock(RefreshTokenGenerator.class);
        TokenHashEncoder tokenHashEncoder =
                mock(TokenHashEncoder.class);
        RefreshTokenStore refreshTokenStore =
                mock(RefreshTokenStore.class);

        AuthTokenService authTokenService =
                new AuthTokenService(
                        accessTokenProvider,
                        refreshTokenGenerator,
                        tokenHashEncoder,
                        refreshTokenStore,
                        REFRESH_TOKEN_EXPIRATION_SECONDS
                );

        Member member = Member.createParent(
                "parent@example.com",
                "김하나",
                null
        );
        ReflectionTestUtils.setField(member, "memberId", 1L);

        when(accessTokenProvider.issue(member))
                .thenReturn("access-token");
        when(accessTokenProvider.getExpirationSeconds())
                .thenReturn(3600L);
        when(refreshTokenGenerator.generate())
                .thenReturn("refresh-token");
        when(tokenHashEncoder.encode("refresh-token"))
                .thenReturn("hashed-refresh-token");

        LocalDateTime earliestExpiresAt =
                LocalDateTime.now(ZoneOffset.UTC)
                        .plusSeconds(REFRESH_TOKEN_EXPIRATION_SECONDS);

        AuthTokenPair tokenPair =
                authTokenService.issue(member);

        LocalDateTime latestExpiresAt =
                LocalDateTime.now(ZoneOffset.UTC)
                        .plusSeconds(REFRESH_TOKEN_EXPIRATION_SECONDS);

        assertEquals("access-token", tokenPair.getAccessToken());
        assertEquals("refresh-token", tokenPair.getRefreshToken());
        assertEquals(
                3600L,
                tokenPair.getAccessTokenExpiresInSeconds()
        );

        ArgumentCaptor<RefreshToken> tokenCaptor =
                ArgumentCaptor.forClass(RefreshToken.class);

        verify(refreshTokenStore).save(tokenCaptor.capture());

        RefreshToken storedToken = tokenCaptor.getValue();

        assertEquals(1L, storedToken.getMemberId());
        assertEquals(
                "hashed-refresh-token",
                storedToken.getTokenHash()
        );
        assertNull(storedToken.getRevokedAt());
        assertFalse(
                storedToken.getExpiresAt()
                        .isBefore(earliestExpiresAt)
        );
        assertFalse(
                storedToken.getExpiresAt()
                        .isAfter(latestExpiresAt)
        );
    }
}