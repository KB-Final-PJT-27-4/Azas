package com.azas.domain.auth.service;

import com.azas.domain.member.entity.Member;
import com.azas.domain.member.entity.MemberType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class JwtAccessTokenProviderTest {

    private static final String TEST_SECRET_BASE64 =
            Encoders.BASE64.encode(
                    "01234567890123456789012345678901"
                            .getBytes(StandardCharsets.UTF_8)
            );

    @Test
    void issuesSignedAccessTokenWithMemberClaims() {
        JwtAccessTokenProvider tokenProvider =
                new JwtAccessTokenProvider(
                        TEST_SECRET_BASE64,
                        3600L
                );

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

        String accessToken =
                tokenProvider.issue(member);

        SecretKey verificationKey =
                Keys.hmacShaKeyFor(
                        Decoders.BASE64.decode(
                                TEST_SECRET_BASE64
                        )
                );

        Claims claims = Jwts.parser()
                .verifyWith(verificationKey)
                .build()
                .parseSignedClaims(accessToken)
                .getPayload();

        assertEquals("azas", claims.getIssuer());
        assertEquals("1", claims.getSubject());
        assertEquals(
                MemberType.PARENT.name(),
                claims.get("member_type", String.class)
        );
        assertNull(claims.get("role"));
        assertEquals(
                "access",
                claims.get("token_type", String.class)
        );

        long expirationDifference =
                claims.getExpiration()
                        .toInstant()
                        .getEpochSecond()
                        - claims.getIssuedAt()
                        .toInstant()
                        .getEpochSecond();

        assertEquals(3600L, expirationDifference);
        assertEquals(
                3600L,
                tokenProvider.getExpirationSeconds()
        );
    }
}
