package com.azas.domain.auth.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    private Long refreshTokenId;
    private Long memberId;
    private String tokenHash;
    private LocalDateTime expiresAt;
    private LocalDateTime revokedAt;
    private LocalDateTime createdAt;

    public static RefreshToken issue(
            long memberId,
            String tokenHash,
            LocalDateTime expiresAt
    ) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.memberId = memberId;
        refreshToken.tokenHash = tokenHash;
        refreshToken.expiresAt = expiresAt;
        return refreshToken;
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public boolean isExpiredAt(LocalDateTime now) {
        return !expiresAt.isAfter(now);
    }

    public boolean isActiveAt(LocalDateTime now) {
        return !isRevoked() && !isExpiredAt(now);
    }
}
