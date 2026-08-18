package com.azas.domain.finance.account.controller;

import com.azas.domain.finance.account.entity.ChildUsageMode;
import com.azas.domain.finance.account.entity.FinancialAccountUsagePolicy;
import com.azas.domain.finance.account.service.ChildAccountUsagePolicyService;
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
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ChildAccountUsagePolicyControllerTest {

    private static final long MEMBER_ID = 1L;
    private static final long ACCOUNT_ID = 15L;

    @Mock
    private ChildAccountUsagePolicyService
            childAccountUsagePolicyService;

    @Mock
    private AccessTokenMemberResolver
            accessTokenMemberResolver;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper =
                new ObjectMapper()
                        .registerModule(new JavaTimeModule())
                        .disable(
                                SerializationFeature
                                        .WRITE_DATES_AS_TIMESTAMPS
                        );

        LocalValidatorFactoryBean validator =
                new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        ChildAccountUsagePolicyController controller =
                new ChildAccountUsagePolicyController(
                        childAccountUsagePolicyService,
                        accessTokenMemberResolver
                );

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(
                        new GlobalExceptionHandler()
                )
                .setMessageConverters(
                        new MappingJackson2HttpMessageConverter(
                                objectMapper
                        )
                )
                .setValidator(validator)
                .build();
    }

    @Test
    void parentUpdatesCoManagedUsagePolicy()
            throws Exception {
        FinancialAccountUsagePolicy policy =
                policy(
                        ChildUsageMode.CO_MANAGED,
                        new BigDecimal("50000.00")
                );

        when(
                accessTokenMemberResolver.resolveMemberId(
                        "Bearer access-token"
                )
        ).thenReturn(MEMBER_ID);

        when(
                childAccountUsagePolicyService
                        .updateUsagePolicy(
                                eq(MEMBER_ID),
                                eq(ACCOUNT_ID),
                                eq(ChildUsageMode.CO_MANAGED),
                                eq(new BigDecimal("50000"))
                        )
        ).thenReturn(policy);

        mockMvc.perform(
                        patch(
                                "/api/v1/accounts/{account_id}/child-usage-policy",
                                ACCOUNT_ID
                        )
                                .header(
                                        "Authorization",
                                        "Bearer access-token"
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "child_usage_mode": "CO_MANAGED",
                                          "child_monthly_budget_amount": 50000
                                        }
                                        """
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.account_id")
                                .value(ACCOUNT_ID)
                )
                .andExpect(
                        jsonPath("$.child_id")
                                .value(6L)
                )
                .andExpect(
                        jsonPath("$.child_usage_mode")
                                .value("CO_MANAGED")
                )
                .andExpect(
                        jsonPath("$.child_monthly_budget_amount")
                                .value(50000)
                )
                .andExpect(
                        jsonPath("$.usage_policy_updated_at")
                                .value(
                                        "2026-08-08T10:00:00Z"
                                )
                );
    }

    @Test
    void parentUpdatesUnrestrictedUsagePolicy()
            throws Exception {
        FinancialAccountUsagePolicy policy =
                policy(
                        ChildUsageMode.UNRESTRICTED,
                        null
                );

        when(
                accessTokenMemberResolver.resolveMemberId(
                        "Bearer access-token"
                )
        ).thenReturn(MEMBER_ID);

        when(
                childAccountUsagePolicyService
                        .updateUsagePolicy(
                                MEMBER_ID,
                                ACCOUNT_ID,
                                ChildUsageMode.UNRESTRICTED,
                                null
                        )
        ).thenReturn(policy);

        mockMvc.perform(
                        patch(
                                "/api/v1/accounts/{account_id}/child-usage-policy",
                                ACCOUNT_ID
                        )
                                .header(
                                        "Authorization",
                                        "Bearer access-token"
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "child_usage_mode": "UNRESTRICTED",
                                          "child_monthly_budget_amount": null
                                        }
                                        """
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.child_usage_mode")
                                .value("UNRESTRICTED")
                )
                .andExpect(
                        jsonPath(
                                "$.child_monthly_budget_amount"
                        ).value(
                                org.hamcrest.Matchers.nullValue()
                        )
                );
    }

    @Test
    void parentOrLinkedChildReadsUsagePolicy()
            throws Exception {
        FinancialAccountUsagePolicy policy =
                policy(
                        ChildUsageMode.CO_MANAGED,
                        new BigDecimal("50000.00")
                );

        when(
                accessTokenMemberResolver.resolveMemberId(
                        "Bearer access-token"
                )
        ).thenReturn(MEMBER_ID);

        when(
                childAccountUsagePolicyService
                        .getUsagePolicy(
                                MEMBER_ID,
                                ACCOUNT_ID
                        )
        ).thenReturn(policy);

        mockMvc.perform(
                        get(
                                "/api/v1/accounts/{account_id}/child-usage-policy",
                                ACCOUNT_ID
                        )
                                .header(
                                        "Authorization",
                                        "Bearer access-token"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.account_id")
                                .value(ACCOUNT_ID)
                )
                .andExpect(
                        jsonPath("$.child_usage_mode")
                                .value("CO_MANAGED")
                );

        verify(childAccountUsagePolicyService)
                .getUsagePolicy(
                        MEMBER_ID,
                        ACCOUNT_ID
                );
    }

    @Test
    void rejectsRequestWithoutAccessToken()
            throws Exception {
        when(
                accessTokenMemberResolver.resolveMemberId(
                        null
                )
        ).thenThrow(
                new BusinessException(
                        ErrorCode.ACCESS_TOKEN_REQUIRED
                )
        );

        mockMvc.perform(
                        get(
                                "/api/v1/accounts/{account_id}/child-usage-policy",
                                ACCOUNT_ID
                        )
                )
                .andExpect(status().isUnauthorized())
                .andExpect(
                        jsonPath("$.error.code")
                                .value("ACCESS_TOKEN_REQUIRED")
                );

        verifyNoInteractions(
                childAccountUsagePolicyService
        );
    }

    @Test
    void rejectsRequestWithoutUsageMode()
            throws Exception {
        mockMvc.perform(
                        patch(
                                "/api/v1/accounts/{account_id}/child-usage-policy",
                                ACCOUNT_ID
                        )
                                .header(
                                        "Authorization",
                                        "Bearer access-token"
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "child_monthly_budget_amount": 50000
                                        }
                                        """
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.error.code")
                                .value("BADREQUEST")
                );

        verifyNoInteractions(
                childAccountUsagePolicyService
        );
    }

    @Test
    void returnsUnprocessableEntityForIneligibleAccount()
            throws Exception {
        when(
                accessTokenMemberResolver.resolveMemberId(
                        "Bearer access-token"
                )
        ).thenReturn(MEMBER_ID);

        when(
                childAccountUsagePolicyService
                        .getUsagePolicy(
                                MEMBER_ID,
                                ACCOUNT_ID
                        )
        ).thenThrow(
                new BusinessException(
                        ErrorCode
                                .INELIGIBLE_CHILD_USAGE_POLICY_ACCOUNT
                )
        );

        mockMvc.perform(
                        get(
                                "/api/v1/accounts/{account_id}/child-usage-policy",
                                ACCOUNT_ID
                        )
                                .header(
                                        "Authorization",
                                        "Bearer access-token"
                                )
                )
                .andExpect(
                        status().isUnprocessableEntity()
                )
                .andExpect(
                        jsonPath("$.error.code")
                                .value(
                                        "INELIGIBLE_CHILD_USAGE_POLICY_ACCOUNT"
                                )
                );
    }

    private FinancialAccountUsagePolicy policy(
            ChildUsageMode childUsageMode,
            BigDecimal amount
    ) {
        FinancialAccountUsagePolicy policy =
                new FinancialAccountUsagePolicy();

        ReflectionTestUtils.setField(
                policy,
                "financialAccountId",
                ACCOUNT_ID
        );
        ReflectionTestUtils.setField(
                policy,
                "childId",
                6L
        );
        ReflectionTestUtils.setField(
                policy,
                "accountProductType",
                "DEMAND_DEPOSIT"
        );
        ReflectionTestUtils.setField(
                policy,
                "accountStatus",
                "ACTIVE"
        );
        ReflectionTestUtils.setField(
                policy,
                "linkStatus",
                "ACTIVE"
        );
        ReflectionTestUtils.setField(
                policy,
                "childUsageMode",
                childUsageMode
        );
        ReflectionTestUtils.setField(
                policy,
                "childMonthlyBudgetAmount",
                amount
        );
        ReflectionTestUtils.setField(
                policy,
                "usagePolicyUpdatedAt",
                LocalDateTime.of(
                        2026,
                        8,
                        8,
                        10,
                        0
                )
        );

        return policy;
    }
}