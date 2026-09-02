package com.azas.domain.finance.account.controller;

import com.azas.domain.finance.account.service.AccountPrimaryService;
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

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AccountPrimaryControllerTest {

    private static final long MEMBER_ID = 8L;
    private static final long ACCOUNT_ID = 3L;

    @Mock
    private AccountPrimaryService accountPrimaryService;

    @Mock
    private AccessTokenMemberResolver accessTokenMemberResolver;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        AccountPrimaryController controller =
                new AccountPrimaryController(
                        accountPrimaryService,
                        accessTokenMemberResolver
                );

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void setsAccessibleAccountAsPrimary() throws Exception {
        when(accessTokenMemberResolver.resolveMemberId(
                "Bearer access-token"
        )).thenReturn(MEMBER_ID);

        performRequest(ACCOUNT_ID)
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(accountPrimaryService).setPrimaryAccount(
                MEMBER_ID,
                ACCOUNT_ID
        );
    }

    @Test
    void repeatedRequestReturnsNoContent() throws Exception {
        when(accessTokenMemberResolver.resolveMemberId(
                "Bearer access-token"
        )).thenReturn(MEMBER_ID);

        performRequest(ACCOUNT_ID)
                .andExpect(status().isNoContent());
    }

    @Test
    void rejectsMissingAccessToken() throws Exception {
        when(accessTokenMemberResolver.resolveMemberId(null))
                .thenThrow(new BusinessException(
                        ErrorCode.ACCESS_TOKEN_REQUIRED
                ));

        mockMvc.perform(patch(
                        "/api/v1/accounts/{account_id}/primary",
                        ACCOUNT_ID
                ))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code")
                        .value("ACCESS_TOKEN_REQUIRED"));

        verifyNoInteractions(accountPrimaryService);
    }

    @Test
    void rejectsInvalidAccountId() throws Exception {
        mockServiceFailure(ErrorCode.BADREQUEST, 0L);

        performRequest(0L)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code")
                        .value("BADREQUEST"));
    }

    @Test
    void rejectsInaccessibleAccount() throws Exception {
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
    void returnsNotFoundForMissingOrInactiveAccount()
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
    void returnsInternalServerError() throws Exception {
        mockServiceFailure(
                ErrorCode.INTERNAL_SERVER_ERROR,
                ACCOUNT_ID
        );

        performRequest(ACCOUNT_ID)
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error.code")
                        .value("INTERNAL_SERVER_ERROR"));
    }

    private org.springframework.test.web.servlet.ResultActions
    performRequest(long accountId) throws Exception {
        return mockMvc.perform(
                patch(
                        "/api/v1/accounts/{account_id}/primary",
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
        doThrow(new BusinessException(errorCode))
                .when(accountPrimaryService)
                .setPrimaryAccount(MEMBER_ID, accountId);
    }
}
