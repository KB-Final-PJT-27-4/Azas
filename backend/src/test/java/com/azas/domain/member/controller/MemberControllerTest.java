package com.azas.domain.member.controller;

import com.azas.domain.auth.entity.OAuthProvider;
import com.azas.domain.auth.entity.SocialAccount;
import com.azas.domain.member.dto.MemberProfileResult;
import com.azas.domain.member.dto.MemberProfileUpdateCommand;
import com.azas.domain.member.entity.Member;
import com.azas.domain.member.service.MemberProfileService;
import com.azas.domain.member.service.MemberProfileUpdateService;
import com.azas.domain.member.service.MemberWithdrawalService;
import com.azas.global.security.AccessTokenMemberResolver;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import com.azas.global.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MemberControllerTest {

    @Mock
    private MemberProfileService memberProfileService;

    @Mock
    private MemberProfileUpdateService memberProfileUpdateService;

    @Mock
    private AccessTokenMemberResolver accessTokenMemberResolver;

    @Mock
    private MemberWithdrawalService
            memberWithdrawalService;

    @InjectMocks
    private MemberController memberController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper =
                new ObjectMapper()
                        .registerModule(new JavaTimeModule())
                        .disable(
                                SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
                        );

        mockMvc = MockMvcBuilders
                .standaloneSetup(memberController)
                .setControllerAdvice(
                        new GlobalExceptionHandler()
                )
                .setMessageConverters(
                        new MappingJackson2HttpMessageConverter(
                                objectMapper
                        )
                )
                .build();
    }

    @Test
    void returnsCurrentMemberProfile()
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
        ReflectionTestUtils.setField(
                member,
                "updatedAt",
                LocalDateTime.of(2026, 7, 24, 1, 0)
        );

        SocialAccount socialAccount =
                SocialAccount.create(
                        1L,
                        OAuthProvider.GOOGLE,
                        "google-subject"
                );

        ReflectionTestUtils.setField(
                socialAccount,
                "createdAt",
                LocalDateTime.of(2026, 7, 23, 3, 0)
        );

        when(
                accessTokenMemberResolver.resolveMemberId(
                        "Bearer access-token"
                )
        ).thenReturn(1L);

        when(memberProfileService.getMyProfile(1L))
                .thenReturn(
                        new MemberProfileResult(
                                member,
                                List.of(socialAccount),
                                null
                        )
                );

        mockMvc.perform(
                        get("/api/v1/members/me")
                                .header(
                                        "Authorization",
                                        "Bearer access-token"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.member_id")
                                .value(1L)
                )
                .andExpect(
                        jsonPath("$.email")
                                .value("parent@example.com")
                )
                .andExpect(
                        jsonPath("$.member_type")
                                .value("PARENT")
                )
                .andExpect(
                        jsonPath("$.phone_verified")
                                .value(false)
                )
                .andExpect(
                        jsonPath(
                                "$.social_accounts[0].provider"
                        ).value("GOOGLE")
                );

        verify(accessTokenMemberResolver)
                .resolveMemberId(
                        "Bearer access-token"
                );
        verify(memberProfileService)
                .getMyProfile(1L);
    }

    @Test
    void returnsUnauthorizedWhenAccessTokenIsMissing()
            throws Exception {
        when(
                accessTokenMemberResolver.resolveMemberId(
                        null
                )
        ).thenThrow(
                new BusinessException(
                        ErrorCode.ACCESS_TOKEN_REQUIRED
                )
        );

        mockMvc.perform(
                        get("/api/v1/members/me")
                )
                .andExpect(status().isUnauthorized())
                .andExpect(
                        jsonPath("$.error.code")
                                .value("ACCESS_TOKEN_REQUIRED")
                );

        verifyNoInteractions(memberProfileService);
    }

    @Test
    void updatesCurrentMemberProfile()
            throws Exception {
        Member member = Member.createParent(
                "parent@example.com",
                "김하나",
                "https://example.com/new-profile.png"
        );

        ReflectionTestUtils.setField(
                member,
                "memberId",
                1L
        );
        ReflectionTestUtils.setField(
                member,
                "birthDate",
                java.time.LocalDate.of(
                        1992,
                        4,
                        15
                )
        );

        when(
                accessTokenMemberResolver.resolveMemberId(
                        "Bearer access-token"
                )
        ).thenReturn(1L);

        when(memberProfileService.getMyProfile(1L))
                .thenReturn(
                        new MemberProfileResult(
                                member,
                                List.of(),
                                "010-****-5678"
                        )
                );

        mockMvc.perform(
                        patch("/api/v1/members/me")
                                .header(
                                        "Authorization",
                                        "Bearer access-token"
                                )
                                .contentType(
                                        org.springframework.http
                                                .MediaType
                                                .APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "birth_date": "1992-04-15",
                                          "profile_image_url":
                                            "https://example.com/new-profile.png",
                                          "phone_verification_token":
                                            "verification-token"
                                        }
                                        """
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.birth_date")
                                .value("1992-04-15")
                )
                .andExpect(
                        jsonPath("$.profile_image_url")
                                .value(
                                        "https://example.com/new-profile.png"
                                )
                )
                .andExpect(
                        jsonPath("$.phone_number")
                                .value("010-****-5678")
                );

        ArgumentCaptor<MemberProfileUpdateCommand> captor =
                ArgumentCaptor.forClass(
                        MemberProfileUpdateCommand.class
                );

        verify(memberProfileUpdateService)
                .updateMyProfile(
                        eq(1L),
                        captor.capture()
                );

        MemberProfileUpdateCommand command =
                captor.getValue();

        assertTrue(command.isBirthDateProvided());
        assertEquals(
                java.time.LocalDate.of(1992, 4, 15),
                command.getBirthDate()
        );
        assertTrue(
                command.isPhoneVerificationTokenProvided()
        );
        assertEquals(
                "verification-token",
                command.getPhoneVerificationToken()
        );

        verify(memberProfileService)
                .getMyProfile(1L);
    }

    @Test
    void withdrawsCurrentMember()
            throws Exception {
        when(
                accessTokenMemberResolver.resolveMemberId(
                        "Bearer access-token"
                )
        ).thenReturn(1L);

        mockMvc.perform(
                        delete("/api/v1/members/me")
                                .header(
                                        "Authorization",
                                        "Bearer access-token"
                                )
                )
                .andExpect(status().isNoContent());

        verify(accessTokenMemberResolver)
                .resolveMemberId(
                        "Bearer access-token"
                );
        verify(memberWithdrawalService)
                .withdrawMyMembership(1L);
    }

    @Test
    void rejectsWithdrawalWithoutAccessToken()
            throws Exception {
        when(
                accessTokenMemberResolver.resolveMemberId(
                        null
                )
        ).thenThrow(
                new BusinessException(
                        ErrorCode.ACCESS_TOKEN_REQUIRED
                )
        );

        mockMvc.perform(
                        delete("/api/v1/members/me")
                )
                .andExpect(status().isUnauthorized())
                .andExpect(
                        jsonPath("$.error.code")
                                .value(
                                        "ACCESS_TOKEN_REQUIRED"
                                )
                );

        verifyNoInteractions(memberWithdrawalService);
    }

    @Test
    void rejectsAlreadyWithdrawnMember()
            throws Exception {
        when(
                accessTokenMemberResolver.resolveMemberId(
                        "Bearer access-token"
                )
        ).thenReturn(1L);

        doThrow(
                new BusinessException(
                        ErrorCode.WITHDRAWN_MEMBER
                )
        ).when(memberWithdrawalService)
                .withdrawMyMembership(1L);

        mockMvc.perform(
                        delete("/api/v1/members/me")
                                .header(
                                        "Authorization",
                                        "Bearer access-token"
                                )
                )
                .andExpect(status().isUnauthorized())
                .andExpect(
                        jsonPath("$.error.code")
                                .value("WITHDRAWN_MEMBER")
                );

        verify(memberWithdrawalService)
                .withdrawMyMembership(1L);
    }
}