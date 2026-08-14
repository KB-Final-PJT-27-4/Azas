package com.azas.domain.finance.account.controller;

import com.azas.domain.finance.account.dto.AccountLinkResult;
import com.azas.domain.finance.account.service.AccountLinkService;
import com.azas.global.exception.GlobalExceptionHandler;
import com.azas.global.security.AccessTokenMemberResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AccountLinkControllerTest {
    @Mock AccountLinkService service;
    @Mock AccessTokenMemberResolver resolver;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new AccountLinkController(service, resolver)
                ).setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void linksSelectedAccounts() throws Exception {
        when(resolver.resolveMemberId("Bearer token")).thenReturn(1L);
        when(service.link(org.mockito.ArgumentMatchers.eq(1L), any()))
                .thenReturn(new AccountLinkResult(List.of(), List.of()));

        mockMvc.perform(post("/api/v1/accounts/link")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"owner_type":"PARENT","child_id":null,"account_ids":[11]}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.linked_count").value(0))
                .andExpect(jsonPath("$.goal_setup_account_ids").isArray());
    }
}
