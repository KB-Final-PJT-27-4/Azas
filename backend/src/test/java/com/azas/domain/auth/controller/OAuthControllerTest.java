package com.azas.domain.auth.controller;

import com.azas.domain.auth.dto.AuthTokenPair;
import com.azas.domain.auth.dto.ChildInviteOAuthResult;
import com.azas.domain.auth.dto.OAuthLoginRequest;
import com.azas.domain.auth.dto.OAuthLoginResult;
import com.azas.domain.auth.service.ChildInviteOAuthService;
import com.azas.domain.auth.service.OAuthLoginService;
import com.azas.domain.member.entity.Member;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import com.azas.global.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OAuthControllerTest {

    @Mock
    private OAuthLoginService oauthLoginService;

    @Mock
    private ChildInviteOAuthService childInviteOAuthService;

    @InjectMocks
    private OAuthController oauthController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper =
                new ObjectMapper()
                        .registerModule(new JavaTimeModule())
                        .disable(
                                SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
                        );

        LocalValidatorFactoryBean validator =
                new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(oauthController)
                .setControllerAdvice(
                        new GlobalExceptionHandler()
                )
                .setValidator(validator)
                .setMessageConverters(
                        new MappingJackson2HttpMessageConverter(
                                objectMapper
                        )
                )
                .build();
    }

    @Test
    void returnsOAuthLoginResponseForValidRequest()
            throws Exception {
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
        ReflectionTestUtils.setField(
                member,
                "createdAt",
                LocalDateTime.of(2026, 7, 23, 3, 0)
        );

        AuthTokenPair tokenPair =
                new AuthTokenPair(
                        "access-token",
                        "refresh-token",
                        3600L
                );

        OAuthLoginResult loginResult =
                new OAuthLoginResult(
                        tokenPair,
                        member,
                        true
                );

        when(
                oauthLoginService.login(
                        eq("google"),
                        any(OAuthLoginRequest.class)
                )
        ).thenReturn(loginResult);

        mockMvc.perform(
                        post("/api/v1/auth/oauth/google")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "authorization_code": "oauth-code",
                                          "redirect_uri": "http://localhost:8080/auth/google/callback"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(
                        content().contentTypeCompatibleWith(
                                MediaType.APPLICATION_JSON
                        )
                )
                .andExpect(
                        jsonPath("$.access_token")
                                .value("access-token")
                )
                .andExpect(
                        jsonPath("$.refresh_token")
                                .value("refresh-token")
                )
                .andExpect(
                        jsonPath("$.token_type")
                                .value("Bearer")
                )
                .andExpect(
                        jsonPath("$.expires_in")
                                .value(3600)
                )
                .andExpect(
                        jsonPath("$.is_new_member")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.member.member_id")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.member.member_type")
                                .value("PARENT")
                )
                .andExpect(
                        jsonPath("$.member.phone_verified")
                                .value(false)
                )
                .andExpect(
                        jsonPath("$.member.phone_verified_at")
                                .value(nullValue())
                )
                .andExpect(
                        jsonPath("$.member.created_at")
                                .value("2026-07-23T03:00:00Z")
                )
                .andExpect(
                        jsonPath("$.member.role")
                                .doesNotExist()
                )
                .andExpect(
                        jsonPath("$.member.memberType")
                                .doesNotExist()
                );

        ArgumentCaptor<OAuthLoginRequest> requestCaptor =
                ArgumentCaptor.forClass(
                        OAuthLoginRequest.class
                );

        verify(oauthLoginService).login(
                eq("google"),
                requestCaptor.capture()
        );

        OAuthLoginRequest capturedRequest =
                requestCaptor.getValue();

        assertEquals(
                "oauth-code",
                capturedRequest.getAuthorizationCode()
        );
        assertEquals(
                "http://localhost:8080/auth/google/callback",
                capturedRequest.getRedirectUri()
        );
    }

    @Test
    void returnsBadRequestForInvalidRequest()
            throws Exception {
        mockMvc.perform(
                        post("/api/v1/auth/oauth/google")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "authorization_code": ""
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.error.code")
                                .value("BADREQUEST")
                );

        verifyNoInteractions(oauthLoginService);
    }

    @ParameterizedTest
    @MethodSource("oauthFailures")
    void returnsExpectedErrorResponseForOAuthFailure(
            String provider,
            ErrorCode errorCode
    ) throws Exception {
        when(
                oauthLoginService.login(
                        eq(provider),
                        any(OAuthLoginRequest.class)
                )
        ).thenThrow(
                new BusinessException(errorCode)
        );

        mockMvc.perform(
                        post("/api/v1/auth/oauth/{provider}", provider)
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                    {
                                      "authorization_code": "oauth-code",
                                      "redirect_uri": "http://localhost:8080/auth/callback"
                                    }
                                    """)
                )
                .andExpect(
                        status().is(
                                errorCode.getHttpStatus().value()
                        )
                )
                .andExpect(
                        jsonPath("$.error.code")
                                .value(errorCode.name())
                )
                .andExpect(
                        jsonPath("$.error.message")
                                .value(errorCode.getMessage())
                );
    }

    private static Stream<Arguments> oauthFailures() {
        return Stream.of(
                Arguments.of(
                        "naver",
                        ErrorCode.UNSUPPORTED_OAUTH_PROVIDER
                ),
                Arguments.of(
                        "google",
                        ErrorCode.INVALID_AUTHORIZATION_CODE
                ),
                Arguments.of(
                        "kakao",
                        ErrorCode.OAUTH_PROVIDER_ERROR
                )
        );
    }

    @Test
    void returnsChildInviteOAuthResponseForValidRequest()
            throws Exception {
        Member member = Member.createChild(
                "child@example.com",
                "김자녀",
                null
        );

        ReflectionTestUtils.setField(
                member,
                "memberId",
                20L
        );
        ReflectionTestUtils.setField(
                member,
                "createdAt",
                LocalDateTime.of(2026, 8, 5, 2, 0)
        );

        ChildInviteOAuthResult result =
                new ChildInviteOAuthResult(
                        new AuthTokenPair(
                                "child-access-token",
                                "child-refresh-token",
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
                                5,
                                3,
                                0
                        )
                );

        when(
                childInviteOAuthService.login(
                        "kakao",
                        "oauth-code",
                        "http://localhost:5173/auth/kakao/child-invite/callback",
                        "raw-child-invite-token"
                )
        ).thenReturn(result);

        mockMvc.perform(
                        post(
                                "/api/v1/auth/oauth/kakao/child-invite"
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                    {
                                      "authorization_code": "oauth-code",
                                      "redirect_uri": "http://localhost:5173/auth/kakao/child-invite/callback",
                                      "invite_token": "raw-child-invite-token"
                                    }
                                    """)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.access_token")
                                .value("child-access-token")
                )
                .andExpect(
                        jsonPath("$.refresh_token")
                                .value("child-refresh-token")
                )
                .andExpect(
                        jsonPath("$.token_type")
                                .value("Bearer")
                )
                .andExpect(
                        jsonPath("$.expires_in")
                                .value(3600)
                )
                .andExpect(
                        jsonPath("$.is_new_member")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.member.member_id")
                                .value(20)
                )
                .andExpect(
                        jsonPath("$.member.member_type")
                                .value("CHILD")
                )
                .andExpect(
                        jsonPath("$.child.child_id")
                                .value(10)
                )
                .andExpect(
                        jsonPath("$.child.name")
                                .value("김자녀")
                )
                .andExpect(
                        jsonPath("$.child.member_linked")
                                .value(true)
                )
                .andExpect(
                        jsonPath(
                                "$.invitation.family_invitation_id"
                        ).value(30)
                )
                .andExpect(
                        jsonPath("$.invitation.invitee_type")
                                .value("CHILD")
                )
                .andExpect(
                        jsonPath("$.invitation.status")
                                .value("ACCEPTED")
                )
                .andExpect(
                        jsonPath("$.invitation.accepted_at")
                                .value("2026-08-05T03:00:00Z")
                );

        verify(childInviteOAuthService)
                .login(
                        "kakao",
                        "oauth-code",
                        "http://localhost:5173/auth/kakao/child-invite/callback",
                        "raw-child-invite-token"
                );
    }

    @Test
    void returnsBadRequestWhenChildInviteTokenIsMissing()
            throws Exception {
        mockMvc.perform(
                        post(
                                "/api/v1/auth/oauth/kakao/child-invite"
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                    {
                                      "authorization_code": "oauth-code",
                                      "redirect_uri": "http://localhost:5173/auth/kakao/child-invite/callback"
                                    }
                                    """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.error.code")
                                .value("BADREQUEST")
                );

        verifyNoInteractions(childInviteOAuthService);
    }

    @ParameterizedTest
    @MethodSource("childInviteFailures")
    void returnsExpectedErrorForChildInviteFailure(
            ErrorCode errorCode
    ) throws Exception {
        when(
                childInviteOAuthService.login(
                        "kakao",
                        "oauth-code",
                        "http://localhost:5173/auth/kakao/child-invite/callback",
                        "raw-child-invite-token"
                )
        ).thenThrow(
                new BusinessException(errorCode)
        );

        mockMvc.perform(
                        post(
                                "/api/v1/auth/oauth/kakao/child-invite"
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                    {
                                      "authorization_code": "oauth-code",
                                      "redirect_uri": "http://localhost:5173/auth/kakao/child-invite/callback",
                                      "invite_token": "raw-child-invite-token"
                                    }
                                    """)
                )
                .andExpect(
                        status().is(
                                errorCode.getHttpStatus().value()
                        )
                )
                .andExpect(
                        jsonPath("$.error.code")
                                .value(errorCode.name())
                )
                .andExpect(
                        jsonPath("$.error.message")
                                .value(errorCode.getMessage())
                );
    }

    private static Stream<Arguments> childInviteFailures() {
        return Stream.of(
                Arguments.of(
                        ErrorCode.INVALID_FAMILY_INVITATION
                ),
                Arguments.of(
                        ErrorCode.FAMILY_MEMBER_ALREADY_LINKED
                ),
                Arguments.of(
                        ErrorCode.MEMBER_TYPE_CONFLICT
                )
        );
    }
}