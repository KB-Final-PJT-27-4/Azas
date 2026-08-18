package com.azas.domain.finance.account.controller;

import com.azas.domain.finance.account.dto.AccountBalanceResult;
import com.azas.domain.finance.account.service.AccountBalanceService;
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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AccountBalanceControllerTest {

    private static final long MEMBER_ID = 8L;
    private static final long ACCOUNT_ID = 3L;

    @Mock
    private AccountBalanceService accountBalanceService;

    @Mock
    private AccessTokenMemberResolver accessTokenMemberResolver;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        AccountBalanceController controller = new AccountBalanceController(
                accountBalanceService,
                accessTokenMemberResolver
        );

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(
                        new MappingJackson2HttpMessageConverter(objectMapper)
                )
                .build();
    }

    @Test
    void accessibleMemberReadsLatestAccountBalance()
            throws Exception {
        when(accessTokenMemberResolver.resolveMemberId(
                "Bearer access-token"
        )).thenReturn(MEMBER_ID);
        when(accountBalanceService.getLatestBalance(
                MEMBER_ID,
                ACCOUNT_ID
        )).thenReturn(balanceResult());

        mockMvc.perform(
                        get(
                                "/api/v1/accounts/{account_id}/balance",
                                ACCOUNT_ID
                        ).header("Authorization", "Bearer access-token")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.account_id").value(ACCOUNT_ID))
                .andExpect(jsonPath("$.balance").value(1250000.00))
                .andExpect(jsonPath("$.balance_updated_at")
                        .value("2026-08-10T05:30:00Z"));

        verify(accountBalanceService).getLatestBalance(
                MEMBER_ID,
                ACCOUNT_ID
        );
    }

    @Test
    void rejectsMissingAccessToken()
            throws Exception {
        when(accessTokenMemberResolver.resolveMemberId(null))
                .thenThrow(new BusinessException(
                        ErrorCode.ACCESS_TOKEN_REQUIRED
                ));

        mockMvc.perform(get(
                        "/api/v1/accounts/{account_id}/balance",
                        ACCOUNT_ID
                ))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code")
                        .value("ACCESS_TOKEN_REQUIRED"));

        verifyNoInteractions(accountBalanceService);
    }

    @Test
    void rejectsInvalidAccountId()
            throws Exception {
        mockServiceFailure(ErrorCode.BADREQUEST, 0L);

        performRequest(0L)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("BADREQUEST"));
    }

    @Test
    void rejectsMemberWithoutAccountAccess()
            throws Exception {
        mockServiceFailure(
                ErrorCode.FINANCIAL_ACCOUNT_ACCESS_DENIED,
                ACCOUNT_ID
        );

        performRequest(ACCOUNT_ID)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code")
                        .value("FINANCIAL_ACCOUNT_ACCESS_DENIED"));
    }

    @Test
    void returnsNotFoundForMissingOrUnavailableAccount()
            throws Exception {
        mockServiceFailure(
                ErrorCode.FINANCIAL_ACCOUNT_NOT_FOUND,
                ACCOUNT_ID
        );

        performRequest(ACCOUNT_ID)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code")
                        .value("FINANCIAL_ACCOUNT_NOT_FOUND"));
    }

    @Test
    void returnsConflictWhenBalanceWasNeverSynchronized()
            throws Exception {
        mockServiceFailure(
                ErrorCode.ACCOUNT_BALANCE_NOT_AVAILABLE,
                ACCOUNT_ID
        );

        performRequest(ACCOUNT_ID)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code")
                        .value("ACCOUNT_BALANCE_NOT_AVAILABLE"));
    }

    @Test
    void returnsInternalServerError()
            throws Exception {
        mockServiceFailure(
                ErrorCode.INTERNAL_SERVER_ERROR,
                ACCOUNT_ID
        );

        performRequest(ACCOUNT_ID)
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error.code")
                        .value("INTERNAL_SERVER_ERROR"));
    }

    private org.springframework.test.web.servlet.ResultActions performRequest(
            long accountId
    ) throws Exception {
        return mockMvc.perform(
                get(
                        "/api/v1/accounts/{account_id}/balance",
                        accountId
                ).header("Authorization", "Bearer access-token")
        );
    }

    private void mockServiceFailure(
            ErrorCode errorCode,
            long accountId
    ) {
        when(accessTokenMemberResolver.resolveMemberId(
                "Bearer access-token"
        )).thenReturn(MEMBER_ID);
        when(accountBalanceService.getLatestBalance(
                MEMBER_ID,
                accountId
        )).thenThrow(new BusinessException(errorCode));
    }

    private AccountBalanceResult balanceResult() {
        return new AccountBalanceResult(
                ACCOUNT_ID,
                new BigDecimal("1250000.00"),
                LocalDateTime.of(2026, 8, 10, 5, 30)
        );
    }
}
