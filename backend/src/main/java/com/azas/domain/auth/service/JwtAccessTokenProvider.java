package com.azas.domain.auth.service;

import com.azas.domain.member.entity.Member;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtAccessTokenProvider {

    private static final String ISSUER = "azas";
    private static final String TOKEN_TYPE = "access";

    private final SecretKey secretKey;

    @Getter
    private final long expirationSeconds;

    public JwtAccessTokenProvider(
            @Value("${JWT_SECRET_BASE64}")
            String secretBase64,
            @Value("${JWT_ACCESS_TOKEN_EXPIRATION_SECONDS:3600}")
            long expirationSeconds
    ) {
        // JWT 비밀키는 일반 문자열이 아닌 Base64로 인코딩된 키를 사용한다.
        this.secretKey = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(secretBase64)
        );
        this.expirationSeconds = expirationSeconds;
    }

    public String issue(Member member) {
        Instant issuedAt = Instant.now();
        Instant expiresAt =
                issuedAt.plusSeconds(expirationSeconds);

        return Jwts.builder()
                .issuer(ISSUER)
                .subject(
                        String.valueOf(member.getMemberId())
                )
                .claim(
                        "member_type",
                        member.getMemberType().name()
                )
                .claim(
                        "token_type",
                        TOKEN_TYPE
                )
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey)
                .compact();
    }
}
