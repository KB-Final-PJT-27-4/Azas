package com.azas.domain.auth.controller;

import com.azas.domain.auth.dto.AuthTokenPair;
import com.azas.domain.auth.service.TokenRefreshService;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import com.azas.global.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.stream.Stream;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthTokenControllerTest {

    @Mock
    private TokenRefreshService tokenRefreshService;

    @InjectMocks
    private AuthTokenController authTokenController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator =
                new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(authTokenController)
                .setControllerAdvice(
                        new GlobalExceptionHandler()
                )
                .setValidator(validator)
                .setMessageConverters(
                        new MappingJackson2HttpMessageConverter(
                                new ObjectMapper()
                        )
                )
                .build();
    }

    @Test
    void returnsNewTokenPairForValidRefreshToken()
            throws Exception {
        AuthTokenPair tokenPair = new AuthTokenPair(
                "new-access-token",
                "new-refresh-token",
                3600L
        );

        when(tokenRefreshService.refresh("raw-refresh-token"))
                .thenReturn(tokenPair);

        mockMvc.perform(
                        post("/api/v1/auth/token/refresh")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "refresh_token": "raw-refresh-token"
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
                                .value("new-access-token")
                )
                .andExpect(
                        jsonPath("$.refresh_token")
                                .value("new-refresh-token")
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
                        jsonPath("$.accessToken")
                                .doesNotExist()
                );

        verify(tokenRefreshService).refresh(
                "raw-refresh-token"
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{}",
            "{\"refresh_token\":\"\"}",
            "{\"refresh_token\":\"   \"}"
    })
    void returnsBadRequestForMissingOrBlankRefreshToken(
            String requestBody
    ) throws Exception {
        mockMvc.perform(
                        post("/api/v1/auth/token/refresh")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.error.code")
                                .value("BADREQUEST")
                );

        verifyNoInteractions(tokenRefreshService);
    }

    @ParameterizedTest
    @MethodSource("tokenRefreshFailures")
    void returnsUnauthorizedForTokenRefreshFailure(
            ErrorCode errorCode
    ) throws Exception {
        when(tokenRefreshService.refresh("raw-refresh-token"))
                .thenThrow(
                        new BusinessException(errorCode)
                );

        mockMvc.perform(
                        post("/api/v1/auth/token/refresh")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "refresh_token": "raw-refresh-token"
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

    private static Stream<ErrorCode> tokenRefreshFailures() {
        return Stream.of(
                ErrorCode.INVALID_REFRESH_TOKEN,
                ErrorCode.WITHDRAWN_MEMBER
        );
    }
}
