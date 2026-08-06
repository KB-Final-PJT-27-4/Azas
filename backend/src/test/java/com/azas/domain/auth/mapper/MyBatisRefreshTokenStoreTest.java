package com.azas.domain.auth.mapper;

import com.azas.domain.auth.entity.RefreshToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MyBatisRefreshTokenStoreTest {

    private static final LocalDateTime NOW =
            LocalDateTime.of(2026, 8, 4, 12, 0);

    @Mock
    private RefreshTokenMapper refreshTokenMapper;

    @InjectMocks
    private MyBatisRefreshTokenStore refreshTokenStore;

    @Test
    void returnsRefreshTokenWhenMapperFindsToken() {
        RefreshToken refreshToken = RefreshToken.issue(
                1L,
                "token-hash",
                NOW.plusDays(1)
        );

        when(refreshTokenMapper.findByTokenHash("token-hash"))
                .thenReturn(refreshToken);

        Optional<RefreshToken> result =
                refreshTokenStore.findByTokenHash("token-hash");

        assertTrue(result.isPresent());
        assertSame(refreshToken, result.get());
    }

    @Test
    void returnsEmptyWhenMapperDoesNotFindToken() {
        when(refreshTokenMapper.findByTokenHash("token-hash"))
                .thenReturn(null);

        Optional<RefreshToken> result =
                refreshTokenStore.findByTokenHash("token-hash");

        assertTrue(result.isEmpty());
    }

    @Test
    void returnsTrueWhenMapperRevokesActiveToken() {
        when(
                refreshTokenMapper.revokeIfActive(
                        "token-hash",
                        NOW
                )
        ).thenReturn(1);

        boolean revoked = refreshTokenStore.revokeIfActive(
                "token-hash",
                NOW
        );

        assertTrue(revoked);
    }

    @Test
    void returnsFalseWhenMapperDoesNotRevokeToken() {
        when(
                refreshTokenMapper.revokeIfActive(
                        "token-hash",
                        NOW
                )
        ).thenReturn(0);

        boolean revoked = refreshTokenStore.revokeIfActive(
                "token-hash",
                NOW
        );

        assertFalse(revoked);
    }
}
