package com.azas.domain.auth.service;

import com.azas.domain.auth.dto.AuthTokenPair;
import com.azas.domain.auth.dto.OAuthProfile;
import com.azas.domain.auth.dto.ParentInviteOAuthResult;
import com.azas.domain.auth.entity.OAuthProvider;
import com.azas.domain.child.entity.RelationType;
import com.azas.domain.member.entity.Member;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ParentInviteOAuthServiceTest {

    @Test
    void fetchesOAuthProfileBeforeAcceptingParentInvitation() {
        OAuthClientRegistry oauthClientRegistry =
                mock(OAuthClientRegistry.class);

        OAuthClient oauthClient =
                mock(OAuthClient.class);

        ParentInviteAcceptanceService acceptanceService =
                mock(ParentInviteAcceptanceService.class);

        ParentInviteOAuthService service =
                new ParentInviteOAuthService(
                        oauthClientRegistry,
                        acceptanceService
                );

        OAuthProfile profile = new OAuthProfile(
                OAuthProvider.GOOGLE,
                "google-parent-subject",
                "parent@example.com",
                "김부모",
                null
        );

        Member member = Member.createParent(
                "parent@example.com",
                "김부모",
                null
        );

        ParentInviteOAuthResult expected =
                new ParentInviteOAuthResult(
                        new AuthTokenPair(
                                "access-token",
                                "refresh-token",
                                3600L
                        ),
                        member,
                        true,
                        10L,
                        "김자녀",
                        RelationType.GUARDIAN,
                        30L,
                        LocalDateTime.of(
                                2026,
                                8,
                                5,
                                3,
                                0
                        )
                );

        when(
                oauthClientRegistry.get(
                        OAuthProvider.GOOGLE
                )
        ).thenReturn(oauthClient);

        when(
                oauthClient.fetchProfile(
                        "authorization-code",
                        "http://localhost:5173/auth/google/parent-invite/callback"
                )
        ).thenReturn(profile);

        when(
                acceptanceService.accept(
                        "raw-parent-invite-token",
                        RelationType.GUARDIAN,
                        profile
                )
        ).thenReturn(expected);

        ParentInviteOAuthResult actual =
                service.login(
                        "google",
                        "authorization-code",
                        "http://localhost:5173/auth/google/parent-invite/callback",
                        "raw-parent-invite-token",
                        RelationType.GUARDIAN
                );

        assertSame(expected, actual);

        InOrder callOrder = inOrder(
                oauthClientRegistry,
                oauthClient,
                acceptanceService
        );

        callOrder.verify(oauthClientRegistry)
                .get(OAuthProvider.GOOGLE);

        callOrder.verify(oauthClient)
                .fetchProfile(
                        "authorization-code",
                        "http://localhost:5173/auth/google/parent-invite/callback"
                );

        callOrder.verify(acceptanceService)
                .accept(
                        "raw-parent-invite-token",
                        RelationType.GUARDIAN,
                        profile
                );
    }
}