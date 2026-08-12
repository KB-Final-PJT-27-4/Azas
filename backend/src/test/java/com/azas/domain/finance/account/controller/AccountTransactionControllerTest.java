package com.azas.domain.finance.account.controller;

import com.azas.domain.finance.account.dto.AccountTransactionAccountResult;
import com.azas.domain.finance.account.dto.AccountTransactionItemResult;
import com.azas.domain.finance.account.dto.AccountTransactionListResult;
import com.azas.domain.finance.account.service.AccountTransactionService;
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
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AccountTransactionControllerTest {

    private static final long MEMBER_ID = 8L;
    private static final long ACCOUNT_ID = 3L;

    @Mock
    private AccountTransactionService accountTransactionService;

    @Mock
    private AccessTokenMemberResolver accessTokenMemberResolver;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        AccountTransactionController controller =
                new AccountTransactionController(
                        accountTransactionService,
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
    void returnsAccountTransactionsWithDetailFields()
            throws Exception {
        when(accessTokenMemberResolver.resolveMemberId(
                "Bearer access-token"
        )).thenReturn(MEMBER_ID);
        when(accountTransactionService.getTransactions(
                MEMBER_ID,
                ACCOUNT_ID,
                "next-page",
                5
        )).thenReturn(transactionResult());

        mockMvc.perform(
                        get(
                                "/api/v1/accounts/{account_id}/transactions",
                                ACCOUNT_ID
                        )
                                .header(
                                        "Authorization",
                                        "Bearer access-token"
                                )
                                .param("cursor", "next-page")
                                .param("size", "5")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.account_id").value(ACCOUNT_ID))
                .andExpect(jsonPath("$.transactions[0].account_transaction_id")
                        .value(901L))
                .andExpect(jsonPath("$.transactions[0].occurred_at")
                        .value("2026-07-21T02:01:00Z"))
                .andExpect(jsonPath("$.transactions[0].direction")
                        .value("CREDIT"))
                .andExpect(jsonPath("$.transactions[0].amount")
                        .value(100000.00))
                .andExpect(jsonPath("$.transactions[0].memo")
                        .value("첫 용돈"))
                .andExpect(jsonPath("$.transactions[0].deposit_account.account_id")
                        .value(ACCOUNT_ID))
                .andExpect(jsonPath("$.transactions[0].withdrawal_account.account_number")
                        .value("123-456-789"))
                .andExpect(jsonPath("$.transactions[0].balance_after")
                        .value(500000.00))
                .andExpect(jsonPath("$.next_cursor").value("opaque-cursor"))
                .andExpect(jsonPath("$.has_next").value(true));

        verify(accountTransactionService).getTransactions(
                MEMBER_ID,
                ACCOUNT_ID,
                "next-page",
                5
        );
    }

    @Test
    void returnsEmptyTransactionArray()
            throws Exception {
        when(accessTokenMemberResolver.resolveMemberId(
                "Bearer access-token"
        )).thenReturn(MEMBER_ID);
        when(accountTransactionService.getTransactions(
                MEMBER_ID,
                ACCOUNT_ID,
                null,
                null
        )).thenReturn(new AccountTransactionListResult(
                ACCOUNT_ID,
                List.of(),
                null,
                false
        ));

        mockMvc.perform(
                        get(
                                "/api/v1/accounts/{account_id}/transactions",
                                ACCOUNT_ID
                        ).header("Authorization", "Bearer access-token")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions").isArray())
                .andExpect(jsonPath("$.transactions").isEmpty())
                .andExpect(jsonPath("$.next_cursor").doesNotExist())
                .andExpect(jsonPath("$.has_next").value(false));
    }

    @Test
    void rejectsMissingAccessToken()
            throws Exception {
        when(accessTokenMemberResolver.resolveMemberId(null))
                .thenThrow(new BusinessException(
                        ErrorCode.ACCESS_TOKEN_REQUIRED
                ));

        mockMvc.perform(get(
                        "/api/v1/accounts/{account_id}/transactions",
                        ACCOUNT_ID
                ))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code")
                        .value("ACCESS_TOKEN_REQUIRED"));

        verifyNoInteractions(accountTransactionService);
    }

    @Test
    void returnsExpectedAccountTransactionErrors()
            throws Exception {
        assertServiceError(ErrorCode.INVALID_QUERY_PARAMETER, 400);
        assertServiceError(
                ErrorCode.FINANCIAL_ACCOUNT_ACCESS_DENIED,
                403
        );
        assertServiceError(ErrorCode.FINANCIAL_ACCOUNT_NOT_FOUND, 404);
        assertServiceError(ErrorCode.INTERNAL_SERVER_ERROR, 500);
    }

    private void assertServiceError(
            ErrorCode errorCode,
            int expectedStatus
    ) throws Exception {
        when(accessTokenMemberResolver.resolveMemberId(
                "Bearer access-token"
        )).thenReturn(MEMBER_ID);
        doThrow(new BusinessException(errorCode))
                .when(accountTransactionService)
                .getTransactions(
                        MEMBER_ID,
                        ACCOUNT_ID,
                        null,
                        null
                );

        mockMvc.perform(
                        get(
                                "/api/v1/accounts/{account_id}/transactions",
                                ACCOUNT_ID
                        ).header("Authorization", "Bearer access-token")
                )
                .andExpect(status().is(expectedStatus))
                .andExpect(jsonPath("$.error.code")
                        .value(errorCode.name()));
    }

    private AccountTransactionListResult transactionResult() {
        AccountTransactionAccountResult depositAccount =
                new AccountTransactionAccountResult(
                        ACCOUNT_ID,
                        "KB국민은행",
                        "아이사랑적금1",
                        "952-17362605-43"
                );
        AccountTransactionAccountResult withdrawalAccount =
                new AccountTransactionAccountResult(
                        1L,
                        "KB국민은행",
                        "KB국민 5678",
                        "123-456-789"
                );

        return new AccountTransactionListResult(
                ACCOUNT_ID,
                List.of(new AccountTransactionItemResult(
                        901L,
                        LocalDateTime.of(2026, 7, 21, 2, 1),
                        "CREDIT",
                        new BigDecimal("100000.00"),
                        "첫 용돈",
                        depositAccount,
                        withdrawalAccount,
                        new BigDecimal("500000.00")
                )),
                "opaque-cursor",
                true
        );
    }
}
