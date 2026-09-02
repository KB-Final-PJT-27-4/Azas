package com.azas.global.security;

import com.azas.domain.member.entity.Member;
import com.azas.domain.member.entity.MemberStatus;
import com.azas.domain.member.mapper.MemberMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccessTokenMemberResolverTest {

    private static final String TEST_SECRET_BASE64 =
            Encoders.BASE64.encode(
                    "01234567890123456789012345678901"
                            .getBytes(StandardCharsets.UTF_8)
            );

    private static final String OTHER_SECRET_BASE64 =
            Encoders.BASE64.encode(
                    "abcdefghijklmnopqrstuvwxyz123456"
                            .getBytes(StandardCharsets.UTF_8)
            );

    @Mock
    private MemberMapper memberMapper;

    private AccessTokenMemberResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new AccessTokenMemberResolver(
                TEST_SECRET_BASE64,
                memberMapper
        );
    }

    @Test
    void returnsMemberIdFromValidAccessToken() {
        Member member = activeMember();

        when(memberMapper.findById(7L))
                .thenReturn(member);

        String accessToken = createToken(
                "7",
                "azas",
                "access",
                Instant.now().plusSeconds(300),
                TEST_SECRET_BASE64
        );

        long memberId = resolver.resolveMemberId(
                "Bearer " + accessToken
        );

        assertEquals(7L, memberId);
        verify(memberMapper).findById(7L);
    }

    @Test
    void rejectsTokenForMissingMember() {
        String accessToken = createToken(
                "7",
                "azas",
                "access",
                Instant.now().plusSeconds(300),
                TEST_SECRET_BASE64
        );

        when(memberMapper.findById(7L))
                .thenReturn(null);

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
        verify(memberMapper).findById(7L);
    }

    @Test
    void rejectsTokenForWithdrawnMember() {
        Member member = activeMember();

        ReflectionTestUtils.setField(
                member,
                "status",
                MemberStatus.WITHDRAWN
        );

        when(memberMapper.findById(7L))
                .thenReturn(member);

        String accessToken = createToken(
                "7",
                "azas",
                "access",
                Instant.now().plusSeconds(300),
                TEST_SECRET_BASE64
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> resolver.resolveMemberId(
                        "Bearer " + accessToken
                )
        );

        assertEquals(
                ErrorCode.WITHDRAWN_MEMBER,
                exception.getErrorCode()
        );
        verify(memberMapper).findById(7L);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            "Basic token",
            "Bearer ",
            "bearer token"
    })
    void rejectsMissingOrInvalidAuthorizationHeader(
            String authorizationHeader
    ) {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> resolver.resolveMemberId(
                        authorizationHeader
                )
        );

        assertEquals(
                ErrorCode.ACCESS_TOKEN_REQUIRED,
                exception.getErrorCode()
        );
        verifyNoInteractions(memberMapper);
    }

    @Test
    void rejectsTokenWithInvalidSignature() {
        String accessToken = createToken(
                "7",
                "azas",
                "access",
                Instant.now().plusSeconds(300),
                OTHER_SECRET_BASE64
        );

        assertInvalidAccessToken(accessToken);
        verifyNoInteractions(memberMapper);
    }

    @Test
    void rejectsTokenWithUnexpectedIssuer() {
        String accessToken = createToken(
                "7",
                "other-service",
                "access",
                Instant.now().plusSeconds(300),
                TEST_SECRET_BASE64
        );

        assertInvalidAccessToken(accessToken);
        verifyNoInteractions(memberMapper);
    }

    @Test
    void rejectsTokenWithUnexpectedType() {
        String accessToken = createToken(
                "7",
                "azas",
                "refresh",
                Instant.now().plusSeconds(300),
                TEST_SECRET_BASE64
        );

        assertInvalidAccessToken(accessToken);
        verifyNoInteractions(memberMapper);
    }

    @Test
    void rejectsExpiredAccessToken() {
        String accessToken = createToken(
                "7",
                "azas",
                "access",
                Instant.now().minusSeconds(300),
                TEST_SECRET_BASE64
        );

        assertInvalidAccessToken(accessToken);
        verifyNoInteractions(memberMapper);
    }

    @Test
    void rejectsTokenWithoutExpiration() {
        String accessToken = createToken(
                "7",
                "azas",
                "access",
                null,
                TEST_SECRET_BASE64
        );

        assertInvalidAccessToken(accessToken);
        verifyNoInteractions(memberMapper);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "0",
            "-1",
            "not-a-number"
    })
    void rejectsInvalidMemberId(
            String subject
    ) {
        String accessToken = createToken(
                subject,
                "azas",
                "access",
                Instant.now().plusSeconds(300),
                TEST_SECRET_BASE64
        );

        assertInvalidAccessToken(accessToken);
        verifyNoInteractions(memberMapper);
    }

    private void assertInvalidAccessToken(
            String accessToken
    ) {
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

    private Member activeMember() {
        Member member = Member.createParent(
                "parent@example.com",
                "김하나",
                null
        );

        ReflectionTestUtils.setField(
                member,
                "memberId",
                7L
        );

        return member;
    }

    private String createToken(
            String subject,
            String issuer,
            String tokenType,
            Instant expiresAt,
            String secretBase64
    ) {
        JwtBuilder tokenBuilder = Jwts.builder()
                .subject(subject)
                .issuer(issuer)
                .claim(
                        "token_type",
                        tokenType
                );

        if (expiresAt != null) {
            tokenBuilder.expiration(
                    Date.from(expiresAt)
            );
        }

        return tokenBuilder
                .signWith(
                        Keys.hmacShaKeyFor(
                                Decoders.BASE64.decode(
                                        secretBase64
                                )
                        )
                )
                .compact();
    }
}