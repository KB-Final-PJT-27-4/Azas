package com.azas.domain.member.controller;

import com.azas.domain.member.dto.PhoneVerificationConfirmResult;
import com.azas.domain.member.dto.PhoneVerificationSendResult;
import com.azas.domain.member.service.PhoneVerificationConfirmService;
import com.azas.domain.member.service.PhoneVerificationSendService;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import com.azas.global.exception.GlobalExceptionHandler;
import com.azas.global.security.AccessTokenMemberResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.LocalDateTime;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PhoneVerificationControllerTest {

    @Mock
    private PhoneVerificationSendService
            phoneVerificationSendService;

    @Mock
    private PhoneVerificationConfirmService
            phoneVerificationConfirmService;

    @Mock
    private AccessTokenMemberResolver
            accessTokenMemberResolver;

    @InjectMocks
    private PhoneVerificationController
            phoneVerificationController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper =
                new ObjectMapper()
                        .registerModule(new JavaTimeModule())
                        .disable(
                                SerializationFeature
                                        .WRITE_DATES_AS_TIMESTAMPS
                        );

        LocalValidatorFactoryBean validator =
                new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(
                        phoneVerificationController
                )
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
    void sendsPhoneVerificationCode()
            throws Exception {
        when(
                accessTokenMemberResolver
                        .resolveMemberId(
                                "Bearer access-token"
                        )
        ).thenReturn(1L);

        when(
                phoneVerificationSendService.send(
                        1L,
                        "010-1234-5678"
                )
        ).thenReturn(
                new PhoneVerificationSendResult(
                        10L,
                        LocalDateTime.of(
                                2026, 8, 6, 3, 3
                        ),
                        LocalDateTime.of(
                                2026, 8, 6, 3, 1
                        )
                )
        );

        mockMvc.perform(
                        post(
                                "/api/v1/members/me"
                                        + "/phone-verifications"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer access-token"
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "phone_number": "010-1234-5678"
                                        }
                                        """)
                )
                .andExpect(status().isAccepted())
                .andExpect(
                        jsonPath("$.verification_id")
                                .value(10L)
                )
                .andExpect(
                        jsonPath("$.expires_at")
                                .value(
                                        "2026-08-06T03:03:00Z"
                                )
                )
                .andExpect(
                        jsonPath("$.resend_available_at")
                                .value(
                                        "2026-08-06T03:01:00Z"
                                )
                );

        verify(phoneVerificationSendService)
                .send(
                        1L,
                        "010-1234-5678"
                );
    }

    @Test
    void confirmsPhoneVerificationCode()
            throws Exception {
        when(
                accessTokenMemberResolver
                        .resolveMemberId(
                                "Bearer access-token"
                        )
        ).thenReturn(1L);

        when(
                phoneVerificationConfirmService.confirm(
                        1L,
                        10L,
                        "123456"
                )
        ).thenReturn(
                new PhoneVerificationConfirmResult(
                        10L,
                        "010-****-5678",
                        LocalDateTime.of(
                                2026, 8, 6, 3, 1, 30
                        ),
                        "phone-verification-token",
                        LocalDateTime.of(
                                2026, 8, 6, 3, 11, 30
                        )
                )
        );

        mockMvc.perform(
                        post(
                                "/api/v1/members/me"
                                        + "/phone-verifications"
                                        + "/10/confirm"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer access-token"
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "verification_code": "123456"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.verification_id")
                                .value(10L)
                )
                .andExpect(
                        jsonPath("$.phone_number")
                                .value("010-****-5678")
                )
                .andExpect(
                        jsonPath("$.verified_at")
                                .value(
                                        "2026-08-06T03:01:30Z"
                                )
                )
                .andExpect(
                        jsonPath(
                                "$.phone_verification_token"
                        ).value(
                                "phone-verification-token"
                        )
                )
                .andExpect(
                        jsonPath("$.token_expires_at")
                                .value(
                                        "2026-08-06T03:11:30Z"
                                )
                );

        verify(phoneVerificationConfirmService)
                .confirm(
                        1L,
                        10L,
                        "123456"
                );
    }

    @Test
    void returnsBadRequestForBlankPhoneNumber()
            throws Exception {
        mockMvc.perform(
                        post(
                                "/api/v1/members/me"
                                        + "/phone-verifications"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer access-token"
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "phone_number": " "
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.error.code")
                                .value("BADREQUEST")
                );

        verifyNoInteractions(
                accessTokenMemberResolver,
                phoneVerificationSendService
        );
    }

    @Test
    void returnsBadRequestForBlankVerificationCode()
            throws Exception {
        mockMvc.perform(
                        post(
                                "/api/v1/members/me"
                                        + "/phone-verifications"
                                        + "/10/confirm"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer access-token"
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "verification_code": " "
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.error.code")
                                .value("BADREQUEST")
                );

        verifyNoInteractions(
                accessTokenMemberResolver,
                phoneVerificationConfirmService
        );
    }

    @Test
    void returnsUnauthorizedWhenAccessTokenIsMissing()
            throws Exception {
        when(
                accessTokenMemberResolver
                        .resolveMemberId(null)
        ).thenThrow(
                new BusinessException(
                        ErrorCode.ACCESS_TOKEN_REQUIRED
                )
        );

        mockMvc.perform(
                        post(
                                "/api/v1/members/me"
                                        + "/phone-verifications"
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "phone_number": "010-1234-5678"
                                        }
                                        """)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(
                        jsonPath("$.error.code")
                                .value(
                                        "ACCESS_TOKEN_REQUIRED"
                                )
                );

        verifyNoInteractions(
                phoneVerificationSendService
        );
    }
}