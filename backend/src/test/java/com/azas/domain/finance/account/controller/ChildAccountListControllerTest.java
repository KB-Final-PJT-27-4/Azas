package com.azas.domain.finance.account.controller;

import com.azas.domain.finance.account.dto.ChildAccountListItemResult;
import com.azas.domain.finance.account.dto.ChildAccountListResult;
import com.azas.domain.finance.account.service.ChildAccountListService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ChildAccountListControllerTest {

    private static final long MEMBER_ID = 8L;
    private static final long CHILD_ID = 3L;

    @Mock
    private ChildAccountListService childAccountListService;

    @Mock
    private AccessTokenMemberResolver accessTokenMemberResolver;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        ChildAccountListController controller =
                new ChildAccountListController(
                        childAccountListService,
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
    void accessibleMemberReadsChildAccounts()
            throws Exception {
        when(accessTokenMemberResolver.resolveMemberId(
                "Bearer access-token"
        )).thenReturn(MEMBER_ID);
        when(childAccountListService.getChildAccounts(
                MEMBER_ID,
                CHILD_ID
        )).thenReturn(new ChildAccountListResult(
                CHILD_ID,
                List.of(accountResult())
        ));

        mockMvc.perform(
                        get("/api/v1/children/{child_id}/accounts", CHILD_ID)
                                .header("Authorization", "Bearer access-token")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.child_id").value(CHILD_ID))
                .andExpect(jsonPath("$.total_count").value(1))
                .andExpect(jsonPath("$.accounts[0].account_id").value(2L))
                .andExpect(jsonPath("$.accounts[0].account_number")
                        .value("123-4567-8901"))
                .andExpect(jsonPath("$.accounts[0].is_primary")
                        .value(true));

        verify(childAccountListService).getChildAccounts(
                MEMBER_ID,
                CHILD_ID
        );
    }

    @Test
    void returnsEmptyAccountList()
            throws Exception {
        when(accessTokenMemberResolver.resolveMemberId(
                "Bearer access-token"
        )).thenReturn(MEMBER_ID);
        when(childAccountListService.getChildAccounts(
                MEMBER_ID,
                CHILD_ID
        )).thenReturn(new ChildAccountListResult(
                CHILD_ID,
                List.of()
        ));

        mockMvc.perform(
                        get("/api/v1/children/{child_id}/accounts", CHILD_ID)
                                .header("Authorization", "Bearer access-token")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.child_id").value(CHILD_ID))
                .andExpect(jsonPath("$.total_count").value(0))
                .andExpect(jsonPath("$.accounts").isEmpty());
    }

    @Test
    void rejectsMissingAccessToken()
            throws Exception {
        when(accessTokenMemberResolver.resolveMemberId(null))
                .thenThrow(new BusinessException(
                        ErrorCode.ACCESS_TOKEN_REQUIRED
                ));

        mockMvc.perform(
                        get("/api/v1/children/{child_id}/accounts", CHILD_ID)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code")
                        .value("ACCESS_TOKEN_REQUIRED"));

        verifyNoInteractions(childAccountListService);
    }

    @Test
    void rejectsMemberWithoutChildAccess()
            throws Exception {
        when(accessTokenMemberResolver.resolveMemberId(
                "Bearer access-token"
        )).thenReturn(MEMBER_ID);
        when(childAccountListService.getChildAccounts(
                MEMBER_ID,
                CHILD_ID
        )).thenThrow(new BusinessException(
                ErrorCode.CHILD_ACCESS_DENIED
        ));

        mockMvc.perform(
                        get("/api/v1/children/{child_id}/accounts", CHILD_ID)
                                .header("Authorization", "Bearer access-token")
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code")
                        .value("CHILD_ACCESS_DENIED"));
    }

    @Test
    void returnsNotFoundForMissingChild()
            throws Exception {
        when(accessTokenMemberResolver.resolveMemberId(
                "Bearer access-token"
        )).thenReturn(MEMBER_ID);
        when(childAccountListService.getChildAccounts(
                MEMBER_ID,
                CHILD_ID
        )).thenThrow(new BusinessException(
                ErrorCode.CHILD_NOT_FOUND
        ));

        mockMvc.perform(
                        get("/api/v1/children/{child_id}/accounts", CHILD_ID)
                                .header("Authorization", "Bearer access-token")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code")
                        .value("CHILD_NOT_FOUND"));
    }

    private ChildAccountListItemResult accountResult() {
        return new ChildAccountListItemResult(
                2L,
                "004",
                "KB국민은행",
                "KB Young Youth 입출금통장",
                "123-4567-8901",
                "DEMAND_DEPOSIT",
                new BigDecimal("1250000.00"),
                LocalDateTime.of(2026, 8, 9, 5, 30),
                "ACTIVE",
                true
        );
    }
}
