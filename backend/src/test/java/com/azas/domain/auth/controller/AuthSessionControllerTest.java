package com.azas.domain.auth.controller;

import com.azas.domain.auth.service.TokenLogoutService;
import com.azas.global.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthSessionControllerTest {

    @Mock
    private TokenLogoutService tokenLogoutService;

    @InjectMocks
    private AuthSessionController authSessionController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator =
                new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(authSessionController)
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
    void returnsNoContentAfterLogout()
            throws Exception {
        mockMvc.perform(
                        post("/api/v1/auth/logout")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "refresh_token": "raw-refresh-token"
                                        }
                                        """)
                )
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(tokenLogoutService).logout(
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
                        post("/api/v1/auth/logout")
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

        verifyNoInteractions(tokenLogoutService);
    }
}