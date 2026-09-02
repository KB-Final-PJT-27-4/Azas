package com.azas.domain.finance.account.controller;

import com.azas.domain.finance.account.dto.DiscoveredAccountListResult;
import com.azas.domain.finance.account.dto.DiscoveredAccountResult;
import com.azas.domain.finance.account.service.DiscoveredAccountService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DiscoveredAccountControllerTest {

    @Mock DiscoveredAccountService discoveredAccountService;
    @Mock AccessTokenMemberResolver accessTokenMemberResolver;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new DiscoveredAccountController(
                                discoveredAccountService,
                                accessTokenMemberResolver
                        )
                )
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void returnsDiscoveredParentAccounts() throws Exception {
        when(accessTokenMemberResolver.resolveMemberId("Bearer token"))
                .thenReturn(1L);
        when(discoveredAccountService.getDiscoveredAccounts(
                1L, "PARENT", null
        )).thenReturn(new DiscoveredAccountListResult(List.of(
                new DiscoveredAccountResult(
                        101L,
                        "KB국민은행",
                        "KB Young Youth 적금",
                        "1234-567-890123",
                        "DEMAND_DEPOSIT",
                        new BigDecimal("12450000")
                )
        )));

        mockMvc.perform(get("/api/v1/accounts/discovered")
                        .header("Authorization", "Bearer token")
                        .param("owner_type", "PARENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accounts[0].account_id")
                        .value(101L))
                .andExpect(jsonPath("$.accounts[0].bank_name")
                        .value("KB국민은행"))
                .andExpect(jsonPath("$.accounts[0].account_name")
                        .value("KB Young Youth 적금"))
                .andExpect(jsonPath("$.accounts[0].account_number")
                        .value("1234-567-890123"));

        verify(discoveredAccountService).getDiscoveredAccounts(
                1L, "PARENT", null
        );
    }

    @Test
    void forwardsChildScope() throws Exception {
        when(accessTokenMemberResolver.resolveMemberId("Bearer token"))
                .thenReturn(1L);
        when(discoveredAccountService.getDiscoveredAccounts(
                1L, "CHILD", 6L
        )).thenReturn(new DiscoveredAccountListResult(List.of()));

        mockMvc.perform(get("/api/v1/accounts/discovered")
                        .header("Authorization", "Bearer token")
                        .param("owner_type", "CHILD")
                        .param("child_id", "6"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accounts").isEmpty());
    }
}
