package com.azas.domain.auth.service;

import com.azas.domain.auth.dto.AuthTokenPair;
import com.azas.domain.auth.entity.RefreshToken;
import com.azas.domain.member.entity.Member;
import com.azas.domain.member.entity.MemberStatus;
import com.azas.domain.member.mapper.MemberMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenRefreshServiceTest {

    @Mock
    private TokenHashEncoder tokenHashEncoder;

    @Mock
    private RefreshTokenStore refreshTokenStore;

    @Mock
    private MemberMapper memberMapper;

    @Mock
    private AuthTokenService authTokenService;

    private TokenRefreshService tokenRefreshService;

    @BeforeEach
    void setUp() {
        tokenRefreshService = new TokenRefreshService(
                tokenHashEncoder,
                refreshTokenStore,
                memberMapper,
                authTokenService
        );
    }

    @Test
    void rotatesActiveRefreshTokenAndIssuesNewTokenPair() {
        RefreshToken storedToken = activeRefreshToken();
        Member member = activeMember();
        AuthTokenPair tokenPair = new AuthTokenPair(
                "new-access-token",
                "new-refresh-token",
                3600L
        );

        when(tokenHashEncoder.encode("raw-refresh-token"))
                .thenReturn("token-hash");
        when(refreshTokenStore.findByTokenHash("token-hash"))
                .thenReturn(Optional.of(storedToken));
        when(memberMapper.findById(1L))
                .thenReturn(member);
        when(
                refreshTokenStore.revokeIfActive(
                        eq("token-hash"),
                        any(LocalDateTime.class)
                )
        ).thenReturn(true);
        when(authTokenService.issue(member))
                .thenReturn(tokenPair);

        AuthTokenPair result = tokenRefreshService.refresh(
                "raw-refresh-token"
        );

        assertSame(tokenPair, result);

        InOrder callOrder = inOrder(
                tokenHashEncoder,
                refreshTokenStore,
                memberMapper,
                authTokenService
        );

        callOrder.verify(tokenHashEncoder)
                .encode("raw-refresh-token");
        callOrder.verify(refreshTokenStore)
                .findByTokenHash("token-hash");
        callOrder.verify(memberMapper)
                .findById(1L);
        callOrder.verify(refreshTokenStore)
                .revokeIfActive(
                        eq("token-hash"),
                        any(LocalDateTime.class)
                );
        callOrder.verify(authTokenService)
                .issue(member);
    }

    @Test
    void rejectsUnknownRefreshToken() {
        when(tokenHashEncoder.encode("unknown-token"))
                .thenReturn("unknown-hash");
        when(refreshTokenStore.findByTokenHash("unknown-hash"))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tokenRefreshService.refresh(
                        "unknown-token"
                )
        );

        assertEquals(
                ErrorCode.INVALID_REFRESH_TOKEN,
                exception.getErrorCode()
        );
        verifyNoInteractions(memberMapper, authTokenService);
    }

    @Test
    void rejectsExpiredRefreshToken() {
        RefreshToken expiredToken = RefreshToken.issue(
                1L,
                "token-hash",
                LocalDateTime.of(2000, 1, 1, 0, 0)
        );

        stubStoredToken(expiredToken);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tokenRefreshService.refresh(
                        "raw-refresh-token"
                )
        );

        assertEquals(
                ErrorCode.INVALID_REFRESH_TOKEN,
                exception.getErrorCode()
        );
        verifyNoInteractions(memberMapper, authTokenService);
    }

    @Test
    void rejectsRevokedRefreshToken() {
        RefreshToken revokedToken = activeRefreshToken();
        ReflectionTestUtils.setField(
                revokedToken,
                "revokedAt",
                LocalDateTime.of(2026, 8, 4, 12, 0)
        );

        stubStoredToken(revokedToken);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tokenRefreshService.refresh(
                        "raw-refresh-token"
                )
        );

        assertEquals(
                ErrorCode.INVALID_REFRESH_TOKEN,
                exception.getErrorCode()
        );
        verifyNoInteractions(memberMapper, authTokenService);
    }

    @Test
    void rejectsTokenWhenMemberDoesNotExist() {
        RefreshToken storedToken = activeRefreshToken();

        stubStoredToken(storedToken);
        when(memberMapper.findById(1L))
                .thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tokenRefreshService.refresh(
                        "raw-refresh-token"
                )
        );

        assertEquals(
                ErrorCode.INVALID_REFRESH_TOKEN,
                exception.getErrorCode()
        );
        verifyNoInteractions(authTokenService);
    }

    @Test
    void rejectsWithdrawnMember() {
        RefreshToken storedToken = activeRefreshToken();
        Member member = activeMember();
        ReflectionTestUtils.setField(
                member,
                "status",
                MemberStatus.WITHDRAWN
        );

        stubStoredToken(storedToken);
        when(memberMapper.findById(1L))
                .thenReturn(member);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tokenRefreshService.refresh(
                        "raw-refresh-token"
                )
        );

        assertEquals(
                ErrorCode.WITHDRAWN_MEMBER,
                exception.getErrorCode()
        );
        verify(
                refreshTokenStore,
                never()
        ).revokeIfActive(
                eq("token-hash"),
                any(LocalDateTime.class)
        );
        verifyNoInteractions(authTokenService);
    }

    @Test
    void rejectsTokenWhenConcurrentRequestRevokedItFirst() {
        RefreshToken storedToken = activeRefreshToken();
        Member member = activeMember();

        stubStoredToken(storedToken);
        when(memberMapper.findById(1L))
                .thenReturn(member);
        when(
                refreshTokenStore.revokeIfActive(
                        eq("token-hash"),
                        any(LocalDateTime.class)
                )
        ).thenReturn(false);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> tokenRefreshService.refresh(
                        "raw-refresh-token"
                )
        );

        assertEquals(
                ErrorCode.INVALID_REFRESH_TOKEN,
                exception.getErrorCode()
        );
        verifyNoInteractions(authTokenService);
    }

    private void stubStoredToken(RefreshToken refreshToken) {
        when(tokenHashEncoder.encode("raw-refresh-token"))
                .thenReturn("token-hash");
        when(refreshTokenStore.findByTokenHash("token-hash"))
                .thenReturn(Optional.of(refreshToken));
    }

    private RefreshToken activeRefreshToken() {
        return RefreshToken.issue(
                1L,
                "token-hash",
                LocalDateTime.of(2100, 1, 1, 0, 0)
        );
    }

    private Member activeMember() {
        Member member = Member.createParent(
                "parent@example.com",
                "김하나",
                null
        );
        ReflectionTestUtils.setField(
                member,
                "memberId",
                1L
        );
        return member;
    }
}
