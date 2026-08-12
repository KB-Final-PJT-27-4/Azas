package com.azas.domain.finance.account.controller;

import com.azas.domain.finance.account.dto.ChildAvailableAmountResult;
import com.azas.domain.finance.account.entity.ChildUsageMode;
import com.azas.domain.finance.account.service.ChildAvailableAmountService;
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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ChildAvailableAmountControllerTest {

    private static final long MEMBER_ID = 9L;

    @Mock
    private ChildAvailableAmountService childAvailableAmountService;

    @Mock
    private AccessTokenMemberResolver accessTokenMemberResolver;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        ChildAvailableAmountController controller =
                new ChildAvailableAmountController(
                        childAvailableAmountService,
                        accessTokenMemberResolver
                );

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(
                        new MappingJackson2HttpMessageConverter(
                                objectMapper
                        )
                )
                .build();
    }

    @Test
    void childReadsCurrentMonthUsage() throws Exception {
        when(accessTokenMemberResolver.resolveMemberId(
                "Bearer child-access-token"
        )).thenReturn(MEMBER_ID);
        when(childAvailableAmountService.getCurrentMonthUsage(
                MEMBER_ID
        )).thenReturn(result());

        mockMvc.perform(
                        get(
                                "/api/v1/children/me/available-amount"
                        ).header(
                                "Authorization",
                                "Bearer child-access-token"
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.child_id").value(6L))
                .andExpect(jsonPath("$.account_id").value(15L))
                .andExpect(jsonPath("$.child_usage_mode")
                        .value("CO_MANAGED"))
                .andExpect(jsonPath("$.child_monthly_budget_amount")
                        .value(100000.00))
                .andExpect(jsonPath("$.current_month_spent_amount")
                        .value(35000.00))
                .andExpect(jsonPath("$.remaining_guidance_amount")
                        .value(65000.00))
                .andExpect(jsonPath("$.budget_exceeded")
                        .value(false))
                .andExpect(jsonPath("$.period").value("2026-08"))
                .andExpect(jsonPath("$.calculated_at")
                        .value("2026-08-11T10:00:00Z"));

        verify(childAvailableAmountService)
                .getCurrentMonthUsage(MEMBER_ID);
    }

    @Test
    void rejectsMissingAccessToken() throws Exception {
        when(accessTokenMemberResolver.resolveMemberId(null))
                .thenThrow(new BusinessException(
                        ErrorCode.ACCESS_TOKEN_REQUIRED
                ));

        mockMvc.perform(get(
                        "/api/v1/children/me/available-amount"
                ))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code")
                        .value("ACCESS_TOKEN_REQUIRED"));

        verifyNoInteractions(childAvailableAmountService);
    }

    @Test
    void rejectsParentMember() throws Exception {
        mockServiceFailure(
                ErrorCode.CHILD_MEMBER_ACCESS_REQUIRED
        );

        performRequest()
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code")
                        .value("CHILD_MEMBER_ACCESS_REQUIRED"));
    }

    @Test
    void returnsNotFoundForMissingChildProfile() throws Exception {
        mockServiceFailure(ErrorCode.CHILD_NOT_FOUND);

        performRequest()
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code")
                        .value("CHILD_NOT_FOUND"));
    }

    @Test
    void returnsNotFoundForMissingPrimaryAccount() throws Exception {
        mockServiceFailure(ErrorCode.FINANCIAL_ACCOUNT_NOT_FOUND);

        performRequest()
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code")
                        .value("FINANCIAL_ACCOUNT_NOT_FOUND"));
    }

    @Test
    void returnsConflictForMissingUsagePolicy() throws Exception {
        mockServiceFailure(
                ErrorCode.CHILD_USAGE_POLICY_NOT_CONFIGURED
        );

        performRequest()
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code")
                        .value("CHILD_USAGE_POLICY_NOT_CONFIGURED"));
    }

    private org.springframework.test.web.servlet.ResultActions
    performRequest() throws Exception {
        return mockMvc.perform(
                get("/api/v1/children/me/available-amount")
                        .header(
                                "Authorization",
                                "Bearer child-access-token"
                        )
        );
    }

    private void mockServiceFailure(ErrorCode errorCode) {
        when(accessTokenMemberResolver.resolveMemberId(
                "Bearer child-access-token"
        )).thenReturn(MEMBER_ID);
        when(childAvailableAmountService.getCurrentMonthUsage(
                MEMBER_ID
        )).thenThrow(new BusinessException(errorCode));
    }

    private ChildAvailableAmountResult result() {
        return new ChildAvailableAmountResult(
                6L,
                15L,
                ChildUsageMode.CO_MANAGED,
                new BigDecimal("100000.00"),
                new BigDecimal("35000.00"),
                new BigDecimal("65000.00"),
                false,
                "2026-08",
                LocalDateTime.of(2026, 8, 11, 10, 0)
        );
    }
}
