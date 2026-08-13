package com.azas.domain.finance.account.controller;

import com.azas.domain.finance.account.dto.ParentAccountListItemResult;
import com.azas.domain.finance.account.dto.ParentAccountListResult;
import com.azas.domain.finance.account.service.ParentAccountListService;
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
        ParentAccountController controller = new ParentAccountController(
                parentAccountListService,
                accessTokenMemberResolver
        );
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void parentReadsOwnAccountListSummary() throws Exception {
        when(accessTokenMemberResolver.resolveMemberId(
                "Bearer access-token"
        )).thenReturn(MEMBER_ID);
        when(parentAccountListService.getMyAccounts(MEMBER_ID))
                .thenReturn(new ParentAccountListResult(
                        new BigDecimal("9600000.00"),
                        List.of(accountResult())
                ));

        mockMvc.perform(get("/api/v1/members/me/accounts")
                        .header("Authorization", "Bearer access-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total_balance").value(9600000.00))
                .andExpect(jsonPath("$.total_count").value(1))
                .andExpect(jsonPath("$.accounts[0].account_id").value(2L))
                .andExpect(jsonPath("$.accounts[0].account_name")
                        .value("아이사랑적금1"))
                .andExpect(jsonPath("$.accounts[0].account_number")
                        .value("952-17362605-43"))
                .andExpect(jsonPath("$.accounts[0].account_product_type")
                        .value("SAVINGS"))
                .andExpect(jsonPath("$.accounts[0].balance")
                        .value(9600000.00))
                .andExpect(jsonPath("$.accounts[0].is_primary")
                        .doesNotExist());

        verify(parentAccountListService).getMyAccounts(MEMBER_ID);
    }

    @Test
    void returnsZeroSummaryWhenAccountListIsEmpty() throws Exception {
        when(accessTokenMemberResolver.resolveMemberId(
                "Bearer access-token"
        )).thenReturn(MEMBER_ID);
        when(parentAccountListService.getMyAccounts(MEMBER_ID))
                .thenReturn(new ParentAccountListResult(
                        BigDecimal.ZERO,
                        List.of()
                ));

        mockMvc.perform(get("/api/v1/members/me/accounts")
                        .header("Authorization", "Bearer access-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total_balance").value(0))
                .andExpect(jsonPath("$.total_count").value(0))
                .andExpect(jsonPath("$.accounts").isEmpty());
    }

    @Test
    void rejectsRequestWithoutAccessToken() throws Exception {
        when(accessTokenMemberResolver.resolveMemberId(null))
                .thenThrow(new BusinessException(
                        ErrorCode.ACCESS_TOKEN_REQUIRED
                ));

        mockMvc.perform(get("/api/v1/members/me/accounts"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code")
                        .value("ACCESS_TOKEN_REQUIRED"));

        verifyNoInteractions(parentAccountListService);
    }

    @Test
    void rejectsChildMember() throws Exception {
        when(accessTokenMemberResolver.resolveMemberId(
                "Bearer child-access-token"
        )).thenReturn(MEMBER_ID);
        when(parentAccountListService.getMyAccounts(MEMBER_ID))
                .thenThrow(new BusinessException(
                        ErrorCode.PARENT_ACCESS_REQUIRED
                ));

        mockMvc.perform(get("/api/v1/members/me/accounts")
                        .header(
                                "Authorization",
                                "Bearer child-access-token"
                        ))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code")
                        .value("PARENT_ACCESS_REQUIRED"));
    }

    private ParentAccountListItemResult accountResult() {
        return new ParentAccountListItemResult(
                2L,
                "아이사랑적금1",
                "952-17362605-43",
                "SAVINGS",
                new BigDecimal("9600000.00")
        );
    }
}
