package com.azas.domain.finance.account.controller;

import com.azas.domain.finance.account.dto.AccountDetailResult;
import com.azas.domain.finance.account.service.AccountDetailService;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import com.azas.global.exception.GlobalExceptionHandler;
import com.azas.global.security.AccessTokenMemberResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AccountDetailControllerTest {

    private static final long MEMBER_ID = 8L;
    private static final long ACCOUNT_ID = 5L;

    @Mock
    private AccountDetailService accountDetailService;

    @Mock
    private AccessTokenMemberResolver accessTokenMemberResolver;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        AccountDetailController controller = new AccountDetailController(
                accountDetailService,
                accessTokenMemberResolver
        );
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void accessibleMemberReadsMinimalAccountDetail() throws Exception {
        when(accessTokenMemberResolver.resolveMemberId(
                "Bearer access-token"
        )).thenReturn(MEMBER_ID);
        when(accountDetailService.getAccountDetail(MEMBER_ID, ACCOUNT_ID))
                .thenReturn(accountResult());

        mockMvc.perform(get("/api/v1/accounts/{account_id}", ACCOUNT_ID)
                        .header("Authorization", "Bearer access-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.account_id").value(ACCOUNT_ID))
                .andExpect(jsonPath("$.owner_type").value("CHILD"))
                .andExpect(jsonPath("$.bank_name").value("KB국민은행"))
                .andExpect(jsonPath("$.account_name")
                        .value("아이사랑적금1"))
                .andExpect(jsonPath("$.account_number")
                        .value("952-17362605-43"))
                .andExpect(jsonPath("$.account_holder_name").value("깨비"))
                .andExpect(jsonPath("$.account_product_type")
                        .value("SAVINGS"))
                .andExpect(jsonPath("$.balance").value(100000.00))
                .andExpect(jsonPath("$.child").doesNotExist())
                .andExpect(jsonPath("$.financial_goal").doesNotExist())
                .andExpect(jsonPath("$.time_capsule").doesNotExist())
                .andExpect(jsonPath("$.account_status").doesNotExist());

        verify(accountDetailService).getAccountDetail(
                MEMBER_ID,
                ACCOUNT_ID
        );
    }

    @Test
    void rejectsMissingAccessToken() throws Exception {
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
    void rejectsInvalidAccountId() throws Exception {
        mockServiceFailure(ErrorCode.BADREQUEST, 0L);

        mockMvc.perform(get("/api/v1/accounts/{account_id}", 0L)
                        .header("Authorization", "Bearer access-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("BADREQUEST"));
    }

    @Test
    void rejectsMemberWithoutAccountAccess() throws Exception {
        mockServiceFailure(
                ErrorCode.FINANCIAL_ACCOUNT_ACCESS_DENIED,
                ACCOUNT_ID
        );

        mockMvc.perform(get("/api/v1/accounts/{account_id}", ACCOUNT_ID)
                        .header("Authorization", "Bearer access-token"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code")
                        .value("FINANCIAL_ACCOUNT_ACCESS_DENIED"));
    }

    @Test
    void returnsNotFoundForMissingOrUnlinkedAccount() throws Exception {
        mockServiceFailure(
                ErrorCode.FINANCIAL_ACCOUNT_NOT_FOUND,
                ACCOUNT_ID
        );

        mockMvc.perform(get("/api/v1/accounts/{account_id}", ACCOUNT_ID)
                        .header("Authorization", "Bearer access-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code")
                        .value("FINANCIAL_ACCOUNT_NOT_FOUND"));
    }

    @Test
    void returnsInternalErrorForAccountProtectionFailure()
            throws Exception {
        mockServiceFailure(ErrorCode.INTERNAL_SERVER_ERROR, ACCOUNT_ID);

        mockMvc.perform(get("/api/v1/accounts/{account_id}", ACCOUNT_ID)
                        .header("Authorization", "Bearer access-token"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error.code")
                        .value("INTERNAL_SERVER_ERROR"));
    }

    private void mockServiceFailure(ErrorCode errorCode, long accountId) {
        when(accessTokenMemberResolver.resolveMemberId(
                "Bearer access-token"
        )).thenReturn(MEMBER_ID);
        when(accountDetailService.getAccountDetail(MEMBER_ID, accountId))
                .thenThrow(new BusinessException(errorCode));
    }

    private AccountDetailResult accountResult() {
        return new AccountDetailResult(
                ACCOUNT_ID,
                "CHILD",
                "KB국민은행",
                "아이사랑적금1",
                "952-17362605-43",
                "깨비",
                "SAVINGS",
                new BigDecimal("100000.00")
        );
    }
}
