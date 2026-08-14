package com.azas.domain.finance.account.controller;

import com.azas.domain.finance.account.dto.AccountTransactionDetailResult;
import com.azas.domain.finance.account.dto.AccountTransactionPartyResult;
import com.azas.domain.finance.account.service.AccountTransactionDetailService;
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

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AccountTransactionDetailControllerTest {

    private static final long MEMBER_ID = 8L;
    private static final long TRANSACTION_ID = 901L;

    @Mock
    private AccountTransactionDetailService transactionDetailService;

    @Mock
    private AccessTokenMemberResolver accessTokenMemberResolver;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        AccountTransactionDetailController controller =
                new AccountTransactionDetailController(
                        transactionDetailService,
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
    void returnsTransactionDetail() throws Exception {
        when(accessTokenMemberResolver.resolveMemberId(
                "Bearer access-token"
        )).thenReturn(MEMBER_ID);
        when(transactionDetailService.getTransactionDetail(
                MEMBER_ID,
                TRANSACTION_ID
        )).thenReturn(detailResult());

        mockMvc.perform(
                        get(
                                "/api/v1/account-transactions/{account_transaction_id}",
                                TRANSACTION_ID
                        ).header("Authorization", "Bearer access-token")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.account_transaction_id")
                        .value(TRANSACTION_ID))
                .andExpect(jsonPath("$.occurred_at")
                        .value("2026-07-23T06:00:00Z"))
                .andExpect(jsonPath("$.direction").value("CREDIT"))
                .andExpect(jsonPath("$.amount").value(100000.00))
                .andExpect(jsonPath("$.memo").value("첫 용돈"))
                .andExpect(jsonPath("$.deposit_account.bank_name")
                        .value("KB국민은행"))
                .andExpect(jsonPath("$.deposit_account.account_name")
                        .value("아이사랑적금1"))
                .andExpect(jsonPath("$.deposit_account.account_number")
                        .value("952-17362605-43"))
                .andExpect(jsonPath("$.withdrawal_account.account_name")
                        .value("KB국민 5678"))
                .andExpect(jsonPath("$.withdrawal_account.account_number")
                        .value("123-456-789"))
                .andExpect(jsonPath("$.deposit_account.account_id")
                        .doesNotExist())
                .andExpect(jsonPath("$.balance_after")
                        .value(500000.00));

        verify(transactionDetailService).getTransactionDetail(
                MEMBER_ID,
                TRANSACTION_ID
        );
    }

    @Test
    void rejectsMissingAccessToken() throws Exception {
        when(accessTokenMemberResolver.resolveMemberId(null))
                .thenThrow(new BusinessException(
                        ErrorCode.ACCESS_TOKEN_REQUIRED
                ));

        mockMvc.perform(get(
                        "/api/v1/account-transactions/{account_transaction_id}",
                        TRANSACTION_ID
                ))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code")
                        .value("ACCESS_TOKEN_REQUIRED"));

        verifyNoInteractions(transactionDetailService);
    }

    @Test
    void returnsExpectedTransactionDetailErrors() throws Exception {
        assertServiceError(ErrorCode.BADREQUEST, 400);
        assertServiceError(
                ErrorCode.FINANCIAL_ACCOUNT_ACCESS_DENIED,
                403
        );
        assertServiceError(
                ErrorCode.ACCOUNT_TRANSACTION_NOT_FOUND,
                404
        );
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
                .when(transactionDetailService)
                .getTransactionDetail(MEMBER_ID, TRANSACTION_ID);

        mockMvc.perform(
                        get(
                                "/api/v1/account-transactions/{account_transaction_id}",
                                TRANSACTION_ID
                        ).header("Authorization", "Bearer access-token")
                )
                .andExpect(status().is(expectedStatus))
                .andExpect(jsonPath("$.error.code")
                        .value(errorCode.name()));
    }

    private AccountTransactionDetailResult detailResult() {
        return new AccountTransactionDetailResult(
                TRANSACTION_ID,
                LocalDateTime.of(2026, 7, 23, 6, 0),
                "CREDIT",
                new BigDecimal("100000.00"),
                "첫 용돈",
                new AccountTransactionPartyResult(
                        "KB국민은행",
                        "아이사랑적금1",
                        "952-17362605-43"
                ),
                new AccountTransactionPartyResult(
                        "KB국민은행",
                        "KB국민 5678",
                        "123-456-789"
                ),
                new BigDecimal("500000.00")
        );
    }
}
