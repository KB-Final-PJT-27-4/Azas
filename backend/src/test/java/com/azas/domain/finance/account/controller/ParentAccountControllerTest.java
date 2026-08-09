package com.azas.domain.finance.account.controller;

import com.azas.domain.finance.account.dto.ParentAccountListItemResult;
import com.azas.domain.finance.account.dto.ParentAccountListResult;
import com.azas.domain.finance.account.service.ParentAccountListService;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ParentAccountControllerTest {

    private static final long MEMBER_ID = 1L;

    @Mock
    private ParentAccountListService parentAccountListService;

    @Mock
    private AccessTokenMemberResolver accessTokenMemberResolver;

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

        ParentAccountController controller =
                new ParentAccountController(
                        parentAccountListService,
                        accessTokenMemberResolver
                );

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
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
    void parentReadsOwnActiveAccountList()
            throws Exception {
        when(accessTokenMemberResolver.resolveMemberId(
                "Bearer access-token"
        )).thenReturn(MEMBER_ID);
        when(parentAccountListService.getMyAccounts(MEMBER_ID))
                .thenReturn(new ParentAccountListResult(
                        List.of(accountResult())
                ));

        mockMvc.perform(
                        get("/api/v1/members/me/accounts")
                                .header(
                                        "Authorization",
                                        "Bearer access-token"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total_count").value(1))
                .andExpect(jsonPath("$.accounts[0].account_id").value(2L))
                .andExpect(jsonPath("$.accounts[0].account_number")
                        .value("987-6543-5678"))
                .andExpect(jsonPath("$.accounts[0].is_primary")
                        .value(true));

        verify(parentAccountListService)
                .getMyAccounts(MEMBER_ID);
    }

    @Test
    void returnsEmptyAccountList()
            throws Exception {
        when(accessTokenMemberResolver.resolveMemberId(
                "Bearer access-token"
        )).thenReturn(MEMBER_ID);
        when(parentAccountListService.getMyAccounts(MEMBER_ID))
                .thenReturn(new ParentAccountListResult(
                        List.of()
                ));

        mockMvc.perform(
                        get("/api/v1/members/me/accounts")
                                .header(
                                        "Authorization",
                                        "Bearer access-token"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total_count").value(0))
                .andExpect(jsonPath("$.accounts").isArray())
                .andExpect(jsonPath("$.accounts").isEmpty());
    }

    @Test
    void rejectsRequestWithoutAccessToken()
            throws Exception {
        when(accessTokenMemberResolver.resolveMemberId(null))
                .thenThrow(new BusinessException(
                        ErrorCode.ACCESS_TOKEN_REQUIRED
                ));

        mockMvc.perform(
                        get("/api/v1/members/me/accounts")
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code")
                        .value("ACCESS_TOKEN_REQUIRED"));

        verifyNoInteractions(parentAccountListService);
    }

    @Test
    void rejectsChildMember()
            throws Exception {
        when(accessTokenMemberResolver.resolveMemberId(
                "Bearer child-access-token"
        )).thenReturn(MEMBER_ID);
        when(parentAccountListService.getMyAccounts(MEMBER_ID))
                .thenThrow(new BusinessException(
                        ErrorCode.PARENT_ACCESS_REQUIRED
                ));

        mockMvc.perform(
                        get("/api/v1/members/me/accounts")
                                .header(
                                        "Authorization",
                                        "Bearer child-access-token"
                                )
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code")
                        .value("PARENT_ACCESS_REQUIRED"));
    }

    private ParentAccountListItemResult accountResult() {
        return new ParentAccountListItemResult(
                2L,
                "004",
                "KB국민은행",
                "KB Young Youth 입출금통장",
                "987-6543-5678",
                "DEMAND_DEPOSIT",
                new BigDecimal("1250000.00"),
                LocalDateTime.of(2026, 8, 8, 5, 30),
                "ACTIVE",
                true
        );
    }
}
