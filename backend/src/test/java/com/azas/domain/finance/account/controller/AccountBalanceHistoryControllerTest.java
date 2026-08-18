package com.azas.domain.finance.account.controller;

import com.azas.domain.finance.account.dto.AccountBalanceHistoryResult;
import com.azas.domain.finance.account.dto.MonthlyAccountBalanceResult;
import com.azas.domain.finance.account.service.AccountBalanceHistoryService;
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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AccountBalanceHistoryControllerTest {

    private static final long MEMBER_ID = 8L;
    private static final long ACCOUNT_ID = 3L;

    @Mock
    private AccountBalanceHistoryService accountBalanceHistoryService;

    @Mock
    private AccessTokenMemberResolver accessTokenMemberResolver;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        AccountBalanceHistoryController controller =
                new AccountBalanceHistoryController(
                        accountBalanceHistoryService,
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
    void accessibleMemberReadsAccountBalanceHistory()
            throws Exception {
        mockSuccess(3);

        performRequest(ACCOUNT_ID, "3")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.account_id").value(ACCOUNT_ID))
                .andExpect(jsonPath("$.months").value(3))
                .andExpect(jsonPath("$.start_month").value("2026-06"))
                .andExpect(jsonPath("$.end_month").value("2026-08"))
                .andExpect(jsonPath("$.balance_history.length()").value(3))
                .andExpect(jsonPath("$.balance_history[0].month")
                        .value("2026-06"))
                .andExpect(jsonPath("$.balance_history[0].change_amount")
                        .value(100000.00))
                .andExpect(jsonPath("$.balance_history[0].observed_at")
                        .value("2026-06-30T14:00:00Z"));

        verify(accountBalanceHistoryService).getBalanceHistory(
                MEMBER_ID,
                ACCOUNT_ID,
                3
        );
    }

    @Test
    void usesSixMonthsWhenQueryParameterIsOmitted()
            throws Exception {
        when(accessTokenMemberResolver.resolveMemberId(
                "Bearer access-token"
        )).thenReturn(MEMBER_ID);
        when(accountBalanceHistoryService.getBalanceHistory(
                MEMBER_ID,
                ACCOUNT_ID,
                6
        )).thenReturn(emptyHistoryResult(6));

        mockMvc.perform(
                        get(
                                "/api/v1/accounts/{account_id}/balance-history",
                                ACCOUNT_ID
                        ).header("Authorization", "Bearer access-token")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.months").value(6))
                .andExpect(jsonPath("$.balance_history.length()").value(6));
    }

    @Test
    void rejectsMissingAccessToken()
            throws Exception {
        when(accessTokenMemberResolver.resolveMemberId(null))
                .thenThrow(new BusinessException(
                        ErrorCode.ACCESS_TOKEN_REQUIRED
                ));

        mockMvc.perform(get(
                        "/api/v1/accounts/{account_id}/balance-history",
                        ACCOUNT_ID
                ))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code")
                        .value("ACCESS_TOKEN_REQUIRED"));

        verifyNoInteractions(accountBalanceHistoryService);
    }

    @Test
    void rejectsInvalidAccountId()
            throws Exception {
        mockServiceFailure(ErrorCode.BADREQUEST, 0L, 6);

        performRequest(0L, null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("BADREQUEST"));
    }

    @Test
    void rejectsMonthsOutsideAllowedRange()
            throws Exception {
        mockServiceFailure(
                ErrorCode.INVALID_BALANCE_HISTORY_MONTHS,
                ACCOUNT_ID,
                13
        );

        performRequest(ACCOUNT_ID, "13")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code")
                        .value("INVALID_BALANCE_HISTORY_MONTHS"));
    }

    @Test
    void rejectsMemberWithoutAccountAccess()
            throws Exception {
        mockServiceFailure(
                ErrorCode.FINANCIAL_ACCOUNT_ACCESS_DENIED,
                ACCOUNT_ID,
                6
        );

        performRequest(ACCOUNT_ID, null)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code")
                        .value("FINANCIAL_ACCOUNT_ACCESS_DENIED"));
    }

    @Test
    void returnsNotFoundForUnavailableAccount()
            throws Exception {
        mockServiceFailure(
                ErrorCode.FINANCIAL_ACCOUNT_NOT_FOUND,
                ACCOUNT_ID,
                6
        );

        performRequest(ACCOUNT_ID, null)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code")
                        .value("FINANCIAL_ACCOUNT_NOT_FOUND"));
    }

    @Test
    void returnsInternalServerError()
            throws Exception {
        mockServiceFailure(
                ErrorCode.INTERNAL_SERVER_ERROR,
                ACCOUNT_ID,
                6
        );

        performRequest(ACCOUNT_ID, null)
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error.code")
                        .value("INTERNAL_SERVER_ERROR"));
    }

    private ResultActions performRequest(
            long accountId,
            String months
    ) throws Exception {
        org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
                request = get(
                "/api/v1/accounts/{account_id}/balance-history",
                accountId
        ).header("Authorization", "Bearer access-token");

        if (months != null) {
            request.param("months", months);
        }

        return mockMvc.perform(request);
    }

    private void mockSuccess(int months) {
        when(accessTokenMemberResolver.resolveMemberId(
                "Bearer access-token"
        )).thenReturn(MEMBER_ID);
        when(accountBalanceHistoryService.getBalanceHistory(
                MEMBER_ID,
                ACCOUNT_ID,
                months
        )).thenReturn(historyResult());
    }

    private void mockServiceFailure(
            ErrorCode errorCode,
            long accountId,
            int months
    ) {
        when(accessTokenMemberResolver.resolveMemberId(
                "Bearer access-token"
        )).thenReturn(MEMBER_ID);
        when(accountBalanceHistoryService.getBalanceHistory(
                MEMBER_ID,
                accountId,
                months
        )).thenThrow(new BusinessException(errorCode));
    }

    private AccountBalanceHistoryResult historyResult() {
        return new AccountBalanceHistoryResult(
                ACCOUNT_ID,
                3,
                YearMonth.of(2026, 6),
                YearMonth.of(2026, 8),
                List.of(
                        monthlyResult(2026, 6, "1000000.00", "100000.00"),
                        monthlyResult(2026, 7, "1100000.00", "100000.00"),
                        monthlyResult(2026, 8, "1300000.00", "200000.00")
                )
        );
    }

    private AccountBalanceHistoryResult emptyHistoryResult(int months) {
        List<MonthlyAccountBalanceResult> history =
                java.util.stream.IntStream.range(0, months)
                        .mapToObj(index -> new MonthlyAccountBalanceResult(
                                YearMonth.of(2026, 8)
                                        .minusMonths(months - 1L - index),
                                null,
                                null,
                                null
                        ))
                        .toList();

        return new AccountBalanceHistoryResult(
                ACCOUNT_ID,
                months,
                YearMonth.of(2026, 8).minusMonths(months - 1L),
                YearMonth.of(2026, 8),
                history
        );
    }

    private MonthlyAccountBalanceResult monthlyResult(
            int year,
            int month,
            String balance,
            String changeAmount
    ) {
        return new MonthlyAccountBalanceResult(
                YearMonth.of(year, month),
                new BigDecimal(balance),
                new BigDecimal(changeAmount),
                LocalDateTime.of(year, month, 30, 14, 0)
        );
    }
}
