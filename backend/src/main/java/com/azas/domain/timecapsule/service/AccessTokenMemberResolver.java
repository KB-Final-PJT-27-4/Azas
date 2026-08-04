package com.azas.domain.timecapsule.service;

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

    // [JMG] CAPSULE-1 Authorization 헤더의 Access Token에서 요청 회원 ID를 검증해 추출한다.
    public long resolveMemberId(String authorizationHeader) {
        if (authorizationHeader == null
                || !authorizationHeader.startsWith("Bearer ")) {
            throw new BusinessException(
                    ErrorCode.ACCESS_TOKEN_REQUIRED
            );
        }

        String accessToken = authorizationHeader.substring(
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
                    claims.get("token_type", String.class)
            )) {
                throw new BusinessException(
                        ErrorCode.INVALID_ACCESS_TOKEN
                );
            }

            long memberId = Long.parseLong(claims.getSubject());

            if (memberId <= 0) {
                throw new BusinessException(
                        ErrorCode.INVALID_ACCESS_TOKEN
                );
            }

            return memberId;
        } catch (BusinessException exception) {
            throw exception;
        } catch (JwtException | IllegalArgumentException exception) {
            throw new BusinessException(
                    ErrorCode.INVALID_ACCESS_TOKEN
            );
        }
    }
}
