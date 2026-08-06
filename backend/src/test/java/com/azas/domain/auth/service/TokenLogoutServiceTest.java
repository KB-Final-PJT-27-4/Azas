package com.azas.domain.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenLogoutServiceTest {

    @Mock
    private TokenHashEncoder tokenHashEncoder;

    @Mock
    private RefreshTokenStore refreshTokenStore;

    private TokenLogoutService tokenLogoutService;

    @BeforeEach
    void setUp() {
        tokenLogoutService = new TokenLogoutService(
                tokenHashEncoder,
                refreshTokenStore
        );
    }

    @Test
    void revokesActiveRefreshTokenByHash() {
        when(tokenHashEncoder.encode("raw-refresh-token"))
                .thenReturn("token-hash");
        when(
                refreshTokenStore.revokeIfActive(
                        eq("token-hash"),
                        any(LocalDateTime.class)
                )
        ).thenReturn(true);

        tokenLogoutService.logout("raw-refresh-token");

        InOrder callOrder = inOrder(
                tokenHashEncoder,
                refreshTokenStore
        );

        callOrder.verify(tokenHashEncoder)
                .encode("raw-refresh-token");
        callOrder.verify(refreshTokenStore)
                .revokeIfActive(
                        eq("token-hash"),
                        any(LocalDateTime.class)
                );
    }

    @Test
    void completesWhenRefreshTokenIsAlreadyUnavailable() {
        when(tokenHashEncoder.encode("unavailable-token"))
                .thenReturn("unavailable-hash");
        when(
                refreshTokenStore.revokeIfActive(
                        eq("unavailable-hash"),
                        any(LocalDateTime.class)
                )
        ).thenReturn(false);

        assertDoesNotThrow(
                () -> tokenLogoutService.logout(
                        "unavailable-token"
                )
        );

        verify(refreshTokenStore)
                .revokeIfActive(
                        eq("unavailable-hash"),
                        any(LocalDateTime.class)
                );
    }
}