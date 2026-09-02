package com.azas.domain.auth.service;

import com.azas.domain.auth.dto.AuthTokenPair;
import com.azas.domain.auth.dto.ChildInviteOAuthResult;
import com.azas.domain.auth.dto.OAuthProfile;
import com.azas.domain.auth.entity.OAuthProvider;
import com.azas.domain.member.entity.Member;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ChildInviteOAuthServiceTest {

    @Test
    void fetchesOAuthProfileBeforePreparingChildInviteLogin() {
        OAuthClientRegistry oauthClientRegistry =
                mock(OAuthClientRegistry.class);

        OAuthClient oauthClient =
                mock(OAuthClient.class);

        ChildInviteLoginService loginService =
                mock(ChildInviteLoginService.class);

        ChildInviteOAuthService service =
                new ChildInviteOAuthService(
                        oauthClientRegistry,
                        loginService
                );

        OAuthProfile profile = new OAuthProfile(
                OAuthProvider.KAKAO,
                "kakao-subject",
                "child@example.com",
                "김자녀",
                null
        );

        Member member = Member.createChild(
                "child@example.com",
                "김자녀",
                null
        );

        ChildInviteOAuthResult expected =
                new ChildInviteOAuthResult(
                        new AuthTokenPair(
                                "access-token",
                                "refresh-token",
                                3600L
                        ),
                        member,
                        true,
                        10L,
                        "김자녀",
                        30L,
                        LocalDateTime.of(
                                2026,
                                8,
                                4,
                                3,
                                0
                        )
                );

        when(
                oauthClientRegistry.get(
                        OAuthProvider.KAKAO
                )
        ).thenReturn(oauthClient);

        when(
                oauthClient.fetchProfile(
                        "authorization-code",
                        "http://localhost:5173/auth/kakao/child-invite/callback"
                )
        ).thenReturn(profile);

        when(
                loginService.login(
                        "raw-invite-token",
                        profile
                )
        ).thenReturn(expected);

        ChildInviteOAuthResult actual =
                service.login(
                        "kakao",
                        "authorization-code",
                        "http://localhost:5173/auth/kakao/child-invite/callback",
                        "raw-invite-token"
                );

        assertSame(expected, actual);

        var callOrder = inOrder(
                oauthClientRegistry,
                oauthClient,
                loginService
        );

        callOrder.verify(oauthClientRegistry)
                .get(OAuthProvider.KAKAO);

        callOrder.verify(oauthClient)
                .fetchProfile(
                        "authorization-code",
                        "http://localhost:5173/auth/kakao/child-invite/callback"
                );

        callOrder.verify(loginService)
                .login(
                        "raw-invite-token",
                        profile
                );
    }
}