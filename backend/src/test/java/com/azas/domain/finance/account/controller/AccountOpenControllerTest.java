package com.azas.domain.finance.account.controller;

import com.azas.domain.finance.account.dto.AccountOpenResult;
import com.azas.domain.finance.account.service.AccountOpenService;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AccountOpenControllerTest {

    @Mock
    AccountOpenService service;
    @Mock
    AccessTokenMemberResolver resolver;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new AccountOpenController(service, resolver)
                )
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void opensMockAccountWithoutEmbeddedGoal() throws Exception {
        when(resolver.resolveMemberId("Bearer token")).thenReturn(1L);
        when(service.open(eq(1L), any())).thenReturn(
                new AccountOpenResult(
                        10L,
                        "PARENT",
                        null,
                        2L,
                        "KB국민은행",
                        "KB Mock 통장",
                        "123-456-789012",
                        "DEMAND_DEPOSIT",
                        BigDecimal.ZERO,
                        true,
                        LocalDateTime.of(2026, 8, 13, 5, 0)
                )
        );

        mockMvc.perform(
                        post("/api/v1/accounts/open")
                                .header("Authorization", "Bearer token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "owner_type": "PARENT",
                                          "child_id": null,
                                          "financial_product_id": 2,
                                          "initial_deposit_amount": 0
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.account_id").value(10L))
                .andExpect(
                        jsonPath("$.account_number")
                                .value("123-456-789012")
                )
                .andExpect(jsonPath("$.is_primary").value(true))
                .andExpect(jsonPath("$.financial_goal").doesNotExist());
    }
}
