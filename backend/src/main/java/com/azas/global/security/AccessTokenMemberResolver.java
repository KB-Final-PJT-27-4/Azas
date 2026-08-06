package com.azas.global.security;

import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

@Component
public class AccessTokenMemberResolver {

    private static final String ISSUER = "azas";
    private static final String TOKEN_TYPE = "access";

    private final SecretKey secretKey;

    public AccessTokenMemberResolver(
            @Value("${JWT_SECRET_BASE64}")
            String secretBase64
    ) {
        this.secretKey = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(secretBase64)
        );
    }

    public long resolveMemberId(
            String authorizationHeader
    ) {
        if (authorizationHeader == null
                || !authorizationHeader.startsWith(
                "Bearer "
        )) {
            throw new BusinessException(
                    ErrorCode.ACCESS_TOKEN_REQUIRED
            );
        }

        String accessToken =
                authorizationHeader.substring(
                        "Bearer ".length()
                ).trim();

        if (accessToken.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.ACCESS_TOKEN_REQUIRED
            );
        }

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(accessToken)
                    .getPayload();

            if (!ISSUER.equals(claims.getIssuer())
                    || !TOKEN_TYPE.equals(
                    claims.get(
                            "token_type",
                            String.class
                    )
            )) {
                throw invalidAccessToken();
            }

            // 만료 시각이 없는 토큰이 장기 인증 수단으로 사용되지 않도록 차단한다.
            if (claims.getExpiration() == null) {
                throw invalidAccessToken();
            }

            long memberId = Long.parseLong(
                    claims.getSubject()
            );

            if (memberId <= 0) {
                throw invalidAccessToken();
            }

            return memberId;
        } catch (BusinessException exception) {
            throw exception;
        } catch (
                JwtException
                | IllegalArgumentException exception
        ) {
            throw invalidAccessToken();
        }
    }

    private BusinessException invalidAccessToken() {
        return new BusinessException(
                ErrorCode.INVALID_ACCESS_TOKEN
        );
    }
}