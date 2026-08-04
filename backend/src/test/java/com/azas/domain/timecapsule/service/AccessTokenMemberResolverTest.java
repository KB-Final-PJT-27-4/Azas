package com.azas.domain.timecapsule.service;

import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccessTokenMemberResolverTest {

    private static final String TEST_SECRET_BASE64 =
            Encoders.BASE64.encode(
                    "01234567890123456789012345678901"
                            .getBytes(StandardCharsets.UTF_8)
            );

    @Test
    // [JMG] CAPSULE-1 정상 Access Token에서 요청 회원 ID를 추출한다.
    void resolveMemberIdReturnsTokenSubject() {
        AccessTokenMemberResolver resolver =
                new AccessTokenMemberResolver(TEST_SECRET_BASE64);
        String accessToken = createToken(
                "7",
                "azas",
                "access"
        );

        long memberId = resolver.resolveMemberId(
                "Bearer " + accessToken
        );

        assertEquals(7L, memberId);
    }

    @Test
    // [JMG] CAPSULE-1 Access Token이 아닌 JWT는 인증 오류로 거부한다.
    void resolveMemberIdRejectsTokenWithUnexpectedType() {
        AccessTokenMemberResolver resolver =
                new AccessTokenMemberResolver(TEST_SECRET_BASE64);
        String accessToken = createToken(
                "7",
                "azas",
                "refresh"
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> resolver.resolveMemberId(
                        "Bearer " + accessToken
                )
        );

        assertEquals(
                ErrorCode.INVALID_ACCESS_TOKEN,
                exception.getErrorCode()
        );
    }

    // [JMG] CAPSULE-1 테스트용 JWT를 발급한다.
    private String createToken(
            String subject,
            String issuer,
            String tokenType
    ) {
        return Jwts.builder()
                .subject(subject)
                .issuer(issuer)
                .claim("token_type", tokenType)
                .signWith(
                        Keys.hmacShaKeyFor(
                                Decoders.BASE64.decode(
                                        TEST_SECRET_BASE64
                                )
                        )
                )
                .compact();
    }
}
