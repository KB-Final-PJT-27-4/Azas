package com.azas.domain.auth.service;

import com.azas.domain.auth.dto.AuthTokenPair;
import com.azas.domain.auth.dto.OAuthLoginRequest;
import com.azas.domain.auth.dto.OAuthLoginResult;
import com.azas.domain.auth.dto.OAuthMemberResult;
import com.azas.domain.auth.dto.OAuthProfile;
import com.azas.domain.auth.entity.OAuthProvider;
import com.azas.domain.member.entity.Member;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OAuthLoginServiceTest {

    @Test
    void logsInWithSelectedOAuthProviderAndIssuesTokens() {
        OAuthClientRegistry oauthClientRegistry =
                mock(OAuthClientRegistry.class);
        OAuthClient oauthClient =
                mock(OAuthClient.class);
        OAuthMemberService oauthMemberService =
                mock(OAuthMemberService.class);
        AuthTokenService authTokenService =
                mock(AuthTokenService.class);

        OAuthLoginService oauthLoginService =
                new OAuthLoginService(
                        oauthClientRegistry,
                        oauthMemberService,
                        authTokenService
                );

        OAuthLoginRequest request =
                new OAuthLoginRequest();

        ReflectionTestUtils.setField(
                request,
                "authorizationCode",
                "authorization-code"
        );
        ReflectionTestUtils.setField(
                request,
                "redirectUri",
                "http://localhost:8080/auth/google/callback"
        );

        OAuthProfile profile =
                new OAuthProfile(
                        OAuthProvider.GOOGLE,
                        "google-subject",
                        "parent@example.com",
                        "김하나",
                        null
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

        OAuthMemberResult memberResult =
                new OAuthMemberResult(
                        member,
                        true
                );

        AuthTokenPair tokenPair =
                new AuthTokenPair(
                        "access-token",
                        "refresh-token",
                        3600L
                );

        when(oauthClientRegistry.get(OAuthProvider.GOOGLE))
                .thenReturn(oauthClient);
        when(oauthClient.fetchProfile(
                "authorization-code",
                "http://localhost:8080/auth/google/callback"
        )).thenReturn(profile);
        when(oauthMemberService.findOrCreate(profile))
                .thenReturn(memberResult);
        when(authTokenService.issue(member))
                .thenReturn(tokenPair);

        OAuthLoginResult result =
                oauthLoginService.login(
                        "google",
                        request
                );

        assertSame(tokenPair, result.getTokenPair());
        assertSame(member, result.getMember());
        assertTrue(result.isNewMember());

        InOrder callOrder = inOrder(
                oauthClientRegistry,
                oauthClient,
                oauthMemberService,
                authTokenService
        );

        callOrder.verify(oauthClientRegistry)
                .get(OAuthProvider.GOOGLE);
        callOrder.verify(oauthClient)
                .fetchProfile(
                        "authorization-code",
                        "http://localhost:8080/auth/google/callback"
                );
        callOrder.verify(oauthMemberService)
                .findOrCreate(profile);
        callOrder.verify(authTokenService)
                .issue(member);
    }
}