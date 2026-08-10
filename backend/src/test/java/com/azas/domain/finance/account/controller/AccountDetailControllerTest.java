package com.azas.domain.finance.account.controller;

import com.azas.domain.finance.account.dto.AccountDetailChildResult;
import com.azas.domain.finance.account.dto.AccountDetailResult;
import com.azas.domain.finance.account.dto.AccountFinancialGoalResult;
import com.azas.domain.finance.account.service.AccountDetailService;
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
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AccountDetailControllerTest {

    private static final long MEMBER_ID = 8L;
    private static final long ACCOUNT_ID = 2L;

    @Mock
    private AccountDetailService accountDetailService;

    @Mock
    private AccessTokenMemberResolver accessTokenMemberResolver;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        AccountDetailController controller = new AccountDetailController(
                accountDetailService,
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
    void accessibleMemberReadsAccountDetail()
            throws Exception {
        when(accessTokenMemberResolver.resolveMemberId(
                "Bearer access-token"
        )).thenReturn(MEMBER_ID);
        when(accountDetailService.getAccountDetail(
                MEMBER_ID,
                ACCOUNT_ID
        )).thenReturn(accountResult());

        mockMvc.perform(
                        get("/api/v1/accounts/{account_id}", ACCOUNT_ID)
                                .header("Authorization", "Bearer access-token")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.account_id").value(ACCOUNT_ID))
                .andExpect(jsonPath("$.owner_type").value("CHILD"))
                .andExpect(jsonPath("$.child.child_id").value(1L))
                .andExpect(jsonPath("$.account_number")
                        .value("123-4567-8901"))
                .andExpect(jsonPath("$.balance_updated_at")
                        .value("2026-08-09T05:30:00Z"))
                .andExpect(jsonPath("$.financial_goal.goal_name")
                        .value("노트북 구매"));

        verify(accountDetailService).getAccountDetail(
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

        mockMvc.perform(get("/api/v1/accounts/{account_id}", ACCOUNT_ID))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code")
                        .value("ACCESS_TOKEN_REQUIRED"));

        verifyNoInteractions(accountDetailService);
    }

    @Test
    void rejectsInvalidAccountId()
            throws Exception {
        mockServiceFailure(ErrorCode.BADREQUEST, 0L);

        mockMvc.perform(
                        get("/api/v1/accounts/{account_id}", 0L)
                                .header("Authorization", "Bearer access-token")
                )
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

        mockMvc.perform(
                        get("/api/v1/accounts/{account_id}", ACCOUNT_ID)
                                .header("Authorization", "Bearer access-token")
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code")
                        .value("FINANCIAL_ACCOUNT_ACCESS_DENIED"));
    }

    @Test
    void returnsNotFoundForMissingOrUnlinkedAccount()
            throws Exception {
        mockServiceFailure(
                ErrorCode.FINANCIAL_ACCOUNT_NOT_FOUND,
                ACCOUNT_ID
        );

        mockMvc.perform(
                        get("/api/v1/accounts/{account_id}", ACCOUNT_ID)
                                .header("Authorization", "Bearer access-token")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code")
                        .value("FINANCIAL_ACCOUNT_NOT_FOUND"));
    }

    @Test
    void returnsInternalErrorForAccountProtectionFailure()
            throws Exception {
        mockServiceFailure(
                ErrorCode.INTERNAL_SERVER_ERROR,
                ACCOUNT_ID
        );

        mockMvc.perform(
                        get("/api/v1/accounts/{account_id}", ACCOUNT_ID)
                                .header("Authorization", "Bearer access-token")
                )
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error.code")
                        .value("INTERNAL_SERVER_ERROR"));
    }

    private void mockServiceFailure(
            ErrorCode errorCode,
            long accountId
    ) {
        when(accessTokenMemberResolver.resolveMemberId(
                "Bearer access-token"
        )).thenReturn(MEMBER_ID);
        when(accountDetailService.getAccountDetail(
                MEMBER_ID,
                accountId
        )).thenThrow(new BusinessException(errorCode));
    }

    private AccountDetailResult accountResult() {
        return new AccountDetailResult(
                ACCOUNT_ID,
                "CHILD",
                new AccountDetailChildResult(1L, "김하늘"),
                "004",
                "KB국민은행",
                "KB Young Youth 입출금통장",
                "123-4567-8901",
                "DEMAND_DEPOSIT",
                new BigDecimal("1250000.00"),
                LocalDateTime.of(2026, 8, 9, 5, 30),
                "ACTIVE",
                true,
                LocalDateTime.of(2024, 3, 1, 0, 0),
                LocalDate.of(2027, 3, 1),
                LocalDateTime.of(2026, 8, 4, 7, 29, 20),
                new AccountFinancialGoalResult(
                        "노트북 구매",
                        new BigDecimal("1500000.00"),
                        LocalDate.of(2027, 2, 28)
                )
        );
    }
}
